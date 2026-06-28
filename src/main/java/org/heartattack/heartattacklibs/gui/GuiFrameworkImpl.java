package org.heartattack.heartattacklibs.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.gui.model.ActionDefinition;
import org.heartattack.heartattacklibs.gui.model.DynamicSectionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuItemDefinition;
import org.heartattack.heartattacklibs.gui.model.RequirementDefinition;
import org.heartattack.heartattacklibs.gui.model.TextureDefinition;
import org.heartattack.heartattacklibs.item.ItemBuilder;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;
import org.heartattack.heartattacklibs.util.MaterialResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class GuiFrameworkImpl implements GuiFramework {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();

    private final Plugin frameworkPlugin;
    private final Logger logger;

    private final Map<String, RegisteredMenu> menus = new HashMap<>();
    private final Map<String, String> pluginFolders = new HashMap<>();
    private final Map<UUID, SessionView> sessions = new HashMap<>();

    private final Map<String, Map<String, GuiActionHandler>> customActions = new HashMap<>();
    private final Map<String, Map<String, GuiRequirementChecker>> customRequirements = new HashMap<>();
    private final Map<String, Map<String, GuiPlaceholderResolver>> customPlaceholders = new HashMap<>();
    private final Map<String, Map<String, GuiRenderer>> customRenderers = new HashMap<>();

    public GuiFrameworkImpl(Plugin frameworkPlugin) {
        this.frameworkPlugin = frameworkPlugin;
        this.logger = frameworkPlugin.getLogger();
    }

    @Override
    public void registerMenus(Plugin plugin, String folderPath) {
        pluginFolders.put(pluginKey(plugin), folderPath);
        loadPluginMenus(plugin, folderPath);
    }

    @Override
    public void reloadPluginMenus(Plugin plugin) {
        String key = pluginKey(plugin);
        String folder = pluginFolders.getOrDefault(key, "menus");
        menus.entrySet().removeIf(entry -> entry.getValue().owner().getName().equalsIgnoreCase(plugin.getName()));
        loadPluginMenus(plugin, folder);
    }

    @Override
    public void open(Player player, String menuId, GuiOpenContext context) {
        RegisteredMenu registered = menus.get(menuId.toLowerCase(Locale.ROOT));
        if (registered == null) {
            logger.warning("Unknown menu id: " + menuId);
            return;
        }

        MenuDefinition menu = registered.menu();
        Map<String, String> placeholders = new LinkedHashMap<>(menu.placeholders());
        placeholders.putAll(context.placeholders());
        placeholders.put("player_name", player.getName());
        placeholders.put("player_uuid", player.getUniqueId().toString());
        placeholders.put("menu_id", menu.id());

        UUID sessionId = UUID.randomUUID();
        GuiSession session = new GuiSession(sessionId, registered.owner(), menu, placeholders);

        if (!passesRequirements(registered.owner(), player, menu.openRequirements(), placeholders, session, true)) {
            return;
        }

        SessionView view = buildView(player, session);
        sessions.put(session.id(), view);
        player.openInventory(view.inventory());
    }

    @Override
    public void bindAction(Plugin plugin, String actionType, GuiActionHandler handler) {
        customActions.computeIfAbsent(pluginKey(plugin), unused -> new HashMap<>())
                .put(actionType.toLowerCase(Locale.ROOT), handler);
    }

    @Override
    public void bindRequirement(Plugin plugin, String requirementType, GuiRequirementChecker checker) {
        customRequirements.computeIfAbsent(pluginKey(plugin), unused -> new HashMap<>())
                .put(requirementType.toLowerCase(Locale.ROOT), checker);
    }

    @Override
    public void bindPlaceholder(Plugin plugin, String key, GuiPlaceholderResolver resolver) {
        customPlaceholders.computeIfAbsent(pluginKey(plugin), unused -> new HashMap<>())
                .put(key.toLowerCase(Locale.ROOT), resolver);
    }

    @Override
    public void bindRenderer(Plugin plugin, String rendererId, GuiRenderer renderer) {
        customRenderers.computeIfAbsent(pluginKey(plugin), unused -> new HashMap<>())
                .put(rendererId.toLowerCase(Locale.ROOT), renderer);
    }

    @Override
    public LiveMenu.Builder liveMenu(Plugin owner, String title, int rows) {
        return new LiveMenu.Builder(owner, title, rows);
    }

    void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof LiveMenu.Holder live && live.menu() != null) {
            live.menu().handleClick(event);
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(top.getHolder() instanceof GuiHolder holder)) {
            return;
        }

        SessionView view = sessions.get(holder.sessionId());
        if (view == null) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= top.getSize()) {
            if (view.session().menu().allowPlayerInventoryClick()) {
                // Explicitly un-cancel for bottom inventory so players can pick/place items.
                event.setCancelled(false);
            } else if (view.session().menu().cancelClicks()) {
                event.setCancelled(true);
            }
            return;
        }

        if (view.session().menu().cancelClicks()) {
            event.setCancelled(true);
        }

        SlotBinding binding = view.slotBindings().get(rawSlot);
        if (binding == null) {
            return;
        }

        Map<String, String> placeholders = new LinkedHashMap<>(view.session().placeholders());
        placeholders.putAll(binding.placeholders());
        placeholders.put("slot", String.valueOf(rawSlot));
        placeholders.put("click", event.getClick().name().toLowerCase(Locale.ROOT));

        if (!passesRequirements(view.session().owner(), player, binding.clickRequirements(), placeholders, view.session(), true)) {
            return;
        }

        List<ActionDefinition> actions = binding.actions().getOrDefault(event.getClick(), Collections.emptyList());
        if (actions.isEmpty()) {
            actions = binding.actions().getOrDefault(ClickType.LEFT, Collections.emptyList());
        }
        for (ActionDefinition action : actions) {
            executeAction(view.session().owner(), new GuiActionContext(
                    view.session().owner(),
                    view.session().menu(),
                    player,
                    top,
                    rawSlot,
                    event.getClick(),
                    action,
                    placeholders,
                    view.session()
            ));
        }
    }

    void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof LiveMenu.Holder live && live.menu() != null) {
            live.menu().handleDrag(event);
            return;
        }
        if (!(top.getHolder() instanceof GuiHolder holder)) {
            return;
        }

        SessionView view = sessions.get(holder.sessionId());
        if (view == null) {
            return;
        }

        if (!view.session().menu().cancelDrag()) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    void onClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof LiveMenu.Holder live && live.menu() != null) {
            live.menu().handleClose(event);
            return;
        }
        if (top.getHolder() instanceof GuiHolder holder) {
            sessions.remove(holder.sessionId());
        }
    }

    private void refreshView(Player player, GuiSession session) {
        SessionView newView = buildView(player, session);
        sessions.put(session.id(), newView);
        player.getOpenInventory().getTopInventory().setContents(newView.inventory().getContents());
    }

    private SessionView buildView(Player player, GuiSession session) {
        String title = applyPlaceholders(session.owner(), session.menu(), player, session, session.menu().title(), session.placeholders());
        // Titles support both MiniMessage format (e.g. "<gold>Menu") and legacy & codes.
        // MiniMessage is tried first when the title contains angle brackets; otherwise
        // legacy & → § translation is applied for backwards compatibility.
        String transformedTitle = UnicodeSmallCaps.apply(title);
        Component titleComponent;
        if (transformedTitle.contains("<") && transformedTitle.contains(">")) {
            titleComponent = MINI_MESSAGE.deserialize(transformedTitle);
        } else {
            String legacyTitle = org.bukkit.ChatColor.translateAlternateColorCodes('&', transformedTitle);
            titleComponent = LEGACY_SECTION.deserialize(legacyTitle);
        }
        titleComponent = titleComponent.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        Inventory inventory = Bukkit.createInventory(new GuiHolder(session.id()), session.menu().size(), titleComponent);
        Map<Integer, SlotBinding> slotBindings = new HashMap<>();

        for (MenuItemDefinition item : session.menu().items()) {
            if (!passesRequirements(session.owner(), player, item.viewRequirements(), session.placeholders(), session, false)) {
                continue;
            }
            for (int slot : item.slots()) {
                if (slot < 0 || slot >= inventory.getSize()) {
                    continue;
                }
                ItemStack stack = buildItem(item, session.owner(), session.menu(), player, session, session.placeholders());
                inventory.setItem(slot, stack);
                slotBindings.put(slot, new SlotBinding(item, null, item.clickRequirements(), item.clickActions(), Collections.emptyMap()));
            }
        }

        for (DynamicSectionDefinition section : session.menu().dynamicSections()) {
            if (!passesRequirements(session.owner(), player, section.viewRequirements(), session.placeholders(), session, false)) {
                continue;
            }

            GuiRenderer renderer = renderer(session.owner(), section.rendererId());
            if (renderer == null) {
                logger.warning("Missing renderer '" + section.rendererId() + "' for menu '" + session.menu().id() + "'.");
                continue;
            }

            List<GuiRenderedItem> rendered = renderer.render(new GuiRenderContext(player, session.menu(), section, session.placeholders(), session));
            if (rendered == null || rendered.isEmpty()) {
                continue;
            }

            List<Integer> slots = section.slots();
            int page = 1;
            int start = 0;
            int end = Math.min(rendered.size(), slots.size());

            if (section.pagination().enabled()) {
                int pageSize = Math.max(1, Math.min(section.pagination().pageSize(), slots.size()));
                int maxPage = Math.max(1, (int) Math.ceil(rendered.size() / (double) pageSize));
                page = Math.min(Math.max(1, session.pageOf(section)), maxPage);
                session.setPage(section, page);
                start = (page - 1) * pageSize;
                end = Math.min(rendered.size(), start + pageSize);
            }

            int cursor = 0;
            for (int i = start; i < end && cursor < slots.size(); i++) {
                int slot = slots.get(cursor++);
                if (slot < 0 || slot >= inventory.getSize()) {
                    continue;
                }

                GuiRenderedItem renderedItem = rendered.get(i);
                inventory.setItem(slot, renderedItem.item());
                Map<String, String> placeholders = renderedItem.placeholders() == null
                        ? Collections.emptyMap()
                        : new LinkedHashMap<>(renderedItem.placeholders());
                slotBindings.put(slot, new SlotBinding(null, section, section.clickRequirements(), section.clickActions(), placeholders));
            }

            if (section.pagination().enabled()) {
                int maxPage = Math.max(1, (int) Math.ceil(rendered.size() / (double) Math.max(1, section.pagination().pageSize())));
                if (page > 1 && section.pagination().previousSlot() >= 0 && section.pagination().previousSlot() < inventory.getSize()) {
                    int prevSlot = section.pagination().previousSlot();
                    inventory.setItem(prevSlot, buildPageButton(section.pagination().prevButton()));
                    Map<ClickType, List<ActionDefinition>> actions = new EnumMap<>(ClickType.class);
                    actions.put(ClickType.LEFT, List.of(new ActionDefinition("internal_prev_page", section.key(), Collections.emptyMap())));
                    slotBindings.put(prevSlot, new SlotBinding(null, section, Collections.emptyList(), actions, Collections.emptyMap()));
                }
                if (page < maxPage && section.pagination().nextSlot() >= 0 && section.pagination().nextSlot() < inventory.getSize()) {
                    int nextSlot = section.pagination().nextSlot();
                    inventory.setItem(nextSlot, buildPageButton(section.pagination().nextButton()));
                    Map<ClickType, List<ActionDefinition>> actions = new EnumMap<>(ClickType.class);
                    actions.put(ClickType.LEFT, List.of(new ActionDefinition("internal_next_page", section.key(), Collections.emptyMap())));
                    slotBindings.put(nextSlot, new SlotBinding(null, section, Collections.emptyList(), actions, Collections.emptyMap()));
                }
            }
        }

        return new SessionView(session, inventory, slotBindings);
    }

    private void executeAction(Plugin owner, GuiActionContext context) {
        String type = context.action().type().toLowerCase(Locale.ROOT);
        switch (type) {
            case "internal_prev_page" -> {
                DynamicSectionDefinition section = sectionByKey(context.menu(), context.action().value());
                if (section != null) {
                    context.session().setPage(section, context.session().pageOf(section) - 1);
                    refreshView(context.player(), context.session());
                }
            }
            case "internal_next_page" -> {
                DynamicSectionDefinition section = sectionByKey(context.menu(), context.action().value());
                if (section != null) {
                    context.session().setPage(section, context.session().pageOf(section) + 1);
                    refreshView(context.player(), context.session());
                }
            }
            case "close" -> context.player().closeInventory();
            case "open_menu" -> {
                String target = applyPlaceholders(owner, context.menu(), context.player(), context.session(), context.action().value(), context.placeholders());
                open(context.player(), target, GuiOpenContext.of(context.session().placeholders()));
            }
            case "message" -> {
                String location = guiActionLocation(context);
                if (frameworkPlugin instanceof HeartAttackLibs HeartAttackLibs
                        && !HeartAttackLibs.playerSettings().isEnabled(context.player(), owner.getName().toLowerCase(Locale.ROOT), "messages", location)) {
                    return;
                }
                String text = applyPlaceholders(owner, context.menu(), context.player(), context.session(), context.action().value(), context.placeholders());
                context.player().sendRichMessage(UnicodeSmallCaps.apply(text));
            }
            case "command" -> {
                String command = applyPlaceholders(owner, context.menu(), context.player(), context.session(), context.action().value(), context.placeholders());
                context.player().performCommand(stripLeadingSlash(command));
            }
            case "console_command" -> {
                String command = applyPlaceholders(owner, context.menu(), context.player(), context.session(), context.action().value(), context.placeholders());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripLeadingSlash(command));
            }
            case "sound" -> {
                String location = guiActionLocation(context);
                if (frameworkPlugin instanceof HeartAttackLibs HeartAttackLibs
                        && !HeartAttackLibs.playerSettings().isEnabled(context.player(), owner.getName().toLowerCase(Locale.ROOT), "sounds", location)) {
                    return;
                }
                String rendered = applyPlaceholders(owner, context.menu(), context.player(), context.session(), context.action().value(), context.placeholders());
                String[] parts = rendered.split(";");
                try {
                    Sound sound = Sound.valueOf(parts[0].trim().toUpperCase(Locale.ROOT));
                    float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
                    float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;
                    context.player().playSound(context.player().getLocation(), sound, volume, pitch);
                } catch (Exception ignored) {
                    logger.warning("Invalid sound action: " + rendered);
                }
            }
            case "refresh" -> open(context.player(), context.menu().id(), GuiOpenContext.of(context.session().placeholders()));
            default -> {
                GuiActionHandler handler = customActions.getOrDefault(pluginKey(owner), Collections.emptyMap()).get(type);
                if (handler != null) {
                    handler.execute(context);
                }
            }
        }
    }

    private String guiActionLocation(GuiActionContext context) {
        return context.menu().id() + "." + context.action().type() + "." + context.slot();
    }

    private boolean passesRequirements(Plugin owner, Player player, List<RequirementDefinition> requirements, Map<String, String> placeholders, GuiSession session, boolean notify) {
        for (RequirementDefinition requirement : requirements) {
            if (checkRequirement(owner, player, requirement, placeholders, session)) {
                continue;
            }

            if (notify && !requirement.denyMessage().isBlank()) {
                String deny = applyPlaceholders(owner, session.menu(), player, session, requirement.denyMessage(), placeholders);
                player.sendRichMessage(UnicodeSmallCaps.apply(deny));
            }
            return false;
        }
        return true;
    }

    private boolean checkRequirement(Plugin owner, Player player, RequirementDefinition requirement, Map<String, String> placeholders, GuiSession session) {
        String type = requirement.type().toLowerCase(Locale.ROOT);
        switch (type) {
            case "permission" -> {
                String permission = requirement.params().getOrDefault("permission", requirement.params().getOrDefault("value", ""));
                permission = applyPlaceholders(owner, session.menu(), player, session, permission, placeholders);
                return !permission.isBlank() && player.hasPermission(permission);
            }
            case "placeholder_equals" -> {
                String key = requirement.params().getOrDefault("key", "");
                String expected = requirement.params().getOrDefault("value", "");
                String current = placeholders.getOrDefault(key, "");
                return current.equalsIgnoreCase(applyPlaceholders(owner, session.menu(), player, session, expected, placeholders));
            }
            default -> {
                GuiRequirementChecker checker = customRequirements.getOrDefault(pluginKey(owner), Collections.emptyMap()).get(type);
                return checker == null || checker.test(new GuiRequirementContext(player, session.menu(), requirement, placeholders, session));
            }
        }
    }

    private ItemStack buildItem(MenuItemDefinition item, Plugin owner, MenuDefinition menu, Player player, GuiSession session, Map<String, String> placeholders) {
        Material material = MaterialResolver.parseModern(item.material());
        if (material == null) {
            logger.warning("Invalid modern material '" + item.material() + "' for menu '" + menu.id() + "', item '" + item.key() + "'. Falling back to STONE.");
            material = Material.STONE;
        }

        ItemBuilder builder = ItemBuilder.of(material).amount(item.amount());

        if (item.name() != null && !item.name().isBlank()) {
            builder.guiName(applyPlaceholders(owner, menu, player, session, item.name(), placeholders));
        }

        if (item.lore() != null && !item.lore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : item.lore()) {
                lore.add(applyPlaceholders(owner, menu, player, session, line, placeholders));
            }
            builder.guiLore(lore);
        }

        if (item.glow()) {
            builder.glow();
        }

        for (String rawFlag : item.flags()) {
            try {
                builder.flags(ItemFlag.valueOf(rawFlag.toUpperCase(Locale.ROOT)));
            } catch (Exception ignored) {
                logger.warning("Unknown item flag '" + rawFlag + "' in menu '" + menu.id() + "'.");
            }
        }

        TextureDefinition texture = item.texture();
        if (texture != null && texture.value() != null && !texture.value().isBlank()) {
            String value = applyPlaceholders(owner, menu, player, session, texture.value(), placeholders);
            builder.texture(texture.type(), value);
        }

        if (item.customModelData() != null) {
            builder.customModelData(item.customModelData());
        }

        return builder.build();
    }

    private ItemStack buildPageButton(org.heartattack.heartattacklibs.gui.model.PageButtonDefinition btn) {
        Material mat = MaterialResolver.parseModern(btn.material());
        if (mat == null) mat = Material.ARROW;
        ItemBuilder builder = ItemBuilder.of(mat).guiName(btn.name());
        if (btn.lore() != null && !btn.lore().isEmpty()) {
            builder.guiLore(btn.lore());
        }
        if (btn.customModelData() != null) {
            builder.customModelData(btn.customModelData());
        }
        return builder.build();
    }

    private DynamicSectionDefinition sectionByKey(MenuDefinition menu, String key) {
        for (DynamicSectionDefinition section : menu.dynamicSections()) {
            if (section.key().equalsIgnoreCase(key)) {
                return section;
            }
        }
        return null;
    }

    private GuiRenderer renderer(Plugin owner, String rendererId) {
        return customRenderers.getOrDefault(pluginKey(owner), Collections.emptyMap())
                .get(rendererId.toLowerCase(Locale.ROOT));
    }

    private String applyPlaceholders(Plugin owner, MenuDefinition menu, Player player, GuiSession session, String input, Map<String, String> values) {
        String output = input == null ? "" : input;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            output = replaceToken(output, entry.getKey(), entry.getValue());
        }

        Map<String, GuiPlaceholderResolver> resolvers = customPlaceholders.getOrDefault(pluginKey(owner), Collections.emptyMap());
        for (Map.Entry<String, GuiPlaceholderResolver> entry : resolvers.entrySet()) {
            String key = entry.getKey();
            if (!containsToken(output, key)) {
                continue;
            }
            String value = entry.getValue().resolve(new GuiPlaceholderContext(player, menu, key, values, session));
            output = replaceToken(output, key, value == null ? "" : value);
        }

        return output;
    }

    private boolean containsToken(String input, String key) {
        return input.contains("{" + key + "}") || input.contains("<" + key + ">") || input.contains("%" + key + "%");
    }

    private String replaceToken(String input, String key, String value) {
        return input
                .replace("{" + key + "}", value)
                .replace("<" + key + ">", value)
                .replace("%" + key + "%", value);
    }

    private String stripLeadingSlash(String command) {
        if (command == null) {
            return "";
        }
        return command.startsWith("/") ? command.substring(1) : command;
    }

    private void loadPluginMenus(Plugin plugin, String folderPath) {
        File asFile = new File(folderPath);
        File folder = asFile.isAbsolute() ? asFile : new File(plugin.getDataFolder(), folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            logger.warning("Could not create menu directory: " + folder.getAbsolutePath());
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            String defaultId = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            MenuDefinition menu = MenuParser.parse(defaultId, config, logger);
            if (menu == null) {
                continue;
            }
            menus.put(menu.id().toLowerCase(Locale.ROOT), new RegisteredMenu(plugin, menu));
            registerGuiSettingsLocations(plugin, menu);
        }

        logger.info("Loaded " + menus.values().stream().filter(entry -> entry.owner().getName().equalsIgnoreCase(plugin.getName())).count() + " GUI menu(s) for " + plugin.getName() + ".");
    }

    private void registerGuiSettingsLocations(Plugin owner, MenuDefinition menu) {
        if (!(frameworkPlugin instanceof HeartAttackLibs HeartAttackLibs)) {
            return;
        }
        String pluginId = owner.getName().toLowerCase(Locale.ROOT);
        menu.items().forEach(item -> item.clickActions().values().forEach(actions -> actions.forEach(action -> {
            String category = switch (action.type().toLowerCase(Locale.ROOT)) {
                case "message" -> "messages";
                case "sound" -> "sounds";
                default -> null;
            };
            if (category != null) {
                for (int slot : item.slots()) {
                    String location = menu.id() + "." + action.type() + "." + slot;
                    HeartAttackLibs.playerSettings().registerLocation(pluginId, category, location, menu.id() + " " + item.key());
                }
            }
        })));
    }

    private String pluginKey(Plugin plugin) {
        return plugin.getName().toLowerCase(Locale.ROOT);
    }

    private record RegisteredMenu(Plugin owner, MenuDefinition menu) {
    }

    private record SessionView(GuiSession session, Inventory inventory, Map<Integer, SlotBinding> slotBindings) {
    }
}

package org.heartattack.heartattacklibs.command.demo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;
import org.heartattack.heartattacklibs.gui.GuiOpenContext;
import org.heartattack.heartattacklibs.gui.GuiRenderedItem;
import org.heartattack.heartattacklibs.item.ItemBuilder;
import org.heartattack.heartattacklibs.settings.PlayerSettingCategory;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.settings.SettingsDisplayConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HeartAttackLibsSettingsCommand implements SimpleCommand {
    private static final String MENU_MAIN = "settings_main";
    private static final String MENU_PLUGINS = "settings_plugins";
    private static final String MENU_PLUGIN_CATEGORIES = "settings_plugin_categories";
    private static final String MENU_LOCATIONS = "settings_locations";

    private final HeartAttackLibs plugin;
    private final PlayerSettingsService settingsService;

    public HeartAttackLibsSettingsCommand(HeartAttackLibs plugin, PlayerSettingsService settingsService) {
        this.plugin = plugin;
        this.settingsService = settingsService;
        bindGuiIntegrations();
    }

    @Override
    public String name() {
        return "settings";
    }

    @Override
    public String description() {
        return "Open personal message and effect settings.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String permission() {
        return "heartattacklibs.settings";
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sender().sendRichMessage(playerOnlyMessage());
            return;
        }
        if (!settingsService.isFeatureEnabled()) {
            plugin.miniMessage().sendWithPrefix(context.sender(), "<red>Player settings are currently disabled.");
            return;
        }
        String[] args = context.args();
        if (args.length >= 4 && args[0].equalsIgnoreCase("toggle")) {
            handleToggle(context.asPlayer(), args[1], args[2], args[3]);
            return;
        }
        plugin.guiFramework().open(context.asPlayer(), MENU_PLUGINS, GuiOpenContext.EMPTY);
    }

    private void handleToggle(Player player, String pluginId, String category, String location) {
        boolean current = settingsService.isEnabled(player, pluginId, category, location);
        settingsService.setEnabled(player, pluginId, category, location, !current);
        String state = !current ? "<green>enabled" : "<red>disabled";
        plugin.miniMessage().sendWithPrefix(player, "<gray>Notification " + state + "<gray>.");
    }

    private void bindGuiIntegrations() {
        plugin.guiFramework().bindAction(plugin, "settings_toggle_global", context -> {
            Player player = context.player();
            String category = context.action().value().trim().toLowerCase(Locale.ROOT);
            boolean current = settingsService.isEnabled(player, category);
            settingsService.setEnabled(player, category, !current);
            plugin.miniMessage().sendWithPrefix(player, "<gray>Updated <white>" + category + "<gray>: <white>" + (!current));
            plugin.guiFramework().open(player, MENU_MAIN, GuiOpenContext.of(context.session().placeholders()));
        });

        plugin.guiFramework().bindAction(plugin, "settings_toggle_scoped", context -> {
            Player player = context.player();
            String pluginId = context.placeholders().getOrDefault("plugin_scope", "");
            String category = context.placeholders().getOrDefault("setting_category", "").toLowerCase(Locale.ROOT);
            if (pluginId.isBlank() || category.isBlank()) {
                return;
            }
            boolean current = settingsService.isEnabled(player, pluginId, category);
            settingsService.setEnabled(player, pluginId, category, !current);
            plugin.miniMessage().sendWithPrefix(player, "<gray>Updated <white>" + pluginId + "." + category + "<gray>: <white>" + (!current));
            plugin.guiFramework().open(player, MENU_PLUGIN_CATEGORIES, GuiOpenContext.of(context.session().placeholders()));
        });

        plugin.guiFramework().bindAction(plugin, "settings_open_plugin", context -> {
            String pluginId = context.placeholders().getOrDefault("plugin_scope", "");
            if (pluginId.isBlank()) {
                return;
            }
            plugin.guiFramework().open(context.player(), MENU_PLUGIN_CATEGORIES, GuiOpenContext.of(Map.of("plugin_scope", pluginId)));
        });

        plugin.guiFramework().bindAction(plugin, "settings_open_category", context -> {
            String pluginId = context.placeholders().getOrDefault("plugin_scope", context.session().placeholders().getOrDefault("plugin_scope", ""));
            String category = context.placeholders().getOrDefault("setting_category", "");
            if (pluginId.isBlank() || category.isBlank()) {
                return;
            }
            plugin.guiFramework().open(context.player(), MENU_LOCATIONS, GuiOpenContext.of(Map.of(
                    "plugin_scope", pluginId,
                    "setting_category", category
            )));
        });

        plugin.guiFramework().bindAction(plugin, "settings_toggle_location", context -> {
            Player player = context.player();
            String pluginId = context.placeholders().getOrDefault("plugin_scope", context.session().placeholders().getOrDefault("plugin_scope", ""));
            String category = context.placeholders().getOrDefault("setting_category", context.session().placeholders().getOrDefault("setting_category", ""));
            String location = context.placeholders().getOrDefault("setting_location", "");
            if (pluginId.isBlank() || category.isBlank() || location.isBlank()) {
                return;
            }
            boolean current = settingsService.isEnabled(player, pluginId, category, location);
            settingsService.setEnabled(player, pluginId, category, location, !current);
            plugin.miniMessage().sendWithPrefix(player, "<gray>Updated <white>" + pluginId + "." + category + "." + location + "<gray>: <white>" + (!current));
            plugin.guiFramework().open(player, MENU_LOCATIONS, GuiOpenContext.of(context.session().placeholders()));
        });

        plugin.guiFramework().bindPlaceholder(plugin, "settings_messages_state", context -> stateText(context.player(), "messages"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_actionbars_state", context -> stateText(context.player(), "actionbars"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_titles_state", context -> stateText(context.player(), "titles"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_particles_state", context -> stateText(context.player(), "particles"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_sounds_state", context -> stateText(context.player(), "sounds"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_has_overrides", context -> String.valueOf(!settingsService.pluginScopes().isEmpty()));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_selected_plugin", context -> context.session().placeholders().getOrDefault("plugin_scope", "plugin"));
        plugin.guiFramework().bindPlaceholder(plugin, "settings_selected_category", context -> context.session().placeholders().getOrDefault("setting_category", "category"));

        plugin.guiFramework().bindRenderer(plugin, "settings_plugins", context -> renderPlugins());
        plugin.guiFramework().bindRenderer(plugin, "settings_plugin_categories", context -> renderPluginCategories(context.player(), context.placeholders().getOrDefault("plugin_scope", "")));
        plugin.guiFramework().bindRenderer(plugin, "settings_locations", context -> renderLocations(
                context.player(),
                context.placeholders().getOrDefault("plugin_scope", ""),
                context.placeholders().getOrDefault("setting_category", "")
        ));
    }

    private List<GuiRenderedItem> renderPlugins() {
        List<String> scopes = settingsService.pluginScopes();
        if (scopes.isEmpty()) {
            return Collections.emptyList();
        }

        List<GuiRenderedItem> rendered = new ArrayList<>();
        for (String pluginId : scopes) {
            ItemBuilder builder = ItemBuilder.of(Material.BOOK)
                    .name("<!i><gold>" + capitalize(pluginId))
                    .lore("<!i><gray>Open message and media controls.", "<!i><yellow>Click to open");
            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("plugin_scope", pluginId);
            rendered.add(new GuiRenderedItem(builder.build(), placeholders));
        }
        return rendered;
    }

    private List<GuiRenderedItem> renderPluginCategories(Player player, String pluginId) {
        if (pluginId.isBlank()) {
            return Collections.emptyList();
        }
        SettingsDisplayConfig display = settingsService.displayConfig();
        List<GuiRenderedItem> rendered = new ArrayList<>();
        for (PlayerSettingCategory category : PlayerSettingCategory.values()) {
            if (settingsService.locations(pluginId, category.key()).isEmpty()) {
                continue;
            }
            boolean enabled = settingsService.isEnabled(player, pluginId, category.key());
            Material material = display.material(enabled);
            String statusLine = display.statusLine(enabled);
            ItemBuilder builder = ItemBuilder.of(material)
                    .name("<!i><gold>" + capitalize(category.key()))
                    .lore(
                            statusLine,
                            "<!i><yellow>Left click: <white>configure per-message",
                            "<!i><yellow>Right click: <white>toggle all"
                    );

            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("plugin_scope", pluginId);
            placeholders.put("setting_category", category.key());
            rendered.add(new GuiRenderedItem(builder.build(), placeholders));
        }
        return rendered;
    }

    private List<GuiRenderedItem> renderLocations(Player player, String pluginId, String category) {
        if (pluginId.isBlank() || category.isBlank()) {
            return Collections.emptyList();
        }
        SettingsDisplayConfig display = settingsService.displayConfig();
        List<GuiRenderedItem> rendered = new ArrayList<>();
        for (var location : settingsService.locations(pluginId, category)) {
            boolean enabled = settingsService.isEnabled(player, pluginId, category, location.location());
            Material material = resolveIconMaterial(location.material(), enabled, display);
            String statusLine = display.statusLine(enabled);
            List<String> loreLines = new ArrayList<>();
            if (!location.lore().isEmpty()) {
                for (String line : location.lore()) {
                    loreLines.add("<!i>" + line);
                }
                loreLines.add("");
            }
            loreLines.add(statusLine);
            loreLines.add("<!i><yellow>Click to toggle");
            ItemBuilder builder = ItemBuilder.of(material)
                    .name("<!i><gold>" + location.displayName())
                    .lore(loreLines);

            Map<String, String> placeholders = new LinkedHashMap<>();
            placeholders.put("plugin_scope", pluginId);
            placeholders.put("setting_category", category);
            placeholders.put("setting_location", location.location());
            rendered.add(new GuiRenderedItem(builder.build(), placeholders));
        }
        return rendered;
    }

    private static Material resolveIconMaterial(String configured, boolean enabled, SettingsDisplayConfig display) {
        if (configured != null && !configured.isBlank()) {
            try {
                return Material.valueOf(configured.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return display.material(enabled);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String stateText(Player player, String category) {
        boolean enabled = settingsService.isEnabled(player, category);
        return settingsService.displayConfig().stateText(enabled);
    }
}

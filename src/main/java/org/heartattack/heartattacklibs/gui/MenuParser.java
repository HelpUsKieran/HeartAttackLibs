package org.heartattack.heartattacklibs.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.heartattack.heartattacklibs.gui.model.ActionDefinition;
import org.heartattack.heartattacklibs.gui.model.DynamicSectionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuItemDefinition;
import org.heartattack.heartattacklibs.gui.model.PageButtonDefinition;
import org.heartattack.heartattacklibs.gui.model.PaginationDefinition;
import org.heartattack.heartattacklibs.gui.model.RequirementDefinition;
import org.heartattack.heartattacklibs.gui.model.TextureDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

final class MenuParser {
    private MenuParser() {
    }

    static MenuDefinition parse(String defaultId, FileConfiguration config, Logger logger) {
        ConfigurationSection menuSection = config.getConfigurationSection("menu");
        if (menuSection == null) {
            logger.warning("Skipping menu file without 'menu' section: " + defaultId);
            return null;
        }

        String id = menuSection.getString("id", defaultId).toLowerCase(Locale.ROOT);
        String title = menuSection.getString("title", "GUI");
        int size = normalizeSize(menuSection.getInt("size", 54));

        ConfigurationSection settingsSection = menuSection.getConfigurationSection("settings");
        boolean cancelClicks = settingsSection == null || settingsSection.getBoolean("cancel-clicks", true);
        boolean cancelDrag = settingsSection == null || settingsSection.getBoolean("cancel-drag", true);
        boolean allowPlayerInventoryClick = settingsSection != null && settingsSection.getBoolean("allow-player-inventory-click", false);

        Map<String, String> placeholders = readMap(menuSection.getConfigurationSection("placeholders"));
        List<RequirementDefinition> openRequirements = parseRequirements(menuSection, "open-requirements");

        List<MenuItemDefinition> items = new ArrayList<>();
        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection section = itemsSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                MenuItemDefinition item = parseItem(key, section);
                if (item != null) {
                    items.add(item);
                }
            }
        }

        List<DynamicSectionDefinition> dynamicSections = new ArrayList<>();
        ConfigurationSection dynamicSection = menuSection.getConfigurationSection("dynamic");
        if (dynamicSection != null) {
            for (String key : dynamicSection.getKeys(false)) {
                ConfigurationSection section = dynamicSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                DynamicSectionDefinition parsed = parseDynamic(key, section);
                if (parsed != null) {
                    dynamicSections.add(parsed);
                }
            }
        }

        return new MenuDefinition(id, title, size, cancelClicks, cancelDrag, allowPlayerInventoryClick, placeholders, openRequirements, items, dynamicSections);
    }

    private static int normalizeSize(int size) {
        int clamped = Math.max(9, Math.min(54, size));
        int remainder = clamped % 9;
        if (remainder == 0) {
            return clamped;
        }
        return clamped + (9 - remainder);
    }

    private static MenuItemDefinition parseItem(String key, ConfigurationSection section) {
        List<Integer> slots = parseSlots(section);
        if (slots.isEmpty()) {
            return null;
        }

        String material = section.getString("material", "STONE");
        int amount = Math.max(1, section.getInt("amount", 1));
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        boolean glow = section.getBoolean("glow", false);
        List<String> flags = section.getStringList("flags");
        Integer customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
        TextureDefinition texture = parseTexture(section);

        List<RequirementDefinition> viewRequirements = parseRequirements(section, "view-requirements");
        List<RequirementDefinition> clickRequirements = parseRequirements(section, "click-requirements");
        Map<ClickType, List<ActionDefinition>> clickActions = parseClickActions(section);

        return new MenuItemDefinition(
                key.toLowerCase(Locale.ROOT),
                slots,
                material,
                amount,
                name,
                lore,
                glow,
                flags,
                customModelData,
                texture,
                viewRequirements,
                clickRequirements,
                clickActions
        );
    }

    private static DynamicSectionDefinition parseDynamic(String key, ConfigurationSection section) {
        String rendererId = section.getString("renderer-id", "").toLowerCase(Locale.ROOT);
        if (rendererId.isBlank()) {
            return null;
        }

        List<Integer> slots = parseSlots(section);
        if (slots.isEmpty()) {
            return null;
        }

        ConfigurationSection pagingSection = section.getConfigurationSection("pagination");
        PaginationDefinition pagination = new PaginationDefinition(
                pagingSection != null && pagingSection.getBoolean("enabled", false),
                pagingSection == null ? slots.size() : Math.max(1, pagingSection.getInt("page-size", slots.size())),
                pagingSection == null ? -1 : pagingSection.getInt("next-slot", -1),
                pagingSection == null ? -1 : pagingSection.getInt("previous-slot", -1),
                parsePageButton(pagingSection, "prev-button", PageButtonDefinition.prevDefault()),
                parsePageButton(pagingSection, "next-button", PageButtonDefinition.nextDefault())
        );

        List<RequirementDefinition> viewRequirements = parseRequirements(section, "view-requirements");
        List<RequirementDefinition> clickRequirements = parseRequirements(section, "click-requirements");
        Map<ClickType, List<ActionDefinition>> clickActions = parseClickActions(section);

        return new DynamicSectionDefinition(
                key.toLowerCase(Locale.ROOT),
                rendererId,
                slots,
                pagination,
                viewRequirements,
                clickRequirements,
                clickActions
        );
    }

    private static TextureDefinition parseTexture(ConfigurationSection section) {
        ConfigurationSection textureSection = section.getConfigurationSection("texture");
        if (textureSection != null) {
            String type = textureSection.getString("type", "url");
            String value = textureSection.getString("value", "");
            if (!value.isBlank()) {
                return new TextureDefinition(type.toLowerCase(Locale.ROOT), value);
            }
        }

        String legacy = section.getString("texture", "");
        if (!legacy.isBlank()) {
            return new TextureDefinition("url", legacy);
        }
        return null;
    }

    private static List<Integer> parseSlots(ConfigurationSection section) {
        if (section.contains("slots")) {
            return section.getIntegerList("slots");
        }
        if (section.contains("slot")) {
            return List.of(section.getInt("slot"));
        }
        return Collections.emptyList();
    }

    private static Map<ClickType, List<ActionDefinition>> parseClickActions(ConfigurationSection section) {
        Map<ClickType, List<ActionDefinition>> map = new EnumMap<>(ClickType.class);
        bindActions(map, List.of(ClickType.LEFT, ClickType.RIGHT, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT), parseActions(section, "click-actions"));
        bindActions(map, List.of(ClickType.LEFT), parseActions(section, "left-click-actions"));
        bindActions(map, List.of(ClickType.RIGHT), parseActions(section, "right-click-actions"));
        bindActions(map, List.of(ClickType.SHIFT_LEFT), parseActions(section, "shift-left-click-actions"));
        bindActions(map, List.of(ClickType.SHIFT_RIGHT), parseActions(section, "shift-right-click-actions"));
        return map;
    }

    private static void bindActions(Map<ClickType, List<ActionDefinition>> map, List<ClickType> clickTypes, List<ActionDefinition> actions) {
        if (actions.isEmpty()) {
            return;
        }
        for (ClickType clickType : clickTypes) {
            map.computeIfAbsent(clickType, unused -> new ArrayList<>()).addAll(actions);
        }
    }

    private static List<ActionDefinition> parseActions(ConfigurationSection section, String path) {
        if (!section.contains(path)) {
            return Collections.emptyList();
        }

        List<ActionDefinition> list = new ArrayList<>();
        List<?> values = section.getList(path, Collections.emptyList());
        for (Object value : values) {
            ActionDefinition parsed = parseAction(value);
            if (parsed != null) {
                list.add(parsed);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static ActionDefinition parseAction(Object node) {
        if (node instanceof String raw) {
            String text = raw.trim();
            if (text.startsWith("[") && text.contains("]")) {
                int end = text.indexOf(']');
                String type = text.substring(1, end).trim();
                String value = text.substring(end + 1).trim();
                return new ActionDefinition(type.toLowerCase(Locale.ROOT), value, Collections.emptyMap());
            }
            return new ActionDefinition("message", text, Collections.emptyMap());
        }

        if (node instanceof Map<?, ?> mapRaw) {
            Map<String, String> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapRaw.entrySet()) {
                map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            String type = map.getOrDefault("type", "message").toLowerCase(Locale.ROOT);
            String value = map.getOrDefault("value", "");
            map.remove("type");
            map.remove("value");
            return new ActionDefinition(type, value, map);
        }

        return null;
    }

    private static List<RequirementDefinition> parseRequirements(ConfigurationSection section, String path) {
        if (!section.contains(path)) {
            return Collections.emptyList();
        }

        List<RequirementDefinition> list = new ArrayList<>();
        List<?> values = section.getList(path, Collections.emptyList());
        for (Object value : values) {
            RequirementDefinition parsed = parseRequirement(value);
            if (parsed != null) {
                list.add(parsed);
            }
        }
        return list;
    }

    private static RequirementDefinition parseRequirement(Object node) {
        if (node instanceof String raw) {
            String[] parts = raw.split(":", 2);
            String type = parts[0].trim().toLowerCase(Locale.ROOT);
            Map<String, String> params = new LinkedHashMap<>();
            if (parts.length > 1) {
                params.put("value", parts[1].trim());
            }
            return new RequirementDefinition(type, params, "");
        }

        if (node instanceof Map<?, ?> mapRaw) {
            Map<String, String> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapRaw.entrySet()) {
                map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            String type = map.getOrDefault("type", "permission").toLowerCase(Locale.ROOT);
            String deny = map.getOrDefault("deny-message", "");
            map.remove("type");
            map.remove("deny-message");
            return new RequirementDefinition(type, map, deny);
        }

        return null;
    }

    private static PageButtonDefinition parsePageButton(ConfigurationSection pagingSection, String key, PageButtonDefinition fallback) {
        if (pagingSection == null) return fallback;
        ConfigurationSection btn = pagingSection.getConfigurationSection(key);
        if (btn == null) return fallback;
        String material = btn.getString("material", fallback.material());
        String name = btn.getString("name", fallback.name());
        List<String> lore = btn.getStringList("lore");
        Integer cmd = btn.contains("custom-model-data") ? btn.getInt("custom-model-data") : null;
        return new PageButtonDefinition(material, name, lore.isEmpty() ? fallback.lore() : lore, cmd);
    }

    private static Map<String, String> readMap(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            map.put(key, section.getString(key, ""));
        }
        return map;
    }
}


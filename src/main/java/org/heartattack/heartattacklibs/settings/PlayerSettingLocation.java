package org.heartattack.heartattacklibs.settings;

import java.util.List;

public record PlayerSettingLocation(
        String pluginId,
        String category,
        String location,
        String displayName,
        String material,
        List<String> lore,
        boolean toggleable
) {
    public PlayerSettingLocation {
        pluginId = PlayerSettingsKey.normalizePlugin(pluginId);
        category = PlayerSettingsKey.normalizeCategory(category);
        location = PlayerSettingsKey.normalizeLocation(location);
        if (displayName == null || displayName.isBlank()) {
            displayName = location;
        }
        if (lore == null) {
            lore = List.of();
        }
    }

    /** Convenience constructor — defaults {@code toggleable} to {@code true}. */
    public PlayerSettingLocation(String pluginId, String category, String location,
                                 String displayName, String material, List<String> lore) {
        this(pluginId, category, location, displayName, material, lore, true);
    }

    /** Convenience constructor — defaults {@code material}, {@code lore}, and {@code toggleable}. */
    public PlayerSettingLocation(String pluginId, String category, String location, String displayName) {
        this(pluginId, category, location, displayName, null, List.of(), true);
    }

    public String storageKey() {
        return PlayerSettingsKey.location(pluginId, category, location);
    }
}

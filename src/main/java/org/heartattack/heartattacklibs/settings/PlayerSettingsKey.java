package org.heartattack.heartattacklibs.settings;

import java.util.Locale;

public record PlayerSettingsKey(String storageKey, String pluginScope, PlayerSettingCategory category) {
    public static PlayerSettingsKey global(String category) {
        return global(PlayerSettingCategory.fromKey(category));
    }

    public static PlayerSettingsKey global(PlayerSettingCategory category) {
        return new PlayerSettingsKey(category.key(), null, category);
    }

    public static PlayerSettingsKey scoped(String pluginId, String category) {
        return scoped(pluginId, PlayerSettingCategory.fromKey(category));
    }

    public static PlayerSettingsKey scoped(String pluginId, PlayerSettingCategory category) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("Plugin scope cannot be blank.");
        }
        String scope = pluginId.trim().toLowerCase(Locale.ROOT);
        return new PlayerSettingsKey(scope + "." + category.key(), scope, category);
    }

    public static String location(String pluginId, String category, String location) {
        String scope = normalizePlugin(pluginId);
        String normalizedCategory = normalizeCategory(category);
        String normalizedLocation = normalizeLocation(location);
        return scope + "." + normalizedCategory + "." + normalizedLocation;
    }

    public static String scopedStorageKey(String pluginId, String category) {
        return normalizePlugin(pluginId) + "." + normalizeCategory(category);
    }

    public static String globalStorageKey(String category) {
        return normalizeCategory(category);
    }

    public static String normalizePlugin(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("Plugin scope cannot be blank.");
        }
        return pluginId.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeCategory(String category) {
        return PlayerSettingCategory.fromKey(category).key();
    }

    public static String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Setting location cannot be blank.");
        }
        return location.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('/', '.');
    }
}

package org.heartattack.heartattacklibs.settings;

import java.util.Locale;

public enum PlayerSettingCategory {
    MESSAGES("messages"),
    ACTIONBARS("actionbars"),
    TITLES("titles"),
    PARTICLES("particles"),
    SOUNDS("sounds");

    private final String key;

    PlayerSettingCategory(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static PlayerSettingCategory fromKey(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Setting category cannot be blank.");
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (PlayerSettingCategory category : values()) {
            if (category.key.equals(normalized)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown setting category: " + raw);
    }
}

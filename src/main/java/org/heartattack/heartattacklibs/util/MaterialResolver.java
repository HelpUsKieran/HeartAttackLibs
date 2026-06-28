package org.heartattack.heartattacklibs.util;

import org.bukkit.Material;

public final class MaterialResolver {
    private MaterialResolver() {
    }

    public static Material parseModern(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return Material.matchMaterial(input.trim());
    }

    public static Material parseModernOrDefault(String input, Material fallback) {
        Material parsed = parseModern(input);
        return parsed == null ? fallback : parsed;
    }
}

package org.heartattack.heartattacklibs.module.core;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;

public final class HeartAttackLibsPlaceholderProvider implements DPlaceholderProvider {
    @Override
    public String key() {
        return "HeartAttackLibs";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] params) {
        if (params.length == 0) {
            return "";
        }
        String sub = params[0].toLowerCase();
        return switch (sub) {
            case "name" -> player == null ? "unknown" : player.getName();
            case "world" -> player == null ? "unknown" : player.getWorld().getName();
            default -> null;
        };
    }
}

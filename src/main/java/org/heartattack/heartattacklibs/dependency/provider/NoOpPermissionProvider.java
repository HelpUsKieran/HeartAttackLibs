package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public final class NoOpPermissionProvider implements PermissionProvider {
    @Override
    public boolean available() {
        return false;
    }

    @Override
    public boolean has(Player player, String permission) {
        return player != null && player.hasPermission(permission);
    }

    @Override
    public String primaryGroup(Player player) {
        return "default";
    }
}

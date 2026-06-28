package org.heartattack.heartattacklibs.dependency.vault;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.PermissionProvider;

public final class VaultPermissionProvider implements PermissionProvider {
    private final Permission permission;

    public VaultPermissionProvider(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean available() {
        return permission != null;
    }

    @Override
    public boolean has(Player player, String perm) {
        if (player == null) {
            return false;
        }
        return permission.playerHas(player.getWorld().getName(), player, perm);
    }

    @Override
    public String primaryGroup(Player player) {
        if (player == null) {
            return "default";
        }
        return permission.getPrimaryGroup(player);
    }
}

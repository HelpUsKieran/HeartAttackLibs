package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public interface PermissionProvider {
    boolean available();

    boolean has(Player player, String permission);

    String primaryGroup(Player player);
}

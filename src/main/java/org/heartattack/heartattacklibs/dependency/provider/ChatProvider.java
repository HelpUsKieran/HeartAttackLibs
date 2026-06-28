package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public interface ChatProvider {
    boolean available();

    String prefix(Player player);

    String suffix(Player player);
}

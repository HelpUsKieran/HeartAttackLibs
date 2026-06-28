package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public interface DPlaceholderProvider {
    String key();

    String onPlaceholderRequest(Player player, String[] params);

    default String onRelationalPlaceholderRequest(Player one, Player two, String[] params) {
        return null;
    }
}

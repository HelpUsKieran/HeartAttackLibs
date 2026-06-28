package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public final class NoOpChatProvider implements ChatProvider {
    @Override
    public boolean available() {
        return false;
    }

    @Override
    public String prefix(Player player) {
        return "";
    }

    @Override
    public String suffix(Player player) {
        return "";
    }
}

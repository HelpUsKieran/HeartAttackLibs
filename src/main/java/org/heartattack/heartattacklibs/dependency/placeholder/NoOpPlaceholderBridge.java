package org.heartattack.heartattacklibs.dependency.placeholder;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;

public final class NoOpPlaceholderBridge implements PlaceholderBridge {
    @Override
    public boolean initialize() {
        return false;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void registerProvider(DPlaceholderProvider provider) {
    }

    @Override
    public void unregisterProvider(String key) {
    }

    @Override
    public String parse(Player player, String text) {
        return text;
    }
}

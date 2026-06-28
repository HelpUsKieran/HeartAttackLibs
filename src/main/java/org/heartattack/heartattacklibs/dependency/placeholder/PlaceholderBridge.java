package org.heartattack.heartattacklibs.dependency.placeholder;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;

public interface PlaceholderBridge {
    boolean initialize();

    void shutdown();

    void registerProvider(DPlaceholderProvider provider);

    void unregisterProvider(String key);

    String parse(Player player, String text);
}

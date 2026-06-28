package org.heartattack.heartattacklibs.dependency;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.ChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;
import org.heartattack.heartattacklibs.dependency.provider.EconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.HologramProvider;
import org.heartattack.heartattacklibs.dependency.provider.PermissionProvider;

import java.util.Map;

public interface DependencyFramework {
    DependencyManager manager();

    void initialize();

    void shutdown();

    void registerPlaceholderProvider(DPlaceholderProvider provider);

    void unregisterPlaceholderProvider(String key);

    String parsePlaceholders(Player player, String text);

    EconomyProvider economy();

    PermissionProvider permission();

    ChatProvider chat();

    HologramProvider holograms();

    DependencyStatus status(DependencyCapability capability);

    Map<DependencyCapability, DependencyStatus> statuses();
}

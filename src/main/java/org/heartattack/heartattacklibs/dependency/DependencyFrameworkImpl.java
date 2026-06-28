package org.heartattack.heartattacklibs.dependency;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.ChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;
import org.heartattack.heartattacklibs.dependency.provider.EconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.HologramProvider;
import org.heartattack.heartattacklibs.dependency.provider.PermissionProvider;

import java.util.Map;

public final class DependencyFrameworkImpl implements DependencyFramework {
    private final DependencyManager manager;

    public DependencyFrameworkImpl(DependencyManager manager) {
        this.manager = manager;
    }

    @Override
    public DependencyManager manager() {
        return manager;
    }

    @Override
    public void initialize() {
        manager.initialize();
    }

    @Override
    public void shutdown() {
        manager.shutdown();
    }

    @Override
    public void registerPlaceholderProvider(DPlaceholderProvider provider) {
        manager.registerPlaceholderProvider(provider);
    }

    @Override
    public void unregisterPlaceholderProvider(String key) {
        manager.unregisterPlaceholderProvider(key);
    }

    @Override
    public String parsePlaceholders(Player player, String text) {
        return manager.parsePlaceholders(player, text);
    }

    @Override
    public EconomyProvider economy() {
        return manager.economy();
    }

    @Override
    public PermissionProvider permission() {
        return manager.permission();
    }

    @Override
    public ChatProvider chat() {
        return manager.chat();
    }

    @Override
    public HologramProvider holograms() {
        return manager.holograms();
    }

    @Override
    public DependencyStatus status(DependencyCapability capability) {
        return manager.status(capability);
    }

    @Override
    public Map<DependencyCapability, DependencyStatus> statuses() {
        return manager.statuses();
    }
}

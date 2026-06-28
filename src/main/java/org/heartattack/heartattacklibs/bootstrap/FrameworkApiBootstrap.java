package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApi;

@Singleton
public final class FrameworkApiBootstrap {
    private final HeartAttackLibs plugin;
    private final HeartAttackLibsApi api;

    @Inject
    public FrameworkApiBootstrap(HeartAttackLibs plugin, HeartAttackLibsApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void register() {
        Bukkit.getServicesManager().register(HeartAttackLibsApi.class, api, plugin, ServicePriority.Normal);
    }

    public void unregister() {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }
}

package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.database.DatabaseManager;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.hologram.HologramManager;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.settings.ExternalMessageFilter;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;

@Singleton
public final class PluginBootstrap {
    private final HeartAttackLibs plugin;
    private final DatabaseManager databaseManager;
    private final DebugManager debugManager;
    private final HologramManager hologramManager;
    private final MessageManager messageManager;
    private final DependencyManager dependencyManager;
    private final FrameworkApiBootstrap apiBootstrap;
    private final FrameworkGuiBootstrap guiBootstrap;
    private final FrameworkCommandBootstrap commandBootstrap;
    private final FrameworkModuleBootstrap moduleBootstrap;
    private final FrameworkReloadBootstrap reloadBootstrap;
    private final PlayerSettingsService playerSettingsService;
    private final ExternalMessageFilter externalMessageFilter;

    private volatile boolean reloading;

    @Inject
    public PluginBootstrap(
            HeartAttackLibs plugin,
            DatabaseManager databaseManager,
            DebugManager debugManager,
            HologramManager hologramManager,
            MessageManager messageManager,
            DependencyManager dependencyManager,
            FrameworkApiBootstrap apiBootstrap,
            FrameworkGuiBootstrap guiBootstrap,
            FrameworkCommandBootstrap commandBootstrap,
            FrameworkModuleBootstrap moduleBootstrap,
            FrameworkReloadBootstrap reloadBootstrap,
            PlayerSettingsService playerSettingsService,
            ExternalMessageFilter externalMessageFilter
    ) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.debugManager = debugManager;
        this.hologramManager = hologramManager;
        this.messageManager = messageManager;
        this.dependencyManager = dependencyManager;
        this.apiBootstrap = apiBootstrap;
        this.guiBootstrap = guiBootstrap;
        this.commandBootstrap = commandBootstrap;
        this.moduleBootstrap = moduleBootstrap;
        this.reloadBootstrap = reloadBootstrap;
        this.playerSettingsService = playerSettingsService;
        this.externalMessageFilter = externalMessageFilter;
    }

    public void enable() {
        playerSettingsService.initializeStorage();
        Bukkit.getPluginManager().registerEvents(externalMessageFilter, plugin);
        guiBootstrap.enable();
        messageManager.reload();
        dependencyManager.initialize();
        commandBootstrap.registerFrameworkCommands();
        moduleBootstrap.enableModules();
        apiBootstrap.register();
        debugManager.debug("bootstrap", "Managers initialized successfully.");
        plugin.getLogger().info("HeartAttackLibs enabled with module, command, db, gui, hologram, and message systems.");

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (playerSettingsService.writeNewDiscoveries()) {
                plugin.getLogger().info("[HeartAttackLibs] Wrote initial settings files to plugins/HeartAttackLibs/settings/");
            }
        });
    }

    public void disable() {
        HandlerList.unregisterAll(externalMessageFilter);
        apiBootstrap.unregister();
        moduleBootstrap.disableModules();
        dependencyManager.shutdown();
        databaseManager.close();
        hologramManager.despawnAll();
        commandBootstrap.unregisterAll();
    }

    public void reloadFramework() {
        if (reloading) {
            return;
        }

        reloading = true;
        try {
            reloadBootstrap.reloadFramework();
            debugManager.debug("reload", "Framework reload completed.");
        } finally {
            reloading = false;
        }
    }
}

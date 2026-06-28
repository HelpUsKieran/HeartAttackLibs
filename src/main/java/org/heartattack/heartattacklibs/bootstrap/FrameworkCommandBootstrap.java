package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.command.demo.HeartAttackLibsAdminCommand;
import org.heartattack.heartattacklibs.command.demo.HeartAttackLibsDemoCommand;
import org.heartattack.heartattacklibs.command.demo.HeartAttackLibsSettingsCommand;
import org.heartattack.heartattacklibs.command.simple.SimpleCommands;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;

@Singleton
public final class FrameworkCommandBootstrap {
    private final HeartAttackLibs plugin;
    private final CommandManager commandManager;
    private final PlayerSettingsService playerSettingsService;

    @Inject
    public FrameworkCommandBootstrap(HeartAttackLibs plugin, CommandManager commandManager, PlayerSettingsService playerSettingsService) {
        this.plugin = plugin;
        this.commandManager = commandManager;
        this.playerSettingsService = playerSettingsService;
    }

    public void registerFrameworkCommands() {
        commandManager.registerAll(
                new HeartAttackLibsAdminCommand(plugin),
                new HeartAttackLibsDemoCommand(plugin),
                new HeartAttackLibsSettingsCommand(plugin, playerSettingsService),
                SimpleCommands.command("heartattacklibsreload")
                        .aliases("halreload", "heartlibsreload")
                        .description("Reload HeartAttackLibs framework files.")
                        .usage("/heartattacklibsreload")
                        .permission("heartattacklibs.reload")
                        .executor(context -> {
                            plugin.reloadFramework();
                            plugin.messageManager().container("framework-reloaded").send(context.sender());
                        })
                        .build()
        );
    }

    public void unregisterAll() {
        commandManager.unregisterAll();
    }
}

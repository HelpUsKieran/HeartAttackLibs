package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.heartattack.heartattacklibs.command.CommandManager;

@Singleton
public final class FrameworkCommandBootstrap {
    private final CommandManager commandManager;

    @Inject
    public FrameworkCommandBootstrap(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void registerFrameworkCommands() {
    }

    public void unregisterAll() {
        commandManager.unregisterAll();
    }
}

package org.heartattack.heartattacklibs.command;

import org.heartattack.heartattacklibs.command.simple.SimpleCommandBuilder;
import org.heartattack.heartattacklibs.command.simple.SimpleCommands;
import org.heartattack.heartattacklibs.command.simple.SubcommandRouterBuilder;

public final class CommandFrameworkImpl implements CommandFramework {
    private final CommandManager commandManager;

    public CommandFrameworkImpl(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public CommandManager manager() {
        return commandManager;
    }

    @Override
    public void register(SimpleCommand command) {
        commandManager.register(command);
    }

    @Override
    public void registerAll(SimpleCommand... commands) {
        commandManager.registerAll(commands);
    }

    @Override
    public boolean unregister(String commandName) {
        return commandManager.unregister(commandName);
    }

    @Override
    public void unregisterAll() {
        commandManager.unregisterAll();
    }

    @Override
    public SimpleCommandBuilder command(String name) {
        return SimpleCommands.command(name);
    }

    @Override
    public SubcommandRouterBuilder router(String name) {
        return SimpleCommands.router(name);
    }
}

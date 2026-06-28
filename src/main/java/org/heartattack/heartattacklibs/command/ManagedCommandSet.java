package org.heartattack.heartattacklibs.command;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ManagedCommandSet {
    private final CommandManager commandManager;
    private final Set<String> registeredNames = new LinkedHashSet<>();

    public ManagedCommandSet(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void register(SimpleCommand command) {
        String commandName = command.name().toLowerCase();
        if (registeredNames.contains(commandName)) {
            commandManager.unregister(commandName);
            registeredNames.remove(commandName);
        }
        commandManager.register(command);
        registeredNames.add(commandName);
    }

    public void unregisterAll() {
        for (String commandName : registeredNames) {
            commandManager.unregister(commandName);
        }
        registeredNames.clear();
    }
}

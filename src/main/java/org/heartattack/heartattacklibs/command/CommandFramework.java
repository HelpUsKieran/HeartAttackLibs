package org.heartattack.heartattacklibs.command;

import org.heartattack.heartattacklibs.command.simple.SimpleCommandBuilder;
import org.heartattack.heartattacklibs.command.simple.SubcommandRouterBuilder;

public interface CommandFramework {
    CommandManager manager();

    void register(SimpleCommand command);

    void registerAll(SimpleCommand... commands);

    boolean unregister(String commandName);

    void unregisterAll();

    SimpleCommandBuilder command(String name);

    SubcommandRouterBuilder router(String name);
}

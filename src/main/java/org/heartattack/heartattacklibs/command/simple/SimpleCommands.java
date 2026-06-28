package org.heartattack.heartattacklibs.command.simple;

public final class SimpleCommands {
    private SimpleCommands() {
    }

    public static SimpleCommandBuilder command(String name) {
        return new SimpleCommandBuilder(name);
    }

    public static SubcommandRouterBuilder router(String name) {
        return new SubcommandRouterBuilder(name);
    }
}

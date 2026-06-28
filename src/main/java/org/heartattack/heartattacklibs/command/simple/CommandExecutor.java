package org.heartattack.heartattacklibs.command.simple;

import org.heartattack.heartattacklibs.command.CommandContext;

@FunctionalInterface
public interface CommandExecutor {
    void execute(CommandContext context);
}

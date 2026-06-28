package org.heartattack.heartattacklibs.command.simple;

import org.heartattack.heartattacklibs.command.CommandContext;

import java.util.List;

@FunctionalInterface
public interface CommandCompleter {
    List<String> complete(CommandContext context);
}

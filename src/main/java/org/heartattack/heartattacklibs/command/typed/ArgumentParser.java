package org.heartattack.heartattacklibs.command.typed;

import org.heartattack.heartattacklibs.command.CommandContext;

import java.util.List;

public interface ArgumentParser<T> {
    T parse(CommandContext context, String input) throws IllegalArgumentException;

    default List<String> suggest(CommandContext context, String input) {
        return List.of();
    }

    String typeName();
}

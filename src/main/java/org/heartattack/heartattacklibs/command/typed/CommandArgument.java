package org.heartattack.heartattacklibs.command.typed;

public record CommandArgument<T>(
        String name,
        ArgumentParser<T> parser,
        boolean optional
) {
    public static <T> CommandArgument<T> required(String name, ArgumentParser<T> parser) {
        return new CommandArgument<>(name, parser, false);
    }

    public static <T> CommandArgument<T> optional(String name, ArgumentParser<T> parser) {
        return new CommandArgument<>(name, parser, true);
    }
}

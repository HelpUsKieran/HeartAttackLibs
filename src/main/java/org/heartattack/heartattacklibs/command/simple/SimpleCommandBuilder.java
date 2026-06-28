package org.heartattack.heartattacklibs.command.simple;

import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SimpleCommandBuilder {
    private final String name;
    private final List<String> aliases = new ArrayList<>();

    private String description = "No description.";
    private String usage;
    private String permission;
    private boolean playerOnly;

    private String permissionMessage = "<red>You do not have permission to use this command.";
    private String playerOnlyMessage = "<red>This command can only be used by players.";
    private String errorMessage = "<red>An error occurred while running this command.";

    private CommandExecutor executor = context -> {};
    private CommandCompleter completer = context -> List.of();

    SimpleCommandBuilder(String name) {
        this.name = normalize(name);
        this.usage = "/" + this.name;
    }

    public SimpleCommandBuilder aliases(String... aliases) {
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            this.aliases.add(normalize(alias));
        }
        return this;
    }

    public SimpleCommandBuilder description(String description) {
        this.description = description == null || description.isBlank() ? this.description : description;
        return this;
    }

    public SimpleCommandBuilder usage(String usage) {
        this.usage = usage == null || usage.isBlank() ? this.usage : usage;
        return this;
    }

    public SimpleCommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SimpleCommandBuilder playerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
        return this;
    }

    public SimpleCommandBuilder permissionMessage(String permissionMessage) {
        if (permissionMessage != null && !permissionMessage.isBlank()) {
            this.permissionMessage = permissionMessage;
        }
        return this;
    }

    public SimpleCommandBuilder playerOnlyMessage(String playerOnlyMessage) {
        if (playerOnlyMessage != null && !playerOnlyMessage.isBlank()) {
            this.playerOnlyMessage = playerOnlyMessage;
        }
        return this;
    }

    public SimpleCommandBuilder errorMessage(String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            this.errorMessage = errorMessage;
        }
        return this;
    }

    public SimpleCommandBuilder executor(CommandExecutor executor) {
        this.executor = executor == null ? context -> {} : executor;
        return this;
    }

    public SimpleCommandBuilder completer(CommandCompleter completer) {
        this.completer = completer == null ? context -> List.of() : completer;
        return this;
    }

    public SimpleCommand build() {
        return new BuiltSimpleCommand(
                name,
                List.copyOf(aliases),
                description,
                usage,
                permission,
                playerOnly,
                permissionMessage,
                playerOnlyMessage,
                errorMessage,
                executor,
                completer
        );
    }

    private static String normalize(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
    }

    private record BuiltSimpleCommand(
            String name,
            List<String> aliases,
            String description,
            String usage,
            String permission,
            boolean playerOnly,
            String permissionMessage,
            String playerOnlyMessage,
            String errorMessage,
            CommandExecutor executor,
            CommandCompleter completer
    ) implements SimpleCommand {
        @Override
        public List<String> aliases() {
            return aliases;
        }

        @Override
        public void execute(CommandContext context) {
            executor.execute(context);
        }

        @Override
        public List<String> tabComplete(CommandContext context) {
            List<String> suggestions = completer.complete(context);
            return suggestions == null ? Collections.emptyList() : suggestions;
        }
    }
}

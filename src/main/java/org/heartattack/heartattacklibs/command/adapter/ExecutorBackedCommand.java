package org.heartattack.heartattacklibs.command.adapter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;

import java.util.List;

public final class ExecutorBackedCommand implements SimpleCommand {
    private final String name;
    private final List<String> aliases;
    private final String description;
    private final String usage;
    private final CommandExecutor executor;
    private final TabCompleter completer;

    public ExecutorBackedCommand(
            String name,
            List<String> aliases,
            String description,
            String usage,
            CommandExecutor executor,
            TabCompleter completer
    ) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
        this.executor = executor;
        this.completer = completer;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String usage() {
        return usage;
    }

    @Override
    public void execute(CommandContext context) {
        executor.onCommand(context.sender(), (Command) null, context.label(), context.args());
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (completer == null) {
            return List.of();
        }
        List<String> suggestions = completer.onTabComplete(context.sender(), (Command) null, context.label(), context.args());
        return suggestions == null ? List.of() : suggestions;
    }
}

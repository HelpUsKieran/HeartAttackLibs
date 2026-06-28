package org.heartattack.heartattacklibs.command.sub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class SubCommandDispatcher implements CommandExecutor, TabCompleter {
    private final List<? extends ManagedSubCommand> subCommands;
    private final Consumer<CommandSender> noPermissionSender;
    private final Consumer<CommandSender> helpHeaderSender;
    private final BiConsumer<CommandSender, ManagedSubCommand> helpLineSender;

    public SubCommandDispatcher(
            List<? extends ManagedSubCommand> subCommands,
            Consumer<CommandSender> noPermissionSender,
            Consumer<CommandSender> helpHeaderSender,
            BiConsumer<CommandSender, ManagedSubCommand> helpLineSender
    ) {
        this.subCommands = subCommands;
        this.noPermissionSender = noPermissionSender;
        this.helpHeaderSender = helpHeaderSender;
        this.helpLineSender = helpLineSender;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            for (ManagedSubCommand subCommand : subCommands) {
                if (!subCommand.getName().equalsIgnoreCase(args[0])) {
                    continue;
                }
                if (!sender.hasPermission(subCommand.permission())) {
                    noPermissionSender.accept(sender);
                    return true;
                }
                subCommand.perform(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
        }

        helpHeaderSender.accept(sender);
        for (ManagedSubCommand subCommand : subCommands) {
            helpLineSender.accept(sender, subCommand);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String argument = args[0].toLowerCase();
            for (ManagedSubCommand subCommand : subCommands) {
                String name = subCommand.getName().toLowerCase();
                if (name.startsWith(argument) && sender.hasPermission(subCommand.permission())) {
                    completions.add(name);
                }
            }
            return completions;
        }

        for (ManagedSubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[0]) && sender.hasPermission(subCommand.permission())) {
                return subCommand.onTabComplete(player, args);
            }
        }

        return null;
    }
}

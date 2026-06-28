package org.heartattack.heartattacklibs.command.simple;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class SubcommandRouterBuilder {
    private final String name;
    private final Map<String, RouteEntry> routes = new LinkedHashMap<>();
    private final List<String> aliases = new ArrayList<>();

    private String description = "No description.";
    private String usage;
    private String permission;
    private CommandExecutor defaultExecutor = context -> context.sender().sendRichMessage(UnicodeSmallCaps.apply("<red>Unknown subcommand."));

    SubcommandRouterBuilder(String name) {
        this.name = normalize(name);
        this.usage = "/" + this.name + " <subcommand>";
    }

    public SubcommandRouterBuilder aliases(String... aliases) {
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            this.aliases.add(normalize(alias));
        }
        return this;
    }

    public SubcommandRouterBuilder description(String description) {
        this.description = description == null || description.isBlank() ? this.description : description;
        return this;
    }

    public SubcommandRouterBuilder usage(String usage) {
        this.usage = usage == null || usage.isBlank() ? this.usage : usage;
        return this;
    }

    public SubcommandRouterBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SubcommandRouterBuilder defaultExecutor(CommandExecutor executor) {
        this.defaultExecutor = executor == null ? this.defaultExecutor : executor;
        return this;
    }

    public SubcommandRouterBuilder subcommand(String name, Consumer<SimpleCommandBuilder> consumer) {
        SimpleCommandBuilder builder = SimpleCommands.command(name);
        consumer.accept(builder);
        SimpleCommand command = builder.build();

        RouteEntry entry = new RouteEntry(command, normalize(command.name()));
        routes.put(entry.primaryKey, entry);
        for (String alias : command.aliases()) {
            routes.put(normalize(alias), entry);
        }
        return this;
    }

    public SimpleCommand build() {
        return new RouterCommand(name, List.copyOf(aliases), description, usage, permission, new LinkedHashMap<>(routes), defaultExecutor);
    }

    private static String normalize(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
    }

    private record RouteEntry(SimpleCommand command, String primaryKey) {
    }

    private record RouterCommand(
            String name,
            List<String> aliases,
            String description,
            String usage,
            String permission,
            Map<String, RouteEntry> routes,
            CommandExecutor defaultExecutor
    ) implements SimpleCommand {
        @Override
        public List<String> aliases() {
            return aliases;
        }

        @Override
        public void execute(CommandContext context) {
            if (context.args().length == 0) {
                defaultExecutor.execute(context);
                return;
            }

            String sub = normalize(context.args()[0]);
            RouteEntry route = routes.get(sub);
            if (route == null) {
                defaultExecutor.execute(context);
                return;
            }

            SimpleCommand subCommand = route.command;
            String permission = subCommand.permission();
            if (permission != null && !permission.isBlank() && !context.sender().hasPermission(permission)) {
                context.sender().sendRichMessage(UnicodeSmallCaps.apply(subCommand.permissionMessage()));
                return;
            }
            if (subCommand.playerOnly() && !(context.sender() instanceof Player)) {
                context.sender().sendRichMessage(UnicodeSmallCaps.apply(subCommand.playerOnlyMessage()));
                return;
            }

            String[] delegated = java.util.Arrays.copyOfRange(context.args(), 1, context.args().length);
            subCommand.execute(new CommandContext(context.plugin(), context.sender(), subCommand.name(), delegated));
        }

        @Override
        public List<String> tabComplete(CommandContext context) {
            if (context.args().length <= 1) {
                String input = context.args().length == 0 ? "" : normalize(context.args()[0]);
                return routes.entrySet().stream()
                        .filter(entry -> entry.getValue().primaryKey.equals(entry.getKey()))
                        .filter(entry -> canUse(context, entry.getValue().command))
                        .map(Map.Entry::getKey)
                        .filter(name -> name.startsWith(input))
                        .toList();
            }

            RouteEntry route = routes.get(normalize(context.args()[0]));
            if (route == null || !canUse(context, route.command)) {
                return List.of();
            }

            String[] delegated = java.util.Arrays.copyOfRange(context.args(), 1, context.args().length);
            return route.command.tabComplete(new CommandContext(context.plugin(), context.sender(), route.command.name(), delegated));
        }

        private static boolean canUse(CommandContext context, SimpleCommand command) {
            String permission = command.permission();
            return permission == null || permission.isBlank() || context.sender().hasPermission(permission);
        }
    }
}

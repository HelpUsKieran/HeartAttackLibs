package org.heartattack.heartattacklibs.command.typed;

import org.bukkit.command.CommandSender;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;

import java.util.ArrayList;
import java.util.List;

public abstract class TypedCommand implements SimpleCommand {
    protected abstract List<CommandArgument<?>> arguments();

    protected abstract void executeTyped(CommandContext context, ParsedArguments arguments);

    protected void onArgumentError(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    @Override
    public final void execute(CommandContext context) {
        ParsedArguments parsed = new ParsedArguments();
        List<CommandArgument<?>> argumentDefinitions = arguments();
        String[] raw = context.args();
        if (raw.length > argumentDefinitions.size()) {
            onArgumentError(context.sender(), "Too many arguments.");
            return;
        }

        for (int i = 0; i < argumentDefinitions.size(); i++) {
            CommandArgument<?> definition = argumentDefinitions.get(i);
            if (i >= raw.length) {
                if (!definition.optional()) {
                    onArgumentError(context.sender(), "Missing argument <" + definition.name() + ":" + definition.parser().typeName() + ">");
                    return;
                }
                continue;
            }

            String input = raw[i];
            try {
                Object parsedValue = definition.parser().parse(context, input);
                parsed.put(definition.name(), parsedValue);
            } catch (IllegalArgumentException exception) {
                onArgumentError(
                        context.sender(),
                        "Invalid argument '" + definition.name() + "': " + exception.getMessage()
                );
                return;
            }
        }

        executeTyped(context, parsed);
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        List<CommandArgument<?>> argumentDefinitions = arguments();
        int rawLength = context.args().length;
        if (rawLength == 0 || rawLength > argumentDefinitions.size()) {
            return List.of();
        }

        int targetIndex = rawLength - 1;
        CommandArgument<?> argument = argumentDefinitions.get(targetIndex);
        String input = context.args()[targetIndex];

        List<String> suggestions = new ArrayList<>(argument.parser().suggest(context, input));
        suggestions.removeIf(s -> !s.toLowerCase().startsWith(input.toLowerCase()));
        return suggestions;
    }
}

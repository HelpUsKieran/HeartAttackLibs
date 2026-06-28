package org.heartattack.heartattacklibs.command;

import java.util.Collections;
import java.util.List;

public interface SimpleCommand {
    String name();

    default List<String> aliases() {
        return Collections.emptyList();
    }

    default String description() {
        return "No description.";
    }

    default String usage() {
        return "/" + name();
    }

    default String permission() {
        return null;
    }

    default String permissionMessage() {
        return "<red>You do not have permission to use this command.";
    }

    default boolean playerOnly() {
        return false;
    }

    default String playerOnlyMessage() {
        return "<red>This command can only be used by players.";
    }

    default String errorMessage() {
        return "<red>An error occurred while running this command.";
    }

    void execute(CommandContext context);

    default List<String> tabComplete(CommandContext context) {
        return Collections.emptyList();
    }

    default boolean shouldShowInTab(CommandContext context) {
        return permission() == null || permission().isBlank() || context.sender().hasPermission(permission());
    }
}

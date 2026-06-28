package org.heartattack.heartattacklibs.command.sub;

import org.bukkit.entity.Player;

import java.util.List;

public interface ManagedSubCommand {
    String getName();

    String getDescription();

    String getSyntax();

    void perform(org.bukkit.command.CommandSender sender, String[] args);

    String permission();

    List<String> onTabComplete(Player player, String[] args);
}

package org.heartattack.heartattacklibs.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandContext {
    private final JavaPlugin plugin;
    private final CommandSender sender;
    private final String label;
    private final String[] args;

    public CommandContext(JavaPlugin plugin, CommandSender sender, String label, String[] args) {
        this.plugin = plugin;
        this.sender = sender;
        this.label = label;
        this.args = args;
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public CommandSender sender() {
        return sender;
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player asPlayer() {
        if (!(sender instanceof Player player)) {
            throw new IllegalStateException("Command sender is not a player.");
        }
        return player;
    }
}

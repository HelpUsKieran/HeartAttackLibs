package org.heartattack.heartattacklibs.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandManager {
    private final JavaPlugin plugin;
    private final CommandMap commandMap;
    private final Map<String, ManagedCommand> byPrimary = new ConcurrentHashMap<>();
    private final Map<String, ManagedCommand> activeLookup = new ConcurrentHashMap<>();

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = resolveCommandMap();
    }

    public void register(SimpleCommand simpleCommand) {
        String primaryKey = normalize(simpleCommand.name());
        ManagedCommand existing = byPrimary.get(primaryKey);
        if (existing != null) {
            existing.rebind(simpleCommand);
            existing.setActive(true);
            refreshLookupFor(existing);
            return;
        }

        ManagedCommand command = new ManagedCommand(plugin, simpleCommand);
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
        byPrimary.put(primaryKey, command);
        refreshLookupFor(command);
        syncCommandsIfPossible();
    }

    public void registerAll(SimpleCommand... commands) {
        for (SimpleCommand command : commands) {
            register(command);
        }
    }

    public boolean unregister(String commandName) {
        String key = normalize(commandName);
        ManagedCommand managedCommand = byPrimary.remove(key);
        if (managedCommand == null) {
            managedCommand = activeLookup.remove(key);
            if (managedCommand != null) {
                byPrimary.remove(normalize(managedCommand.getName()));
            }
        }
        if (managedCommand == null) {
            return false;
        }
        managedCommand.setActive(false);
        removeLookupFor(managedCommand);
        try {
            managedCommand.unregister(commandMap);
            Map<String, Command> knownCommands = resolveKnownCommands();
            if (knownCommands != null) {
                knownCommands.remove(managedCommand.getName());
                knownCommands.remove(plugin.getName().toLowerCase(Locale.ROOT) + ":" + managedCommand.getName());
                for (String alias : managedCommand.getAliases()) {
                    knownCommands.remove(alias);
                    knownCommands.remove(plugin.getName().toLowerCase(Locale.ROOT) + ":" + alias);
                }
            }
            syncCommandsIfPossible();
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to fully unregister command " + managedCommand.getName() + ": " + exception.getMessage());
        }
        return true;
    }

    public void unregisterAll() {
        if (byPrimary.isEmpty()) {
            return;
        }

        try {
            Map<String, Command> knownCommands = resolveKnownCommands();
            for (ManagedCommand managedCommand : new ArrayList<>(byPrimary.values())) {
                try {
                    managedCommand.unregister(commandMap);
                    if (knownCommands != null) {
                        knownCommands.remove(managedCommand.getName());
                        knownCommands.remove(plugin.getName().toLowerCase(Locale.ROOT) + ":" + managedCommand.getName());
                        for (String alias : managedCommand.getAliases()) {
                            knownCommands.remove(alias);
                            knownCommands.remove(plugin.getName().toLowerCase(Locale.ROOT) + ":" + alias);
                        }
                    }
                } catch (Exception exception) {
                    plugin.getLogger().warning("Failed to unregister command " + managedCommand.getName() + ": " + exception.getMessage());
                }
            }
            activeLookup.clear();
            byPrimary.clear();
            syncCommandsIfPossible();
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to unregister commands cleanly: " + exception.getMessage());
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Method getter = Bukkit.getServer().getClass().getMethod("getCommandMap");
            Object map = getter.invoke(Bukkit.getServer());
            if (map instanceof CommandMap commandMap) {
                return commandMap;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Object craftServer = Bukkit.getServer();
            Field field = craftServer.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(craftServer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not access Bukkit command map.", exception);
        }
    }

    private void syncCommandsIfPossible() {
        try {
            Method method = Bukkit.getServer().getClass().getMethod("syncCommands");
            method.invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> resolveKnownCommands() {
        if (commandMap instanceof SimpleCommandMap simpleCommandMap) {
            return simpleCommandMap.getKnownCommands();
        }

        try {
            Method getter = commandMap.getClass().getMethod("getKnownCommands");
            Object result = getter.invoke(commandMap);
            if (result instanceof Map<?, ?> map) {
                return (Map<String, Command>) map;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public Set<String> registeredCommands() {
        return new HashSet<>(byPrimary.keySet());
    }

    private void refreshLookupFor(ManagedCommand command) {
        removeLookupFor(command);

        Set<String> keys = new LinkedHashSet<>();
        keys.add(normalize(command.getName()));
        keys.add(normalize(plugin.getName()) + ":" + normalize(command.getName()));
        for (String alias : command.getAliases()) {
            keys.add(normalize(alias));
            keys.add(normalize(plugin.getName()) + ":" + normalize(alias));
        }

        for (String key : keys) {
            ManagedCommand existing = activeLookup.get(key);
            if (existing != null && existing != command) {
                plugin.getLogger().warning("Command label conflict for '" + key + "', newest registration from " + plugin.getName() + " takes precedence.");
            }
            activeLookup.put(key, command);
        }
    }

    private void removeLookupFor(ManagedCommand command) {
        activeLookup.entrySet().removeIf(entry -> entry.getValue() == command);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private static final class ManagedCommand extends Command {
        private final JavaPlugin plugin;
        private volatile SimpleCommand delegate;
        private volatile boolean active = true;

        private ManagedCommand(JavaPlugin plugin, SimpleCommand delegate) {
            super(delegate.name(), delegate.description(), delegate.usage(), delegate.aliases());
            this.plugin = plugin;
            this.delegate = delegate;
            applyDelegateMetadata(delegate);
        }

        private void rebind(SimpleCommand delegate) {
            this.delegate = delegate;
            applyDelegateMetadata(delegate);
        }

        private void setActive(boolean active) {
            this.active = active;
        }

        private void applyDelegateMetadata(SimpleCommand delegate) {
            if (delegate.permission() != null && !delegate.permission().isBlank()) {
                setPermission(delegate.permission());
            } else {
                setPermission(null);
            }
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (!active) {
                sender.sendMessage("This command is currently unavailable.");
                return true;
            }

            SimpleCommand current = delegate;
            if (getPermission() != null && !getPermission().isBlank() && !sender.hasPermission(getPermission())) {
                sender.sendRichMessage(UnicodeSmallCaps.apply(current.permissionMessage()));
                return true;
            }
            if (current.playerOnly() && !(sender instanceof Player)) {
                sender.sendRichMessage(UnicodeSmallCaps.apply(current.playerOnlyMessage()));
                return true;
            }

            try {
                current.execute(new CommandContext(plugin, sender, commandLabel, args));
            } catch (Exception exception) {
                sender.sendRichMessage(UnicodeSmallCaps.apply(current.errorMessage()));
                plugin.getLogger().warning("Command error for /" + commandLabel + ": " + exception.getMessage());
                exception.printStackTrace();
            }
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (!active) {
                return Collections.emptyList();
            }
            try {
                SimpleCommand current = delegate;
                if (!current.shouldShowInTab(new CommandContext(plugin, sender, alias, args))) {
                    return Collections.emptyList();
                }
                List<String> options = current.tabComplete(new CommandContext(plugin, sender, alias, args));
                return options == null ? Collections.emptyList() : options;
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
        }
    }
}

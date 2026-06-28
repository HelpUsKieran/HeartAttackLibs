package org.heartattack.heartattacklibs.command.demo;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.SimpleCommand;
import org.heartattack.heartattacklibs.command.simple.SimpleCommands;
import org.heartattack.heartattacklibs.dependency.DependencyCapability;
import org.heartattack.heartattacklibs.dependency.DependencyStatus;
import org.heartattack.heartattacklibs.module.ModuleState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HeartAttackLibsAdminCommand implements SimpleCommand {
    private final HeartAttackLibs plugin;
    private final SimpleCommand delegate;

    public HeartAttackLibsAdminCommand(HeartAttackLibs plugin) {
        this.plugin = plugin;
        this.delegate = buildDelegate();
    }

    private SimpleCommand buildDelegate() {
        return SimpleCommands.router("heartattacklibs")
                .aliases("halibs", "heartlibs")
                .description("HeartAttackLibs admin command.")
                .usage("/heartattacklibs <help|list|demo|debug|reload|modules|deps>")
                .permission("heartattacklibs.admin")
                .defaultExecutor(context -> sendHelp(context.sender()))
                .subcommand("help", command -> command
                        .description("Show command help")
                        .executor(context -> sendHelp(context.sender()))
                )
                .subcommand("list", command -> command
                        .description("List plugins hooked into HeartAttackLibs")
                        .executor(context -> listHookedPlugins(context.sender()))
                )
                .subcommand("demo", command -> command
                        .description("Forward to /heartattacklibsdemo")
                        .usage("/heartattacklibs demo <...>")
                        .executor(this::handleDemoForward)
                )
                .subcommand("debug", command -> command
                        .description("Toggle or inspect debug mode")
                        .usage("/heartattacklibs debug <status|toggle|on|off>")
                        .executor(context -> handleDebug(context.sender(), context.args()))
                        .completer(context -> context.args().length <= 1 ? List.of("status", "toggle", "on", "off") : List.of())
                )
                .subcommand("reload", command -> command
                        .description("Reload HeartAttackLibs framework files")
                        .executor(context -> {
                            plugin.reloadFramework();
                            plugin.messageManager().container("framework-reloaded").send(context.sender());
                        })
                )
                .subcommand("modules", command -> command
                        .description("Show module states")
                        .executor(context -> {
                            for (Map.Entry<String, ModuleState> entry : plugin.moduleManager().states().entrySet()) {
                                plugin.miniMessage().sendWithPrefix(context.sender(), "<yellow>" + entry.getKey() + "<gray>: <white>" + entry.getValue().name());
                            }
                        })
                )
                .subcommand("deps", command -> command
                        .description("Show dependency states")
                        .executor(context -> {
                            for (Map.Entry<DependencyCapability, DependencyStatus> entry : plugin.dependencyManager().statuses().entrySet()) {
                                plugin.miniMessage().sendWithPrefix(context.sender(), "<yellow>" + entry.getKey().name() + "<gray>: <white>" + entry.getValue().available() + " <dark_gray>(" + entry.getValue().reason() + ")");
                            }
                        })
                )
                .build();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public List<String> aliases() {
        return delegate.aliases();
    }

    @Override
    public String description() {
        return delegate.description();
    }

    @Override
    public String usage() {
        return delegate.usage();
    }

    @Override
    public String permission() {
        return delegate.permission();
    }

    @Override
    public void execute(CommandContext context) {
        delegate.execute(context);
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        return delegate.tabComplete(context);
    }

    private void handleDemoForward(CommandContext context) {
        String[] args = context.args();
        if (args.length == 0) {
            plugin.miniMessage().sendWithPrefix(context.sender(), "<gray>Use <white>/heartattacklibsdemo <gui|template|item|reload|debug|hologram|module|deps|balance>");
            return;
        }

        Bukkit.dispatchCommand(context.sender(), "heartattacklibsdemo " + String.join(" ", args));
    }

    private void handleDebug(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            plugin.miniMessage().sendWithPrefix(sender, "<gray>Debug: <white>" + plugin.debugManager().isEnabled());
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "toggle" -> plugin.miniMessage().sendWithPrefix(sender, "<gray>Debug: <white>" + plugin.debugManager().toggle());
            case "on" -> {
                plugin.debugManager().setEnabled(true);
                plugin.miniMessage().sendWithPrefix(sender, "<gray>Debug: <white>true");
            }
            case "off" -> {
                plugin.debugManager().setEnabled(false);
                plugin.miniMessage().sendWithPrefix(sender, "<gray>Debug: <white>false");
            }
            default -> plugin.miniMessage().sendWithPrefix(sender, "<red>Usage: <white>/heartattacklibs debug <status|toggle|on|off>");
        }
    }

    private void sendHelp(CommandSender sender) {
        plugin.miniMessage().sendWithPrefix(sender, "<gold>HeartAttackLibs Admin");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs list <dark_gray>- <white>plugins hooked into HeartAttackLibs");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs demo <...> <dark_gray>- <white>run demo command");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs debug <status|toggle|on|off>");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs modules");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs deps");
        plugin.miniMessage().sendWithPrefix(sender, "<gray>/heartattacklibs reload");
    }

    private void listHookedPlugins(CommandSender sender) {
        List<Plugin> hooked = new ArrayList<>();
        for (Plugin candidate : Bukkit.getPluginManager().getPlugins()) {
            if (candidate.getName().equalsIgnoreCase("HeartAttackLibs")) {
                continue;
            }
            List<String> depend = candidate.getDescription().getDepend();
            List<String> softDepend = candidate.getDescription().getSoftDepend();
            boolean usesLibrary = depend.stream().anyMatch(name -> name.equalsIgnoreCase("HeartAttackLibs"))
                    || softDepend.stream().anyMatch(name -> name.equalsIgnoreCase("HeartAttackLibs"));
            if (usesLibrary) {
                hooked.add(candidate);
            }
        }

        if (hooked.isEmpty()) {
            plugin.miniMessage().sendWithPrefix(sender, "<gray>No plugins are currently hooked into HeartAttackLibs.");
            return;
        }

        plugin.miniMessage().sendWithPrefix(sender, "<gold>Plugins hooked into HeartAttackLibs:");
        for (Plugin hookedPlugin : hooked) {
            String state = hookedPlugin.isEnabled() ? "<green>enabled" : "<red>disabled";
            plugin.miniMessage().sendWithPrefix(
                    sender,
                    "<yellow>" + hookedPlugin.getName() + "<gray> v<white>" + hookedPlugin.getDescription().getVersion() + " <dark_gray>(" + state + "<dark_gray>)"
            );
        }
    }
}

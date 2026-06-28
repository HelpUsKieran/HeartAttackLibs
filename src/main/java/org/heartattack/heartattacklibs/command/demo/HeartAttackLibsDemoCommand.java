package org.heartattack.heartattacklibs.command.demo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandContext;
import org.heartattack.heartattacklibs.command.typed.ArgumentRegistry;
import org.heartattack.heartattacklibs.command.typed.CommandArgument;
import org.heartattack.heartattacklibs.command.typed.ParsedArguments;
import org.heartattack.heartattacklibs.command.typed.TypedCommand;
import org.heartattack.heartattacklibs.dependency.DependencyCapability;
import org.heartattack.heartattacklibs.gui.GuiOpenContext;
import org.heartattack.heartattacklibs.hologram.Hologram;
import org.heartattack.heartattacklibs.hologram.HologramBuilder;
import org.heartattack.heartattacklibs.item.ItemBuilder;
import org.heartattack.heartattacklibs.message.MessagePlaceholder;

import java.util.List;

public final class HeartAttackLibsDemoCommand extends TypedCommand {
    private final HeartAttackLibs plugin;

    private enum Action {
        GUI, TEMPLATE, ITEM, RELOAD, DEBUG, HOLOGRAM, MODULE, DEPS, BALANCE
    }

    public HeartAttackLibsDemoCommand(HeartAttackLibs plugin) {
        this.plugin = plugin;
        this.plugin.guiFramework().bindAction(plugin, "demo_confirm", context -> {
            this.plugin.messageManager().container("template-confirm").send(context.player());
            context.player().closeInventory();
        });
        this.plugin.guiFramework().bindPlaceholder(plugin, "player_health", context ->
                String.valueOf((int) Math.floor(context.player().getHealth()))
        );
    }

    @Override
    public String name() {
        return "heartattacklibsdemo";
    }

    @Override
    public String description() {
        return "Demo command for HeartAttackLibs features.";
    }

    @Override
    public String usage() {
        return "/heartattacklibsdemo <gui|template|item|reload|debug|hologram|module|deps|balance>";
    }

    @Override
    public String permission() {
        return "heartattacklibs.demo";
    }

    @Override
    protected List<CommandArgument<?>> arguments() {
        return List.of(CommandArgument.required("action", ArgumentRegistry.enumValue(Action.class)));
    }

    @Override
    protected void executeTyped(CommandContext context, ParsedArguments arguments) {
        Action action = arguments.get("action");

        switch (action) {
            case GUI -> openDemoGui(context);
            case TEMPLATE -> openTemplateGui(context);
            case ITEM -> giveDemoItem(context);
            case RELOAD -> {
                plugin.reloadFramework();
                plugin.messageManager().container("framework-reloaded").send(context.sender());
            }
            case DEBUG -> {
                boolean enabled = plugin.debugManager().toggle();
                plugin.messageManager().container(enabled ? "debug-enabled" : "debug-disabled").send(context.sender());
            }
            case HOLOGRAM -> spawnDemoHologram(context);
            case MODULE -> plugin.moduleManager().states().forEach(
                    (module, state) -> plugin.miniMessage().sendWithPrefix(context.sender(), "<yellow>" + module + "<gray>: <white>" + state.name())
            );
            case DEPS -> plugin.dependencyManager().statuses().forEach(
                    (capability, status) -> plugin.miniMessage().sendWithPrefix(
                            context.sender(),
                            "<yellow>" + capability.name() + "<gray>: <white>" + status.available() + " <dark_gray>(" + status.reason() + ")"
                    )
            );
            case BALANCE -> showBalance(context);
        }
    }

    @Override
    protected void onArgumentError(org.bukkit.command.CommandSender sender, String message) {
        plugin.miniMessage().sendWithPrefix(sender, "<red>" + message + " <gray>Usage: <white>" + usage());
    }

    private void openDemoGui(CommandContext context) {
        if (!context.isPlayer()) {
            plugin.messageManager().container("not-player").send(context.sender());
            return;
        }

        plugin.guiFramework().open(context.asPlayer(), "demo", GuiOpenContext.EMPTY);
    }

    private void openTemplateGui(CommandContext context) {
        if (!context.isPlayer()) {
            plugin.messageManager().container("not-player").send(context.sender());
            return;
        }

        plugin.guiFramework().open(context.asPlayer(), "demo", GuiOpenContext.of(java.util.Map.of("source", "template")));
    }

    private void giveDemoItem(CommandContext context) {
        if (!context.isPlayer()) {
            plugin.messageManager().container("not-player").send(context.sender());
            return;
        }

        Player player = context.asPlayer();
        player.getInventory().addItem(
                ItemBuilder.of(Material.DIAMOND_SWORD)
                        .name("<gold><bold>HeartAttackLibs Blade")
                        .lore("<gray>Built with ItemBuilder", "<yellow>Use this as your template.")
                        .glow()
                        .unbreakable(true)
                        .build()
        );
        plugin.messageManager().container("item-given").send(player);
    }

    private void spawnDemoHologram(CommandContext context) {
        if (!context.isPlayer()) {
            plugin.messageManager().container("not-player").send(context.sender());
            return;
        }

        Player player = context.asPlayer();
        Hologram hologram = plugin.hologramManager().create(
                HologramBuilder.at(player.getLocation().add(0.0, 2.2, 0.0))
                        .lines("HeartAttackLibs Hologram", "Powered by TextDisplay")
                        .lineSpacing(0.3)
                        .viewRange(48.0f)
        );
        hologram.spawn();

        plugin.messageManager().container("hologram-spawned")
                .send(player, MessagePlaceholder.of("seconds", 10));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.hologramManager().remove(hologram), 200L);
    }

    private void showBalance(CommandContext context) {
        if (!context.isPlayer()) {
            plugin.messageManager().container("not-player").send(context.sender());
            return;
        }
        Player player = context.asPlayer();
        if (!plugin.dependencyManager().status(DependencyCapability.ECONOMY).available()) {
            plugin.miniMessage().sendWithPrefix(player, "<red>No economy provider available.");
            return;
        }
        double balance = plugin.dependencyManager().economy().balance(player);
        plugin.miniMessage().sendWithPrefix(player, "<green>Balance: <white>" + balance);
    }
}

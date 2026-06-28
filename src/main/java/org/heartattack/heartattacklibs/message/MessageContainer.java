package org.heartattack.heartattacklibs.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.text.MiniMessageService;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageContainer {
    private final String pluginScope;
    private final String location;
    private final MessageTemplate template;
    private final MiniMessageService miniMessageService;
    private final PlayerSettingsService settingsService;

    MessageContainer(String pluginScope, String location, MessageTemplate template, MiniMessageService miniMessageService, PlayerSettingsService settingsService) {
        this.pluginScope = pluginScope;
        this.location = location;
        this.template = template;
        this.miniMessageService = miniMessageService;
        this.settingsService = settingsService;
    }

    public void send(CommandSender sender, MessagePlaceholder... placeholders) {
        Map<String, String> map = asMap(placeholders);
        if (!(sender instanceof Player player) || settingsService.isEnabled(player, pluginScope, "messages", location)) {
            List<String> lines = template.lines();
            boolean toggleable = sender instanceof Player
                    && settingsService.isToggleable(pluginScope, "messages", location);
            for (int i = 0; i < lines.size(); i++) {
                String rendered = apply(lines.get(i), map);
                if (toggleable && i == lines.size() - 1) {
                    Component msg = miniMessageService.buildWithPrefix(rendered)
                            .append(toggleWidget(pluginScope, "messages", location));
                    sender.sendMessage(msg);
                } else {
                    miniMessageService.sendWithPrefix(sender, rendered);
                }
            }
        }

        if (sender instanceof Player player) {
            if (template.actionbar() != null
                    && !template.actionbar().isBlank()
                    && settingsService.isEnabled(player, pluginScope, "actionbars", location)) {
                player.sendActionBar(miniMessageService.deserialize(apply(template.actionbar(), map)));
            }
            if ((template.title() != null || template.subtitle() != null)
                    && settingsService.isEnabled(player, pluginScope, "titles", location)) {
                player.showTitle(
                        Title.title(
                                miniMessageService.deserialize(apply(nullToEmpty(template.title()), map)),
                                miniMessageService.deserialize(apply(nullToEmpty(template.subtitle()), map)),
                                Title.Times.times(
                                        Duration.ofMillis(template.fadeIn() * 50L),
                                        Duration.ofMillis(template.stay() * 50L),
                                        Duration.ofMillis(template.fadeOut() * 50L)
                                )
                        )
                );
            }
            if (template.sound() != null && settingsService.isEnabled(player, pluginScope, "sounds", location)) {
                player.playSound(player.getLocation(), template.sound(), template.volume(), template.pitch());
            }
        }
    }

    public void broadcast(MessagePlaceholder... placeholders) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, placeholders);
        }
    }

    private Map<String, String> asMap(MessagePlaceholder[] placeholders) {
        Map<String, String> map = new LinkedHashMap<>();
        for (MessagePlaceholder placeholder : placeholders) {
            map.put(placeholder.key(), placeholder.value());
        }
        return map;
    }

    private String apply(String input, Map<String, String> placeholders) {
        String value = input == null ? "" : input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return value;
    }

    private String nullToEmpty(String input) {
        return input == null ? "" : input;
    }

    public static Component toggleWidget(String pluginId, String category, String location) {
        String cmd = "/settings toggle " + pluginId + " " + category + " " + location;
        return Component.text()
                .append(Component.text(" [").color(NamedTextColor.DARK_GRAY))
                .append(Component.text("✕").color(NamedTextColor.RED))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY))
                .clickEvent(ClickEvent.runCommand(cmd))
                .hoverEvent(HoverEvent.showText(Component.text("Click to disable").color(NamedTextColor.GRAY)))
                .build();
    }
}

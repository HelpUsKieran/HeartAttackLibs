package org.heartattack.heartattacklibs.message;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.text.ColorUtils;
import org.heartattack.heartattacklibs.util.PlaceholderMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PathMessageService {
    private final DependencyManager dependencyManager;

    public PathMessageService(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public String raw(FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        String base = configuration.getString(path, fallback == null ? "" : fallback);
        return apply(base, placeholders);
    }

    public String legacy(FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        return ColorUtils.colorForLegacy(raw(configuration, path, fallback, placeholders));
    }

    public Component component(FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        return ColorUtils.color(raw(configuration, path, fallback, placeholders));
    }

    public List<Component> componentList(FileConfiguration configuration, String path, Map<String, String> placeholders) {
        List<String> lines = configuration.getStringList(path);
        if (lines.isEmpty()) {
            return List.of();
        }

        List<Component> output = new ArrayList<>(lines.size());
        for (String line : lines) {
            output.add(ColorUtils.color(apply(line, placeholders)));
        }
        return output;
    }

    public void send(CommandSender sender, FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        sender.sendMessage(component(configuration, path, fallback, placeholders));
    }

    public void sendToPlayer(Player player, FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        String parsed = dependencyManager.parsePlaceholders(player, raw(configuration, path, fallback, placeholders));
        player.sendMessage(ColorUtils.color(parsed));
    }

    public void broadcast(FileConfiguration configuration, String path, String fallback, Map<String, String> placeholders) {
        Bukkit.broadcast(component(configuration, path, fallback, placeholders));
    }

    private String apply(String input, Map<String, String> placeholders) {
        String value = input == null ? "" : input;
        if (placeholders == null || placeholders.isEmpty()) {
            return value;
        }

        PlaceholderMap map = PlaceholderMap.create();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String replacement = entry.getValue();
            value = value.replace(key, replacement);
            map.with(key, replacement);
        }
        return map.apply(value);
    }
}

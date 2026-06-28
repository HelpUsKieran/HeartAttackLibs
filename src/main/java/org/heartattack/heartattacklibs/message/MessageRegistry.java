package org.heartattack.heartattacklibs.message;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.config.ConfigFileHandle;
import org.heartattack.heartattacklibs.text.MiniMessageService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MessageRegistry {
    private final Plugin plugin;
    private final String fileName;
    private final ConfigFileHandle config;
    private final MiniMessageService miniMessageService;
    private final PlayerSettingsService settingsService;
    private final Map<String, MessageTemplate> templates = new HashMap<>();

    MessageRegistry(Plugin plugin, String fileName, ConfigFileHandle config, MiniMessageService miniMessageService, PlayerSettingsService settingsService) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.config = config;
        this.miniMessageService = miniMessageService;
        this.settingsService = settingsService;
    }

    public Plugin plugin() {
        return plugin;
    }

    public String fileName() {
        return fileName;
    }

    public void reload() {
        templates.clear();
        config.reload();
        FileConfiguration raw = config.raw();
        ConfigurationSection section = raw.getConfigurationSection("messages");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection messageSection = section.getConfigurationSection(key);
            if (messageSection == null) {
                continue;
            }
            String normalizedKey = key.toLowerCase(Locale.ROOT);
            MessageTemplate template = parseTemplate(normalizedKey, messageSection);
            templates.put(normalizedKey, template);
            registerSettingsLocations(normalizedKey, template);
        }
    }

    public MessageContainer container(String key) {
        MessageTemplate template = templates.get(key.toLowerCase(Locale.ROOT));
        if (template == null) {
            MessageTemplate fallback = new MessageTemplate();
            fallback.setKey(key);
            fallback.lines().add("<red>Missing message key: " + key + " in " + fileName);
            return new MessageContainer(plugin.getName().toLowerCase(), key, fallback, miniMessageService, settingsService);
        }
        return new MessageContainer(plugin.getName().toLowerCase(), template.key(), template, miniMessageService, settingsService);
    }

    public void send(String key, CommandSender sender, MessagePlaceholder... placeholders) {
        container(key).send(sender, placeholders);
    }

    public void broadcast(String key, MessagePlaceholder... placeholders) {
        container(key).broadcast(placeholders);
    }

    public void broadcastToOnlinePlayers(String key, MessagePlaceholder... placeholders) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(key, player, placeholders);
        }
    }

    public Map<String, MessageTemplate> templates() {
        return Collections.unmodifiableMap(templates);
    }

    private MessageTemplate parseTemplate(String key, ConfigurationSection section) {
        MessageTemplate template = new MessageTemplate();
        template.setKey(key);

        List<String> lines = section.getStringList("lines");
        if (!lines.isEmpty()) {
            template.lines().addAll(lines);
        } else if (section.contains("line")) {
            template.lines().add(section.getString("line", ""));
        }

        template.setActionbar(section.getString("actionbar"));
        template.setTitle(section.getString("title"));
        template.setSubtitle(section.getString("subtitle"));
        template.setFadeIn(section.getInt("times.fadeIn", template.fadeIn()));
        template.setStay(section.getInt("times.stay", template.stay()));
        template.setFadeOut(section.getInt("times.fadeOut", template.fadeOut()));

        String soundName = section.getString("sound.name");
        if (soundName != null && !soundName.isBlank()) {
            try {
                template.setSound(Sound.valueOf(soundName.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Invalid sound in " + fileName + ": " + soundName);
            }
        }
        template.setVolume((float) section.getDouble("sound.volume", 1.0d));
        template.setPitch((float) section.getDouble("sound.pitch", 1.0d));
        return template;
    }

    private void registerSettingsLocations(String key, MessageTemplate template) {
        String pluginId = plugin.getName().toLowerCase(Locale.ROOT);
        String display = key.replace('_', ' ');
        if (!template.lines().isEmpty()) {
            settingsService.registerLocation(pluginId, "messages", key, display);
        }
        if (template.actionbar() != null && !template.actionbar().isBlank()) {
            settingsService.registerLocation(pluginId, "actionbars", key, display);
        }
        if (template.title() != null || template.subtitle() != null) {
            settingsService.registerLocation(pluginId, "titles", key, display);
        }
        if (template.sound() != null) {
            settingsService.registerLocation(pluginId, "sounds", key, display);
        }
    }
}

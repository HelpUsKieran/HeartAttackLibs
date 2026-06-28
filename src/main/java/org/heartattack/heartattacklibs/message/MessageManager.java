package org.heartattack.heartattacklibs.message;

import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.text.MiniMessageService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MessageManager {
    private final JavaPlugin plugin;
    private final MiniMessageService miniMessageService;
    private final PlayerSettingsService settingsService;
    private final MessageFramework messageFramework;
    private final Map<String, MessageTemplate> templates = new HashMap<>();

    public MessageManager(JavaPlugin plugin, MiniMessageService miniMessageService, PlayerSettingsService settingsService, MessageFramework messageFramework) {
        this.plugin = plugin;
        this.miniMessageService = miniMessageService;
        this.settingsService = settingsService;
        this.messageFramework = messageFramework;
    }

    public void reload() {
        templates.clear();
        MessageRegistry registry = messageFramework.getOrRegister(plugin, "messages.yml");
        registry.reload();
        templates.putAll(registry.templates());
    }

    public MessageContainer container(String key) {
        MessageTemplate template = templates.get(key.toLowerCase());
        if (template == null) {
            MessageTemplate fallback = new MessageTemplate();
            fallback.setKey(key);
            fallback.lines().add("<red>Missing message key: " + key);
            return new MessageContainer(plugin.getName().toLowerCase(), key, fallback, miniMessageService, settingsService);
        }
        return new MessageContainer(plugin.getName().toLowerCase(), template.key(), template, miniMessageService, settingsService);
    }

    public Map<String, MessageTemplate> templates() {
        return Collections.unmodifiableMap(templates);
    }
}

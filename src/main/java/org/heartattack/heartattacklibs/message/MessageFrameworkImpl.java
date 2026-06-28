package org.heartattack.heartattacklibs.message;

import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.config.ConfigFileHandle;
import org.heartattack.heartattacklibs.config.ConfigFramework;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.text.MiniMessageService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageFrameworkImpl implements MessageFramework {
    private final ConfigFramework configFramework;
    private final MiniMessageService miniMessageService;
    private final PlayerSettingsService settingsService;
    private final Map<String, MessageRegistry> registries = new ConcurrentHashMap<>();

    public MessageFrameworkImpl(ConfigFramework configFramework, MiniMessageService miniMessageService, PlayerSettingsService settingsService) {
        this.configFramework = configFramework;
        this.miniMessageService = miniMessageService;
        this.settingsService = settingsService;
    }

    @Override
    public MessageRegistry getOrRegister(Plugin plugin, String fileName) {
        String key = key(plugin, fileName);
        return registries.computeIfAbsent(key, ignored -> {
            String normalized = normalize(fileName);
            ConfigFileHandle handle = configFramework.getOrRegister(plugin, normalized, true);
            MessageRegistry registry = new MessageRegistry(plugin, normalized, handle, miniMessageService, settingsService);
            registry.reload();
            return registry;
        });
    }

    @Override
    public MessageRegistry get(Plugin plugin, String fileName) {
        return registries.get(key(plugin, fileName));
    }

    @Override
    public void reloadPlugin(Plugin plugin) {
        String prefix = plugin.getName().toLowerCase() + ":";
        for (Map.Entry<String, MessageRegistry> entry : registries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                entry.getValue().reload();
            }
        }
    }

    @Override
    public void reloadAll() {
        for (MessageRegistry registry : registries.values()) {
            registry.reload();
        }
    }

    private String key(Plugin plugin, String fileName) {
        return plugin.getName().toLowerCase() + ":" + normalize(fileName);
    }

    private String normalize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be blank");
        }
        return fileName.endsWith(".yml") ? fileName : fileName + ".yml";
    }
}

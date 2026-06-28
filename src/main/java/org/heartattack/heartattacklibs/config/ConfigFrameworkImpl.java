package org.heartattack.heartattacklibs.config;

import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigFrameworkImpl implements ConfigFramework {
    private final Map<String, ConfigFileHandle> handles = new ConcurrentHashMap<>();

    @Override
    public ConfigFileHandle getOrRegister(Plugin plugin, String fileName) {
        return getOrRegister(plugin, fileName, true);
    }

    @Override
    public ConfigFileHandle getOrRegister(Plugin plugin, String fileName, boolean copyDefaultsFromResource) {
        String key = key(plugin, fileName);
        return handles.computeIfAbsent(key, ignored -> {
            ConfigFileHandle handle = new ConfigFileHandle(plugin, normalize(fileName), copyDefaultsFromResource);
            handle.reload();
            return handle;
        });
    }

    @Override
    public ConfigFileHandle get(Plugin plugin, String fileName) {
        return handles.get(key(plugin, fileName));
    }

    @Override
    public void reloadPlugin(Plugin plugin) {
        String prefix = plugin.getName().toLowerCase() + ":";
        for (Map.Entry<String, ConfigFileHandle> entry : handles.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                entry.getValue().reload();
            }
        }
    }

    @Override
    public void reloadAll() {
        for (ConfigFileHandle handle : handles.values()) {
            handle.reload();
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

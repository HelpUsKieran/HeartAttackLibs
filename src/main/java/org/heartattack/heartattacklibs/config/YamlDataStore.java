package org.heartattack.heartattacklibs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashSet;
import java.util.Set;

public final class YamlDataStore {
    private final ConfigManager.ConfigFile configFile;

    public YamlDataStore(ConfigManager.ConfigFile configFile) {
        this.configFile = configFile;
    }

    public FileConfiguration config() {
        return configFile.config();
    }

    public void reload() {
        configFile.reload();
    }

    public void save() {
        configFile.save();
    }

    public void set(String path, Object value) {
        config().set(path, value);
    }

    public String getString(String path, String fallback) {
        return config().getString(path, fallback);
    }

    public long getLong(String path, long fallback) {
        return config().getLong(path, fallback);
    }

    public boolean contains(String path) {
        return config().contains(path);
    }

    public Set<String> keys(String path) {
        if (config().getConfigurationSection(path) == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(config().getConfigurationSection(path).getKeys(false));
    }
}

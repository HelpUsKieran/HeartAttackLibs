package org.heartattack.heartattacklibs.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, ConfigFile> files = new ConcurrentHashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration config() {
        return plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public ConfigFile register(String fileName) {
        String normalized = normalize(fileName);
        ConfigFile configFile = new ConfigFile(plugin, normalized);
        configFile.load();
        files.put(normalized, configFile);
        return configFile;
    }

    public ConfigFile get(String fileName) {
        return files.get(normalize(fileName));
    }

    public ConfigFile getOrRegister(String fileName) {
        return files.computeIfAbsent(normalize(fileName), name -> {
            ConfigFile file = new ConfigFile(plugin, name);
            file.load();
            return file;
        });
    }

    public void reloadAll() {
        plugin.reloadConfig();
        for (ConfigFile configFile : files.values()) {
            configFile.load();
        }
    }

    public String getString(String path) {
        String value = config().getString(path);
        if (value == null) {
            throw new IllegalStateException("Missing config value: '" + path + "' in config.yml");
        }
        return value;
    }

    public String getString(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        String value = file.config().getString(path);
        if (value == null) {
            throw new IllegalStateException("Missing config value: '" + path + "' in " + normalized);
        }
        return value;
    }

    public Integer getOptionalInt(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            return null;
        }
        return file.config().getInt(path);
    }

    public int getInt(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            throw new IllegalStateException("Missing config value: '" + path + "' in " + normalized);
        }
        return file.config().getInt(path);
    }

    public boolean getBoolean(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            throw new IllegalStateException("Missing config value: '" + path + "' in " + normalized);
        }
        return file.config().getBoolean(path);
    }

    public double getDouble(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            throw new IllegalStateException("Missing config value: '" + path + "' in " + normalized);
        }
        return file.config().getDouble(path);
    }

    public List<String> getStringList(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            throw new IllegalStateException("Missing config value: '" + path + "' in " + normalized);
        }
        return file.config().getStringList(path);
    }

    public List<String> getOptionalStringList(String fileName, String path) {
        String normalized = normalize(fileName);
        ConfigFile file = getOrRegister(normalized);
        if (!file.config().contains(path)) {
            return null;
        }
        return file.config().getStringList(path);
    }

    private String normalize(String fileName) {
        return fileName.endsWith(".yml") ? fileName : fileName + ".yml";
    }

    public static final class ConfigFile {
        private final JavaPlugin plugin;
        private final String fileName;
        private final File file;
        private FileConfiguration configuration;

        private ConfigFile(JavaPlugin plugin, String fileName) {
            this.plugin = plugin;
            this.fileName = fileName;
            this.file = new File(plugin.getDataFolder(), fileName);
        }

        public String fileName() {
            return fileName;
        }

        public void load() {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IllegalStateException("Could not create plugin data folder.");
            }

            if (!file.exists()) {
                if (plugin.getResource(fileName) != null) {
                    plugin.saveResource(fileName, false);
                } else {
                    try {
                        if (!file.createNewFile()) {
                            throw new IllegalStateException("Could not create file " + fileName);
                        }
                    } catch (IOException exception) {
                        throw new IllegalStateException("Could not create file " + fileName, exception);
                    }
                }
            }

            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.load(file);
            } catch (IOException | InvalidConfigurationException exception) {
                throw new IllegalStateException("Failed to load file " + fileName, exception);
            }
            this.configuration = yamlConfiguration;
        }

        public FileConfiguration config() {
            if (configuration == null) {
                load();
            }
            return configuration;
        }

        public void save() {
            if (configuration == null) {
                return;
            }
            try {
                configuration.save(file);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to save file " + fileName, exception);
            }
        }

        public void reload() {
            load();
        }

        public File file() {
            return file;
        }
    }
}

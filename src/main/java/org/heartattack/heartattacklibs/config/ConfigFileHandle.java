package org.heartattack.heartattacklibs.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ConfigFileHandle {
    private final Plugin owner;
    private final String fileName;
    private final boolean copyDefaultsFromResource;
    private final File file;
    private final List<Consumer<ConfigFileHandle>> reloadListeners = new CopyOnWriteArrayList<>();

    private volatile FileConfiguration configuration;

    ConfigFileHandle(Plugin owner, String fileName, boolean copyDefaultsFromResource) {
        this.owner = owner;
        this.fileName = fileName;
        this.copyDefaultsFromResource = copyDefaultsFromResource;
        this.file = new File(owner.getDataFolder(), fileName);
    }

    public Plugin owner() {
        return owner;
    }

    public String fileName() {
        return fileName;
    }

    public File file() {
        return file;
    }

    public FileConfiguration raw() {
        if (configuration == null) {
            reload();
        }
        return configuration;
    }

    public void reload() {
        ensureExists();
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            throw new IllegalStateException("Failed to load config file " + fileName + " for " + owner.getName(), exception);
        }
        this.configuration = yaml;
        for (Consumer<ConfigFileHandle> listener : reloadListeners) {
            try {
                listener.accept(this);
            } catch (Exception ignored) {
            }
        }
    }

    public void save() {
        if (configuration == null) {
            return;
        }
        try {
            configuration.save(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save config file " + fileName + " for " + owner.getName(), exception);
        }
    }

    public void onReload(Consumer<ConfigFileHandle> listener) {
        if (listener != null) {
            reloadListeners.add(listener);
        }
    }

    public boolean contains(String path) {
        return raw().contains(path);
    }

    public ConfigurationSection section(String path) {
        return raw().getConfigurationSection(path);
    }

    public Set<String> keys(String path) {
        ConfigurationSection section = raw().getConfigurationSection(path);
        if (section == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(section.getKeys(false));
    }

    public void set(String path, Object value) {
        raw().set(path, value);
    }

    public String string(String path, String fallback) {
        return raw().getString(path, fallback);
    }

    public String requireString(String path) {
        String value = raw().getString(path);
        if (value == null) {
            throw new IllegalStateException("Missing required string '" + path + "' in " + fileName + " (" + owner.getName() + ")");
        }
        return value;
    }

    public int intValue(String path, int fallback) {
        return raw().getInt(path, fallback);
    }

    public Integer optionalInt(String path) {
        return raw().contains(path) ? raw().getInt(path) : null;
    }

    public boolean booleanValue(String path, boolean fallback) {
        return raw().getBoolean(path, fallback);
    }

    public double doubleValue(String path, double fallback) {
        return raw().getDouble(path, fallback);
    }

    public long longValue(String path, long fallback) {
        return raw().getLong(path, fallback);
    }

    public List<String> stringList(String path) {
        return new ArrayList<>(raw().getStringList(path));
    }

    private void ensureExists() {
        if (!owner.getDataFolder().exists() && !owner.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create data folder for " + owner.getName());
        }

        if (file.exists()) {
            return;
        }

        if (copyDefaultsFromResource && owner.getResource(fileName) != null) {
            owner.saveResource(fileName, false);
            return;
        }

        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Could not create " + file.getAbsolutePath());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create " + file.getAbsolutePath(), exception);
        }
    }
}

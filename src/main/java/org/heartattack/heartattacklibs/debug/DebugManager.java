package org.heartattack.heartattacklibs.debug;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DebugManager {
    private final JavaPlugin plugin;
    private boolean enabled;
    private final Set<String> categories = new HashSet<>();

    public DebugManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadFromConfig(FileConfiguration config) {
        this.enabled = config.getBoolean("debug.enabled", false);
        this.categories.clear();
        List<String> loaded = config.getStringList("debug.categories");
        if (loaded.isEmpty()) {
            categories.add("*");
        } else {
            for (String category : loaded) {
                categories.add(category.toLowerCase(Locale.ROOT));
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean toggle() {
        this.enabled = !this.enabled;
        return this.enabled;
    }

    public void debug(String message) {
        if (!enabled) {
            return;
        }
        plugin.getLogger().info("[DEBUG] " + message);
    }

    public void debug(String category, String message) {
        if (!enabled) {
            return;
        }
        String lower = category.toLowerCase(Locale.ROOT);
        if (!categories.contains("*") && !categories.contains(lower)) {
            return;
        }
        plugin.getLogger().info("[DEBUG/" + category.toUpperCase(Locale.ROOT) + "] " + message);
    }
}

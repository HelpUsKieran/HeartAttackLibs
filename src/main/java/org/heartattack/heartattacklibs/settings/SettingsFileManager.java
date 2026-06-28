package org.heartattack.heartattacklibs.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-plugin settings YAML files under {@code plugins/HeartAttackLibs/settings/}.
 *
 * <p>Each file is named {@code <pluginId>.yml} and contains one section per
 * {@link PlayerSettingCategory} key, with individual location entries beneath it.
 * External plugins may additionally declare {@code filter:} rules on their entries
 * to enable pattern-based message interception.
 *
 * <p>Call {@link #loadAll(FileConfiguration)} on each reload.  The returned list
 * populates {@link PlayerSettingsService}'s {@code registeredLocations}.  Filter
 * rules are cached internally and exposed via {@link #loadFilterRules()}.
 */
public final class SettingsFileManager {

    private final JavaPlugin plugin;
    private final File settingsDir;

    // Populated by loadAll(); storageKey → rules
    private volatile Map<String, List<FilterRule>> cachedFilterRules = Map.of();

    public SettingsFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.settingsDir = new File(plugin.getDataFolder(), "settings");
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Loads all per-plugin YAML files in the {@code settings/} directory and
     * returns every {@link PlayerSettingLocation} found.
     *
     * <p>If the directory is empty (or does not exist) and {@code legacyConfig}
     * still contains the old {@code settings.toggleable-locations} block, the
     * entries are migrated to per-plugin files automatically.
     *
     * @param legacyConfig current main config, used only for migration
     * @return all parsed locations (may be empty on a brand-new install)
     */
    public List<PlayerSettingLocation> loadAll(FileConfiguration legacyConfig) {
        // Save any example/external-plugin settings files bundled inside the JAR
        // (e.g. settings/clearlaggenhanced.yml) before we scan the folder.
        // saveResource uses false = never overwrite admin customisations.
        saveBundledResources();

        settingsDir.mkdirs();

        // Migrate from legacy config.yml if this is the first run
        if (isDirectoryEmpty(settingsDir)) {
            ConfigurationSection legacySection =
                    legacyConfig.getConfigurationSection("settings.toggleable-locations");
            if (legacySection != null && !legacySection.getKeys(false).isEmpty()) {
                migrate(legacySection);
            }
        }

        List<PlayerSettingLocation> locations = new ArrayList<>();
        Map<String, List<FilterRule>> filterRules = new ConcurrentHashMap<>();

        File[] files = settingsDir.listFiles(f -> f.isFile() && f.getName().endsWith(".yml"));
        if (files == null) {
            cachedFilterRules = Map.of();
            return locations;
        }

        for (File file : files) {
            String pluginId = file.getName().substring(0, file.getName().length() - 4)
                    .toLowerCase(Locale.ROOT);
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            for (String categoryKey : cfg.getKeys(false)) {
                if (!cfg.isConfigurationSection(categoryKey)) continue;

                // Validate that it is a known category
                PlayerSettingCategory category;
                try {
                    category = PlayerSettingCategory.fromKey(categoryKey);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                ConfigurationSection catSection = cfg.getConfigurationSection(categoryKey);
                if (catSection == null) continue;

                for (String locationKey : catSection.getKeys(false)) {
                    if (!catSection.isConfigurationSection(locationKey)) continue;
                    ConfigurationSection locSection = catSection.getConfigurationSection(locationKey);
                    if (locSection == null) continue;

                    PlayerSettingLocation loc = parseLocation(pluginId, category.key(), locationKey, locSection);
                    locations.add(loc);

                    // Collect filter rules (external plugin interception)
                    List<FilterRule> rules = parseFilterRules(locSection);
                    if (!rules.isEmpty()) {
                        filterRules.put(loc.storageKey(), rules);
                    }
                }
            }
        }

        cachedFilterRules = Map.copyOf(filterRules);
        return locations;
    }

    /**
     * Writes any newly discovered locations (those not already present in
     * their plugin's YAML file) into the appropriate per-plugin file.
     *
     * @param discoveries all locations reported by registered plugins
     * @return {@code true} if at least one file was modified
     */
    public boolean writeDiscoveries(Collection<PlayerSettingLocation> discoveries) {
        if (discoveries.isEmpty()) return false;

        // Group by pluginId
        Map<String, List<PlayerSettingLocation>> byPlugin = new LinkedHashMap<>();
        for (PlayerSettingLocation loc : discoveries) {
            byPlugin.computeIfAbsent(loc.pluginId(), k -> new ArrayList<>()).add(loc);
        }

        boolean anyModified = false;
        for (Map.Entry<String, List<PlayerSettingLocation>> entry : byPlugin.entrySet()) {
            if (writePluginFile(entry.getKey(), entry.getValue())) {
                anyModified = true;
            }
        }
        return anyModified;
    }

    /**
     * Returns the filter rules cached during the last {@link #loadAll} call.
     * The map is keyed by {@link PlayerSettingLocation#storageKey()}.
     */
    public Map<String, List<FilterRule>> loadFilterRules() {
        return cachedFilterRules;
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private boolean writePluginFile(String pluginId, List<PlayerSettingLocation> locs) {
        settingsDir.mkdirs();
        File file = new File(settingsDir, pluginId + ".yml");
        YamlConfiguration cfg = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();

        cfg.options().header(
                "Generated by HeartAttackLibs. Edit display, material, and lore freely.\n"
                        + "toggleable: false → message always sends and is hidden from /settings GUI.");

        boolean modified = false;
        for (PlayerSettingLocation loc : locs) {
            String path = loc.category() + "." + loc.location();
            if (cfg.contains(path)) continue; // already present — don't overwrite

            cfg.set(path + ".display", loc.displayName());
            cfg.set(path + ".toggleable", true);
            if (loc.material() != null) {
                cfg.set(path + ".material", loc.material());
            }
            cfg.set(path + ".lore", loc.lore());
            modified = true;
        }

        if (modified) {
            try {
                cfg.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("[HeartAttackLibs] Could not save settings file "
                        + file.getName() + ": " + e.getMessage());
                return false;
            }
        }
        return modified;
    }

    private PlayerSettingLocation parseLocation(
            String pluginId, String category, String locationKey, ConfigurationSection section) {
        String display   = section.getString("display", toDisplayName(locationKey));
        boolean toggleable = section.getBoolean("toggleable", true);
        String material  = section.getString("material", null);
        List<String> lore = section.getStringList("lore");
        return new PlayerSettingLocation(pluginId, category, locationKey, display, material, lore, toggleable);
    }

    private List<FilterRule> parseFilterRules(ConfigurationSection section) {
        List<FilterRule> rules = new ArrayList<>();
        List<Map<?, ?>> filterList = section.getMapList("filter");
        for (Map<?, ?> map : filterList) {
            Object typeObj  = map.get("type");
            Object valueObj = map.get("value");
            if (typeObj == null || valueObj == null) continue;
            rules.add(new FilterRule(typeObj.toString(), valueObj.toString()));
        }
        return rules;
    }

    /**
     * Migrates {@code settings.toggleable-locations} from the legacy config
     * into per-plugin YAML files and logs a one-time notice.
     */
    private void migrate(ConfigurationSection legacySection) {
        plugin.getLogger().info("[HeartAttackLibs] Migrating settings.toggleable-locations to per-plugin files in plugins/HeartAttackLibs/settings/");
        Map<String, List<PlayerSettingLocation>> byPlugin = new LinkedHashMap<>();

        for (String pluginId : legacySection.getKeys(false)) {
            ConfigurationSection pluginSection = legacySection.getConfigurationSection(pluginId);
            if (pluginSection == null) continue;
            for (String categoryKey : pluginSection.getKeys(false)) {
                ConfigurationSection catSection = pluginSection.getConfigurationSection(categoryKey);
                if (catSection == null) continue;

                PlayerSettingCategory category;
                try {
                    category = PlayerSettingCategory.fromKey(categoryKey);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                for (String locKey : catSection.getKeys(false)) {
                    String display;
                    String material = null;
                    List<String> lore = List.of();
                    if (catSection.isConfigurationSection(locKey)) {
                        ConfigurationSection locSection = catSection.getConfigurationSection(locKey);
                        display   = locSection.getString("display", toDisplayName(locKey));
                        material  = locSection.getString("material", null);
                        lore      = locSection.getStringList("lore");
                    } else {
                        display = catSection.getString(locKey, toDisplayName(locKey));
                    }
                    try {
                        PlayerSettingLocation loc = new PlayerSettingLocation(
                                pluginId, category.key(), locKey, display, material, lore, true);
                        byPlugin.computeIfAbsent(pluginId, k -> new ArrayList<>()).add(loc);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        for (Map.Entry<String, List<PlayerSettingLocation>> entry : byPlugin.entrySet()) {
            writePluginFile(entry.getKey(), entry.getValue());
        }
        plugin.getLogger().info("[HeartAttackLibs] Migration complete. You can remove settings.toggleable-locations from config.yml.");
    }

    /**
     * Saves every {@code settings/*.yml} resource bundled inside the plugin JAR
     * to the on-disk settings folder, using {@code overwrite = false} so that
     * any admin customisations are preserved across restarts.
     *
     * <p>Called at the start of {@link #loadAll} so that example files are
     * always present before the folder is scanned, even on the very first start.
     */
    private void saveBundledResources() {
        java.security.CodeSource codeSource =
                plugin.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) return;
        java.net.URL location = codeSource.getLocation();
        if (location == null) return;

        try {
            java.io.File jarFile = new java.io.File(location.toURI());
            if (!jarFile.isFile()) return; // running from IDE/exploded dir — skip

            try (JarFile jar = new JarFile(jarFile)) {
                jar.stream()
                   .filter(entry -> !entry.isDirectory()
                           && entry.getName().startsWith("settings/")
                           && entry.getName().endsWith(".yml"))
                   .forEach(entry -> {
                       try {
                           plugin.saveResource(entry.getName(), false);
                       } catch (IllegalArgumentException ignored) {}
                   });
            }
        } catch (Exception e) {
            plugin.getLogger().fine("[HeartAttackLibs] saveBundledResources: " + e.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return true;
        String[] files = dir.list();
        return files == null || files.length == 0;
    }

    private static String toDisplayName(String key) {
        if (key == null || key.isBlank()) return key;
        String spaced = key.replace('_', ' ').replace('.', ' ');
        if (spaced.isEmpty()) return spaced;
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}

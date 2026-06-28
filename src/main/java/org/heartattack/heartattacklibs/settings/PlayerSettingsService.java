package org.heartattack.heartattacklibs.settings;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSettingsService {
    private final JavaPlugin plugin;
    private final PlayerSettingsRepository repository;
    private final SettingsFileManager settingsFileManager;

    private final Map<UUID, Map<String, Boolean>> cache = new ConcurrentHashMap<>();
    private final Map<PlayerSettingCategory, Boolean> defaults = new EnumMap<>(PlayerSettingCategory.class);
    private final List<String> pluginOverrides = new ArrayList<>();

    /**
     * Locations loaded from {@code settings/*.yml} files (or registered in
     * allow-all mode). These are the locations shown in the player GUI and
     * respected during {@link #resolve}.
     */
    private final Map<String, PlayerSettingLocation> registeredLocations = new ConcurrentHashMap<>();

    /**
     * All locations reported by plugins via {@link #registerLocation}, used
     * to detect newly discovered messages for auto-population.
     */
    private final Map<String, PlayerSettingLocation> discoveredLocations = new ConcurrentHashMap<>();

    /**
     * When {@code true} (no per-plugin settings files exist yet), every
     * {@link #registerLocation} call also populates {@link #registeredLocations}
     * so the GUI works out-of-the-box on a fresh install.
     */
    private volatile boolean allowAllMode = false;

    private volatile boolean enabled = true;
    private volatile SettingsDisplayConfig displayConfig = SettingsDisplayConfig.defaults();

    public PlayerSettingsService(JavaPlugin plugin, PlayerSettingsRepository repository,
                                 SettingsFileManager settingsFileManager) {
        this.plugin = plugin;
        this.repository = repository;
        this.settingsFileManager = settingsFileManager;
        for (PlayerSettingCategory category : PlayerSettingCategory.values()) {
            defaults.put(category, true);
        }
    }

    public void initializeStorage() {
        repository.initialize();
    }

    public void reloadFromConfig(FileConfiguration config) {
        enabled = config.getBoolean("settings.enabled", true);
        for (PlayerSettingCategory category : PlayerSettingCategory.values()) {
            defaults.put(category, config.getBoolean("settings.defaults." + category.key(), true));
        }

        pluginOverrides.clear();
        for (String raw : config.getStringList("settings.plugin-overrides")) {
            if (raw == null || raw.isBlank()) continue;
            pluginOverrides.add(raw.trim().toLowerCase(Locale.ROOT));
        }

        registeredLocations.clear();
        List<PlayerSettingLocation> loaded = settingsFileManager.loadAll(config);
        for (PlayerSettingLocation loc : loaded) {
            registeredLocations.put(loc.storageKey(), loc);
        }

        // Allow-all mode: no files exist yet — accept every registerLocation() call
        allowAllMode = loaded.isEmpty();

        cache.clear();
        displayConfig = loadDisplayConfig(config);
    }

    // -----------------------------------------------------------------------
    // Location registration (called by plugins on each reload)
    // -----------------------------------------------------------------------

    public void registerLocation(String pluginId, String category, String location, String displayName) {
        PlayerSettingLocation loc;
        try {
            loc = new PlayerSettingLocation(pluginId, category, location, displayName);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        discoveredLocations.put(loc.storageKey(), loc);

        if (allowAllMode) {
            // No files exist yet — populate registeredLocations so GUI works immediately
            registeredLocations.putIfAbsent(loc.storageKey(), loc);
        }
        // In explicit mode, registeredLocations already has admin-defined entries from
        // files; we do NOT override them with plugin-provided display names.
    }

    // -----------------------------------------------------------------------
    // Discovery persistence
    // -----------------------------------------------------------------------

    /**
     * Writes any discovered-but-not-yet-filed locations to their per-plugin
     * YAML files and adds them to the live {@code registeredLocations} map so
     * the GUI reflects them immediately (without a full reload).
     *
     * @return {@code true} if any file was modified
     */
    public boolean writeNewDiscoveries() {
        boolean modified = settingsFileManager.writeDiscoveries(discoveredLocations.values());
        if (modified) {
            // Promote newly filed discoveries into registeredLocations right away
            for (PlayerSettingLocation loc : discoveredLocations.values()) {
                registeredLocations.putIfAbsent(loc.storageKey(), loc);
            }
        }
        return modified;
    }

    // -----------------------------------------------------------------------
    // Query API
    // -----------------------------------------------------------------------

    public boolean isFeatureEnabled() {
        return enabled;
    }

    public List<String> pluginOverrides() {
        return List.copyOf(pluginOverrides);
    }

    public List<String> pluginScopes() {
        List<String> scopes = new ArrayList<>(pluginOverrides);
        for (PlayerSettingLocation location : registeredLocations.values()) {
            if (!scopes.contains(location.pluginId())) {
                scopes.add(location.pluginId());
            }
        }
        scopes.sort(String.CASE_INSENSITIVE_ORDER);
        return List.copyOf(scopes);
    }

    /**
     * Returns {@code true} when the player is permitted to toggle this
     * location.  Locations with {@code toggleable: false} are always-on
     * and are not exposed in the player GUI.
     */
    public boolean isToggleable(String pluginId, String category, String location) {
        String key = PlayerSettingsKey.location(pluginId, category, location);
        PlayerSettingLocation loc = registeredLocations.get(key);
        if (loc == null) return false;
        return loc.toggleable();
    }

    /** Returns only the toggleable locations for a given plugin + category. */
    public List<PlayerSettingLocation> locations(String pluginId, String category) {
        String scope = PlayerSettingsKey.normalizePlugin(pluginId);
        String normalizedCategory = PlayerSettingsKey.normalizeCategory(category);
        return registeredLocations.values().stream()
                .filter(location -> location.pluginId().equals(scope))
                .filter(location -> location.category().equals(normalizedCategory))
                .filter(PlayerSettingLocation::toggleable)
                .sorted(Comparator.comparing(PlayerSettingLocation::location))
                .toList();
    }

    public Map<String, List<PlayerSettingLocation>> locationsByPlugin() {
        Map<String, List<PlayerSettingLocation>> output = new LinkedHashMap<>();
        for (String pluginId : pluginScopes()) {
            List<PlayerSettingLocation> locs = registeredLocations.values().stream()
                    .filter(location -> location.pluginId().equals(pluginId))
                    .filter(PlayerSettingLocation::toggleable)
                    .sorted(Comparator.comparing(PlayerSettingLocation::category)
                            .thenComparing(PlayerSettingLocation::location))
                    .toList();
            output.put(pluginId, locs);
        }
        return output;
    }

    // -----------------------------------------------------------------------
    // Enabled / disabled checks
    // -----------------------------------------------------------------------

    public boolean isEnabled(Player player, String category) {
        return resolve(player.getUniqueId(), null, PlayerSettingCategory.fromKey(category));
    }

    public boolean isEnabled(Player player, String pluginId, String category) {
        return resolve(player.getUniqueId(), pluginId, PlayerSettingCategory.fromKey(category), null);
    }

    public boolean isEnabled(Player player, String pluginId, String category, String location) {
        return resolve(player.getUniqueId(), pluginId, PlayerSettingCategory.fromKey(category), location);
    }

    public void setEnabled(Player player, String category, boolean value) {
        set(player.getUniqueId(), PlayerSettingsKey.global(category), value);
    }

    public void setEnabled(Player player, String pluginId, String category, boolean value) {
        set(player.getUniqueId(), PlayerSettingsKey.scoped(pluginId, category), value);
    }

    public void setEnabled(Player player, String pluginId, String category, String location, boolean value) {
        set(player.getUniqueId(), PlayerSettingsKey.location(pluginId, category, location), value);
    }

    public boolean isEnabled(UUID playerId, String category) {
        return resolve(playerId, null, PlayerSettingCategory.fromKey(category));
    }

    public boolean isEnabled(UUID playerId, String pluginId, String category) {
        return resolve(playerId, pluginId, PlayerSettingCategory.fromKey(category), null);
    }

    public boolean isEnabled(UUID playerId, String pluginId, String category, String location) {
        return resolve(playerId, pluginId, PlayerSettingCategory.fromKey(category), location);
    }

    private boolean resolve(UUID playerId, String pluginId, PlayerSettingCategory category) {
        return resolve(playerId, pluginId, category, null);
    }

    private boolean resolve(UUID playerId, String pluginId, PlayerSettingCategory category, String location) {
        if (!enabled) return true;

        // Non-toggleable locations always send regardless of player preference
        if (pluginId != null && !pluginId.isBlank() && location != null && !location.isBlank()) {
            String key = PlayerSettingsKey.location(pluginId, category.key(), location);
            PlayerSettingLocation loc = registeredLocations.get(key);
            if (loc != null && !loc.toggleable()) return true;
        }

        Map<String, Boolean> settings = cache.computeIfAbsent(playerId, repository::load);

        if (pluginId != null && !pluginId.isBlank()) {
            if (location != null && !location.isBlank()) {
                Boolean locationValue = settings.get(PlayerSettingsKey.location(pluginId, category.key(), location));
                if (locationValue != null) return locationValue;
            }
            Boolean scopedValue = settings.get(PlayerSettingsKey.scoped(pluginId, category).storageKey());
            if (scopedValue != null) return scopedValue;
        }

        Boolean globalValue = settings.get(PlayerSettingsKey.global(category).storageKey());
        if (globalValue != null) return globalValue;
        return defaults.getOrDefault(category, true);
    }

    public boolean canSend(Player player, String pluginId, String category, String location) {
        return isEnabled(player, pluginId, category, location);
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private void set(UUID playerId, PlayerSettingsKey key, boolean value) {
        cache.computeIfAbsent(playerId, repository::load).put(key.storageKey(), value);
        repository.upsert(playerId, key.storageKey(), value);
    }

    private void set(UUID playerId, String storageKey, boolean value) {
        cache.computeIfAbsent(playerId, repository::load).put(storageKey, value);
        repository.upsert(playerId, storageKey, value);
    }

    public Map<String, Boolean> snapshot(UUID playerId) {
        return new HashMap<>(cache.computeIfAbsent(playerId, repository::load));
    }

    public SettingsDisplayConfig displayConfig() {
        return displayConfig;
    }

    private static SettingsDisplayConfig loadDisplayConfig(FileConfiguration config) {
        String enabledSymbol  = config.getString("settings.display.enabled.symbol",   "✓");
        String enabledColor   = config.getString("settings.display.enabled.color",    "green");
        String enabledLabel   = config.getString("settings.display.enabled.label",    "Enabled");
        String enabledMat     = config.getString("settings.display.enabled.material", "LIME_DYE");
        String disabledSymbol = config.getString("settings.display.disabled.symbol",  "✗");
        String disabledColor  = config.getString("settings.display.disabled.color",   "red");
        String disabledLabel  = config.getString("settings.display.disabled.label",   "Disabled");
        String disabledMat    = config.getString("settings.display.disabled.material","GRAY_DYE");

        org.bukkit.Material enabled  = parseMaterial(enabledMat,  org.bukkit.Material.LIME_DYE);
        org.bukkit.Material disabled = parseMaterial(disabledMat, org.bukkit.Material.GRAY_DYE);

        return new SettingsDisplayConfig(
                enabledSymbol, enabledColor, enabledLabel, enabled,
                disabledSymbol, disabledColor, disabledLabel, disabled
        );
    }

    private static org.bukkit.Material parseMaterial(String name, org.bukkit.Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        try {
            return org.bukkit.Material.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}

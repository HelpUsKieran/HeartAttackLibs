package org.heartattack.heartattacklibs.dependency;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.dependency.placeholder.NoOpPlaceholderBridge;
import org.heartattack.heartattacklibs.dependency.placeholder.PlaceholderBridge;
import org.heartattack.heartattacklibs.dependency.provider.ChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;
import org.heartattack.heartattacklibs.dependency.provider.EconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.HologramProvider;
import org.heartattack.heartattacklibs.dependency.provider.InternalTextDisplayHologramProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpEconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpPermissionProvider;
import org.heartattack.heartattacklibs.dependency.provider.PermissionProvider;
import org.heartattack.heartattacklibs.dependency.runtime.RuntimeLibraryLoader;
import org.heartattack.heartattacklibs.hologram.HologramManager;

import java.util.EnumMap;
import java.util.Map;

public final class DependencyManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DebugManager debugManager;
    private final ProviderRegistry providerRegistry = new ProviderRegistry();
    private final Map<DependencyCapability, DependencyStatus> statusMap = new EnumMap<>(DependencyCapability.class);

    private final VaultBridge vaultBridge;
    private PlaceholderBridge placeholderBridge;
    private final RuntimeLibraryLoader runtimeLibraryLoader;
    private final HologramProvider internalHologramProvider;

    public DependencyManager(JavaPlugin plugin, ConfigManager configManager, DebugManager debugManager, HologramManager hologramManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.debugManager = debugManager;
        this.vaultBridge = new VaultBridge(plugin);
        this.placeholderBridge = new NoOpPlaceholderBridge();
        this.runtimeLibraryLoader = new RuntimeLibraryLoader(plugin);
        this.internalHologramProvider = new InternalTextDisplayHologramProvider(hologramManager);
    }

    public void initialize() {
        ConfigManager.ConfigFile dependencyConfig = configManager.getOrRegister("dependencies.yml");
        dependencyConfig.reload();
        FileConfiguration config = dependencyConfig.config();

        boolean runtimeLoaded = runtimeLibraryLoader.loadConfigured(config);
        statusMap.put(
                DependencyCapability.RUNTIME_LIBRARY_LOADER,
                runtimeLoaded
                        ? DependencyStatus.available("Runtime library loader ready.")
                        : DependencyStatus.unavailable("Runtime library loader failed or unavailable.")
        );

        if (config.getBoolean("providers.vault.enabled", true)) {
            DependencyStatus vaultStatus = vaultBridge.initialize();
            statusMap.put(DependencyCapability.ECONOMY, vaultStatus);
            statusMap.put(DependencyCapability.PERMISSION, vaultStatus);
            statusMap.put(DependencyCapability.CHAT, vaultStatus);
            providerRegistry.register(DependencyCapability.ECONOMY, vaultBridge.economyProvider());
            providerRegistry.register(DependencyCapability.PERMISSION, vaultBridge.permissionProvider());
            providerRegistry.register(DependencyCapability.CHAT, vaultBridge.chatProvider());
        } else {
            statusMap.put(DependencyCapability.ECONOMY, DependencyStatus.unavailable("Vault provider disabled in config."));
            statusMap.put(DependencyCapability.PERMISSION, DependencyStatus.unavailable("Vault provider disabled in config."));
            statusMap.put(DependencyCapability.CHAT, DependencyStatus.unavailable("Vault provider disabled in config."));
        }

        this.placeholderBridge = createPlaceholderBridge(config.getBoolean("providers.placeholderapi.enabled", true));
        boolean papiReady = placeholderBridge.initialize();
        statusMap.put(
                DependencyCapability.PLACEHOLDER,
                papiReady
                        ? DependencyStatus.available("PlaceholderAPI bridge ready.")
                        : DependencyStatus.unavailable(config.getBoolean("providers.placeholderapi.enabled", true)
                        ? "PlaceholderAPI not found or failed registration."
                        : "PlaceholderAPI provider disabled in config.")
        );

        providerRegistry.register(DependencyCapability.HOLOGRAM, internalHologramProvider);
        statusMap.put(DependencyCapability.HOLOGRAM, DependencyStatus.available("Using internal TextDisplay hologram provider."));

        debugStatus();
    }

    public void shutdown() {
        placeholderBridge.shutdown();
    }

    public void registerPlaceholderProvider(DPlaceholderProvider provider) {
        placeholderBridge.registerProvider(provider);
    }

    public void unregisterPlaceholderProvider(String key) {
        placeholderBridge.unregisterProvider(key);
    }

    public String parsePlaceholders(Player player, String text) {
        if (!status(DependencyCapability.PLACEHOLDER).available()) {
            return text;
        }
        return placeholderBridge.parse(player, text);
    }

    public EconomyProvider economy() {
        return providerRegistry.get(DependencyCapability.ECONOMY, EconomyProvider.class).orElseGet(NoOpEconomyProvider::new);
    }

    public PermissionProvider permission() {
        return providerRegistry.get(DependencyCapability.PERMISSION, PermissionProvider.class).orElseGet(NoOpPermissionProvider::new);
    }

    public ChatProvider chat() {
        return providerRegistry.get(DependencyCapability.CHAT, ChatProvider.class).orElseGet(NoOpChatProvider::new);
    }

    public HologramProvider holograms() {
        return providerRegistry.get(DependencyCapability.HOLOGRAM, HologramProvider.class).orElse(internalHologramProvider);
    }

    public DependencyStatus status(DependencyCapability capability) {
        return statusMap.getOrDefault(capability, DependencyStatus.unavailable("Unknown"));
    }

    public Map<DependencyCapability, DependencyStatus> statuses() {
        return new EnumMap<>(statusMap);
    }

    private void debugStatus() {
        for (Map.Entry<DependencyCapability, DependencyStatus> entry : statusMap.entrySet()) {
            debugManager.debug(
                    "dependency",
                    entry.getKey().name() + " => " + entry.getValue().available() + " (" + entry.getValue().reason() + ")"
            );
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")
                || Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            debugManager.debug("dependency", "External hologram plugin detected; internal TextDisplay provider remains active.");
        }
    }

    private PlaceholderBridge createPlaceholderBridge(boolean enabled) {
        if (!enabled) {
            return new NoOpPlaceholderBridge();
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return new NoOpPlaceholderBridge();
        }
        try {
            Class<?> bridgeClass = Class.forName("org.heartattack.heartattacklibs.dependency.papi.PapiBridge");
            return (PlaceholderBridge) bridgeClass.getConstructor(JavaPlugin.class).newInstance(plugin);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to initialize PAPI bridge reflectively: " + exception.getMessage());
            return new NoOpPlaceholderBridge();
        }
    }
}

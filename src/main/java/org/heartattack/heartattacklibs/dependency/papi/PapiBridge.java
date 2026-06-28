package org.heartattack.heartattacklibs.dependency.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.dependency.placeholder.PlaceholderBridge;
import org.heartattack.heartattacklibs.dependency.provider.DPlaceholderProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PapiBridge extends PlaceholderExpansion implements Relational, PlaceholderBridge {
    private final JavaPlugin plugin;
    private final Map<String, DPlaceholderProvider> providers = new ConcurrentHashMap<>();
    private boolean registered;

    public PapiBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return false;
        }
        if (!registered) {
            registered = register();
        }
        return registered;
    }

    public void shutdown() {
        if (registered) {
            unregister();
            registered = false;
        }
        providers.clear();
    }

    public void registerProvider(DPlaceholderProvider provider) {
        providers.put(provider.key().toLowerCase(), provider);
    }

    public void unregisterProvider(String key) {
        providers.remove(key.toLowerCase());
    }

    @Override
    public String parse(Player player, String text) {
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isBlank()) {
            return "";
        }

        String[] split = params.split("_");
        DPlaceholderProvider provider = providers.get(split[0].toLowerCase());
        if (provider == null) {
            return null;
        }

        String[] providerParams = new String[Math.max(0, split.length - 1)];
        if (split.length > 1) {
            System.arraycopy(split, 1, providerParams, 0, providerParams.length);
        }
        return provider.onPlaceholderRequest(player, providerParams);
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String params) {
        if (params == null || params.isBlank()) {
            return "";
        }
        String[] split = params.split("_");
        DPlaceholderProvider provider = providers.get(split[0].toLowerCase());
        if (provider == null) {
            return null;
        }

        String[] providerParams = new String[Math.max(0, split.length - 1)];
        if (split.length > 1) {
            System.arraycopy(split, 1, providerParams, 0, providerParams.length);
        }
        return provider.onRelationalPlaceholderRequest(one, two, providerParams);
    }

    @Override
    public String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors().isEmpty() ? java.util.List.of("HeartAttackLibs") : plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }
}

package org.heartattack.heartattacklibs.config;

import org.bukkit.plugin.Plugin;

public interface ConfigFramework {
    ConfigFileHandle getOrRegister(Plugin plugin, String fileName);

    ConfigFileHandle getOrRegister(Plugin plugin, String fileName, boolean copyDefaultsFromResource);

    ConfigFileHandle get(Plugin plugin, String fileName);

    void reloadPlugin(Plugin plugin);

    void reloadAll();
}

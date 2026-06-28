package org.heartattack.heartattacklibs.message;

import org.bukkit.plugin.Plugin;

public interface MessageFramework {
    MessageRegistry getOrRegister(Plugin plugin, String fileName);

    MessageRegistry get(Plugin plugin, String fileName);

    void reloadPlugin(Plugin plugin);

    void reloadAll();
}

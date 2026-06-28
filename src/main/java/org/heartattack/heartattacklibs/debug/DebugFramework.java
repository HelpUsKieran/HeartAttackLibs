package org.heartattack.heartattacklibs.debug;

import org.bukkit.configuration.file.FileConfiguration;

public interface DebugFramework {
    DebugManager manager();

    void loadFromConfig(FileConfiguration config);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean toggle();

    void debug(String message);

    void debug(String category, String message);
}

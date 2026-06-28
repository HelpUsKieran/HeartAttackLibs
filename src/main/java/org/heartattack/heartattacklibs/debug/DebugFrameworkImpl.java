package org.heartattack.heartattacklibs.debug;

import org.bukkit.configuration.file.FileConfiguration;

public final class DebugFrameworkImpl implements DebugFramework {
    private final DebugManager manager;

    public DebugFrameworkImpl(DebugManager manager) {
        this.manager = manager;
    }

    @Override
    public DebugManager manager() {
        return manager;
    }

    @Override
    public void loadFromConfig(FileConfiguration config) {
        manager.loadFromConfig(config);
    }

    @Override
    public boolean isEnabled() {
        return manager.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        manager.setEnabled(enabled);
    }

    @Override
    public boolean toggle() {
        return manager.toggle();
    }

    @Override
    public void debug(String message) {
        manager.debug(message);
    }

    @Override
    public void debug(String category, String message) {
        manager.debug(category, message);
    }
}

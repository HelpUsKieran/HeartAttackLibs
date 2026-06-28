package org.heartattack.heartattacklibs.hologram;

import org.bukkit.Location;

public final class HologramFrameworkImpl implements HologramFramework {
    private final HologramManager manager;

    public HologramFrameworkImpl(HologramManager manager) {
        this.manager = manager;
    }

    @Override
    public HologramManager manager() {
        return manager;
    }

    @Override
    public HologramBuilder builder(Location location) {
        return HologramBuilder.at(location);
    }

    @Override
    public Hologram create(HologramBuilder builder) {
        return manager.create(builder);
    }

    @Override
    public Hologram track(Hologram hologram) {
        return manager.track(hologram);
    }

    @Override
    public void remove(Hologram hologram) {
        manager.remove(hologram);
    }

    @Override
    public void despawnAll() {
        manager.despawnAll();
    }
}

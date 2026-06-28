package org.heartattack.heartattacklibs.hologram;

import org.bukkit.Location;

public interface HologramFramework {
    HologramManager manager();

    HologramBuilder builder(Location location);

    Hologram create(HologramBuilder builder);

    Hologram track(Hologram hologram);

    void remove(Hologram hologram);

    void despawnAll();
}

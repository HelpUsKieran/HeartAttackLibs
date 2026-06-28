package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.Location;
import org.heartattack.heartattacklibs.hologram.Hologram;

import java.util.List;

public interface HologramProvider {
    boolean available();

    Hologram create(Location location, List<String> lines);

    String source();
}

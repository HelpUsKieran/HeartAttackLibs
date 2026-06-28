package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.Location;
import org.heartattack.heartattacklibs.hologram.Hologram;
import org.heartattack.heartattacklibs.hologram.HologramBuilder;
import org.heartattack.heartattacklibs.hologram.HologramManager;

import java.util.List;

public final class InternalTextDisplayHologramProvider implements HologramProvider {
    private final HologramManager hologramManager;

    public InternalTextDisplayHologramProvider(HologramManager hologramManager) {
        this.hologramManager = hologramManager;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public Hologram create(Location location, List<String> lines) {
        Hologram hologram = hologramManager.create(
                HologramBuilder.at(location).lines(lines.toArray(new String[0]))
        );
        hologram.spawn();
        return hologram;
    }

    @Override
    public String source() {
        return "HeartAttackLibs-textdisplay";
    }
}

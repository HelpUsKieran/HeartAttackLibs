package org.heartattack.heartattacklibs.hologram;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramManager {
    private final Set<Hologram> holograms = ConcurrentHashMap.newKeySet();

    public Hologram track(Hologram hologram) {
        holograms.add(hologram);
        return hologram;
    }

    public Hologram create(HologramBuilder builder) {
        Hologram hologram = builder.build();
        holograms.add(hologram);
        return hologram;
    }

    public void remove(Hologram hologram) {
        if (hologram == null) {
            return;
        }
        hologram.despawn();
        holograms.remove(hologram);
    }

    public void despawnAll() {
        for (Hologram hologram : holograms) {
            hologram.despawn();
        }
        holograms.clear();
    }
}

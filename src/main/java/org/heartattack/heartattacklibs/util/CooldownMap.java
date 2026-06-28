package org.heartattack.heartattacklibs.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownMap {
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean onCooldown(UUID uniqueId) {
        Long expiry = cooldowns.get(uniqueId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public long remainingMillis(UUID uniqueId) {
        Long expiry = cooldowns.get(uniqueId);
        if (expiry == null) {
            return 0L;
        }
        return Math.max(0L, expiry - System.currentTimeMillis());
    }

    public void set(UUID uniqueId, long durationMillis) {
        cooldowns.put(uniqueId, System.currentTimeMillis() + durationMillis);
    }

    public void clear(UUID uniqueId) {
        cooldowns.remove(uniqueId);
    }

    public void clearExpired() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
}

package org.heartattack.heartattacklibs.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GroupedCooldownMap {
    private final Map<String, CooldownMap> groups = new ConcurrentHashMap<>();

    public void set(String group, UUID uniqueId, long durationMillis) {
        group(group).set(uniqueId, durationMillis);
    }

    public boolean onCooldown(String group, UUID uniqueId) {
        return group(group).onCooldown(uniqueId);
    }

    public long remainingMillis(String group, UUID uniqueId) {
        return group(group).remainingMillis(uniqueId);
    }

    public long remainingSeconds(String group, UUID uniqueId) {
        long remaining = remainingMillis(group, uniqueId);
        return remaining <= 0L ? 0L : Math.max(1L, remaining / 1000L);
    }

    public void clear(String group, UUID uniqueId) {
        group(group).clear(uniqueId);
    }

    public void clearGroup(String group) {
        groups.remove(group.toLowerCase());
    }

    public void clearExpired() {
        for (CooldownMap map : groups.values()) {
            map.clearExpired();
        }
    }

    private CooldownMap group(String group) {
        return groups.computeIfAbsent(group.toLowerCase(), key -> new CooldownMap());
    }
}

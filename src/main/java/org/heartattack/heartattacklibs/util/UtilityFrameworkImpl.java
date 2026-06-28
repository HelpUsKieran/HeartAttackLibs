package org.heartattack.heartattacklibs.util;

public final class UtilityFrameworkImpl implements UtilityFramework {
    @Override
    public CooldownMap cooldowns() {
        return new CooldownMap();
    }

    @Override
    public GroupedCooldownMap groupedCooldowns() {
        return new GroupedCooldownMap();
    }

    @Override
    public TaskTracker taskTracker() {
        return new TaskTracker();
    }

    @Override
    public PlaceholderMap placeholders() {
        return PlaceholderMap.create();
    }
}

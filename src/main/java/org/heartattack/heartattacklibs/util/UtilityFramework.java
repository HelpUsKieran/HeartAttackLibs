package org.heartattack.heartattacklibs.util;

public interface UtilityFramework {
    CooldownMap cooldowns();

    GroupedCooldownMap groupedCooldowns();

    TaskTracker taskTracker();

    PlaceholderMap placeholders();
}

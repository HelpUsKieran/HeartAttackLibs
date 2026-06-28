package org.heartattack.heartattacklibs.dependency;

public record DependencyStatus(boolean available, String reason) {
    public static DependencyStatus available(String reason) {
        return new DependencyStatus(true, reason);
    }

    public static DependencyStatus unavailable(String reason) {
        return new DependencyStatus(false, reason);
    }
}

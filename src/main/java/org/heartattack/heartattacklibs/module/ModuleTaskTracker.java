package org.heartattack.heartattacklibs.module;

import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ModuleTaskTracker {
    private final Set<BukkitTask> tasks = ConcurrentHashMap.newKeySet();

    public BukkitTask track(BukkitTask task) {
        tasks.add(task);
        return task;
    }

    public void cancelAll() {
        for (BukkitTask task : tasks) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
        tasks.clear();
    }
}

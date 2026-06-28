package org.heartattack.heartattacklibs.util;

import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TaskTracker {
    private final Set<Integer> taskIds = ConcurrentHashMap.newKeySet();

    public BukkitTask track(BukkitTask task) {
        taskIds.add(task.getTaskId());
        return task;
    }

    public void forget(int taskId) {
        taskIds.remove(taskId);
    }

    public void cancelAll() {
        for (Integer taskId : taskIds) {
            BukkitTask task = org.bukkit.Bukkit.getScheduler().getPendingTasks().stream()
                    .filter(pending -> pending.getTaskId() == taskId)
                    .findFirst()
                    .orElse(null);
            if (task != null) {
                task.cancel();
            } else {
                org.bukkit.Bukkit.getScheduler().cancelTask(taskId);
            }
        }
        taskIds.clear();
    }
}

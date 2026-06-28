package org.heartattack.heartattacklibs.module.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PluginModuleManager<C> {
    private final JavaPlugin plugin;
    private final C context;
    private final Predicate<String> enabledPredicate;
    private final Consumer<String> debugLogger;
    private final Map<String, PluginModule<C>> modules = new LinkedHashMap<>();
    private final Map<String, PluginModuleState> states = new HashMap<>();
    private final List<PluginModule<C>> enabledOrder = new ArrayList<>();

    public PluginModuleManager(JavaPlugin plugin, C context, Predicate<String> enabledPredicate, Consumer<String> debugLogger) {
        this.plugin = plugin;
        this.context = context;
        this.enabledPredicate = enabledPredicate;
        this.debugLogger = debugLogger == null ? message -> { } : debugLogger;
    }

    public void register(PluginModule<C> module) {
        String name = module.name().toLowerCase();
        if (modules.containsKey(name)) {
            throw new IllegalStateException("Module already registered: " + module.name());
        }
        modules.put(name, module);
        states.put(name, PluginModuleState.REGISTERED);
    }

    public void loadAndEnableAll() {
        List<PluginModule<C>> ordered = resolveOrder();
        for (PluginModule<C> module : ordered) {
            String name = module.name().toLowerCase();
            if (!enabledPredicate.test(name)) {
                states.put(name, PluginModuleState.DISABLED);
                continue;
            }

            try {
                module.onLoad(context);
                module.onEnable(context);
                states.put(name, PluginModuleState.ENABLED);
                enabledOrder.add(module);
                debugLogger.accept("Enabled module: " + module.name());
            } catch (Exception exception) {
                states.put(name, PluginModuleState.FAILED);
                plugin.getLogger().warning("Failed to enable module " + module.name() + ": " + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }

    public void disableAll() {
        for (int i = enabledOrder.size() - 1; i >= 0; i--) {
            PluginModule<C> module = enabledOrder.get(i);
            String name = module.name().toLowerCase();
            try {
                module.onDisable(context);
                states.put(name, PluginModuleState.DISABLED);
                debugLogger.accept("Disabled module: " + module.name());
            } catch (Exception exception) {
                states.put(name, PluginModuleState.FAILED);
                plugin.getLogger().warning("Failed to disable module " + module.name() + ": " + exception.getMessage());
            }
        }
        enabledOrder.clear();
    }

    public Map<String, PluginModuleState> states() {
        return new HashMap<>(states);
    }

    private List<PluginModule<C>> resolveOrder() {
        List<PluginModule<C>> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (PluginModule<C> module : modules.values()) {
            dfs(module, visited, visiting, ordered);
        }
        return ordered;
    }

    private void dfs(PluginModule<C> module, Set<String> visited, Set<String> visiting, List<PluginModule<C>> ordered) {
        String name = module.name().toLowerCase();
        if (visited.contains(name)) {
            return;
        }
        if (!visiting.add(name)) {
            throw new IllegalStateException("Cycle detected in module dependencies at " + module.name());
        }

        Set<String> hardDependencies = module.dependencies() == null ? Set.of() : module.dependencies();
        Deque<String> dependencies = new ArrayDeque<>(hardDependencies);
        while (!dependencies.isEmpty()) {
            String dependencyName = dependencies.pop().toLowerCase();
            PluginModule<C> dependency = modules.get(dependencyName);
            if (dependency == null) {
                throw new IllegalStateException("Missing dependency " + dependencyName + " for module " + module.name());
            }
            dfs(dependency, visited, visiting, ordered);
        }

        Set<String> softDependencies = module.softDependencies() == null ? Set.of() : module.softDependencies();
        for (String softDependency : softDependencies) {
            PluginModule<C> dependency = modules.get(softDependency.toLowerCase());
            if (dependency != null) {
                dfs(dependency, visited, visiting, ordered);
            }
        }

        visiting.remove(name);
        visited.add(name);
        ordered.add(module);
    }
}

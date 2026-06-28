package org.heartattack.heartattacklibs.module;

import org.bukkit.configuration.file.FileConfiguration;
import org.heartattack.heartattacklibs.HeartAttackLibs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModuleManager {
    private final HeartAttackLibs plugin;
    private final ModuleContext context;
    private final Map<String, DModule> modules = new LinkedHashMap<>();
    private final Map<String, ModuleState> states = new HashMap<>();
    private final List<DModule> enabledOrder = new ArrayList<>();
    private boolean enabling;

    public ModuleManager(HeartAttackLibs plugin, ModuleContext context) {
        this.plugin = plugin;
        this.context = context;
    }

    public void register(DModule module) {
        String name = module.name().toLowerCase();
        if (modules.containsKey(name)) {
            throw new IllegalStateException("Module already registered: " + module.name());
        }
        modules.put(name, module);
        states.put(name, ModuleState.REGISTERED);
    }

    public void loadAndEnableAll() {
        if (enabling) {
            return;
        }

        if (!enabledOrder.isEmpty()) {
            disableAll();
        }

        enabling = true;
        List<DModule> ordered = resolveOrder();
        FileConfiguration config = context.configManager().getOrRegister("modules.yml").config();

        try {
            for (DModule module : ordered) {
                String name = module.name().toLowerCase();
                if (!config.getBoolean("modules." + name + ".enabled", true)) {
                    states.put(name, ModuleState.DISABLED);
                    continue;
                }

                try {
                    module.onLoad(context);
                    module.onEnable(context);
                    states.put(name, ModuleState.ENABLED);
                    enabledOrder.add(module);
                    context.debugManager().debug("module", "Enabled module: " + module.name());
                } catch (Exception exception) {
                    states.put(name, ModuleState.FAILED);
                    plugin.getLogger().warning("Failed to enable module " + module.name() + ": " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        } finally {
            enabling = false;
        }
    }

    public void disableAll() {
        for (int i = enabledOrder.size() - 1; i >= 0; i--) {
            DModule module = enabledOrder.get(i);
            String name = module.name().toLowerCase();
            try {
                module.onDisable(context);
                states.put(name, ModuleState.DISABLED);
                context.debugManager().debug("module", "Disabled module: " + module.name());
            } catch (Exception exception) {
                states.put(name, ModuleState.FAILED);
                plugin.getLogger().warning("Failed to disable module " + module.name() + ": " + exception.getMessage());
            }
        }
        enabledOrder.clear();
    }

    public ModuleState state(String moduleName) {
        return states.getOrDefault(moduleName.toLowerCase(), ModuleState.REGISTERED);
    }

    public Map<String, ModuleState> states() {
        return new HashMap<>(states);
    }

    private List<DModule> resolveOrder() {
        List<DModule> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (DModule module : modules.values()) {
            dfs(module, visited, visiting, ordered);
        }
        return ordered;
    }

    private void dfs(DModule module, Set<String> visited, Set<String> visiting, List<DModule> ordered) {
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
            DModule dependency = modules.get(dependencyName);
            if (dependency == null) {
                throw new IllegalStateException("Missing dependency " + dependencyName + " for module " + module.name());
            }
            dfs(dependency, visited, visiting, ordered);
        }

        Set<String> softDependencies = module.softDependencies() == null ? Set.of() : module.softDependencies();
        for (String softDependency : softDependencies) {
            DModule dependency = modules.get(softDependency.toLowerCase());
            if (dependency != null) {
                dfs(dependency, visited, visiting, ordered);
            }
        }

        visiting.remove(name);
        visited.add(name);
        ordered.add(module);
    }
}

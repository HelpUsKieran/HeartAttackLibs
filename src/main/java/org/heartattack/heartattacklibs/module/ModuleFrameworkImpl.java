package org.heartattack.heartattacklibs.module;

import java.util.Map;

public final class ModuleFrameworkImpl implements ModuleFramework {
    private final ModuleManager manager;

    public ModuleFrameworkImpl(ModuleManager manager) {
        this.manager = manager;
    }

    @Override
    public ModuleManager manager() {
        return manager;
    }

    @Override
    public void register(DModule module) {
        manager.register(module);
    }

    @Override
    public void loadAndEnableAll() {
        manager.loadAndEnableAll();
    }

    @Override
    public void disableAll() {
        manager.disableAll();
    }

    @Override
    public ModuleState state(String moduleName) {
        return manager.state(moduleName);
    }

    @Override
    public Map<String, ModuleState> states() {
        return manager.states();
    }
}

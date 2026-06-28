package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.heartattack.heartattacklibs.module.ModuleManager;
import org.heartattack.heartattacklibs.module.core.CoreModule;

@Singleton
public final class FrameworkModuleBootstrap {
    private final ModuleManager moduleManager;
    private volatile boolean coreRegistered;

    @Inject
    public FrameworkModuleBootstrap(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void enableModules() {
        if (!coreRegistered) {
            moduleManager.register(new CoreModule());
            coreRegistered = true;
        }
        moduleManager.loadAndEnableAll();
    }

    public void reloadModules() {
        moduleManager.disableAll();
        moduleManager.loadAndEnableAll();
    }

    public void disableModules() {
        moduleManager.disableAll();
    }
}

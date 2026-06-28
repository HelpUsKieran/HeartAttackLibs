package org.heartattack.heartattacklibs.module;

import java.util.Map;

public interface ModuleFramework {
    ModuleManager manager();

    void register(DModule module);

    void loadAndEnableAll();

    void disableAll();

    ModuleState state(String moduleName);

    Map<String, ModuleState> states();
}

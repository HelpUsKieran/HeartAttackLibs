package org.heartattack.heartattacklibs.module;

import java.util.Collections;
import java.util.Set;

public interface DModule {
    String name();

    default Set<String> dependencies() {
        return Collections.emptySet();
    }

    default Set<String> softDependencies() {
        return Collections.emptySet();
    }

    default void onLoad(ModuleContext context) {
    }

    default void onEnable(ModuleContext context) {
    }

    default void onDisable(ModuleContext context) {
    }
}

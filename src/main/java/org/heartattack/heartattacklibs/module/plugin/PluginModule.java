package org.heartattack.heartattacklibs.module.plugin;

import java.util.Collections;
import java.util.Set;

public interface PluginModule<C> {
    String name();

    default Set<String> dependencies() {
        return Collections.emptySet();
    }

    default Set<String> softDependencies() {
        return Collections.emptySet();
    }

    default void onLoad(C context) {
    }

    default void onEnable(C context) {
    }

    default void onDisable(C context) {
    }
}

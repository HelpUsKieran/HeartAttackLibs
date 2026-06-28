package org.heartattack.heartattacklibs.module.core;

import org.heartattack.heartattacklibs.module.DModule;
import org.heartattack.heartattacklibs.module.ModuleContext;

public final class CoreModule implements DModule {
    private final HeartAttackLibsPlaceholderProvider placeholderProvider = new HeartAttackLibsPlaceholderProvider();

    @Override
    public String name() {
        return "core";
    }

    @Override
    public void onEnable(ModuleContext context) {
        context.dependencyManager().registerPlaceholderProvider(placeholderProvider);
    }

    @Override
    public void onDisable(ModuleContext context) {
        context.dependencyManager().unregisterPlaceholderProvider(placeholderProvider.key());
    }
}

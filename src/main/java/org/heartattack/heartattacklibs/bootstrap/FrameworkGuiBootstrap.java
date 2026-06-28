package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.gui.GuiFramework;
import org.heartattack.heartattacklibs.gui.GuiFrameworkImpl;
import org.heartattack.heartattacklibs.gui.GuiListener;

@Singleton
public final class FrameworkGuiBootstrap {
    private final HeartAttackLibs plugin;
    private final GuiFramework guiFramework;

    @Inject
    public FrameworkGuiBootstrap(HeartAttackLibs plugin, GuiFramework guiFramework) {
        this.plugin = plugin;
        this.guiFramework = guiFramework;
    }

    public void enable() {
        if (guiFramework instanceof GuiFrameworkImpl impl) {
            Bukkit.getPluginManager().registerEvents(new GuiListener(impl), plugin);
        }
        plugin.saveResource("menus/demo.yml", true);
        plugin.saveResource("menus/settings_main.yml", true);
        plugin.saveResource("menus/settings_overrides.yml", true);
        plugin.saveResource("menus/settings_plugins.yml", true);
        plugin.saveResource("menus/settings_plugin_categories.yml", true);
        plugin.saveResource("menus/settings_locations.yml", true);
        guiFramework.registerMenus(plugin, "menus");
    }

    public void reload() {
        guiFramework.reloadPluginMenus(plugin);
    }
}

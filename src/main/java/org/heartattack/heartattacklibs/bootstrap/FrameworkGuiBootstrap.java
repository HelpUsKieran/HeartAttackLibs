package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.gui.GuiFramework;
import org.heartattack.heartattacklibs.gui.GuiFrameworkImpl;
import org.heartattack.heartattacklibs.gui.GuiListener;

import java.util.List;

@Singleton
public final class FrameworkGuiBootstrap {
    private static final List<String> BUNDLED_MENUS = List.of(
            "menus/demo.yml",
            "menus/settings_main.yml",
            "menus/settings_overrides.yml",
            "menus/settings_plugins.yml",
            "menus/settings_plugin_categories.yml",
            "menus/settings_locations.yml"
    );

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
        for (String menu : BUNDLED_MENUS) {
            saveBundledMenu(menu);
        }
        guiFramework.registerMenus(plugin, "menus");
    }

    public void reload() {
        guiFramework.reloadPluginMenus(plugin);
    }

    private void saveBundledMenu(String path) {
        if (plugin.getResource(path) == null) {
            plugin.getLogger().warning("Bundled GUI menu is missing from the plugin jar: " + path);
            return;
        }
        plugin.saveResource(path, true);
    }
}

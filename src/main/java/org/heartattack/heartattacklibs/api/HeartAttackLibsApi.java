package org.heartattack.heartattacklibs.api;

import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.command.CommandFramework;
import org.heartattack.heartattacklibs.config.ConfigFramework;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.database.DatabaseFramework;
import org.heartattack.heartattacklibs.database.DatabaseManager;
import org.heartattack.heartattacklibs.dependency.DependencyFramework;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.debug.DebugFramework;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.format.FormatFramework;
import org.heartattack.heartattacklibs.gui.GuiFramework;
import org.heartattack.heartattacklibs.hologram.HologramFramework;
import org.heartattack.heartattacklibs.hologram.HologramManager;
import org.heartattack.heartattacklibs.item.ItemFramework;
import org.heartattack.heartattacklibs.message.MessageFramework;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.message.PathMessageService;
import org.heartattack.heartattacklibs.module.ModuleFramework;
import org.heartattack.heartattacklibs.module.ModuleManager;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.text.TextFramework;
import org.heartattack.heartattacklibs.text.MiniMessageService;
import org.heartattack.heartattacklibs.util.UtilityFramework;

public interface HeartAttackLibsApi {
    CommandFramework commandFramework();

    default CommandFramework command() {
        return commandFramework();
    }

    CommandManager commands();

    ConfigFramework configFramework();

    DatabaseManager database();

    DatabaseFramework databaseFramework();

    /**
     * Creates a new, independent DatabaseFramework for the given plugin.
     * The database file will be stored in the plugin's own data folder.
     * Each call returns a fresh instance — call connect() on it before use.
     * Use this instead of databaseFramework() for plugin-specific persistence.
     */
    DatabaseFramework createDatabase(JavaPlugin plugin);

    GuiFramework guiFramework();

    default GuiFramework gui() {
        return guiFramework();
    }

    ConfigManager configs();

    DebugFramework debugFramework();

    MiniMessageService miniMessage();

    FormatFramework formatFramework();

    DebugManager debug();

    HologramFramework hologramFramework();

    HologramManager holograms();

    ItemFramework itemFramework();

    MessageFramework messageFramework();

    MessageManager messages();

    PathMessageService pathMessages();

    ModuleFramework moduleFramework();

    ModuleManager modules();

    TextFramework textFramework();

    PlayerSettingsService playerSettings();

    UtilityFramework utilityFramework();

    DependencyFramework dependencyFramework();

    DependencyManager dependencies();
}


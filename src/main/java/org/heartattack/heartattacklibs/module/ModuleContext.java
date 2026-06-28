package org.heartattack.heartattacklibs.module;

import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandFramework;
import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.heartattack.heartattacklibs.database.DatabaseManager;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.gui.GuiFramework;
import org.heartattack.heartattacklibs.hologram.HologramManager;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.text.MiniMessageService;

public record ModuleContext(
        HeartAttackLibs plugin,
        CommandFramework commandFramework,
        CommandManager commandManager,
        DatabaseManager databaseManager,
        GuiFramework guiFramework,
        ConfigManager configManager,
        MiniMessageService miniMessageService,
        DebugManager debugManager,
        HologramManager hologramManager,
        MessageManager messageManager,
        DependencyManager dependencyManager
) {
}


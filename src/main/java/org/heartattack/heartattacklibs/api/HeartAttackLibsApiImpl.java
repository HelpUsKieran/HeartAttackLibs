package org.heartattack.heartattacklibs.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.command.CommandFramework;
import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.config.ConfigFramework;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.heartattack.heartattacklibs.database.DatabaseFramework;
import org.heartattack.heartattacklibs.database.DatabaseFrameworkImpl;
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
import org.heartattack.heartattacklibs.text.MiniMessageService;
import org.heartattack.heartattacklibs.text.TextFramework;
import org.heartattack.heartattacklibs.util.UtilityFramework;

public final class HeartAttackLibsApiImpl implements HeartAttackLibsApi {
    private final HeartAttackLibs plugin;

    public HeartAttackLibsApiImpl(HeartAttackLibs plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandFramework commandFramework() {
        return plugin.commandFramework();
    }

    @Override
    public CommandManager commands() {
        return plugin.commandManager();
    }

    @Override
    public ConfigFramework configFramework() {
        return plugin.configFramework();
    }

    @Override
    public DatabaseManager database() {
        return plugin.databaseManager();
    }

    @Override
    public DatabaseFramework databaseFramework() {
        return plugin.databaseFramework();
    }

    @Override
    public DatabaseFramework createDatabase(final JavaPlugin plugin) {
        return new DatabaseFrameworkImpl(new DatabaseManager(plugin));
    }

    @Override
    public GuiFramework guiFramework() {
        return plugin.guiFramework();
    }

    @Override
    public ConfigManager configs() {
        return plugin.configManager();
    }

    @Override
    public DebugFramework debugFramework() {
        return plugin.debugFramework();
    }

    @Override
    public MiniMessageService miniMessage() {
        return plugin.miniMessage();
    }

    @Override
    public FormatFramework formatFramework() {
        return plugin.formatFramework();
    }

    @Override
    public DebugManager debug() {
        return plugin.debugManager();
    }

    @Override
    public HologramFramework hologramFramework() {
        return plugin.hologramFramework();
    }

    @Override
    public HologramManager holograms() {
        return plugin.hologramManager();
    }

    @Override
    public ItemFramework itemFramework() {
        return plugin.itemFramework();
    }

    @Override
    public MessageFramework messageFramework() {
        return plugin.messageFramework();
    }

    @Override
    public MessageManager messages() {
        return plugin.messageManager();
    }

    @Override
    public PathMessageService pathMessages() {
        return plugin.pathMessageService();
    }

    @Override
    public ModuleFramework moduleFramework() {
        return plugin.moduleFramework();
    }

    @Override
    public ModuleManager modules() {
        return plugin.moduleManager();
    }

    @Override
    public TextFramework textFramework() {
        return plugin.textFramework();
    }

    @Override
    public PlayerSettingsService playerSettings() {
        return plugin.playerSettings();
    }

    @Override
    public UtilityFramework utilityFramework() {
        return plugin.utilityFramework();
    }

    @Override
    public DependencyFramework dependencyFramework() {
        return plugin.dependencyFramework();
    }

    @Override
    public DependencyManager dependencies() {
        return plugin.dependencyManager();
    }
}


package org.heartattack.heartattacklibs;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApi;
import org.heartattack.heartattacklibs.bootstrap.PluginBootstrap;
import org.heartattack.heartattacklibs.command.CommandFramework;
import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.config.ConfigFramework;
import org.heartattack.heartattacklibs.config.ConfigManager;
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
import org.heartattack.heartattacklibs.inject.HeartAttackLibsModule;
import org.heartattack.heartattacklibs.item.ItemFramework;
import org.heartattack.heartattacklibs.message.MessageFramework;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.message.PathMessageService;
import org.heartattack.heartattacklibs.module.ModuleFramework;
import org.heartattack.heartattacklibs.module.ModuleManager;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.text.MiniMessageService;
import org.heartattack.heartattacklibs.text.TextFramework;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;
import org.heartattack.heartattacklibs.util.UtilityFramework;

public final class HeartAttackLibs extends JavaPlugin {
    @Inject
    private CommandManager commandManager;
    @Inject
    private CommandFramework commandFramework;
    @Inject
    private ConfigFramework configFramework;
    @Inject
    private DatabaseManager databaseManager;
    @Inject
    private DatabaseFramework databaseFramework;
    @Inject
    private GuiFramework guiFramework;
    @Inject
    private ConfigManager configManager;
    @Inject
    private MiniMessageService miniMessage;
    @Inject
    private DebugManager debugManager;
    @Inject
    private DebugFramework debugFramework;
    @Inject
    private FormatFramework formatFramework;
    @Inject
    private HologramManager hologramManager;
    @Inject
    private HologramFramework hologramFramework;
    @Inject
    private ItemFramework itemFramework;
    @Inject
    private MessageManager messageManager;
    @Inject
    private MessageFramework messageFramework;
    @Inject
    private PathMessageService pathMessageService;
    @Inject
    private ModuleManager moduleManager;
    @Inject
    private ModuleFramework moduleFramework;
    @Inject
    private TextFramework textFramework;
    @Inject
    private UtilityFramework utilityFramework;
    @Inject
    private DependencyManager dependencyManager;
    @Inject
    private DependencyFramework dependencyFramework;
    @Inject
    private PlayerSettingsService playerSettingsService;
    @Inject
    private HeartAttackLibsApi api;
    @Inject
    private PluginBootstrap pluginBootstrap;

    private Injector injector;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        UnicodeSmallCaps.setEnabled(getConfig().getBoolean("text.small-caps.enabled", true));
        UnicodeSmallCaps.setAllowedPlugins(getConfig().getStringList("text.small-caps.allowed-plugins"));
        this.injector = Guice.createInjector(new HeartAttackLibsModule(this));
        this.injector.injectMembers(this);
        this.pluginBootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (pluginBootstrap != null) {
            pluginBootstrap.disable();
        }
    }

    public CommandManager commandManager() {
        return commandManager;
    }

    public CommandFramework commandFramework() {
        return commandFramework;
    }

    public ConfigFramework configFramework() {
        return configFramework;
    }

    public DatabaseManager databaseManager() {
        return databaseManager;
    }

    public DatabaseFramework databaseFramework() {
        return databaseFramework;
    }

    public GuiFramework guiFramework() {
        return guiFramework;
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MiniMessageService miniMessage() {
        return miniMessage;
    }

    public TextFramework textFramework() {
        return textFramework;
    }

    public DebugManager debugManager() {
        return debugManager;
    }

    public DebugFramework debugFramework() {
        return debugFramework;
    }

    public FormatFramework formatFramework() {
        return formatFramework;
    }

    public HologramManager hologramManager() {
        return hologramManager;
    }

    public HologramFramework hologramFramework() {
        return hologramFramework;
    }

    public ItemFramework itemFramework() {
        return itemFramework;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public MessageFramework messageFramework() {
        return messageFramework;
    }

    public PathMessageService pathMessageService() {
        return pathMessageService;
    }

    public ModuleManager moduleManager() {
        return moduleManager;
    }

    public ModuleFramework moduleFramework() {
        return moduleFramework;
    }

    public UtilityFramework utilityFramework() {
        return utilityFramework;
    }

    public PlayerSettingsService playerSettings() {
        return playerSettingsService;
    }

    public DependencyManager dependencyManager() {
        return dependencyManager;
    }

    public DependencyFramework dependencyFramework() {
        return dependencyFramework;
    }

    public HeartAttackLibsApi api() {
        return api;
    }

    public void reloadFramework() {
        if (pluginBootstrap != null) {
            pluginBootstrap.reloadFramework();
        }
    }
}



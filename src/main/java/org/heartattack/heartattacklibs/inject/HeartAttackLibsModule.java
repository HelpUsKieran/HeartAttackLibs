package org.heartattack.heartattacklibs.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.heartattack.heartattacklibs.HeartAttackLibs;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApi;
import org.heartattack.heartattacklibs.api.HeartAttackLibsApiImpl;
import org.heartattack.heartattacklibs.command.CommandFramework;
import org.heartattack.heartattacklibs.command.CommandFrameworkImpl;
import org.heartattack.heartattacklibs.command.CommandManager;
import org.heartattack.heartattacklibs.config.ConfigFramework;
import org.heartattack.heartattacklibs.config.ConfigFrameworkImpl;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.heartattack.heartattacklibs.database.DatabaseFramework;
import org.heartattack.heartattacklibs.database.DatabaseFrameworkImpl;
import org.heartattack.heartattacklibs.database.DatabaseManager;
import org.heartattack.heartattacklibs.dependency.DependencyFramework;
import org.heartattack.heartattacklibs.dependency.DependencyFrameworkImpl;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.debug.DebugFramework;
import org.heartattack.heartattacklibs.debug.DebugFrameworkImpl;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.format.FormatFramework;
import org.heartattack.heartattacklibs.format.FormatFrameworkImpl;
import org.heartattack.heartattacklibs.gui.GuiFramework;
import org.heartattack.heartattacklibs.gui.GuiFrameworkImpl;
import org.heartattack.heartattacklibs.hologram.HologramFramework;
import org.heartattack.heartattacklibs.hologram.HologramFrameworkImpl;
import org.heartattack.heartattacklibs.hologram.HologramManager;
import org.heartattack.heartattacklibs.item.ItemFramework;
import org.heartattack.heartattacklibs.item.ItemFrameworkImpl;
import org.heartattack.heartattacklibs.message.MessageFramework;
import org.heartattack.heartattacklibs.message.MessageFrameworkImpl;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.message.PathMessageService;
import org.heartattack.heartattacklibs.module.ModuleFramework;
import org.heartattack.heartattacklibs.module.ModuleFrameworkImpl;
import org.heartattack.heartattacklibs.module.ModuleContext;
import org.heartattack.heartattacklibs.module.ModuleManager;
import org.heartattack.heartattacklibs.settings.ExternalMessageFilter;
import org.heartattack.heartattacklibs.settings.PlayerSettingsRepository;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.settings.SettingsFileManager;
import org.heartattack.heartattacklibs.text.MiniMessageService;
import org.heartattack.heartattacklibs.text.TextFramework;
import org.heartattack.heartattacklibs.text.TextFrameworkImpl;
import org.heartattack.heartattacklibs.util.UtilityFramework;
import org.heartattack.heartattacklibs.util.UtilityFrameworkImpl;

public final class HeartAttackLibsModule extends AbstractModule {
    private final HeartAttackLibs plugin;

    public HeartAttackLibsModule(HeartAttackLibs plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(HeartAttackLibs.class).toInstance(plugin);
    }

    @Provides
    @Singleton
    ConfigManager provideConfigManager(HeartAttackLibs plugin) {
        ConfigManager configManager = new ConfigManager(plugin);
        configManager.getOrRegister("messages.yml");
        configManager.getOrRegister("modules.yml");
        configManager.getOrRegister("dependencies.yml");
        return configManager;
    }

    @Provides
    @Singleton
    ConfigFramework provideConfigFramework() {
        return new ConfigFrameworkImpl();
    }

    @Provides
    @Singleton
    MiniMessageService provideMiniMessageService(ConfigManager configManager) {
        MiniMessageService service = new MiniMessageService();
        service.setPrefix(configManager.config().getString("messages.prefix", "<gray>[<gold>HeartAttackLibs</gold>] </gray>"));
        return service;
    }

    @Provides
    @Singleton
    DebugManager provideDebugManager(HeartAttackLibs plugin, ConfigManager configManager) {
        DebugManager debugManager = new DebugManager(plugin);
        debugManager.loadFromConfig(configManager.config());
        return debugManager;
    }

    @Provides
    @Singleton
    DebugFramework provideDebugFramework(DebugManager debugManager) {
        return new DebugFrameworkImpl(debugManager);
    }

    @Provides
    @Singleton
    CommandManager provideCommandManager(HeartAttackLibs plugin) {
        return new CommandManager(plugin);
    }

    @Provides
    @Singleton
    CommandFramework provideCommandFramework(CommandManager commandManager) {
        return new CommandFrameworkImpl(commandManager);
    }

    @Provides
    @Singleton
    DatabaseManager provideDatabaseManager(HeartAttackLibs plugin) {
        return new DatabaseManager(plugin);
    }

    @Provides
    @Singleton
    DatabaseFramework provideDatabaseFramework(DatabaseManager manager) {
        return new DatabaseFrameworkImpl(manager);
    }

    @Provides
    @Singleton
    GuiFramework provideGuiFramework(HeartAttackLibs plugin) {
        return new GuiFrameworkImpl(plugin);
    }

    @Provides
    @Singleton
    FormatFramework provideFormatFramework() {
        return new FormatFrameworkImpl();
    }

    @Provides
    @Singleton
    HologramManager provideHologramManager() {
        return new HologramManager();
    }

    @Provides
    @Singleton
    HologramFramework provideHologramFramework(HologramManager manager) {
        return new HologramFrameworkImpl(manager);
    }

    @Provides
    @Singleton
    ItemFramework provideItemFramework() {
        return new ItemFrameworkImpl();
    }

    @Provides
    @Singleton
    MessageFramework provideMessageFramework(ConfigFramework configFramework, MiniMessageService miniMessageService, PlayerSettingsService settingsService) {
        return new MessageFrameworkImpl(configFramework, miniMessageService, settingsService);
    }

    @Provides
    @Singleton
    MessageManager provideMessageManager(HeartAttackLibs plugin, MiniMessageService miniMessageService, PlayerSettingsService settingsService, MessageFramework messageFramework) {
        return new MessageManager(plugin, miniMessageService, settingsService, messageFramework);
    }

    @Provides
    @Singleton
    DependencyManager provideDependencyManager(
            HeartAttackLibs plugin,
            ConfigManager configManager,
            DebugManager debugManager,
            HologramManager hologramManager
    ) {
        return new DependencyManager(plugin, configManager, debugManager, hologramManager);
    }

    @Provides
    @Singleton
    DependencyFramework provideDependencyFramework(DependencyManager manager) {
        return new DependencyFrameworkImpl(manager);
    }

    @Provides
    @Singleton
    PathMessageService providePathMessageService(DependencyManager dependencyManager) {
        return new PathMessageService(dependencyManager);
    }

    @Provides
    @Singleton
    TextFramework provideTextFramework(MiniMessageService miniMessageService) {
        return new TextFrameworkImpl(miniMessageService);
    }

    @Provides
    @Singleton
    UtilityFramework provideUtilityFramework() {
        return new UtilityFrameworkImpl();
    }

    @Provides
    @Singleton
    PlayerSettingsRepository providePlayerSettingsRepository(HeartAttackLibs plugin) {
        return new PlayerSettingsRepository(plugin);
    }

    @Provides
    @Singleton
    SettingsFileManager provideSettingsFileManager(HeartAttackLibs plugin) {
        return new SettingsFileManager(plugin);
    }

    @Provides
    @Singleton
    PlayerSettingsService providePlayerSettingsService(HeartAttackLibs plugin, ConfigManager configManager,
            PlayerSettingsRepository repository, SettingsFileManager settingsFileManager) {
        PlayerSettingsService service = new PlayerSettingsService(plugin, repository, settingsFileManager);
        service.reloadFromConfig(configManager.config());
        return service;
    }

    @Provides
    @Singleton
    ExternalMessageFilter provideExternalMessageFilter(PlayerSettingsService settingsService) {
        return new ExternalMessageFilter(settingsService);
    }

    @Provides
    @Singleton
    ModuleContext provideModuleContext(
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
        return new ModuleContext(
                plugin,
                commandFramework,
                commandManager,
                databaseManager,
                guiFramework,
                configManager,
                miniMessageService,
                debugManager,
                hologramManager,
                messageManager,
                dependencyManager
        );
    }

    @Provides
    @Singleton
    ModuleManager provideModuleManager(HeartAttackLibs plugin, ModuleContext context) {
        return new ModuleManager(plugin, context);
    }

    @Provides
    @Singleton
    ModuleFramework provideModuleFramework(ModuleManager manager) {
        return new ModuleFrameworkImpl(manager);
    }

    @Provides
    @Singleton
    HeartAttackLibsApi provideHeartAttackLibsApi(HeartAttackLibs plugin) {
        return new HeartAttackLibsApiImpl(plugin);
    }
}



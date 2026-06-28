package org.heartattack.heartattacklibs.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.heartattack.heartattacklibs.config.ConfigManager;
import org.heartattack.heartattacklibs.dependency.DependencyManager;
import org.heartattack.heartattacklibs.debug.DebugManager;
import org.heartattack.heartattacklibs.message.MessageFramework;
import org.heartattack.heartattacklibs.message.MessageManager;
import org.heartattack.heartattacklibs.settings.ExternalMessageFilter;
import org.heartattack.heartattacklibs.settings.PlayerSettingsService;
import org.heartattack.heartattacklibs.settings.SettingsFileManager;
import org.heartattack.heartattacklibs.text.MiniMessageService;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;

@Singleton
public final class FrameworkReloadBootstrap {
    private final ConfigManager configManager;
    private final MiniMessageService miniMessageService;
    private final DebugManager debugManager;
    private final MessageManager messageManager;
    private final MessageFramework messageFramework;
    private final DependencyManager dependencyManager;
    private final FrameworkGuiBootstrap guiBootstrap;
    private final FrameworkModuleBootstrap moduleBootstrap;
    private final PlayerSettingsService playerSettingsService;
    private final SettingsFileManager settingsFileManager;
    private final ExternalMessageFilter externalMessageFilter;

    @Inject
    public FrameworkReloadBootstrap(
            ConfigManager configManager,
            MiniMessageService miniMessageService,
            DebugManager debugManager,
            MessageManager messageManager,
            MessageFramework messageFramework,
            DependencyManager dependencyManager,
            FrameworkGuiBootstrap guiBootstrap,
            FrameworkModuleBootstrap moduleBootstrap,
            PlayerSettingsService playerSettingsService,
            SettingsFileManager settingsFileManager,
            ExternalMessageFilter externalMessageFilter
    ) {
        this.configManager = configManager;
        this.miniMessageService = miniMessageService;
        this.debugManager = debugManager;
        this.messageManager = messageManager;
        this.messageFramework = messageFramework;
        this.dependencyManager = dependencyManager;
        this.guiBootstrap = guiBootstrap;
        this.moduleBootstrap = moduleBootstrap;
        this.playerSettingsService = playerSettingsService;
        this.settingsFileManager = settingsFileManager;
        this.externalMessageFilter = externalMessageFilter;
    }

    public void reloadFramework() {
        configManager.reloadAll();
        UnicodeSmallCaps.setEnabled(configManager.config().getBoolean("text.small-caps.enabled", true));
        UnicodeSmallCaps.setAllowedPlugins(configManager.config().getStringList("text.small-caps.allowed-plugins"));
        playerSettingsService.initializeStorage();
        playerSettingsService.reloadFromConfig(configManager.config());
        externalMessageFilter.reload(settingsFileManager.loadFilterRules());
        miniMessageService.setPrefix(configManager.getString("messages.prefix"));
        debugManager.loadFromConfig(configManager.config());
        messageManager.reload();
        messageFramework.reloadAll();
        playerSettingsService.writeNewDiscoveries();
        guiBootstrap.reload();
        dependencyManager.initialize();
        moduleBootstrap.reloadModules();
    }
}

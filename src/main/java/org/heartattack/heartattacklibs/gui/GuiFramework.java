package org.heartattack.heartattacklibs.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface GuiFramework {
    void registerMenus(Plugin plugin, String folderPath);

    void reloadPluginMenus(Plugin plugin);

    void open(Player player, String menuId, GuiOpenContext context);

    void bindAction(Plugin plugin, String actionType, GuiActionHandler handler);

    void bindRequirement(Plugin plugin, String requirementType, GuiRequirementChecker checker);

    void bindPlaceholder(Plugin plugin, String key, GuiPlaceholderResolver resolver);

    void bindRenderer(Plugin plugin, String rendererId, GuiRenderer renderer);

    /**
     * Starts building a code-defined, interactive ("live") menu — buttons with callbacks, editable
     * input regions, and lifecycle hooks. Tasks/callbacks are scheduled under {@code owner}.
     *
     * @param owner the plugin that owns this menu (for task scheduling)
     * @param title menu title (MiniMessage or legacy &amp; codes)
     * @param rows  inventory rows, 1-6
     */
    LiveMenu.Builder liveMenu(Plugin owner, String title, int rows);
}


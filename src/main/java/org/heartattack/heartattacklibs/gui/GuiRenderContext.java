package org.heartattack.heartattacklibs.gui;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.gui.model.DynamicSectionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;

import java.util.Map;

public record GuiRenderContext(
        Player player,
        MenuDefinition menu,
        DynamicSectionDefinition section,
        Map<String, String> placeholders,
        GuiSession session
) {
}


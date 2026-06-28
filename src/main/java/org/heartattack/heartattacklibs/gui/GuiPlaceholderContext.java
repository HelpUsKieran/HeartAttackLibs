package org.heartattack.heartattacklibs.gui;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;

import java.util.Map;

public record GuiPlaceholderContext(
        Player player,
        MenuDefinition menu,
        String key,
        Map<String, String> placeholders,
        GuiSession session
) {
}


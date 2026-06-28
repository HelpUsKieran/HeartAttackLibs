package org.heartattack.heartattacklibs.gui;

import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;
import org.heartattack.heartattacklibs.gui.model.RequirementDefinition;

import java.util.Map;

public record GuiRequirementContext(
        Player player,
        MenuDefinition menu,
        RequirementDefinition requirement,
        Map<String, String> placeholders,
        GuiSession session
) {
}


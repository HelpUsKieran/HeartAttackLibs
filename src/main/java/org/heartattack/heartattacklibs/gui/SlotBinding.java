package org.heartattack.heartattacklibs.gui;

import org.heartattack.heartattacklibs.gui.model.ActionDefinition;
import org.heartattack.heartattacklibs.gui.model.DynamicSectionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuItemDefinition;
import org.heartattack.heartattacklibs.gui.model.RequirementDefinition;
import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Map;

public record SlotBinding(
        MenuItemDefinition item,
        DynamicSectionDefinition dynamic,
        List<RequirementDefinition> clickRequirements,
        Map<ClickType, List<ActionDefinition>> actions,
        Map<String, String> placeholders
) {
}


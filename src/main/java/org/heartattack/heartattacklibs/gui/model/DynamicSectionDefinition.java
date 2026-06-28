package org.heartattack.heartattacklibs.gui.model;

import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Map;

public record DynamicSectionDefinition(
        String key,
        String rendererId,
        List<Integer> slots,
        PaginationDefinition pagination,
        List<RequirementDefinition> viewRequirements,
        List<RequirementDefinition> clickRequirements,
        Map<ClickType, List<ActionDefinition>> clickActions
) {
}


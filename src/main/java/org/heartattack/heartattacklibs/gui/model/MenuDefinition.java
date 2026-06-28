package org.heartattack.heartattacklibs.gui.model;

import java.util.List;
import java.util.Map;

public record MenuDefinition(
        String id,
        String title,
        int size,
        boolean cancelClicks,
        boolean cancelDrag,
        boolean allowPlayerInventoryClick,
        Map<String, String> placeholders,
        List<RequirementDefinition> openRequirements,
        List<MenuItemDefinition> items,
        List<DynamicSectionDefinition> dynamicSections
) {
}


package org.heartattack.heartattacklibs.gui.model;

import org.bukkit.event.inventory.ClickType;

import java.util.List;
import java.util.Map;

public record MenuItemDefinition(
        String key,
        List<Integer> slots,
        String material,
        int amount,
        String name,
        List<String> lore,
        boolean glow,
        List<String> flags,
        Integer customModelData,
        TextureDefinition texture,
        List<RequirementDefinition> viewRequirements,
        List<RequirementDefinition> clickRequirements,
        Map<ClickType, List<ActionDefinition>> clickActions
) {
}


package org.heartattack.heartattacklibs.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.gui.model.ActionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

public record GuiActionContext(
        Plugin owner,
        MenuDefinition menu,
        Player player,
        Inventory inventory,
        int slot,
        ClickType clickType,
        ActionDefinition action,
        Map<String, String> placeholders,
        GuiSession session
) {
    public Map<String, String> mutablePlaceholders() {
        return new LinkedHashMap<>(placeholders);
    }
}


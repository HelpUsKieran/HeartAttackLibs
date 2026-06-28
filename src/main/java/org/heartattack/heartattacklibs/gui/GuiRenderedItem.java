package org.heartattack.heartattacklibs.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record GuiRenderedItem(ItemStack item, Map<String, String> placeholders) {
}


package org.heartattack.heartattacklibs.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class GuiListener implements Listener {
    private final GuiFrameworkImpl framework;

    public GuiListener(GuiFrameworkImpl framework) {
        this.framework = framework;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        framework.onClick(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        framework.onDrag(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        framework.onClose(event);
    }
}


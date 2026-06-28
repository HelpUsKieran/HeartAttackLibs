package org.heartattack.heartattacklibs.gui;

import org.bukkit.plugin.Plugin;
import org.heartattack.heartattacklibs.gui.model.DynamicSectionDefinition;
import org.heartattack.heartattacklibs.gui.model.MenuDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuiSession {
    private final UUID id;
    private final Plugin owner;
    private final MenuDefinition menu;
    private final Map<String, String> placeholders;
    private final Map<String, Integer> dynamicPages = new HashMap<>();

    public GuiSession(UUID id, Plugin owner, MenuDefinition menu, Map<String, String> placeholders) {
        this.id = id;
        this.owner = owner;
        this.menu = menu;
        this.placeholders = new HashMap<>(placeholders);
    }

    public UUID id() {
        return id;
    }

    public Plugin owner() {
        return owner;
    }

    public MenuDefinition menu() {
        return menu;
    }

    public Map<String, String> placeholders() {
        return new HashMap<>(placeholders);
    }

    public int pageOf(DynamicSectionDefinition section) {
        return dynamicPages.getOrDefault(section.key(), 1);
    }

    public void setPage(DynamicSectionDefinition section, int page) {
        dynamicPages.put(section.key(), Math.max(1, page));
    }
}


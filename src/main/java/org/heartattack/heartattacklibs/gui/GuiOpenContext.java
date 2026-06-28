package org.heartattack.heartattacklibs.gui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GuiOpenContext {
    public static final GuiOpenContext EMPTY = new GuiOpenContext(Collections.emptyMap());

    private final Map<String, String> placeholders;

    private GuiOpenContext(Map<String, String> placeholders) {
        this.placeholders = Map.copyOf(placeholders);
    }

    public static GuiOpenContext of(Map<String, String> placeholders) {
        return new GuiOpenContext(placeholders == null ? Collections.emptyMap() : placeholders);
    }

    public Map<String, String> placeholders() {
        return new LinkedHashMap<>(placeholders);
    }
}


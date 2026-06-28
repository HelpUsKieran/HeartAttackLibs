package org.heartattack.heartattacklibs.gui.model;

import java.util.List;

public record PageButtonDefinition(String material, String name, List<String> lore, Integer customModelData) {

    public static PageButtonDefinition prevDefault() {
        return new PageButtonDefinition("ARROW", "<!italic><yellow>◄ Previous Page", List.of(), null);
    }

    public static PageButtonDefinition nextDefault() {
        return new PageButtonDefinition("ARROW", "<!italic><yellow>Next Page ►", List.of(), null);
    }
}

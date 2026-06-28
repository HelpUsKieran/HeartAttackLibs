package org.heartattack.heartattacklibs.gui.model;

public record PaginationDefinition(
        boolean enabled,
        int pageSize,
        int nextSlot,
        int previousSlot,
        PageButtonDefinition prevButton,
        PageButtonDefinition nextButton
) {
}


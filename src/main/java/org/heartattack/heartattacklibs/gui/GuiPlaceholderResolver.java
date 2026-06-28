package org.heartattack.heartattacklibs.gui;

@FunctionalInterface
public interface GuiPlaceholderResolver {
    String resolve(GuiPlaceholderContext context);
}


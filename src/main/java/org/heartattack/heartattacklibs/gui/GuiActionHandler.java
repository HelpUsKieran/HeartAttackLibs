package org.heartattack.heartattacklibs.gui;

@FunctionalInterface
public interface GuiActionHandler {
    void execute(GuiActionContext context);
}


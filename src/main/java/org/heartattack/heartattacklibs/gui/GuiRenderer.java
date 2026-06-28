package org.heartattack.heartattacklibs.gui;

import java.util.List;

@FunctionalInterface
public interface GuiRenderer {
    List<GuiRenderedItem> render(GuiRenderContext context);
}


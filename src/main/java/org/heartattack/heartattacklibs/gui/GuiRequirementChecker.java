package org.heartattack.heartattacklibs.gui;

@FunctionalInterface
public interface GuiRequirementChecker {
    boolean test(GuiRequirementContext context);
}


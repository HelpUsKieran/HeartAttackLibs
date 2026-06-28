package org.heartattack.heartattacklibs.gui.model;

import java.util.Map;

public record RequirementDefinition(String type, Map<String, String> params, String denyMessage) {
}


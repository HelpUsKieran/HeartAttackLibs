package org.heartattack.heartattacklibs.gui.model;

import java.util.Map;

public record ActionDefinition(String type, String value, Map<String, String> params) {
}


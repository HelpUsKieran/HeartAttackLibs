package org.heartattack.heartattacklibs.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlaceholderMap {
    private final Map<String, String> values = new LinkedHashMap<>();

    public static PlaceholderMap create() {
        return new PlaceholderMap();
    }

    public PlaceholderMap with(String key, Object value) {
        values.put(key, String.valueOf(value));
        return this;
    }

    public String apply(String input) {
        String text = input == null ? "" : input;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }

    public Map<String, String> values() {
        return new LinkedHashMap<>(values);
    }
}

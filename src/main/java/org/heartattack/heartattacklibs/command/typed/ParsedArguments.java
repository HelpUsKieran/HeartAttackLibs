package org.heartattack.heartattacklibs.command.typed;

import java.util.HashMap;
import java.util.Map;

public final class ParsedArguments {
    private final Map<String, Object> values = new HashMap<>();

    <T> void put(String key, T value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) values.get(key);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }
}

package org.heartattack.heartattacklibs.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;

public final class UnicodeSmallCaps {
    private static final Map<Character, String> SMALL_CAPS_MAP = new HashMap<>();
    private static volatile boolean enabled = true;
    private static volatile Set<String> allowedPlugins = Set.of("*");

    static {
        SMALL_CAPS_MAP.put('a', "\u1d00");
        SMALL_CAPS_MAP.put('b', "\u0299");
        SMALL_CAPS_MAP.put('c', "\u1d04");
        SMALL_CAPS_MAP.put('d', "\u1d05");
        SMALL_CAPS_MAP.put('e', "\u1d07");
        SMALL_CAPS_MAP.put('f', "\ua730");
        SMALL_CAPS_MAP.put('g', "\u0262");
        SMALL_CAPS_MAP.put('h', "\u029c");
        SMALL_CAPS_MAP.put('i', "\u026a");
        SMALL_CAPS_MAP.put('j', "\u1d0a");
        SMALL_CAPS_MAP.put('k', "\u1d0b");
        SMALL_CAPS_MAP.put('l', "\u029f");
        SMALL_CAPS_MAP.put('m', "\u1d0d");
        SMALL_CAPS_MAP.put('n', "\u0274");
        SMALL_CAPS_MAP.put('o', "\u1d0f");
        SMALL_CAPS_MAP.put('p', "\u1d18");
        SMALL_CAPS_MAP.put('q', "\u01eb");
        SMALL_CAPS_MAP.put('r', "\u0280");
        SMALL_CAPS_MAP.put('s', "\ua731");
        SMALL_CAPS_MAP.put('t', "\u1d1b");
        SMALL_CAPS_MAP.put('u', "\u1d1c");
        SMALL_CAPS_MAP.put('v', "\u1d20");
        SMALL_CAPS_MAP.put('w', "\u1d21");
        SMALL_CAPS_MAP.put('x', "x");
        SMALL_CAPS_MAP.put('y', "\u028f");
        SMALL_CAPS_MAP.put('z', "\u1d22");
    }

    private UnicodeSmallCaps() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setAllowedPlugins(Iterable<String> plugins) {
        if (plugins == null) {
            allowedPlugins = Set.of("*");
            return;
        }
        Set<String> normalized = new HashSet<>();
        for (String plugin : plugins) {
            if (plugin == null) {
                continue;
            }
            String value = plugin.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty()) {
                normalized.add(value);
            }
        }
        allowedPlugins = normalized.isEmpty() ? Set.of("*") : Set.copyOf(normalized);
    }

    public static String apply(String input) {
        if (!enabled || input == null || input.isEmpty() || !isAllowedForCaller()) {
            return input == null ? "" : input;
        }

        StringBuilder output = new StringBuilder(input.length());
        boolean insideMiniTag = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '\u00a7' && i + 1 < input.length()) {
                output.append(current).append(input.charAt(i + 1));
                i++;
                continue;
            }

            if (current == '<') {
                insideMiniTag = true;
                output.append(current);
                continue;
            }
            if (current == '>') {
                insideMiniTag = false;
                output.append(current);
                continue;
            }

            if (insideMiniTag) {
                output.append(current);
                continue;
            }

            if (!Character.isLetter(current)) {
                output.append(current);
                continue;
            }

            String mapped = SMALL_CAPS_MAP.get(Character.toLowerCase(current));
            output.append(mapped == null ? current : mapped);
        }
        return output.toString();
    }

    private static boolean isAllowedForCaller() {
        Set<String> allowlist = allowedPlugins;
        if (allowlist.contains("*")) {
            return true;
        }

        String pluginName = resolveCallingPluginName();
        if (pluginName == null || pluginName.isEmpty()) {
            return false;
        }
        return allowlist.contains(pluginName);
    }

    private static String resolveCallingPluginName() {
        String fallback = null;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement frame : stack) {
            String className = frame.getClassName();
            if (className == null
                    || className.startsWith("java.")
                    || className.startsWith("jdk.")
                    || className.startsWith("sun.")) {
                continue;
            }
            try {
                Class<?> type = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                JavaPlugin plugin = JavaPlugin.getProvidingPlugin(type);
                if (plugin == null) {
                    continue;
                }
                String pluginName = plugin.getName().toLowerCase(Locale.ROOT);
                if (fallback == null) {
                    fallback = pluginName;
                }
                if (!"HeartAttackLibs".equals(pluginName)) {
                    return pluginName;
                }
            } catch (Throwable ignored) {
            }
        }
        return fallback;
    }
}

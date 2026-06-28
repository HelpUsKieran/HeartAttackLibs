package org.heartattack.heartattacklibs.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.regex.Pattern;

public final class ColorUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // Matches legacy & color/formatting codes (case-insensitive).
    private static final Pattern LEGACY_CODE = Pattern.compile("(?i)&([0-9a-fk-or])");

    private ColorUtils() {
    }

    /**
     * Converts a string that may contain legacy {@code &X} color codes and/or
     * MiniMessage tags into an Adventure Component.
     *
     * <p>Legacy {@code &X} codes are first converted to their MiniMessage
     * equivalents (e.g. {@code &7} → {@code <gray>}, {@code &b} → {@code <aqua>}).
     * The result is then parsed by MiniMessage.  This allows strings to use
     * either format — or both at once.</p>
     *
     * <p>Previously the method translated {@code &} → {@code §} and then passed
     * the result to MiniMessage, which throws {@code ParsingException} when it
     * encounters the section-sign character.  That caused item names to render as
     * raw {@code §b…} literal text.</p>
     */
    public static Component color(String text) {
        if (text == null) {
            return Component.empty();
        }
        text = LEGACY_CODE.matcher(text).replaceAll(mr -> switch (mr.group(1).toLowerCase()) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obf>";
            case "l" -> "<b>";
            case "m" -> "<st>";
            case "n" -> "<u>";
            case "o" -> "<i>";
            case "r" -> "<reset>";
            default -> mr.group(0);
        });
        try {
            return MINI_MESSAGE.deserialize(text);
        } catch (Exception ignored) {
            return Component.text(text);
        }
    }

    public static Component color(String text, Map<String, String> placeholders) {
        if (text == null) {
            return Component.empty();
        }
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return color(text);
    }

    public static String colorForLegacy(String text) {
        if (text == null) {
            return "";
        }
        text = text.replaceAll("<.*?>", "");
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

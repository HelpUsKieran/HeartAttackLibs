package org.heartattack.heartattacklibs.settings;

import java.util.Locale;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;

public record FilterRule(String type, String value) {

    public boolean matches(String text) {
        if (text == null || value == null) return false;
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "contains"    -> text.contains(value);
            case "startswith"  -> text.startsWith(value);
            case "endswith"    -> text.endsWith(value);
            case "exact"       -> text.equals(value);
            case "regex"       -> {
                try {
                    yield Pattern.compile(value).matcher(text).find();
                } catch (PatternSyntaxException e) {
                    yield false;
                }
            }
            default -> false;
        };
    }
}

package org.heartattack.heartattacklibs.format;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtils {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    private TimeUtils() {
    }

    public static long parseDurationMillis(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Time input is empty");
        }

        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase(Locale.ROOT));
        long totalMillis = 0L;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            switch (unit) {
                case 's' -> totalMillis += value * 1000L;
                case 'm' -> totalMillis += value * 60_000L;
                case 'h' -> totalMillis += value * 3_600_000L;
                case 'd' -> totalMillis += value * 86_400_000L;
                default -> throw new IllegalArgumentException("Invalid time unit");
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Invalid time format");
        }

        return totalMillis;
    }

    public static String formatDuration(long millis) {
        if (millis <= 0L) {
            return "0s";
        }

        long seconds = millis / 1000L;
        long days = seconds / 86_400L;
        seconds %= 86_400L;
        long hours = seconds / 3_600L;
        seconds %= 3_600L;
        long minutes = seconds / 60L;
        seconds %= 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0L) {
            sb.append(days).append("d ");
        }
        if (hours > 0L) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0L) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0L) {
            sb.append(seconds).append("s");
        }
        return sb.toString().trim();
    }
}

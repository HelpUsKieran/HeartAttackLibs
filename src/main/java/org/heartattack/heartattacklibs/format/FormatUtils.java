package org.heartattack.heartattacklibs.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public final class FormatUtils {
    private FormatUtils() {
    }

    public static String formatDurationCompact(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString().trim();
    }

    public static String formatDurationWords(Duration duration) {
        long totalSeconds = Math.max(0L, duration.getSeconds());
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + " day" + (days == 1 ? "" : "s"));
        }
        if (hours > 0) {
            parts.add(hours + " hour" + (hours == 1 ? "" : "s"));
        }
        if (minutes > 0) {
            parts.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
        }
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(seconds + " second" + (seconds == 1 ? "" : "s"));
        }
        return String.join(", ", parts);
    }

    public static String formatMoney(double amount) {
        return formatMoney(amount, Locale.US, Currency.getInstance("USD"));
    }

    public static String formatMoney(double amount, Locale locale, Currency currency) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        currencyFormat.setCurrency(currency);
        currencyFormat.setMaximumFractionDigits(2);
        currencyFormat.setMinimumFractionDigits(2);
        return currencyFormat.format(amount);
    }

    public static String formatMoneyPlain(double amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(amount);
    }

    public static String abbreviateNumber(double value) {
        double abs = Math.abs(value);
        if (abs < 1000) {
            return formatMoneyPlain(value);
        }

        String[] suffixes = {"K", "M", "B", "T", "Q"};
        int index = -1;
        while (abs >= 1000 && index < suffixes.length - 1) {
            abs /= 1000d;
            index++;
        }

        BigDecimal rounded = BigDecimal.valueOf(abs).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        String sign = value < 0 ? "-" : "";
        return sign + rounded.toPlainString() + suffixes[index];
    }
}

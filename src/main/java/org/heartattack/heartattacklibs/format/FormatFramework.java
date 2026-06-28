package org.heartattack.heartattacklibs.format;

import java.time.Duration;
import java.util.Currency;
import java.util.Locale;

public interface FormatFramework {
    String durationCompact(long totalSeconds);

    String durationWords(Duration duration);

    String money(double amount);

    String money(double amount, Locale locale, Currency currency);

    String moneyPlain(double amount);

    String abbreviate(double value);

    long parseDurationMillis(String input);

    String formatDurationMillis(long millis);
}

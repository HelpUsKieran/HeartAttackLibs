package org.heartattack.heartattacklibs.format;

import java.time.Duration;
import java.util.Currency;
import java.util.Locale;

public final class FormatFrameworkImpl implements FormatFramework {
    @Override
    public String durationCompact(long totalSeconds) {
        return FormatUtils.formatDurationCompact(totalSeconds);
    }

    @Override
    public String durationWords(Duration duration) {
        return FormatUtils.formatDurationWords(duration);
    }

    @Override
    public String money(double amount) {
        return FormatUtils.formatMoney(amount);
    }

    @Override
    public String money(double amount, Locale locale, Currency currency) {
        return FormatUtils.formatMoney(amount, locale, currency);
    }

    @Override
    public String moneyPlain(double amount) {
        return FormatUtils.formatMoneyPlain(amount);
    }

    @Override
    public String abbreviate(double value) {
        return FormatUtils.abbreviateNumber(value);
    }

    @Override
    public long parseDurationMillis(String input) {
        return TimeUtils.parseDurationMillis(input);
    }

    @Override
    public String formatDurationMillis(long millis) {
        return TimeUtils.formatDuration(millis);
    }
}

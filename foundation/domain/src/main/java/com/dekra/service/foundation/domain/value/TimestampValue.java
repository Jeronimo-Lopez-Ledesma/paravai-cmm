package com.paravai.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Data
@NoArgsConstructor
public class TimestampValue {
    private Instant instant;

    // --- Constructor ---
    public TimestampValue(Instant instant) {
        this.instant = instant;
    }

    // --- Factory Methods ---
    public static TimestampValue now() {
        return new TimestampValue(Instant.now());
    }

    public static TimestampValue of(Instant instant) {
        return new TimestampValue(instant);
    }

    public static TimestampValue of(LocalDate date) {
        return new TimestampValue(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    public static TimestampValue of(LocalDateTime dateTime) {
        return new TimestampValue(dateTime.atZone(ZoneOffset.UTC).toInstant());
    }

    public static TimestampValue of(String isoDate) {
        Instant instant = Instant.parse(isoDate);
        return new TimestampValue(instant);
    }

    public static TimestampValue orDefault(TimestampValue value, TimestampValue fallback) {
        return value != null ? value : fallback;
    }

    // --- Behavior Methods ---

    public boolean isAfterNow() {
        return this.instant.isAfter(Instant.now());
    }

    public boolean isBeforeNow() {
        return this.instant.isBefore(Instant.now());
    }

    public String formatLocalizedDate(Locale locale, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(zoneId);
        return formatter.format(instant);
    }

    public boolean isBefore(TimestampValue other) {
        Objects.requireNonNull(other, "other");
        return this.instant.isBefore(other.instant);
    }

    public boolean isAfter(TimestampValue other) {
        Objects.requireNonNull(other, "other");
        return this.instant.isAfter(other.instant);
    }

    public boolean isEqual(TimestampValue other) {
        Objects.requireNonNull(other, "other");
        return this.instant.equals(other.instant);
    }

    // --- Formatting methods ---

    public String formatLocalizedDateTime(Locale locale, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(zoneId);
        return formatter.format(instant);
    }

    public String formatLocalizedTime(Locale locale, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedTime(java.time.format.FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(zoneId);
        return formatter.format(instant);
    }

    public DateValue toDateValue(ZoneId zoneId) {
        LocalDate date = LocalDateTime.ofInstant(this.instant, zoneId).toLocalDate();
        return DateValue.of(date);
    }

    public String toDate() {
        return DateTimeFormatter.ISO_DATE.format(LocalDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }

    public String toDateTime() {
        return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }

    public Instant toInstant() {
        return this.instant;
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public String toTime() {
        return DateTimeFormatter.ISO_TIME.format(LocalDateTime.ofInstant(instant, ZoneId.of("UTC")));
    }

    @Override
    public String toString() {
        return instant != null ? instant.toString() : "null";
    }

    @JsonValue
    public String getValue() {
        return this.toString();
    }
}
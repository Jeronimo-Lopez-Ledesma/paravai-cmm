package com.paravai.foundation.domain.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Getter
@EqualsAndHashCode
public class DateValue {

    private final LocalDate date;

    private DateValue(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        this.date = date;
    }

    public static DateValue of(LocalDate date) {
        return new DateValue(date);
    }

    public static DateValue of(String isoDate) {
        return new DateValue(LocalDate.parse(isoDate));
    }

    public String formatLocalized(Locale locale) {
        return date.format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(locale));
    }

    public String toIsoString() {
        return date.toString(); // ISO-8601
    }

    @Override
    public String toString() {
        return toIsoString();
    }
}

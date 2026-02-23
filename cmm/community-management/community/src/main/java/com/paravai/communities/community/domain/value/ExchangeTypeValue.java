package com.paravai.communities.community.domain.value;


import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ExchangeTypeValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, ExchangeTypeValue> CATALOG = Map.ofEntries(
            Map.entry("LEND",   new ExchangeTypeValue("LEND", "Lend")),
            Map.entry("BORROW", new ExchangeTypeValue("BORROW", "Borrow")),
            Map.entry("GIVE",   new ExchangeTypeValue("GIVE", "Give")),
            Map.entry("TRADE",  new ExchangeTypeValue("TRADE", "Trade"))
    );

    private ExchangeTypeValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExchangeTypeValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("ExchangeType code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase(Locale.ROOT);
        ExchangeTypeValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown exchange type code: " + code);
        }
        return v;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static List<Map<String, String>> catalog() {
        return CATALOG.values().stream()
                .map(v -> Map.of("code", v.getCode(), "label", v.getLabel()))
                .toList();
    }

    public static List<ExchangeTypeValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.exchangeType." + code, locale);
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof ExchangeTypeValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
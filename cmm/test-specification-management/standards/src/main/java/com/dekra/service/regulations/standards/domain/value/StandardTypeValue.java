package com.dekra.service.regulations.standards.domain.value;

import com.dekra.service.foundation.localization.LocalizableValueObject;
import com.dekra.service.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StandardTypeValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, StandardTypeValue> CATALOG = Map.ofEntries(

            Map.entry("TECHNICAL_STANDARD", new StandardTypeValue("TECHNICAL_STANDARD","Technical Standard")),
            Map.entry("TECHNICAL_SPECIFICATION",new StandardTypeValue("TECHNICAL_SPECIFICATION","Technical Specification")),
            Map.entry("TEST_SPECIFICATION",new StandardTypeValue("TEST_SPECIFICATION","Test Specification")),
            Map.entry("TECHNICAL_REPORT",new StandardTypeValue("TECHNICAL_REPORT","Technical Report")),
            Map.entry("AMENDMENT",new StandardTypeValue("AMENDMENT","Amendment")),
            Map.entry("REGULATION",new StandardTypeValue("REGULATION","Regulation")),
            Map.entry("CERTIFICATION_SCHEME_RULE",new StandardTypeValue("CERTIFICATION_SCHEME_RULE","Certification Scheme Rule"))
    );
    private StandardTypeValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardTypeValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("StandardType code cannot be null or blank");
        }

        final String key = code.trim().toUpperCase();
        StandardTypeValue value = CATALOG.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Unknown standard type code: " + code);
        }

        return value;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static List<StandardTypeValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof StandardTypeValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("standards.standardType." + code, locale);
    }
}
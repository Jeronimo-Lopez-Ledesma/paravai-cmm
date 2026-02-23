package com.dekra.service.regulations.standards.domain.value;

import com.dekra.service.foundation.localization.LocalizableValueObject;
import com.dekra.service.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StandardVersionStatusValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, StandardVersionStatusValue> CATALOG = Map.ofEntries(

            Map.entry("DRAFT",
                    new StandardVersionStatusValue("DRAFT", "Draft")),

            Map.entry("PUBLISHED",
                    new StandardVersionStatusValue("PUBLISHED", "Published (Approved)")),

            Map.entry("SUPERSEDED",
                    new StandardVersionStatusValue("SUPERSEDED", "Superseded")),

            Map.entry("WITHDRAWN",
                    new StandardVersionStatusValue("WITHDRAWN", "Withdrawn"))
    );

    private StandardVersionStatusValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardVersionStatusValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("StandardVersionStatus code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase();
        StandardVersionStatusValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown standard version status code: " + code);
        }
        return v;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static List<Map<String, String>> catalog() {
        return CATALOG.values().stream()
                .map(v -> Map.of(
                        "code", v.getCode(),
                        "label", v.getLabel()
                ))
                .toList();
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof StandardVersionStatusValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }


    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("standards.standardStatus." + code, locale);
    }

    public static List<StandardVersionStatusValue> values() {
        return List.copyOf(CATALOG.values());
    }

}


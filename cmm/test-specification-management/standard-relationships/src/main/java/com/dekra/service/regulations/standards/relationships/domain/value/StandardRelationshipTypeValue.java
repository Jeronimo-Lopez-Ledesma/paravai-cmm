package com.dekra.service.regulations.standards.relationships.domain.value;

import com.dekra.service.foundation.localization.LocalizableValueObject;
import com.dekra.service.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StandardRelationshipTypeValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, StandardRelationshipTypeValue> CATALOG = Map.ofEntries(
            Map.entry("REPLACES",      new StandardRelationshipTypeValue("REPLACES", "Replaces")),
            Map.entry("REFERS_TO",     new StandardRelationshipTypeValue("REFERS_TO", "Refers To")),
            Map.entry("AMENDS",        new StandardRelationshipTypeValue("AMENDS", "Amends")),
            Map.entry("DERIVED_FROM",  new StandardRelationshipTypeValue("DERIVED_FROM", "Derived From")),
            Map.entry("EQUIVALENT_TO", new StandardRelationshipTypeValue("EQUIVALENT_TO", "Equivalent To"))
    );

    private StandardRelationshipTypeValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardRelationshipTypeValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("StandardRelationshipType code cannot be null or blank");
        }
        String key = code.trim().toUpperCase();

        StandardRelationshipTypeValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown relationship type code: " + code);
        }
        return v;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static List<StandardRelationshipTypeValue> values() {
        return List.copyOf(CATALOG.values());
    }

    public boolean isRefersTo() { return "REFERS_TO".equals(code); }
    public boolean isEquivalentTo() { return "EQUIVALENT_TO".equals(code); }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof StandardRelationshipTypeValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("standards.relationshipType." + code, locale);
    }
}

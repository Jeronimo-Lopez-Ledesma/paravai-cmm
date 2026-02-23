package com.paravai.regulations.standards.relationships.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StandardRelationshipPurposeValue implements LocalizableValueObject  {

    private final String code;
    private final String label;

    private static final Map<String, StandardRelationshipPurposeValue> CATALOG = Map.ofEntries(
            Map.entry("NORMATIVE",   new StandardRelationshipPurposeValue("NORMATIVE", "Normative")),
            Map.entry("INFORMATIVE", new StandardRelationshipPurposeValue("INFORMATIVE", "Informative"))
    );

    private StandardRelationshipPurposeValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StandardRelationshipPurposeValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("StandardRelationshipPurpose code cannot be null or blank");
        }
        String key = code.trim().toUpperCase();

        StandardRelationshipPurposeValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown relationship purpose code: " + code);
        }
        return v;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static List<StandardRelationshipPurposeValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof StandardRelationshipPurposeValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("standards.relationshipPurpose." + code, locale);
    }
}

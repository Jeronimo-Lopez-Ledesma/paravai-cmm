package com.paravai.communities.community.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CommunityStatusValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, CommunityStatusValue> CATALOG = Map.ofEntries(
            Map.entry("ACTIVE",   new CommunityStatusValue("ACTIVE", "Active")),
            Map.entry("ARCHIVED", new CommunityStatusValue("ARCHIVED", "Archived"))
    );

    private CommunityStatusValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CommunityStatusValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("CommunityStatus code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase(Locale.ROOT);
        CommunityStatusValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown community status code: " + code);
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

    public static List<CommunityStatusValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.status." + code, locale);
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof CommunityStatusValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
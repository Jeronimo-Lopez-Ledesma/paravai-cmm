package com.paravai.communities.community.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CommunityVisibilityValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, CommunityVisibilityValue> CATALOG = Map.ofEntries(
            Map.entry("PUBLIC",  new CommunityVisibilityValue("PUBLIC", "Public")),
            Map.entry("PRIVATE", new CommunityVisibilityValue("PRIVATE", "Private"))
    );

    private CommunityVisibilityValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CommunityVisibilityValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("CommunityVisibility code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase(Locale.ROOT);
        CommunityVisibilityValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown community visibility code: " + code);
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

    public static List<CommunityVisibilityValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.visibility." + code, locale);
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof CommunityVisibilityValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
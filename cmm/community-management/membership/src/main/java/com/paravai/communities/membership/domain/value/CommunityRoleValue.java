package com.paravai.communities.membership.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CommunityRoleValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    private static final Map<String, CommunityRoleValue> CATALOG = Map.ofEntries(
            Map.entry("OWNER", new CommunityRoleValue("OWNER", "Owner")),
            Map.entry("ADMIN", new CommunityRoleValue("ADMIN", "Admin")),
            Map.entry("MEMBER", new CommunityRoleValue("MEMBER", "Member")),
            Map.entry("GUEST", new CommunityRoleValue("GUEST", "Guest"))
    );

    private CommunityRoleValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CommunityRoleValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("CommunityRole code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase(Locale.ROOT);
        CommunityRoleValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown community role code: " + code);
        }
        return v;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public boolean isOwner() { return "OWNER".equals(code); }
    public boolean isAdmin() { return "ADMIN".equals(code); }

    public static List<Map<String, String>> catalog() {
        return CATALOG.values().stream()
                .map(v -> Map.of("code", v.getCode(), "label", v.getLabel()))
                .toList();
    }

    public static List<CommunityRoleValue> values() {
        return List.copyOf(CATALOG.values());
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.role." + code, locale);
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof CommunityRoleValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
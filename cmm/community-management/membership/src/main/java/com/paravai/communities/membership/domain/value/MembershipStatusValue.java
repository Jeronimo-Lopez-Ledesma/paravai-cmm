package com.paravai.communities.membership.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class MembershipStatusValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    // ---- Static constants for type-safe usage (instead of enum) ----
    public static final MembershipStatusValue ACTIVE   = new MembershipStatusValue("ACTIVE", "Active");
    public static final MembershipStatusValue PENDING  = new MembershipStatusValue("PENDING", "Pending Invitation");
    public static final MembershipStatusValue REVOKED  = new MembershipStatusValue("REVOKED", "Revoked");
    public static final MembershipStatusValue INACTIVE = new MembershipStatusValue("INACTIVE", "Inactive");

    private static final Map<String, MembershipStatusValue> CATALOG = Map.ofEntries(
            Map.entry("ACTIVE",   ACTIVE),
            Map.entry("PENDING",  PENDING),
            Map.entry("REVOKED",  REVOKED),
            Map.entry("INACTIVE", INACTIVE)
    );

    private MembershipStatusValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MembershipStatusValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("MembershipStatus code cannot be null or blank");
        }
        final String key = code.trim().toUpperCase(Locale.ROOT);
        MembershipStatusValue v = CATALOG.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Unknown membership status code: " + code);
        }
        return v;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public boolean isActive() { return this == ACTIVE; }
    public boolean isPending() { return this == PENDING; }

    public static List<Map<String, String>> catalog() {
        return CATALOG.values().stream()
                .distinct()
                .map(v -> Map.of("code", v.getCode(), "label", v.getLabel()))
                .toList();
    }

    public static List<MembershipStatusValue> values() {
        return List.of(ACTIVE, PENDING, REVOKED, INACTIVE);
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.membershipStatus." + code, locale);
    }

    @Override
    public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof MembershipStatusValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
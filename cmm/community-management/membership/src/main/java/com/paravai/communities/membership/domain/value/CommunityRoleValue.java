package com.paravai.communities.membership.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object: CommunityRoleValue
 *
 * Represents the role of an ACTIVE membership inside a community.
 *
 * MVP supported roles:
 * - ADMIN
 * - MEMBER
 *
 * Notes:
 * - Role only applies when Membership.status == ACTIVE
 * - Users with no membership relation are represented by Membership state NONE
 *   at query level, not by a role such as GUEST
 */
public final class CommunityRoleValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    // ---- Static constants (catalog-style VO instead of enum) ----

    public static final CommunityRoleValue ADMIN  = new CommunityRoleValue("ADMIN", "Admin");
    public static final CommunityRoleValue MEMBER = new CommunityRoleValue("MEMBER", "Member");

    private static final Map<String, CommunityRoleValue> CATALOG = Map.ofEntries(
            Map.entry("ADMIN", ADMIN),
            Map.entry("MEMBER", MEMBER)
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
        CommunityRoleValue value = CATALOG.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Unknown community role code: " + code);
        }

        return value;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    // -------------------------------------------------
    // Domain semantics helpers
    // -------------------------------------------------

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    /**
     * Returns true if this role can manage membership requests and roles.
     *
     * MVP:
     * - only ADMIN has management permissions
     */
    public boolean canManageMembership() {
        return this == ADMIN;
    }

    // -------------------------------------------------
    // Catalog exposure
    // -------------------------------------------------

    public static List<Map<String, String>> catalog() {
        return values().stream()
                .map(v -> Map.of(
                        "code", v.getCode(),
                        "label", v.getLabel()
                ))
                .toList();
    }

    public static List<CommunityRoleValue> values() {
        return List.of(ADMIN, MEMBER);
    }

    // -------------------------------------------------
    // Localization
    // -------------------------------------------------

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("membership.communityRole." + code, locale);
    }

    // -------------------------------------------------
    // Object methods
    // -------------------------------------------------

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                (o instanceof CommunityRoleValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
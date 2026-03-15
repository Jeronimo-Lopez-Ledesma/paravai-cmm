package com.paravai.communities.membership.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object: MembershipStatusValue
 *
 * Represents the lifecycle state of a membership relationship.
 *
 * MVP lifecycle:
 *
 * PENDING  -> request created but not yet decided
 * ACTIVE   -> user is an active member of the community
 * REJECTED -> request was rejected by an administrator
 *
 * State transitions (MVP):
 *
 * NONE    -> PENDING
 * PENDING -> ACTIVE
 * PENDING -> REJECTED
 *
 * REJECTED is considered a terminal state in MVP.
 */
public final class MembershipStatusValue implements LocalizableValueObject {

    private final String code;
    private final String label;

    // ---- Static constants (catalog-style VO instead of enum) ----

    public static final MembershipStatusValue PENDING  = new MembershipStatusValue("PENDING", "Pending");
    public static final MembershipStatusValue ACTIVE   = new MembershipStatusValue("ACTIVE", "Active");
    public static final MembershipStatusValue REJECTED = new MembershipStatusValue("REJECTED", "Rejected");

    private static final Map<String, MembershipStatusValue> CATALOG = Map.ofEntries(
            Map.entry("PENDING",  PENDING),
            Map.entry("ACTIVE",   ACTIVE),
            Map.entry("REJECTED", REJECTED)
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

        MembershipStatusValue value = CATALOG.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Unknown membership status code: " + code);
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

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * Returns true if this status represents a final state
     * in the MVP membership lifecycle.
     */
    public boolean isTerminal() {
        return this == REJECTED;
    }

    // -------------------------------------------------
    // Catalog exposure
    // -------------------------------------------------

    public static List<Map<String, String>> catalog() {
        return CATALOG.values().stream()
                .distinct()
                .map(v -> Map.of(
                        "code", v.getCode(),
                        "label", v.getLabel()
                ))
                .toList();
    }

    public static List<MembershipStatusValue> values() {
        return List.of(PENDING, ACTIVE, REJECTED);
    }

    // -------------------------------------------------
    // Localization
    // -------------------------------------------------

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("community.membershipStatus." + code, locale);
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
                (o instanceof MembershipStatusValue other && code.equals(other.code));
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
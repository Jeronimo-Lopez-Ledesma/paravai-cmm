package com.paravai.communities.community.domain.value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object: CommunityRulesValue
 *
 * Represents the rules configuration of a Community.
 *
 * Conventions:
 * - Immutable
 * - No identity
 * - Equality by structure
 * - Optional informational text (String)
 * - At least one allowedExchangeType required
 */
public final class CommunityRulesValue {

    private final String text; // optional, informational
    private final Set<ExchangeTypeValue> allowedExchangeTypes;

    private CommunityRulesValue(String text,
                                Set<ExchangeTypeValue> allowedExchangeTypes) {

        this.text = normalizeOptional(text);

        Objects.requireNonNull(allowedExchangeTypes, "allowedExchangeTypes is required");

        if (allowedExchangeTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "CommunityRulesValue must contain at least one allowedExchangeType"
            );
        }

        this.allowedExchangeTypes =
                Collections.unmodifiableSet(new HashSet<>(allowedExchangeTypes));
    }

    // -------------------------------------------------
    // Factory
    // -------------------------------------------------

    public static CommunityRulesValue of(String text,
                                         Set<ExchangeTypeValue> allowedExchangeTypes) {
        return new CommunityRulesValue(text, allowedExchangeTypes);
    }

    // -------------------------------------------------
    // Getters
    // -------------------------------------------------

    public String getText() {
        return text;
    }

    public Set<ExchangeTypeValue> getAllowedExchangeTypes() {
        return allowedExchangeTypes;
    }

    // -------------------------------------------------
    // Equality (structural)
    // -------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityRulesValue that)) return false;
        return Objects.equals(text, that.text) &&
                Objects.equals(allowedExchangeTypes, that.allowedExchangeTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, allowedExchangeTypes);
    }

    @Override
    public String toString() {
        return "CommunityRulesValue{text=%s, allowedExchangeTypes=%s}"
                .formatted(text, allowedExchangeTypes);
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private static String normalizeOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
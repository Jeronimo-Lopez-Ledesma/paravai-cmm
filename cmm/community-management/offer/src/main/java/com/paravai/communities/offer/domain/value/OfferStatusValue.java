package com.paravai.communities.offer.domain.value;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object: OfferStatusValue
 *
 * Catalog-like value object representing the lifecycle status
 * of an Offer.
 *
 * MVP supported values:
 * - ACTIVE
 * - PAUSED
 * - WITHDRAWN
 *
 * Invariants:
 * - must not be null
 * - must belong to the supported catalog
 */
public final class OfferStatusValue implements Serializable {

    public static final String ACTIVE_CODE = "ACTIVE";
    public static final String PAUSED_CODE = "PAUSED";
    public static final String WITHDRAWN_CODE = "WITHDRAWN";

    private static final Set<String> SUPPORTED_VALUES = Set.of(
            ACTIVE_CODE,
            PAUSED_CODE,
            WITHDRAWN_CODE
    );

    public static final OfferStatusValue ACTIVE = new OfferStatusValue(ACTIVE_CODE);
    public static final OfferStatusValue PAUSED = new OfferStatusValue(PAUSED_CODE);
    public static final OfferStatusValue WITHDRAWN = new OfferStatusValue(WITHDRAWN_CODE);

    private final String value;

    private OfferStatusValue(String value) {
        this.value = value;
    }

    public static OfferStatusValue of(String value) {
        Objects.requireNonNull(value, "Offer status is required");

        String normalized = value.trim().toUpperCase();

        if (!SUPPORTED_VALUES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported offer status: " + value);
        }

        return switch (normalized) {
            case ACTIVE_CODE -> ACTIVE;
            case PAUSED_CODE -> PAUSED;
            case WITHDRAWN_CODE -> WITHDRAWN;
            default -> throw new IllegalArgumentException("Unsupported offer status: " + value);
        };
    }

    public String value() {
        return value;
    }

    public boolean isActive() {
        return ACTIVE_CODE.equals(value);
    }

    public boolean isPaused() {
        return PAUSED_CODE.equals(value);
    }

    public boolean isWithdrawn() {
        return WITHDRAWN_CODE.equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferStatusValue that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
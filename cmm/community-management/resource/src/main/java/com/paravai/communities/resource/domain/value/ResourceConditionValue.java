package com.paravai.communities.resource.domain.value;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object: ResourceConditionValue
 *
 * Catalog-like value object representing the declared condition
 * of a resource.
 *
 * MVP supported values:
 * - NEW
 * - LIKE_NEW
 * - GOOD
 * - FAIR
 * - POOR
 *
 * Invariants:
 * - must not be null
 * - must belong to the supported catalog
 */
public final class ResourceConditionValue implements Serializable {

    public static final String NEW_CODE = "NEW";
    public static final String LIKE_NEW_CODE = "LIKE_NEW";
    public static final String GOOD_CODE = "GOOD";
    public static final String FAIR_CODE = "FAIR";
    public static final String POOR_CODE = "POOR";

    private static final Set<String> SUPPORTED_VALUES = Set.of(
            NEW_CODE,
            LIKE_NEW_CODE,
            GOOD_CODE,
            FAIR_CODE,
            POOR_CODE
    );

    public static final ResourceConditionValue NEW = new ResourceConditionValue(NEW_CODE);
    public static final ResourceConditionValue LIKE_NEW = new ResourceConditionValue(LIKE_NEW_CODE);
    public static final ResourceConditionValue GOOD = new ResourceConditionValue(GOOD_CODE);
    public static final ResourceConditionValue FAIR = new ResourceConditionValue(FAIR_CODE);
    public static final ResourceConditionValue POOR = new ResourceConditionValue(POOR_CODE);

    private final String value;

    private ResourceConditionValue(String value) {
        this.value = value;
    }

    public static ResourceConditionValue of(String value) {
        Objects.requireNonNull(value, "Resource condition is required");

        String normalized = value.trim().toUpperCase();

        if (!SUPPORTED_VALUES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported resource condition: " + value);
        }

        return switch (normalized) {
            case NEW_CODE -> NEW;
            case LIKE_NEW_CODE -> LIKE_NEW;
            case GOOD_CODE -> GOOD;
            case FAIR_CODE -> FAIR;
            case POOR_CODE -> POOR;
            default -> throw new IllegalArgumentException("Unsupported resource condition: " + value);
        };
    }

    public String value() {
        return value;
    }

    public boolean isNew() {
        return NEW_CODE.equals(value);
    }

    public boolean isLikeNew() {
        return LIKE_NEW_CODE.equals(value);
    }

    public boolean isGood() {
        return GOOD_CODE.equals(value);
    }

    public boolean isFair() {
        return FAIR_CODE.equals(value);
    }

    public boolean isPoor() {
        return POOR_CODE.equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceConditionValue that)) return false;
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
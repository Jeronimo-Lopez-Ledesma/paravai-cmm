package com.paravai.communities.resource.domain.value;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object: ResourceTitleValue
 *
 * Represents the business title of a resource.
 *
 * Invariants:
 * - must not be null
 * - must not be blank
 * - is normalized by trimming surrounding whitespace
 * - must not exceed MAX_LENGTH
 */
public final class ResourceTitleValue implements Serializable {

    private static final int MAX_LENGTH = 150;

    private final String value;

    private ResourceTitleValue(String value) {
        this.value = value;
    }

    public static ResourceTitleValue of(String value) {
        Objects.requireNonNull(value, "Resource title is required");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Resource title cannot be blank");
        }

        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Resource title cannot exceed " + MAX_LENGTH + " characters"
            );
        }

        return new ResourceTitleValue(normalized);
    }

    public String value() {
        return value;
    }

    public int length() {
        return value.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceTitleValue that)) return false;
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
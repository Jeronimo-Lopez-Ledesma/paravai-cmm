package com.dekra.service.foundation.domaincore.value;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object representing an opaque unique identifier.
 * Accepts any non-blank string (UUID, NanoID, legacy IDs, etc.)
 * and generates new IDs using NanoID.
 */
@Schema(hidden = true)
public final class IdValue implements Serializable {

    private static final String UNASSIGNED = "UNASSIGNED";

    private final String value;

    private IdValue(String value) {
        this.value = Objects.requireNonNull(value, "IdValue cannot be null");
    }

    /**
     * Generates a new identifier using NanoID.
     */
    public static IdValue generate() {
        return new IdValue(NanoIdUtils.randomNanoId());
    }

    /**
     * Wraps an existing ID (UUID, NanoID, or any legacy string).
     * Only checks non-null and non-blank.
     */
    public static IdValue of(String existingId) {
        if (existingId == null || existingId.isBlank()) {
            throw new IllegalArgumentException("Existing ID must not be null or blank");
        }
        return new IdValue(existingId);
    }

    /**
     * Creates a special unassigned marker.
     */
    public static IdValue notAssigned() {
        return new IdValue(UNASSIGNED);
    }

    /**
     * Whether this ID is assigned or the special UNASSIGNED value.
     */
    public boolean isAssigned() {
        return !UNASSIGNED.equals(this.value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdValue)) return false;
        IdValue idValue = (IdValue) o;
        return value.equals(idValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Optional tolerant parser used in some ingestion paths.
     * Accepts any non-blank string without validation.
     */
    public static IdValue parseId(String value) {
        if (value == null || value.isBlank()) {
            return IdValue.notAssigned();
        }
        return new IdValue(value);
    }
}

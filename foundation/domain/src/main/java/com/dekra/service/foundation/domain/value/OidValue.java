package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Value Object for managing unique OIDs.
 */
public final class OidValue {

    private static final String UNKNOWN = "UNKNOWN";
    private final String value;

    // Constructor privado para garantizar el control de la creación
    private OidValue(String value) {
        this.value = Objects.requireNonNull(value, "OidValue cannot be null");
    }


    // Método de fábrica para crear un OidValue desde un valor existente
    public static OidValue of(String oid) {
        if (oid == null || oid.isBlank()) {
            throw new IllegalArgumentException("oid must not be null or blank");
        }
        return new OidValue(oid);
    }

    public static OidValue unknown() {
        return new OidValue(UNKNOWN);
    }

    public boolean isKnown() {
        return !UNKNOWN.equals(this.value);
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
        if (o == null || getClass() != o.getClass()) return false;
        OidValue idValue = (OidValue) o;
        return value.equals(idValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

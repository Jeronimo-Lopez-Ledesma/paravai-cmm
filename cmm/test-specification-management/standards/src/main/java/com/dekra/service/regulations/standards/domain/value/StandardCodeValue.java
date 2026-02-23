package com.dekra.service.regulations.standards.domain.value;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Canonical identifier of a standard (e.g., "ETSI EN 300 328", "ISO/IEC 17025:2017").
 * - Stores the raw value as provided.
 * - Provides a normalized key for uniqueness and matching across imports.
 */
public final class StandardCodeValue {

    private final String value;

    private StandardCodeValue(String value) {
        this.value = value;
    }

    public static StandardCodeValue of(String value) {
        String v = normalizeInput(value);
        if (v.isBlank()) {
            throw new IllegalArgumentException("StandardCodeValue must not be blank");
        }
        if (v.length() > 256) {
            throw new IllegalArgumentException("StandardCodeValue must not exceed 256 characters");
        }
        return new StandardCodeValue(v);
    }

    public String value() {
        return value;
    }

    /**
     * Key used for deduplication and comparisons.
     * Keeps Unicode, but normalizes spacing/casing and compatibility forms.
     */
    public String normalizedKey() {
        String s = Normalizer.normalize(value, Normalizer.Form.NFKC);
        s = s.trim().replaceAll("\\s+", " ");
        return s.toLowerCase(Locale.ROOT);
    }

    private static String normalizeInput(String value) {
        if (value == null) return "";
        // Preserve original characters as much as possible, normalize compatibility forms
        String s = Normalizer.normalize(value, Normalizer.Form.NFKC);
        // Normalize whitespace
        return s.trim().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandardCodeValue other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

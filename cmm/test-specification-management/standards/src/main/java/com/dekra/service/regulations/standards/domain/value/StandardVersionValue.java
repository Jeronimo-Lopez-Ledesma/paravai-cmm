package com.dekra.service.regulations.standards.domain.value;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Standard version identifier as found in sources:
 * - "2022"
 * - "v2.2.2"
 * - "V2.2.2 (2019-07)"
 * - "ICS 97.130.20" (legacy misuse: still needs to be representable)
 *
 * v1: treat as opaque but provide normalization for uniqueness checks.
 */
public final class StandardVersionValue {

    private final String value;

    private StandardVersionValue(String value) {
        this.value = value;
    }

    public static StandardVersionValue of(String value) {
        String v = normalizeInput(value);
        if (v.isBlank()) {
            throw new IllegalArgumentException("StandardVersionValue must not be blank");
        }
        if (v.length() > 128) {
            throw new IllegalArgumentException("StandardVersionValue must not exceed 128 characters");
        }
        return new StandardVersionValue(v);
    }

    public String value() {
        return value;
    }

    /**
     * Canonical version key for uniqueness comparisons.
     *
     * Normalization rules (v1):
     * - NFKC normalize
     * - trim
     * - collapse whitespace
     * - lower-case
     *
     * We intentionally do NOT attempt to parse semver or strip parentheses;
     * that can be introduced later if Business confirms semantics.
     */
    public String normalizedKey() {
        String s = Normalizer.normalize(value, Normalizer.Form.NFKC);
        s = s.trim().replaceAll("\\s+", " ");
        return s.toLowerCase(Locale.ROOT);
    }

    private static String normalizeInput(String value) {
        if (value == null) return "";
        String s = Normalizer.normalize(value, Normalizer.Form.NFKC);
        return s.trim().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandardVersionValue other)) return false;
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

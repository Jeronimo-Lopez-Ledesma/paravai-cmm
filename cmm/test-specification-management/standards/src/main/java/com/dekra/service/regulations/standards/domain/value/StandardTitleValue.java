package com.paravai.regulations.standards.domain.value;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Human-readable title of a standard.
 * Keeps Unicode content intact (supports multilingual titles).
 */
public final class StandardTitleValue {

    private final String value;

    private StandardTitleValue(String value) {
        this.value = value;
    }

    public static StandardTitleValue of(String value) {
        String v = normalizeInput(value);
        if (v.isBlank()) {
            throw new IllegalArgumentException("StandardTitleValue must not be blank");
        }
        if (v.length() > 1024) {
            throw new IllegalArgumentException("StandardTitleValue must not exceed 1024 characters");
        }
        return new StandardTitleValue(v);
    }

    public String value() {
        return value;
    }

    /**
     * Useful for case-insensitive search, not for identity.
     */
    public String normalizedForSearch() {
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
        if (!(o instanceof StandardTitleValue other)) return false;
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

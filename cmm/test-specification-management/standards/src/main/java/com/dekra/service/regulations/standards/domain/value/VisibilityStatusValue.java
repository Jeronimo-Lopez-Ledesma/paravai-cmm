package com.paravai.regulations.standards.domain.value;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Catalog-style VO (not enum).
 * Represents whether a standard/version is publicly available.
 */
public final class VisibilityStatusValue {

    public static final VisibilityStatusValue PUBLIC = new VisibilityStatusValue("PUBLIC");
    public static final VisibilityStatusValue INTERNAL = new VisibilityStatusValue("INTERNAL");

    private static final java.util.Map<String, VisibilityStatusValue> VALUES_BY_KEY =
            java.util.stream.Stream.of(PUBLIC, INTERNAL)
                    .collect(java.util.stream.Collectors.toUnmodifiableMap(v -> v.key(), v -> v));

    private final String value;

    private VisibilityStatusValue(String value) {
        this.value = value;
    }

    /**
     * Accepts flexible inputs:
     * - "public", "PUBLIC", "1", "true" => PUBLIC
     * - "internal", "0", "false" => INTERNAL
     */
    public static VisibilityStatusValue of(String raw) {
        String k = normalizeKey(raw);

        // legacy-friendly mappings
        if ("1".equals(k) || "true".equals(k) || "public".equals(k)) {
            return PUBLIC;
        }
        if ("0".equals(k) || "false".equals(k) || "internal".equals(k) || "private".equals(k)) {
            return INTERNAL;
        }

        return Optional.ofNullable(VALUES_BY_KEY.get(k))
                .orElseThrow(() -> new IllegalArgumentException("Invalid VisibilityStatusValue: " + raw));
    }

    /**
     * Convenience when the legacy source uses a boolean column.
     */
    public static VisibilityStatusValue ofBoolean(boolean isPublic) {
        return isPublic ? PUBLIC : INTERNAL;
    }

    public String value() {
        return value;
    }

    public String key() {
        return normalizeKey(value);
    }

    private static String normalizeKey(String raw) {
        if (raw == null) return "";
        String s = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        s = s.trim().replaceAll("\\s+", " ");
        return s.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VisibilityStatusValue other)) return false;
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

package com.paravai.communities.offer.domain.value;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class OfferAvailabilityStatusValue {

    private static final Map<String, OfferAvailabilityStatusValue> CATALOG = Map.of(
            "AVAILABLE", new OfferAvailabilityStatusValue("AVAILABLE", "Available"),
            "UNAVAILABLE", new OfferAvailabilityStatusValue("UNAVAILABLE", "Unavailable")
    );

    private final String code;
    private final String label;

    private OfferAvailabilityStatusValue(String code, String label) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
    }

    public static OfferAvailabilityStatusValue of(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("availability status must not be null or blank");
        }
        OfferAvailabilityStatusValue value = CATALOG.get(code.trim().toUpperCase());
        if (value == null) {
            throw new IllegalArgumentException("unsupported availability status: " + code);
        }
        return value;
    }

    public static OfferAvailabilityStatusValue available() {
        return CATALOG.get("AVAILABLE");
    }

    public static OfferAvailabilityStatusValue unavailable() {
        return CATALOG.get("UNAVAILABLE");
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(code);
    }

    public boolean isUnavailable() {
        return "UNAVAILABLE".equals(code);
    }

    public static Set<String> supportedCodes() {
        return CATALOG.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferAvailabilityStatusValue that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
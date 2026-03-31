package com.paravai.communities.offer.domain.value;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object: ExchangeTypeValue
 *
 * Catalog-like value object representing the type of exchange
 * offered by an Offer.
 *
 * MVP supported values:
 * - LEND
 * - BORROW
 * - GIVE
 * - TRADE
 *
 * Invariants:
 * - must not be null
 * - must belong to the supported catalog
 */
public final class ExchangeTypeValue implements Serializable {

    public static final String LEND_CODE = "LEND";
    public static final String BORROW_CODE = "BORROW";
    public static final String GIVE_CODE = "GIVE";
    public static final String TRADE_CODE = "TRADE";

    private static final Set<String> SUPPORTED_VALUES = Set.of(
            LEND_CODE,
            BORROW_CODE,
            GIVE_CODE,
            TRADE_CODE
    );

    public static final ExchangeTypeValue LEND = new ExchangeTypeValue(LEND_CODE);
    public static final ExchangeTypeValue BORROW = new ExchangeTypeValue(BORROW_CODE);
    public static final ExchangeTypeValue GIVE = new ExchangeTypeValue(GIVE_CODE);
    public static final ExchangeTypeValue TRADE = new ExchangeTypeValue(TRADE_CODE);

    private final String value;

    private ExchangeTypeValue(String value) {
        this.value = value;
    }

    public static ExchangeTypeValue of(String value) {
        Objects.requireNonNull(value, "Exchange type is required");

        String normalized = value.trim().toUpperCase();

        if (!SUPPORTED_VALUES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported exchange type: " + value);
        }

        return switch (normalized) {
            case LEND_CODE -> LEND;
            case BORROW_CODE -> BORROW;
            case GIVE_CODE -> GIVE;
            case TRADE_CODE -> TRADE;
            default -> throw new IllegalArgumentException("Unsupported exchange type: " + value);
        };
    }

    public String value() {
        return value;
    }

    public boolean isLend() {
        return LEND_CODE.equals(value);
    }

    public boolean isBorrow() {
        return BORROW_CODE.equals(value);
    }

    public boolean isGive() {
        return GIVE_CODE.equals(value);
    }

    public boolean isTrade() {
        return TRADE_CODE.equals(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeTypeValue that)) return false;
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
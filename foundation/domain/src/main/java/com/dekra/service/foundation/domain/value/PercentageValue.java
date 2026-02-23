package com.dekra.service.foundation.domain.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PercentageValue {
    private final double value;

    private PercentageValue(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Percentage must be between 0.0 and 1.0");
        }
        this.value = value;
    }

    public static PercentageValue of(double value) {
        return new PercentageValue(value);
    }

    public double toPercentage() {
        return value * 100.0;
    }

    public double applyTo(double baseValue) {
        return baseValue * value;
    }

    @Override
    public String toString() {
        return toPercentage() + "%";
    }
}

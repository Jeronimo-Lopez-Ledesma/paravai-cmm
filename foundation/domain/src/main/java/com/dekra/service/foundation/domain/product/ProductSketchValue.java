package com.dekra.service.foundation.domain.product;

import java.util.Objects;

public final class ProductSketchValue {

    private final String description;

    private ProductSketchValue(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Product sketch description cannot be blank");
        }
        this.description = description.trim();
    }

    public static ProductSketchValue of(String description) {
        return new ProductSketchValue(description);
    }

    public String getValue() {
        return description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSketchValue that)) return false;
        return description.equalsIgnoreCase(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description.toLowerCase());
    }

    @Override
    public String toString() {
        return description;
    }
}

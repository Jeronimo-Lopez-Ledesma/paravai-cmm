package com.paravai.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public final class EntityTypeValue {

    public static final EntityTypeValue COMMUNITY = new EntityTypeValue("Community");
    public static final EntityTypeValue MEMBERSHIP = new EntityTypeValue("Membership");

    private final String value;

    private EntityTypeValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static EntityTypeValue of(String value) {
        return switch (value.trim().toLowerCase()) {
            case "Community" -> COMMUNITY;
            case "Membership" -> MEMBERSHIP;
            default -> throw new IllegalArgumentException("Unsupported entity type: " + value);
        };
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof EntityTypeValue other && value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

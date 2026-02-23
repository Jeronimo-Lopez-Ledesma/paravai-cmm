package com.paravai.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public final class ResourceTypeValue {

    public static final ResourceTypeValue COMMUNITIES = new ResourceTypeValue("communities");
    public static final ResourceTypeValue MEMBERSHIPS = new ResourceTypeValue("memberships");


    private final String value;

    private ResourceTypeValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ResourceTypeValue of(String value) {
        return switch (value.trim().toLowerCase()) {
            case "communities" -> COMMUNITIES;
            case "memberships" -> MEMBERSHIPS;

            default -> throw new IllegalArgumentException("Unsupported resource type: " + value);
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
        return (this == o) || (o instanceof ResourceTypeValue other && value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

package com.dekra.service.foundation.domaincore.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public final class AggregateChangeActionValue {

    public static final AggregateChangeActionValue ASSIGNED = new AggregateChangeActionValue("ASSIGNED");
    public static final AggregateChangeActionValue CLOSED = new AggregateChangeActionValue("CLOSED");
    public static final AggregateChangeActionValue UPDATED = new AggregateChangeActionValue("UPDATED");
    public static final AggregateChangeActionValue ADDED = new AggregateChangeActionValue("ADDED");
    public static final AggregateChangeActionValue REMOVED = new AggregateChangeActionValue("REMOVED");

    private final String value;

    private AggregateChangeActionValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AggregateChangeActionValue of(String value) {
        return switch (value.trim().toUpperCase()) {
            case "ASSIGNED" -> ASSIGNED;
            case "CLOSED" -> CLOSED;
            case "UPDATED" -> UPDATED;
            case "ADDED" -> ADDED;
            case "REMOVED" -> REMOVED;
            default -> throw new IllegalArgumentException("Unsupported change action: " + value);
        };
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

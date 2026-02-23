package com.paravai.foundation.domaincore.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public final class AggregateChangeScopeValue {

    public static final AggregateChangeScopeValue ROOT = new AggregateChangeScopeValue("ROOT");
    public static final AggregateChangeScopeValue CHILD_ENTITY = new AggregateChangeScopeValue("CHILD_ENTITY");
    public static final AggregateChangeScopeValue VALUE_COLLECTION = new AggregateChangeScopeValue("VALUE_COLLECTION");

    private final String value;

    private AggregateChangeScopeValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AggregateChangeScopeValue of(String value) {
        return switch (value.trim().toUpperCase()) {
            case "ROOT" -> ROOT;
            case "CHILD_ENTITY" -> CHILD_ENTITY;
            case "VALUE_COLLECTION" -> VALUE_COLLECTION;
            default -> throw new IllegalArgumentException("Unsupported change scope: " + value);
        };
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

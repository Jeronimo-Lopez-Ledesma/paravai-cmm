package com.dekra.service.foundation.domaincore.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@EqualsAndHashCode
@Getter
public final class AggregateChangeDescriptorValue {

    private final AggregateChangeScopeValue scope;
    private final String path;
    private final AggregateChangeActionValue action; // nullable ok
    private final String itemKey;                    // nullable ok

    @JsonCreator
    public AggregateChangeDescriptorValue(
            @JsonProperty("scope") AggregateChangeScopeValue scope,
            @JsonProperty("path") String path,
            @JsonProperty("action") AggregateChangeActionValue action,
            @JsonProperty("itemKey") String itemKey
    ) {
        this.scope = Objects.requireNonNull(scope, "scope");
        this.path = Objects.requireNonNull(path, "path");
        this.action = action;
        this.itemKey = itemKey;
    }

    public static AggregateChangeDescriptorValue valueCollection(String path, AggregateChangeActionValue action, String itemKey) {
        return new AggregateChangeDescriptorValue(AggregateChangeScopeValue.VALUE_COLLECTION, path, action, itemKey);
    }

    public static AggregateChangeDescriptorValue childEntity(String path, AggregateChangeActionValue action, String itemKey) {
        return new AggregateChangeDescriptorValue(AggregateChangeScopeValue.CHILD_ENTITY, path, action, itemKey);
    }

    public static AggregateChangeDescriptorValue root(String path, AggregateChangeActionValue action) {
        return new AggregateChangeDescriptorValue(AggregateChangeScopeValue.ROOT, path, action, null);
    }
}

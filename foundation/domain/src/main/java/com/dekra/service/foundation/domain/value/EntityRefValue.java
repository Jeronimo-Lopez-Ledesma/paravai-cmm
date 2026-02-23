package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Value Object: EntityRefValue
 * Represents a typed reference to another aggregate/entity in the system
 */
public final class EntityRefValue {

    private final EntityTypeValue type;
    private final IdValue id;

    @JsonCreator
    public static EntityRefValue of(
            @JsonProperty("type") String type,
            @JsonProperty("id") IdValue id) {
        return new EntityRefValue(EntityTypeValue.of(type), id);
    }

    public EntityRefValue(EntityTypeValue type, IdValue id) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public EntityTypeValue type() {
        return type;
    }

    public IdValue id() {
        return id;
    }

    @JsonValue
    public String asString() {
        return type.toString().toLowerCase() + ":" + id.toString();
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityRefValue other)) return false;
        return type.equals(other.type) && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}

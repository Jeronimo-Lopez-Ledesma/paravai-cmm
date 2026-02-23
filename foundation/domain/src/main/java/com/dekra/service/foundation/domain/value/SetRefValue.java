package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Objects;

public final class SetRefValue {

    private final String kind; // e.g. "subscription-set"
    private final String id;   // setId as string

    private SetRefValue(String kind, String id) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.id = Objects.requireNonNull(id, "id");
    }

    @JsonCreator
    public static SetRefValue of(String kind, String id) {
        return new SetRefValue(kind, id);
    }

    public String kind() { return kind; }
    public String id() { return id; }

    @JsonValue
    public Map<String, Object> toMap() {
        return Map.of("kind", kind, "id", id);
    }

    @Override public boolean equals(Object o) { return (this == o) || (o instanceof SetRefValue v && kind.equals(v.kind) && id.equals(v.id)); }
    @Override public int hashCode() { return Objects.hash(kind, id); }
    @Override public String toString() { return kind + ":" + id; }
}

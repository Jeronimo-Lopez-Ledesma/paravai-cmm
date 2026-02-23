package com.paravai.foundation.domaincore.value;

import com.paravai.foundation.domain.value.ResourceTypeValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** VO: referencia tipada a un RECURSO (target). */
public final class ResourceRefValue {

    private final ResourceTypeValue type;
    private final IdValue id;

    @JsonCreator
    public static ResourceRefValue of(@JsonProperty("type") String type,
                                      @JsonProperty("id") IdValue id) {
        return new ResourceRefValue(ResourceTypeValue.of(type), id);
    }

    public ResourceRefValue(ResourceTypeValue type, IdValue id) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
    }

    public ResourceTypeValue type() { return type; }
    public IdValue id() { return id; }

    @Override public String toString(){ return type + ":" + id; }
    @Override public boolean equals(Object o){ return (this==o) || (o instanceof ResourceRefValue v && type.equals(v.type) && id.equals(v.id)); }
    @Override public int hashCode(){ return Objects.hash(type, id); }
}
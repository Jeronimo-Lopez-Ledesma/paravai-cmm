package com.paravai.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** VO: referencia tipada a un SUJETO (actor). */
public final class SubjectRefValue {

    private final SubjectTypeValue type;
    private final IdValue id;

    @JsonCreator
    public static SubjectRefValue of(@JsonProperty("type") String type,
                                     @JsonProperty("id") IdValue id) {
        return new SubjectRefValue(SubjectTypeValue.of(type), id);
    }

    public SubjectRefValue(SubjectTypeValue type, IdValue id) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
    }

    public SubjectTypeValue type() { return type; }
    public IdValue id() { return id; }

    @Override public String toString(){ return type + ":" + id; }
    @Override public boolean equals(Object o){ return (this==o) || (o instanceof SubjectRefValue v && type.equals(v.type) && id.equals(v.id)); }
    @Override public int hashCode(){ return Objects.hash(type, id); }
}



package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** VO catálogo: tipos de SUJETO (actores del sistema). */
public final class SubjectTypeValue {

    private static final Map<String, SubjectTypeValue> CACHE = new ConcurrentHashMap<>();

    public static final SubjectTypeValue USER          = register(new SubjectTypeValue("User"));
    public static final SubjectTypeValue ORGANIZATION  = register(new SubjectTypeValue("Organization"));
    public static final SubjectTypeValue APPLICATION   = register(new SubjectTypeValue("Application"));
    public static final SubjectTypeValue PROJECT       = register(new SubjectTypeValue("Project"));
    // Añadir aquí cuando proceda: Team, Service Account, ..., etc.

    private final String value;

    private SubjectTypeValue(String value) { this.value = value; }

    private static SubjectTypeValue register(SubjectTypeValue v) {
        CACHE.put(v.value, v);
        return v;
    }

    @JsonCreator
    public static SubjectTypeValue of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("subject type is blank");
        }
        var key = raw.trim().toLowerCase();
        var v = CACHE.get(key);
        if (v != null) return v;

        var custom = new SubjectTypeValue(key);
        return register(custom);
    }

    @JsonValue
    public String getValue() { return value; }

    @Override public String toString() { return value; }
    @Override public boolean equals(Object o){ return (this==o) || (o instanceof SubjectTypeValue v && value.equals(v.value)); }
    @Override public int hashCode(){ return Objects.hash(value); }
}

package com.dekra.service.foundation.viewjsonapi.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SortSpecValue {

    private final List<SortFieldValue> fields;

    private SortSpecValue(List<SortFieldValue> fields) {
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
    }

    public static SortSpecValue empty() { return new SortSpecValue(List.of()); }

    public static SortSpecValue of(List<SortFieldValue> fields) {
        return (fields == null || fields.isEmpty()) ? empty() : new SortSpecValue(fields);
    }

    /** Formato soportado: "name,-createdAt" */
    public static SortSpecValue parse(String raw) {
        if (raw == null || raw.isBlank()) return empty();
        String[] tokens = raw.split(",");
        List<SortFieldValue> list = new ArrayList<>(tokens.length);
        for (String t : tokens) {
            String token = t.trim();
            if (token.isEmpty()) continue;
            boolean desc = token.startsWith("-");
            String field = desc ? token.substring(1) : token;
            if (!field.isBlank()) list.add(new SortFieldValue(field, desc));
        }
        return of(list);
    }

    public List<SortFieldValue> fields() { return fields; }

    public boolean isEmpty() { return fields.isEmpty(); }

    @Override public String toString() {
        if (fields.isEmpty()) return "";
        return fields.stream()
                .map(f -> f.descending() ? "-" + f.field() : f.field())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    /** Campo + dirección */
    public record SortFieldValue(String field, boolean descending) {
        public SortFieldValue {
            if (field == null || field.isBlank()) throw new IllegalArgumentException("field must not be blank");
        }
    }
}

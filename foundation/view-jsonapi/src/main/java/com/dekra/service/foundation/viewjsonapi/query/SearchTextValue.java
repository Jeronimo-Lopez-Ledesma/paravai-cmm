package com.paravai.foundation.viewjsonapi.query;

public record SearchTextValue(String value) {
    public static final SearchTextValue EMPTY = new SearchTextValue(null);

    public static SearchTextValue of(String raw) {
        return (raw == null || raw.isBlank()) ? EMPTY : new SearchTextValue(raw.trim());
    }

    public boolean isEmpty() { return value == null; }

    @Override public String toString() { return isEmpty() ? "" : value; }
}

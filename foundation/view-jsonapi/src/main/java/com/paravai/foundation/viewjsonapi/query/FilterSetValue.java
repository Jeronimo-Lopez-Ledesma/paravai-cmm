package com.paravai.foundation.viewjsonapi.query;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record FilterSetValue(Map<String, String> values) {

    public FilterSetValue {
        values = (values == null) ? Map.of() : Map.copyOf(values);
    }

    public static FilterSetValue empty() { return new FilterSetValue(Map.of()); }

    public static FilterSetValue of(Map<String,String> values) { return new FilterSetValue(values); }

    public Set<String> keys() { return values.keySet(); }

    public Optional<String> get(String key) { return Optional.ofNullable(values.get(key)); }

    public boolean isEmpty() { return values.isEmpty(); }

    public FilterSetValue whitelist(Set<String> allowedKeys) {
        if (allowedKeys == null || allowedKeys.isEmpty()) return this;
        Map<String,String> filtered = values.entrySet().stream()
                .filter(e -> allowedKeys.contains(e.getKey()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new FilterSetValue(filtered);
    }
}

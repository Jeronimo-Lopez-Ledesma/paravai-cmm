package com.dekra.service.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Schema(hidden = true)
public class NameValue {
    private final String value;

    private NameValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        this.value = value.trim();
    }

    public static NameValue of(String value) {
        return new NameValue(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonValue
    public String getValue() { return value; }
}

package com.dekra.service.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * Ensures validation and provides common operations like extracting the domain and username.
 */

@EqualsAndHashCode
public class EmailValue {
    private final String value;

    // Basic regex to validate email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailValue(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid email address: " + value);
        }
        this.value = value;
    }

    /**
     * Factory method to create a new EmailValue instance.
     * Example usage:
     * EmailValue email = EmailValue.of("user@example.com");
     */
    public static EmailValue of(String value) {
        return new EmailValue(value);
    }

    public static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public String getDomain() {
        return value.substring(value.indexOf("@") + 1);
    }

    public String getUsername() {
        return value.substring(0, value.indexOf("@"));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
    public String toString() {
        return value;
    }
}

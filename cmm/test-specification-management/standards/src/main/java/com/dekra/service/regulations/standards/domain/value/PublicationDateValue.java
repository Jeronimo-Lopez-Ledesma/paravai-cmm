package com.dekra.service.regulations.standards.domain.value;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Publication date of a Standard version.
  * Kept as date-only (LocalDate) to avoid meaning-less timestamps from legacy DB exports.
 */
public final class PublicationDateValue {

    private final LocalDate value;

    private PublicationDateValue(LocalDate value) {
        this.value = value;
    }

    public static PublicationDateValue of(LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("PublicationDateValue must not be null");
        }
        return new PublicationDateValue(value);
    }

    public LocalDate value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicationDateValue other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

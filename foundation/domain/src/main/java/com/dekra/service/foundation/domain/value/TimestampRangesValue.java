package com.dekra.service.foundation.domaincore.value;


import java.time.Instant;
import java.util.Objects;

public final class TimestampRangesValue {

    private TimestampRangesValue() {
        // Utility Value Object (stateless)
    }

    /**
     * Overlap semantics: [start, end)
     * - end == null → open-ended interval
     */
    public static boolean overlaps(
            TimestampValue start1, TimestampValue end1,
            TimestampValue start2, TimestampValue end2
    ) {
        Objects.requireNonNull(start1, "start1");
        Objects.requireNonNull(start2, "start2");

        Instant s1 = start1.getInstant();
        Instant e1 = end1 != null ? end1.getInstant() : Instant.MAX;

        Instant s2 = start2.getInstant();
        Instant e2 = end2 != null ? end2.getInstant() : Instant.MAX;

        return s1.isBefore(e2) && s2.isBefore(e1);
    }
}

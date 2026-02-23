package com.paravai.foundation.domain.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PercentageValueTest {

    @Test
    void testValidPercentage() {
        PercentageValue percentage = PercentageValue.of(0.25);
        assertNotNull(percentage);
        assertEquals(0.25, percentage.getValue());
    }

    @Test
    void testInvalidPercentageBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> PercentageValue.of(-0.1));
    }

    @Test
    void testInvalidPercentageAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> PercentageValue.of(1.1));
    }

    @Test
    void testToPercentage() {
        PercentageValue percentage = PercentageValue.of(0.25);
        assertEquals(25.0, percentage.toPercentage());
    }

    @Test
    void testApplyTo() {
        PercentageValue percentage = PercentageValue.of(0.25);
        assertEquals(25.0, percentage.applyTo(100.0));
    }

    @Test
    void testToString() {
        PercentageValue percentage = PercentageValue.of(0.25);
        assertEquals("25.0%", percentage.toString());
    }
}

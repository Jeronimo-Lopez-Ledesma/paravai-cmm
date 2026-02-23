package com.paravai.foundation.domain.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameValueTest {

    @Test
    void testValidName() {
        NameValue name = NameValue.of("John Doe");
        assertNotNull(name);
        assertEquals("John Doe", name.toString());
    }

    @Test
    void testNullName() {
        assertThrows(IllegalArgumentException.class, () -> NameValue.of(null));
    }

    @Test
    void testEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> NameValue.of(""));
    }
}

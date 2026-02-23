package com.dekra.service.foundation.domain.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValueTest {

    @Test
    void testValidEmailCreation() {
        // Given a valid email
        String validEmail = "user@example.com";

        // When creating an EmailValue
        EmailValue email = EmailValue.of(validEmail);

        // Then the email should be created successfully
        assertNotNull(email);
        assertEquals(validEmail, email.getValue());
    }

    @Test
    void testInvalidEmailCreation() {
        // Given an invalid email
        String invalidEmail = "invalid-email";

        // When creating an EmailValue
        // Then it should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> EmailValue.of(invalidEmail));
    }

    @Test
    void testNullEmailCreation() {
        // Given a null email
        String nullEmail = null;

        // When creating an EmailValue
        // Then it should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> EmailValue.of(nullEmail));
    }

    @Test
    void testIsValidWithValidEmail() {
        // Given a valid email
        String validEmail = "user@example.com";

        // When validating the email
        boolean isValid = EmailValue.isValid(validEmail);

        // Then it should return true
        assertTrue(isValid);
    }

    @Test
    void testIsValidWithInvalidEmail() {
        // Given an invalid email
        String invalidEmail = "invalid-email";

        // When validating the email
        boolean isValid = EmailValue.isValid(invalidEmail);

        // Then it should return false
        assertFalse(isValid);
    }

    @Test
    void testIsValidWithNullEmail() {
        // Given a null email
        String nullEmail = null;

        // When validating the email
        boolean isValid = EmailValue.isValid(nullEmail);

        // Then it should return false
        assertFalse(isValid);
    }

    @Test
    void testGetDomain() {
        // Given a valid email
        String emailValue = "user@example.com";
        EmailValue email = EmailValue.of(emailValue);

        // When extracting the domain
        String domain = email.getDomain();

        // Then it should return the correct domain
        assertEquals("example.com", domain);
    }

    @Test
    void testGetUsername() {
        // Given a valid email
        String emailValue = "user@example.com";
        EmailValue email = EmailValue.of(emailValue);

        // When extracting the username
        String username = email.getUsername();

        // Then it should return the correct username
        assertEquals("user", username);
    }

    @Test
    void testToString() {
        // Given a valid email
        String emailValue = "user@example.com";
        EmailValue email = EmailValue.of(emailValue);

        // When calling toString
        String result = email.toString();

        // Then it should return the email address as a string
        assertEquals(emailValue, result);
    }

    @Test
    void testEquality() {
        // Given two identical emails
        EmailValue email1 = EmailValue.of("user@example.com");
        EmailValue email2 = EmailValue.of("user@example.com");

        // When comparing equality
        // Then they should be equal
        assertEquals(email1, email2);
        assertTrue(email1.equals(email2));
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void testInequality() {
        // Given two different emails
        EmailValue email1 = EmailValue.of("user1@example.com");
        EmailValue email2 = EmailValue.of("user2@example.com");

        // When comparing equality
        // Then they should not be equal
        assertNotEquals(email1, email2);
        assertFalse(email1.equals(email2));
    }
}

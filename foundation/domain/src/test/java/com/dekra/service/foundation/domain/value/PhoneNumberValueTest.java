package com.paravai.foundation.domain.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberValueTest {

    // ----------------------------
    // STRICT (equivalente a of)
    // ----------------------------

    @Test
    void testValidPhoneNumberUS_Strict() {
        String number = "+12125552368";
        String regionCode = "US";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode); // strict

        assertNotNull(phoneNumber);
        assertTrue(phoneNumber.isE164());
        assertEquals("+12125552368", phoneNumber.getE164Format());
        assertEquals("(212) 555-2368", phoneNumber.getNationalOrRaw());
        assertEquals("+1 212-555-2368", phoneNumber.getInternationalOrRaw());
    }

    @Test
    void testValidPhoneNumberGB_Strict() {
        String number = "+447911123456";
        String regionCode = "GB";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        assertNotNull(phoneNumber);
        assertTrue(phoneNumber.isE164());
        assertEquals("+447911123456", phoneNumber.getE164Format());
        assertEquals("07911 123456", phoneNumber.getNationalOrRaw());
        assertEquals("+44 7911 123456", phoneNumber.getInternationalOrRaw());
    }

    @Test
    void testValidPhoneNumberES_Strict() {
        String number = "+34600123456";
        String regionCode = "ES";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        assertNotNull(phoneNumber);
        assertTrue(phoneNumber.isE164());
        assertEquals("+34600123456", phoneNumber.getE164Format());
        assertEquals("600 12 34 56", phoneNumber.getNationalOrRaw());
        assertEquals("+34 600 12 34 56", phoneNumber.getInternationalOrRaw());
    }

    @Test
    void testValidPhoneNumberNL_Strict() {
        String number = "+31612345678";
        String regionCode = "NL";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        assertNotNull(phoneNumber);
        assertTrue(phoneNumber.isE164());
        assertEquals("+31612345678", phoneNumber.getE164Format());
        assertEquals("06 12345678", phoneNumber.getNationalOrRaw());
        assertEquals("+31 6 12345678", phoneNumber.getInternationalOrRaw());
    }

    @ParameterizedTest
    @CsvSource({
            "12345, US",
            "071234, GB",
            "600123, ES",
            "06123, NL",
            "INVALID_NUMBER, US"
    })
    void testInvalidPhoneNumber_Strict(String number, String regionCode) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PhoneNumberValue.of(number, regionCode));
        assertTrue(ex.getMessage().toLowerCase().contains("e.164"));
    }

    // ----------------------------
    // LENIENT
    // ----------------------------

    @Test
    void testLenientAcceptsNonStandardButNormalizes() {
        String number = "+47 22222299";
        String regionCode = "NO";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        assertNotNull(phoneNumber);
        // Debería parsear y validar (es correcto), así que isE164 = true
        assertTrue(phoneNumber.isE164());
        assertEquals("+4722222299", phoneNumber.getE164OrRaw());
        // Nacional/Internacional disponibles al haber parseo válido
        assertNotNull(phoneNumber.getInternationalOrRaw());
        assertNotNull(phoneNumber.getNationalOrRaw());
    }

    @Test
    void testLenientKeepsRawWhenInvalid() {
        String number = "EXT-1001"; // formato propio/placeholder
        String regionCode = "US";

        PhoneNumberValue phoneNumber = PhoneNumberValue.ofLenient(number, regionCode);

        assertNotNull(phoneNumber);
        assertFalse(phoneNumber.isE164());
        // Al no ser E.164, getE164OrRaw() devuelve el raw normalizado básico
        assertEquals("EXT-1001".replaceAll("[()\\s.-]", ""), phoneNumber.getE164OrRaw());
        // toString enmascara
        assertTrue(phoneNumber.toString().startsWith("Phone("));
    }

    // ----------------------------
    // Igualdad / hashCode (por representación canónica)
    // ----------------------------

    @Test
    void testEqualityUsesCanonicalForm_E164OrNormalizedRaw() {
        String a = "+34 600 12 34 56";
        String b = "+34600123456";
        String region = "ES";

        PhoneNumberValue p1 = PhoneNumberValue.ofLenient(a, region);
        PhoneNumberValue p2 = PhoneNumberValue.of(b, region);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals("+34600123456", p1.getE164OrRaw());
        assertEquals("+34600123456", p2.getE164OrRaw());
    }

    @Test
    void testEqualityFallsBackToNormalizedRawIfNotParsable() {
        String a = "00-EXT-001";
        String b = "00EXT001";
        String region = "US";

        PhoneNumberValue p1 = PhoneNumberValue.ofLenient(a, region);
        PhoneNumberValue p2 = PhoneNumberValue.ofLenient(b, region);

        assertFalse(p1.isE164());
        assertFalse(p2.isE164());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ----------------------------
    // toString() y masked()
    // ----------------------------

    @Test
    void testToStringIsMasked() {
        String number = "+34600123456";
        String regionCode = "ES";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        String s = phoneNumber.toString();
        assertTrue(s.startsWith("Phone("));
        // no debería contener el número completo
        assertFalse(s.contains("+34600123456"));
    }

    @Test
    void testMaskedKeepsOnlyLastTwoDigits() {
        String number = "+12125552368";
        String regionCode = "US";

        PhoneNumberValue phoneNumber = PhoneNumberValue.of(number, regionCode);

        String masked = phoneNumber.masked();
        assertTrue(masked.startsWith("****"));
        assertTrue(masked.endsWith("68"));
    }

}

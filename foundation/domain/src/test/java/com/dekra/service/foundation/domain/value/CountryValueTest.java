package com.dekra.service.foundation.domain.value;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CountryValueTest {

    @Test
    void testValidCountry() {
        CountryValue country = CountryValue.of("ES");
        assertNotNull(country);
        assertEquals("ES", country.getIsoCode());
        assertEquals("España", country.getCountryName());
    }

    @Test
    void testInvalidCountry() {
        assertThrows(IllegalArgumentException.class, () -> CountryValue.of("XX"));
    }

    @Test
    void testCaseInsensitiveIsoCode() {
        CountryValue country = CountryValue.of("es");
        assertEquals("ES", country.getIsoCode());
        assertEquals("España", country.getCountryName());
    }

    @Test
    void testGetCountryName() {
        CountryValue country = CountryValue.of("US");
        assertEquals("Estados Unidos", country.getCountryName());
    }

    @Test
    void testIsValid() {
        assertTrue(CountryValue.isValid("GB"));
        assertFalse(CountryValue.isValid("XX"));
    }

    @Test
    void testGetAllCountries() {
        Map<String, String> countries = CountryValue.getAllCountries();
        assertTrue(countries.containsKey("US"));
        assertEquals("Estados Unidos", countries.get("US"));
    }

    @Test
    void testToString() {
        CountryValue country = CountryValue.of("ES");
        assertEquals("ES (España)", country.toString());
    }

    @Test
    void testGetIso3Code() {
        CountryValue country = CountryValue.of("FR");
        assertEquals("FRA", country.getIso3Code());
    }

    @Test
    void testGetDisplayNameWithLocale() {
        CountryValue country = CountryValue.of("DE");
        assertEquals("Germany", country.getDisplayName(Locale.ENGLISH));
        assertEquals("Alemania", country.getDisplayName(new Locale("es")));
    }

    @Test
    void testGetFlagEmoji() {
        CountryValue country = CountryValue.of("JP");
        assertEquals("\uD83C\uDDEF\uD83C\uDDF5", country.getFlagEmoji()); // 🇯🇵
    }
}

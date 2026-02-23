package com.dekra.service.foundation.domain.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Value object representing a Country, identified by its ISO-3166 Alpha-2 code and name.
 * Provides utility methods for retrieving country details including ISO-3, localized names, and emoji flags.
 */
@Getter
@EqualsAndHashCode
public class CountryValue {

    private final String isoCode;       // ISO-3166 Alpha-2 Code (e.g., "US", "ES")
    private final String countryName;   // Country name in default locale

    private static final Map<String, String> COUNTRY_MAP = new HashMap<>();

    static {
        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);
            COUNTRY_MAP.put(iso, locale.getDisplayCountry());
        }
    }

    private CountryValue(String isoCode, String countryName) {
        this.isoCode = isoCode.toUpperCase();
        this.countryName = countryName;
    }

    /**
     * Factory method to create a CountryValue instance.
     *
     * @param isoCode The ISO-3166 Alpha-2 code.
     * @return A valid CountryValue instance.
     */
    public static CountryValue of(String isoCode) {
        if (!isValid(isoCode)) {
            throw new IllegalArgumentException("Invalid ISO-3166 country code: " + isoCode);
        }
        String upperIso = isoCode.toUpperCase();
        return new CountryValue(upperIso, COUNTRY_MAP.get(upperIso));
    }

    /**
     * Checks whether the ISO Alpha-2 code is valid.
     */
    public static boolean isValid(String isoCode) {
        return isoCode != null && COUNTRY_MAP.containsKey(isoCode.toUpperCase());
    }

    /**
     * Returns a map of all ISO-3166 Alpha-2 codes and corresponding country names.
     */
    public static Map<String, String> getAllCountries() {
        return new HashMap<>(COUNTRY_MAP);
    }

    /**
     * Returns the ISO-3166 Alpha-3 code for this country.
     */
    public String getIso3Code() {
        return new Locale("", this.isoCode).getISO3Country();
    }

    /**
     * Returns the country name in the given locale.
     */
    public String getDisplayName(Locale locale) {
        return new Locale("", this.isoCode).getDisplayCountry(locale);
    }

    /**
     * Returns the flag emoji for this country.
     */
    public String getFlagEmoji() {
        int firstChar = Character.codePointAt(isoCode, 0) - 0x41 + 0x1F1E6;
        int secondChar = Character.codePointAt(isoCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    @Override
    public String toString() {
        return isoCode + " (" + countryName + ")";
    }
}

package com.paravai.foundation.domain.value;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * PhoneNumberValue
 * - Soporta dos políticas de validación: STRICT (lanza si no es válido) y LENIENT (acepta cualquier entrada no vacía).
 * - Conserva el valor original (raw), ofrece normalización básica y formateos de libphonenumber cuando es posible.
 * - Igualdad y hash se basan en E.164 si se pudo parsear/validar; si no, el raw normalizado.
 *
 * Recomendación de uso:
 *   - API/Application (inputs "libres"): usar lenient(...) y validar aparte si hace falta.
 *   - Flujos que exijan consistencia E.164: usar strict(...).
 */
public class PhoneNumberValue {

    public enum ValidationPolicy { STRICT_E164, LENIENT }

    private static final PhoneNumberUtil PN = PhoneNumberUtil.getInstance();

    // Normalización básica para el "raw" cuando no hay parseo
    private static final Pattern BASIC_STRIP = Pattern.compile("[()\\s.-]");

    private final String raw;                // tal cual llega (no se toca)
    private final String normalizedRaw;      // limpieza básica (para igualdad si no hay E.164)
    private final PhoneNumber parsed;        // null en modo lenient si no parsea/valida
    private final ValidationPolicy policy;

    private PhoneNumberValue(String raw, String normalizedRaw, PhoneNumber parsed, ValidationPolicy policy) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Phone number is required");
        if (policy == ValidationPolicy.STRICT_E164 && parsed == null) {
            throw new IllegalArgumentException("Phone must be a valid E.164 number in STRICT mode");
        }
        this.raw = raw;
        this.normalizedRaw = normalizedRaw;
        this.parsed = parsed;
        this.policy = policy;
    }

    /** Factory "libre": acepta y normaliza; si puede, mantiene también el parseo válido. */
    public static PhoneNumberValue ofLenient(String number, String regionCode) {
        String norm = normalizeBasic(number);
        PhoneNumber parsed = tryParseValid(norm, regionCode);
        return new PhoneNumberValue(number, norm, parsed, ValidationPolicy.LENIENT);
    }

    /** Factory privado estricto. Exige que el número sea válido según libphonenumber (E.164 formateable). */
    private static PhoneNumberValue strict(String number, String regionCode) {
        String norm = normalizeBasic(number);
        PhoneNumber parsed = tryParseValid(norm, regionCode);
        return new PhoneNumberValue(number, norm, parsed, ValidationPolicy.STRICT_E164);
    }

    /** Compatibilidad con estilo anterior. STRICT por defecto. */
    public static PhoneNumberValue of(String number, String regionCode) {
        return strict(number, regionCode);
    }

    /** Se ha podido parsear y validar con libphonenumber */
    public boolean isE164() { return parsed != null; }

    @JsonIgnore
    /** E.164 si existe. */
    public String getE164Format() {
        ensureParsed();
        return PN.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @JsonProperty("e164Format")
    /** E.164 si existe; si no, devuelve la forma normalizada básica del raw. */
    public String getE164OrRaw() {
        return parsed != null
                ? PN.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
                : normalizedRaw;
    }

    /** Internacional si existe; si no, raw normalizado. */
    public String getInternationalOrRaw() {
        return parsed != null
                ? PN.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
                : normalizedRaw;
    }

    /** Nacional si existe; si no, raw normalizado. */
    public String getNationalOrRaw() {
        return parsed != null
                ? PN.format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                : normalizedRaw;
    }

    /** Enmascarado para logs/UX: oculta todo salvo los 3 últimos dígitos. */
    public String masked() {
        return this.masked(3);
    }

    public String masked(int visibleDigits) {
        String s = getE164OrRaw().replaceAll("\\D", "");
        if (s.length() <= visibleDigits + 1) {
            return "****";
        }
        return "****" + s.substring(s.length() - visibleDigits);
    }

    public String raw() { return raw; }

    public ValidationPolicy policy() { return policy; }


    private static PhoneNumber tryParseValid(String number, String regionCode) {
        try {
            PhoneNumber p = PN.parse(number, regionCode);
            return PN.isValidNumber(p) ? p : null;
        } catch (NumberParseException e) {
            return null;
        }
    }

    private void ensureParsed() {
        if (parsed == null) {
            throw new IllegalStateException("Phone is not a valid E.164 number for this instance");
        }
    }

    /** Normalización básica */
    private static String normalizeBasic(String s) {
        String t = BASIC_STRIP.matcher(s.trim()).replaceAll("");
        if (t.startsWith("00")) t = "+" + t.substring(2); // 0034... -> +34...
        return t;
    }

    /** Representación canónica para igualdad/hash*/
    private String canonical() {
        return parsed != null
                ? PN.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
                : normalizedRaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumberValue that)) return false;
        return Objects.equals(this.canonical(), that.canonical());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.canonical());
    }

    @Override
    public String toString() {
        // Evitar imprimir números completos en logs por defecto
        return "Phone(" + masked() + ")";
    }
}



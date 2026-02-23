package com.paravai.foundation.domain.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

@EqualsAndHashCode
@Getter
@Schema(hidden = true)
public class CurrencyValue implements LocalizableValueObject {

    private final int code;              // Código numérico legacy (ej. 115 para USD)
    private final Currency currency;

    private static final Map<Integer, String> CODE_TO_ISO = Map.ofEntries(
            Map.entry(115, "USD"),
            Map.entry(43, "EUR"),
            Map.entry(73, "MXN"),
            Map.entry(114, "GBP"),
            Map.entry(32, "CNY")
            // ... añadir más si se desea
    );

    @JsonCreator
    public static CurrencyValue ofLegacy(int code) {
        String isoCode = CODE_TO_ISO.get(code);
        if (isoCode == null) {
            throw new IllegalArgumentException("Unknown currency code: " + code);
        }
        return new CurrencyValue(code, Currency.getInstance(isoCode));
    }

    public static CurrencyValue of(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code cannot be null or blank");
        }

        // Buscar el código correspondiente en el map CODE_TO_ISO
        return CODE_TO_ISO.entrySet().stream()
                .filter(entry -> isoCode.equalsIgnoreCase(entry.getValue()))
                .findFirst()
                .map(entry -> new CurrencyValue(entry.getKey(), Currency.getInstance(isoCode.toUpperCase())))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported ISO code: " + isoCode));
    }

    private CurrencyValue(int code, Currency currency) {
        this.code = code;
        this.currency = currency;
    }

    public String getName() {
        return currency.getDisplayName();
    }

    public String getSymbol() {
        return currency.getSymbol();
    }

    public String getIsoCode() {
        return currency.getCurrencyCode();
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return currency.getDisplayName(locale);
    }

    @Override
    public String toString() {
        return getIsoCode();
    }

    @JsonValue
    public String jsonValue() {
        return getIsoCode();
    }
}

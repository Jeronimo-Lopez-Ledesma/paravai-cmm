package com.dekra.service.foundation.domain.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MoneyValueTest {

    @Test
    void testValidMoneyCreation() {
        MoneyValue money = MoneyValue.of(BigDecimal.valueOf(100.50), CurrencyValue.of("USD"));
        assertNotNull(money);
        assertEquals(BigDecimal.valueOf(100.50), money.getAmount());
        assertEquals("USD", money.getCurrency().getIsoCode());
    }

    @Test
    void testNegativeAmount() {
        BigDecimal amount = BigDecimal.valueOf(-1);
        CurrencyValue currency = CurrencyValue.of("USD");
        assertThrows(IllegalArgumentException.class, () -> MoneyValue.of(amount, currency));
    }

    @Test
    void testNullCurrency() {
        BigDecimal amount = BigDecimal.valueOf(100);
        assertThrows(IllegalArgumentException.class, () -> MoneyValue.of(amount, null));
    }

    @Test
    void testAdd() {
        MoneyValue money1 = MoneyValue.of(BigDecimal.valueOf(50), CurrencyValue.of("USD"));
        MoneyValue money2 = MoneyValue.of(BigDecimal.valueOf(20), CurrencyValue.of("USD"));
        MoneyValue result = money1.add(money2);

        assertEquals(BigDecimal.valueOf(70), result.getAmount());
    }

    @Test
    void testSubtract() {
        MoneyValue money1 = MoneyValue.of(BigDecimal.valueOf(50), CurrencyValue.of("USD"));
        MoneyValue money2 = MoneyValue.of(BigDecimal.valueOf(20), CurrencyValue.of("USD"));
        MoneyValue result = money1.subtract(money2);

        assertEquals(BigDecimal.valueOf(30), result.getAmount());
    }

    @Test
    void testSubtractThrowException() {
        MoneyValue money1 = MoneyValue.of(BigDecimal.valueOf(50), CurrencyValue.of("USD"));
        MoneyValue money2 = MoneyValue.of(BigDecimal.valueOf(20), CurrencyValue.of("EUR"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            money1.subtract(money2);
        });

        assertEquals("Currencies must match", exception.getMessage());
    }

    @Test
    void testDifferentCurrencyAddition() {
        MoneyValue money1 = MoneyValue.of(BigDecimal.valueOf(50), CurrencyValue.of("USD"));
        MoneyValue money2 = MoneyValue.of(BigDecimal.valueOf(20), CurrencyValue.of("EUR"));

        assertThrows(IllegalArgumentException.class, () -> money1.add(money2));
    }

    @Test
    void testToString(){
        MoneyValue money1 = MoneyValue.of(BigDecimal.valueOf(50), CurrencyValue.of("USD"));
        assertEquals("50 USD", money1.toString());
    }

    @Test
    void testFormatWithLocaleUS() {
        MoneyValue money = MoneyValue.of(BigDecimal.valueOf(1234.56), CurrencyValue.of("USD"));
        String formatted = money.format(Locale.US);
        assertEquals("$1,234.56", formatted);
    }

    @Test
    void testFormatWithLocaleGermany() {
        MoneyValue money = MoneyValue.of(BigDecimal.valueOf(1234.56), CurrencyValue.of("EUR"));
        String formatted = money.format(Locale.GERMANY);
        assertEquals("1.234,56 €", formatted); // El espacio puede variar según la JVM
    }

    @Test
    void testFormatWithoutSymbol() {
        MoneyValue money = MoneyValue.of(BigDecimal.valueOf(1234.56), CurrencyValue.of("USD"));
        String formatted = money.formatWithoutSymbol(Locale.GERMANY);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        assertEquals(nf.format(BigDecimal.valueOf(1234.56)), formatted);
    }


}


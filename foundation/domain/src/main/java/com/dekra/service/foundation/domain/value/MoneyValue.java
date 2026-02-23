package com.paravai.foundation.domain.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.text.NumberFormat;

import java.util.Locale;

@Getter
@EqualsAndHashCode
public class MoneyValue {
    private final BigDecimal amount;
    private final CurrencyValue currency;

    private MoneyValue(BigDecimal amount, CurrencyValue currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static MoneyValue of(BigDecimal amount, CurrencyValue currency) {
        return new MoneyValue(amount, currency);
    }

    public MoneyValue add(MoneyValue other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies must match");
        }
        return new MoneyValue(this.amount.add(other.amount), this.currency);
    }

    public MoneyValue subtract(MoneyValue other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies must match");
        }
        return new MoneyValue(this.amount.subtract(other.amount), this.currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency.getIsoCode();
    }

    public String format(Locale locale) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(this.currency.getCurrency());
        return formatter.format(this.amount);
    }

    public String formatWithoutSymbol(Locale locale) {
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMinimumFractionDigits(currency.getCurrency().getDefaultFractionDigits());
        formatter.setMaximumFractionDigits(currency.getCurrency().getDefaultFractionDigits());
        return formatter.format(amount);
    }
}

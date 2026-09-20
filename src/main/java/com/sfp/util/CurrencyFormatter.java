package com.sfp.util;

import com.sfp.model.Currency;

import java.text.NumberFormat;

/**
 * Shared formatting utility ensuring consistent display of monetary values with currency symbols and locale grouping.
 */
public class CurrencyFormatter {

    /**
     * Formats an amount using the specified currency code with two decimal places (e.g., ₹74,300.00, $1,250.00).
     */
    public static String format(double amount, String currencyCode) {
        Currency currency = Currency.fromCode(currencyCode);
        return format(amount, currency);
    }

    /**
     * Formats an amount using the specified Currency enum with two decimal places.
     */
    public static String format(double amount, Currency currency) {
        if (currency == null) {
            currency = Currency.INR;
        }
        NumberFormat nf = NumberFormat.getNumberInstance(currency.getLocale());
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return currency.getSymbol() + nf.format(amount);
    }

    /**
     * Formats an amount as an integer without decimal fractions (e.g., ₹74,300, $1,250).
     */
    public static String formatWhole(double amount, String currencyCode) {
        Currency currency = Currency.fromCode(currencyCode);
        NumberFormat nf = NumberFormat.getNumberInstance(currency.getLocale());
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        return currency.getSymbol() + nf.format(Math.round(amount));
    }
}

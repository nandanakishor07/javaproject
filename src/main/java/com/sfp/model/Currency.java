package com.sfp.model;

import java.util.Locale;

/**
 * Supported display currencies with symbols, descriptions, and locale formatting preferences.
 */
public enum Currency {
    INR("INR", "₹", "Indian Rupee", new Locale("en", "IN")),
    USD("USD", "$", "US Dollar", Locale.US),
    EUR("EUR", "€", "Euro", Locale.GERMANY),
    GBP("GBP", "£", "British Pound", Locale.UK);

    private final String code;
    private final String symbol;
    private final String displayName;
    private final Locale locale;

    Currency(String code, String symbol, String displayName, Locale locale) {
        this.code = code;
        this.symbol = symbol;
        this.displayName = displayName;
        this.locale = locale;
    }

    /**
     * Gets the ISO currency code (e.g., INR, USD).
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the visual currency symbol (e.g., ₹, $).
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the human-readable display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the formatting Locale associated with this currency.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Finds a Currency by its code, defaulting to INR if not found or null.
     */
    public static Currency fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return INR;
        }
        for (Currency c : values()) {
            if (c.code.equalsIgnoreCase(code.trim())) {
                return c;
            }
        }
        return INR;
    }

    @Override
    public String toString() {
        return code + " (" + symbol + ") - " + displayName;
    }
}

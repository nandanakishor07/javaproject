package com.sfp.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Shared validation utility enforcing all business and input constraints across GUI and unit tests.
 */
public class Validator {
    public static final double MIN_INFLATION = 0.0;
    public static final double MAX_INFLATION = 50.0;
    public static final double MIN_SAVINGS_PERCENT = 1.0;
    public static final double MAX_SAVINGS_PERCENT = 100.0;

    /**
     * Validates that an item name is not empty or blank.
     */
    public static String validateItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Item name cannot be empty.";
        }
        return null;
    }

    /**
     * Validates that price is positive (> 0).
     */
    public static String validatePrice(double price) {
        if (price <= 0.0) {
            return "Price must be a positive number.";
        }
        return null;
    }

    /**
     * Validates that target year is in the future (> current year).
     */
    public static String validateTargetYear(int targetYear, int currentYear) {
        if (targetYear <= currentYear) {
            return "Target year must be in the future.";
        }
        return null;
    }

    /**
     * Validates that inflation rate is between 0% and 50%.
     */
    public static String validateInflationRate(double inflationRate) {
        if (inflationRate < MIN_INFLATION || inflationRate > MAX_INFLATION) {
            return "Inflation rate should be between 0% and 50%.";
        }
        return null;
    }

    /**
     * Validates that monthly income is positive (> 0).
     */
    public static String validateMonthlyIncome(double monthlyIncome) {
        if (monthlyIncome <= 0.0) {
            return "Monthly income must be positive.";
        }
        return null;
    }

    /**
     * Validates that savings percentage is between 1% and 100%.
     */
    public static String validateSavingsPercent(double savingsPercent) {
        if (savingsPercent < MIN_SAVINGS_PERCENT || savingsPercent > MAX_SAVINGS_PERCENT) {
            return "Savings % must be between 1 and 100.";
        }
        return null;
    }

    /**
     * Validates that contribution amount is positive (> 0).
     */
    public static String validateContributionAmount(double amount) {
        if (amount <= 0.0) {
            return "Contribution amount must be positive.";
        }
        return null;
    }

    /**
     * Validates that a contribution date is not in the future.
     */
    public static String validateContributionDate(LocalDate date, LocalDate asOf) {
        if (date == null) {
            return "Contribution date is required.";
        }
        if (asOf != null && date.isAfter(asOf)) {
            return "Contribution date cannot be in the future.";
        }
        return null;
    }

    /**
     * Validates contribution date string against today's date.
     */
    public static String validateContributionDateStr(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "Contribution date is required.";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim());
            return validateContributionDate(date, LocalDate.now());
        } catch (DateTimeParseException e) {
            return "Invalid date format. Expected YYYY-MM-DD.";
        }
    }
}

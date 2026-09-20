package com.sfp.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying all 8 validation rules defined in specs.md §13.
 */
class ValidatorTest {

    @Test
    void testValidateItemName() {
        assertNotNull(Validator.validateItemName(null));
        assertNotNull(Validator.validateItemName(""));
        assertNotNull(Validator.validateItemName("   "));
        assertNull(Validator.validateItemName("MacBook Air"));
    }

    @Test
    void testValidatePrice() {
        assertNotNull(Validator.validatePrice(0.0));
        assertNotNull(Validator.validatePrice(-500.0));
        assertNull(Validator.validatePrice(1500.0));
    }

    @Test
    void testValidateTargetYear() {
        int currentYear = 2026;
        assertNotNull(Validator.validateTargetYear(2025, currentYear));
        assertNotNull(Validator.validateTargetYear(2026, currentYear));
        assertNull(Validator.validateTargetYear(2027, currentYear));
    }

    @Test
    void testValidateInflationRate() {
        assertNotNull(Validator.validateInflationRate(-0.1));
        assertNotNull(Validator.validateInflationRate(50.1));
        assertNull(Validator.validateInflationRate(0.0));
        assertNull(Validator.validateInflationRate(6.0));
        assertNull(Validator.validateInflationRate(50.0));
    }

    @Test
    void testValidateMonthlyIncome() {
        assertNotNull(Validator.validateMonthlyIncome(0.0));
        assertNotNull(Validator.validateMonthlyIncome(-1000.0));
        assertNull(Validator.validateMonthlyIncome(75000.0));
    }

    @Test
    void testValidateSavingsPercent() {
        assertNotNull(Validator.validateSavingsPercent(0.9));
        assertNotNull(Validator.validateSavingsPercent(100.1));
        assertNull(Validator.validateSavingsPercent(1.0));
        assertNull(Validator.validateSavingsPercent(20.0));
        assertNull(Validator.validateSavingsPercent(100.0));
    }

    @Test
    void testValidateContributionAmount() {
        assertNotNull(Validator.validateContributionAmount(0.0));
        assertNotNull(Validator.validateContributionAmount(-50.0));
        assertNull(Validator.validateContributionAmount(1000.0));
    }

    @Test
    void testValidateContributionDate() {
        LocalDate today = LocalDate.of(2026, 9, 20);
        LocalDate past = LocalDate.of(2026, 9, 15);
        LocalDate future = LocalDate.of(2026, 9, 21);

        assertNull(Validator.validateContributionDate(past, today));
        assertNull(Validator.validateContributionDate(today, today));
        assertNotNull(Validator.validateContributionDate(future, today));
        assertNotNull(Validator.validateContributionDate(null, today));
    }
}

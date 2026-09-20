package com.sfp.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SavingsPlanner.
 */
class SavingsPlannerTest {
    private SavingsPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new SavingsPlanner();
    }

    @Test
    void testMonthlySavingGuardedAgainstZeroYears() {
        // years = 0 should default to at least 1 month, no divide by zero crash
        double saving = planner.monthlySaving(12000.0, 0);
        assertEquals(12000.0, saving, 0.001);
    }

    @Test
    void testMonthlySavingStandard() {
        // ₹60,000 over 5 years (60 months) = ₹1,000 / month
        double saving = planner.monthlySaving(60000.0, 5);
        assertEquals(1000.0, saving, 0.001);
    }

    @Test
    void testYearlySavingGuardedAgainstZeroYears() {
        double saving = planner.yearlySaving(50000.0, 0);
        assertEquals(50000.0, saving, 0.001);
    }

    @Test
    void testPercentOfIncomeWithZeroIncomeReturnsZero() {
        double percent = planner.percentOfIncome(5000.0, 0.0);
        assertEquals(0.0, percent, 0.001, "Zero income should safely return 0%");
    }

    @Test
    void testPercentOfIncomeStandard() {
        // saving 5000 from 50000 income = 10%
        double percent = planner.percentOfIncome(5000.0, 50000.0);
        assertEquals(10.0, percent, 0.001);
    }

    @Test
    void testIsFeasible() {
        // Income 50,000, max saving 20% = 10,000 cap
        assertTrue(planner.isFeasible(8000.0, 50000.0, 20.0), "8000 <= 10000 should be feasible");
        assertTrue(planner.isFeasible(10000.0, 50000.0, 20.0), "10000 <= 10000 should be feasible");
        assertFalse(planner.isFeasible(12000.0, 50000.0, 20.0), "12000 > 10000 should not be feasible");
        assertFalse(planner.isFeasible(5000.0, 0.0, 20.0), "Zero income should not be feasible");
    }
}

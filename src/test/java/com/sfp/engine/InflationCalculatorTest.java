package com.sfp.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InflationCalculator.
 */
class InflationCalculatorTest {
    private InflationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InflationCalculator();
    }

    @Test
    void testZeroInflationReturnsPriceUnchanged() {
        double price = 45000.0;
        double result = calculator.futureValue(price, 0.0, 5);
        assertEquals(price, result, 0.001, "0% inflation should keep price unchanged");
    }

    @Test
    void testZeroYearsReturnsPriceUnchanged() {
        double price = 50000.0;
        double result = calculator.futureValue(price, 0.06, 0);
        assertEquals(price, result, 0.001, "0 years should keep price unchanged");
    }

    @Test
    void testHandCalculatedFutureValue() {
        // ₹50,000 @ 6% for 5 years: 50000 * (1.06)^5 = 66911.278...
        double result = calculator.futureValue(50000.0, 0.06, 5);
        assertEquals(66911.28, result, 0.5, "₹50,000 @ 6% for 5 years should be ≈ ₹66,911");
    }

    @Test
    void testNegativePriceThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.futureValue(-100.0, 0.05, 3);
        }, "Negative price should throw IllegalArgumentException");
    }

    @Test
    void testNegativeYearsThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.futureValue(1000.0, 0.05, -2);
        }, "Negative years should throw IllegalArgumentException");
    }
}

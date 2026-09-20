package com.sfp.engine;

/**
 * Pure calculation engine for compound inflation adjustments.
 */
public class InflationCalculator {

    /**
     * Calculates the future value of an item using compound inflation: price * (1 + rate)^years.
     *
     * @param price Current price of the item (must be non-negative)
     * @param rate  Annual inflation rate as a decimal (e.g., 0.06 for 6%)
     * @param years Number of years until target purchase (must be non-negative)
     * @return Inflation-adjusted future price
     * @throws IllegalArgumentException if price or years is negative
     */
    public double futureValue(double price, double rate, int years) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        }
        if (years < 0) {
            throw new IllegalArgumentException("Years cannot be negative: " + years);
        }
        if (rate == 0.0 || years == 0) {
            return price;
        }
        return price * Math.pow(1.0 + rate, years);
    }
}

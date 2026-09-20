package com.sfp.engine;

/**
 * Pure calculation engine for reverse-savings planning and affordability feasibility.
 */
public class SavingsPlanner {

    /**
     * Calculates the required monthly savings amount to reach the future cost over given years.
     */
    public double monthlySaving(double futureValue, int years) {
        if (futureValue <= 0) {
            return 0.0;
        }
        int months = Math.max(years * 12, 1);
        return futureValue / months;
    }

    /**
     * Calculates the required yearly savings amount to reach the future cost over given years.
     */
    public double yearlySaving(double futureValue, int years) {
        if (futureValue <= 0) {
            return 0.0;
        }
        int safeYears = Math.max(years, 1);
        return futureValue / safeYears;
    }

    /**
     * Computes the percentage of monthly income consumed by the required monthly saving.
     */
    public double percentOfIncome(double monthlySaving, double monthlyIncome) {
        if (monthlyIncome <= 0 || monthlySaving <= 0) {
            return 0.0;
        }
        return (monthlySaving / monthlyIncome) * 100.0;
    }

    /**
     * Checks whether the required monthly saving fits within the user's maximum savings capacity.
     */
    public boolean isFeasible(double monthlySaving, double monthlyIncome, double savingsPercentCap) {
        if (monthlyIncome <= 0 || savingsPercentCap <= 0) {
            return false;
        }
        double maxAffordable = monthlyIncome * (savingsPercentCap / 100.0);
        return monthlySaving <= maxAffordable;
    }
}

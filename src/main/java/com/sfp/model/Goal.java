package com.sfp.model;

/**
 * Domain model representing a financial savings purchase goal.
 */
public class Goal {
    private int id;
    private int userId;
    private String itemName;
    private double currentPrice;
    private int targetYear;
    private double inflationRate;
    private String termType; // "SHORT" or "LONG"
    private String currencyCode;
    private double futureCost;
    private double monthlySaving;
    private String status; // "ACTIVE", "COMPLETED", "ARCHIVED"
    private String createdAt;
    private String updatedAt;

    /**
     * Default constructor initializing default values.
     */
    public Goal() {
        this.termType = "SHORT";
        this.currencyCode = "INR";
        this.status = "ACTIVE";
    }

    /**
     * Constructor for creating a new goal with calculated future cost and monthly saving.
     */
    public Goal(int userId, String itemName, double currentPrice, int targetYear, 
                double inflationRate, String termType, String currencyCode, 
                double futureCost, double monthlySaving) {
        this.userId = userId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.targetYear = targetYear;
        this.inflationRate = inflationRate;
        this.termType = termType != null ? termType : "SHORT";
        this.currencyCode = currencyCode != null ? currencyCode : "INR";
        this.futureCost = futureCost;
        this.monthlySaving = monthlySaving;
        this.status = "ACTIVE";
    }

    /**
     * Full constructor for reconstructing a goal from database persistence.
     */
    public Goal(int id, int userId, String itemName, double currentPrice, int targetYear, 
                double inflationRate, String termType, String currencyCode, 
                double futureCost, double monthlySaving, String status, 
                String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.targetYear = targetYear;
        this.inflationRate = inflationRate;
        this.termType = termType;
        this.currencyCode = currencyCode;
        this.futureCost = futureCost;
        this.monthlySaving = monthlySaving;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Computes the number of full years remaining until the target year.
     */
    public int getYearsLeft(int currentYear) {
        return Math.max(0, targetYear - currentYear);
    }

    /**
     * Gets the unique goal ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique goal ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the user ID associated with this goal.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID associated with this goal.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the item name or target description.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Sets the item name or target description.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Gets the current today's price of the item.
     */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the current today's price of the item.
     */
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    /**
     * Gets the target completion year.
     */
    public int getTargetYear() {
        return targetYear;
    }

    /**
     * Sets the target completion year.
     */
    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
    }

    /**
     * Gets the annual inflation rate percentage (e.g. 6.0 for 6%).
     */
    public double getInflationRate() {
        return inflationRate;
    }

    /**
     * Sets the annual inflation rate percentage.
     */
    public void setInflationRate(double inflationRate) {
        this.inflationRate = inflationRate;
    }

    /**
     * Gets the term type ('SHORT' or 'LONG').
     */
    public String getTermType() {
        return termType;
    }

    /**
     * Sets the term type ('SHORT' or 'LONG').
     */
    public void setTermType(String termType) {
        this.termType = termType;
    }

    /**
     * Gets the goal currency ISO code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Sets the goal currency ISO code.
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * Gets the inflation-adjusted future cost.
     */
    public double getFutureCost() {
        return futureCost;
    }

    /**
     * Sets the inflation-adjusted future cost.
     */
    public void setFutureCost(double futureCost) {
        this.futureCost = futureCost;
    }

    /**
     * Gets the required monthly saving amount.
     */
    public double getMonthlySaving() {
        return monthlySaving;
    }

    /**
     * Sets the required monthly saving amount.
     */
    public void setMonthlySaving(double monthlySaving) {
        this.monthlySaving = monthlySaving;
    }

    /**
     * Gets the goal lifecycle status ('ACTIVE', 'COMPLETED', or 'ARCHIVED').
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the goal lifecycle status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the creation timestamp.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

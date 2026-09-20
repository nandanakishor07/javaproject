package com.sfp.model;

/**
 * Domain model representing a user profile with financial and display preferences.
 */
public class User {
    private int id;
    private String name;
    private double monthlyIncome;
    private double savingsPercent;
    private String defaultCurrency;
    private String theme;
    private String createdAt;

    /**
     * Default constructor initializing default preferences.
     */
    public User() {
        this.defaultCurrency = "INR";
        this.theme = "LIGHT";
        this.savingsPercent = 20.0;
    }

    /**
     * Parameterized constructor for creating a new user profile.
     */
    public User(String name, double monthlyIncome, double savingsPercent, String defaultCurrency, String theme) {
        this.name = name;
        this.monthlyIncome = monthlyIncome;
        this.savingsPercent = savingsPercent;
        this.defaultCurrency = defaultCurrency != null ? defaultCurrency : "INR";
        this.theme = theme != null ? theme : "LIGHT";
    }

    /**
     * Full constructor for reconstructing a user from database persistence.
     */
    public User(int id, String name, double monthlyIncome, double savingsPercent, 
                String defaultCurrency, String theme, String createdAt) {
        this.id = id;
        this.name = name;
        this.monthlyIncome = monthlyIncome;
        this.savingsPercent = savingsPercent;
        this.defaultCurrency = defaultCurrency;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    /**
     * Gets the unique user ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique user ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the user's display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the monthly income amount.
     */
    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    /**
     * Sets the monthly income amount.
     */
    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    /**
     * Gets the target savings percentage (1-100).
     */
    public double getSavingsPercent() {
        return savingsPercent;
    }

    /**
     * Sets the target savings percentage (1-100).
     */
    public void setSavingsPercent(double savingsPercent) {
        this.savingsPercent = savingsPercent;
    }

    /**
     * Gets the default currency ISO code (e.g. INR).
     */
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    /**
     * Sets the default currency ISO code.
     */
    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    /**
     * Gets the UI theme preference ('LIGHT' or 'DARK').
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the UI theme preference ('LIGHT' or 'DARK').
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Gets the timestamp when the user profile was created.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the user profile was created.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

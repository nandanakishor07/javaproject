package com.sfp.model;

/**
 * Domain model representing a manual savings contribution towards a goal.
 */
public class Contribution {
    private int id;
    private int goalId;
    private double amount;
    private String contributionDate; // ISO-8601 YYYY-MM-DD
    private String note;
    private String createdAt;

    /**
     * Default constructor.
     */
    public Contribution() {
    }

    /**
     * Constructor for creating a new contribution.
     */
    public Contribution(int goalId, double amount, String contributionDate, String note) {
        this.goalId = goalId;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.note = note;
    }

    /**
     * Full constructor for database reconstruction.
     */
    public Contribution(int id, int goalId, double amount, String contributionDate, String note, String createdAt) {
        this.id = id;
        this.goalId = goalId;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.note = note;
        this.createdAt = createdAt;
    }

    /**
     * Gets the contribution ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the contribution ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the parent goal ID.
     */
    public int getGoalId() {
        return goalId;
    }

    /**
     * Sets the parent goal ID.
     */
    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    /**
     * Gets the contribution amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the contribution amount.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gets the contribution date string (YYYY-MM-DD).
     */
    public String getContributionDate() {
        return contributionDate;
    }

    /**
     * Sets the contribution date string (YYYY-MM-DD).
     */
    public void setContributionDate(String contributionDate) {
        this.contributionDate = contributionDate;
    }

    /**
     * Gets the optional memo/note.
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the optional memo/note.
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Gets the timestamp when this entry was created.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when this entry was created.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

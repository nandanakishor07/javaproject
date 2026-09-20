package com.sfp.engine;

import com.sfp.model.Goal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Pure calculation engine for tracking progress and on-track pacing against target goals.
 */
public class ProgressTracker {
    private static final double TOLERANCE = 0.95; // 5% pacing buffer
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Computes the percentage of the future cost accumulated by contributions so far.
     */
    public double getProgressPercent(Goal goal, double totalContributed) {
        if (goal == null || goal.getFutureCost() <= 0 || totalContributed <= 0) {
            return 0.0;
        }
        return (totalContributed / goal.getFutureCost()) * 100.0;
    }

    /**
     * Computes the remaining monetary amount needed to reach the goal's target cost, floored at zero.
     */
    public double getRemainingAmount(Goal goal, double totalContributed) {
        if (goal == null || goal.getFutureCost() <= 0) {
            return 0.0;
        }
        return Math.max(0.0, goal.getFutureCost() - totalContributed);
    }

    /**
     * Determines if actual savings contributions meet or exceed the elapsed-time-weighted expectation as of a date.
     */
    public boolean isOnTrack(Goal goal, double totalContributed, LocalDate asOf) {
        if (goal == null || asOf == null || goal.getFutureCost() <= 0) {
            return true;
        }
        if (totalContributed >= goal.getFutureCost()) {
            return true;
        }

        LocalDate startDate = parseStartDate(goal);
        LocalDate targetDate = LocalDate.of(goal.getTargetYear(), 12, 31);

        if (asOf.isBefore(startDate) || asOf.isEqual(startDate)) {
            return true;
        }
        if (asOf.isAfter(targetDate) || asOf.isEqual(targetDate)) {
            return totalContributed >= goal.getFutureCost();
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, targetDate);
        if (totalDays <= 0) {
            return totalContributed >= goal.getFutureCost();
        }

        long elapsedDays = ChronoUnit.DAYS.between(startDate, asOf);
        double fractionElapsed = (double) elapsedDays / totalDays;
        double expectedSaved = fractionElapsed * goal.getFutureCost();

        return totalContributed >= (expectedSaved * TOLERANCE);
    }

    /**
     * Convenience overload determining if savings pacing is on track as of today.
     */
    public boolean isOnTrack(Goal goal, double totalContributed) {
        return isOnTrack(goal, totalContributed, LocalDate.now());
    }

    /**
     * Parses the creation date of the goal or defaults to the beginning of the goal's timeline.
     */
    private LocalDate parseStartDate(Goal goal) {
        if (goal.getCreatedAt() != null && !goal.getCreatedAt().isEmpty()) {
            try {
                String dateStr = goal.getCreatedAt().substring(0, 10);
                return LocalDate.parse(dateStr, DATE_FORMAT);
            } catch (Exception ignored) {
                // Fall back if unparseable
            }
        }
        return LocalDate.now();
    }
}

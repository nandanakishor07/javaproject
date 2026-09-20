package com.sfp.engine;

import com.sfp.model.Goal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProgressTracker with deterministic date injection.
 */
class ProgressTrackerTest {
    private ProgressTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ProgressTracker();
    }

    @Test
    void testProgressPercentGuardedAgainstZeroFutureCost() {
        Goal goal = new Goal();
        goal.setFutureCost(0.0);

        double percent = tracker.getProgressPercent(goal, 5000.0);
        assertEquals(0.0, percent, 0.001, "Zero future cost should safely return 0%");
    }

    @Test
    void testProgressPercentStandard() {
        Goal goal = new Goal();
        goal.setFutureCost(100000.0);

        double percent = tracker.getProgressPercent(goal, 25000.0);
        assertEquals(25.0, percent, 0.001);
    }

    @Test
    void testRemainingAmount() {
        Goal goal = new Goal();
        goal.setFutureCost(80000.0);

        assertEquals(50000.0, tracker.getRemainingAmount(goal, 30000.0), 0.001);
        // Overcontributed should floor at 0
        assertEquals(0.0, tracker.getRemainingAmount(goal, 90000.0), 0.001);
    }

    @Test
    void testIsOnTrackWithInjectableDate() {
        // Goal created on 2026-01-01, target year 2027 (target deadline 2027-12-31, 2 years total = ~730 days)
        // Total future cost = 100,000
        Goal goal = new Goal();
        goal.setCreatedAt("2026-01-01 00:00:00");
        goal.setTargetYear(2027);
        goal.setFutureCost(100000.0);

        // At halfway point (around 2026-12-31), expected is ~50,000 (with 5% buffer: ~47,500)
        LocalDate midway = LocalDate.of(2026, 12, 31);

        // Saved 50,000 at midpoint -> on track
        assertTrue(tracker.isOnTrack(goal, 50000.0, midway), "50,000 at midpoint should be on track");

        // Saved only 10,000 at midpoint -> behind schedule
        assertFalse(tracker.isOnTrack(goal, 10000.0, midway), "10,000 at midpoint should be behind schedule");

        // Already reached 100,000 -> always on track
        assertTrue(tracker.isOnTrack(goal, 100000.0, midway));
    }
}

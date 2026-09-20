package com.sfp.data;

import com.sfp.model.Contribution;
import com.sfp.model.Goal;
import com.sfp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContributionDao using isolated in-memory SQLite database.
 */
class ContributionDaoTest {
    private DatabaseManager dbManager;
    private UserDao userDao;
    private GoalDao goalDao;
    private ContributionDao contributionDao;
    private int goalId;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initSchema();
        userDao = new UserDao(dbManager);
        goalDao = new GoalDao(dbManager);
        contributionDao = new ContributionDao(dbManager);

        User user = new User("Saver", 60000.0, 20.0, "INR", "LIGHT");
        int userId = userDao.insert(user);

        Goal goal = new Goal(userId, "Laptop", 80000.0, 2027, 5.0, "SHORT", "INR", 90000.0, 3000.0);
        goalId = goalDao.insert(goal);
    }

    @Test
    void testInsertAndListContributions() throws SQLException {
        Contribution c1 = new Contribution(goalId, 5000.0, "2026-01-10", "Initial deposit");
        Contribution c2 = new Contribution(goalId, 10000.0, "2026-02-15", "Bonus deposit");

        int id1 = contributionDao.insert(c1);
        int id2 = contributionDao.insert(c2);
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);

        List<Contribution> list = contributionDao.findByGoalId(goalId);
        assertEquals(2, list.size());
        // Newest date first
        assertEquals("2026-02-15", list.get(0).getContributionDate());
        assertEquals(10000.0, list.get(0).getAmount());
        assertEquals("Bonus deposit", list.get(0).getNote());
    }

    @Test
    void testGetTotalContributed() throws SQLException {
        assertEquals(0.0, contributionDao.getTotalContributed(goalId), 0.001);

        contributionDao.insert(new Contribution(goalId, 3500.50, "2026-01-01", "P1"));
        contributionDao.insert(new Contribution(goalId, 6499.50, "2026-02-01", "P2"));

        double total = contributionDao.getTotalContributed(goalId);
        assertEquals(10000.0, total, 0.001);
    }

    @Test
    void testDeleteContribution() throws SQLException {
        Contribution c = new Contribution(goalId, 2500.0, "2026-01-05", "Mistake");
        int id = contributionDao.insert(c);

        assertEquals(2500.0, contributionDao.getTotalContributed(goalId), 0.001);

        boolean deleted = contributionDao.delete(id);
        assertTrue(deleted);
        assertEquals(0.0, contributionDao.getTotalContributed(goalId), 0.001);
        assertTrue(contributionDao.findByGoalId(goalId).isEmpty());
    }

    @Test
    void testCascadeDeleteOnGoalRemoval() throws SQLException {
        contributionDao.insert(new Contribution(goalId, 5000.0, "2026-01-01", "Part 1"));
        contributionDao.insert(new Contribution(goalId, 5000.0, "2026-02-01", "Part 2"));
        assertEquals(2, contributionDao.findByGoalId(goalId).size());

        // Delete parent goal
        boolean deleted = goalDao.delete(goalId);
        assertTrue(deleted);

        // Due to ON DELETE CASCADE and PRAGMA foreign_keys = ON, contributions must be automatically removed
        List<Contribution> orphanContributions = contributionDao.findByGoalId(goalId);
        assertTrue(orphanContributions.isEmpty(), "Contributions should cascade delete when goal is deleted");
    }
}

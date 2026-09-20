package com.sfp.data;

import com.sfp.model.Goal;
import com.sfp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GoalDao using isolated in-memory SQLite database.
 */
class GoalDaoTest {
    private DatabaseManager dbManager;
    private UserDao userDao;
    private GoalDao goalDao;
    private int userId;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initSchema();
        userDao = new UserDao(dbManager);
        goalDao = new GoalDao(dbManager);

        User user = new User("Test User", 80000.0, 20.0, "INR", "LIGHT");
        userId = userDao.insert(user);
    }

    @Test
    void testInsertAndFindById() throws SQLException {
        Goal goal = new Goal(userId, "MacBook Pro", 150000.0, 2027, 6.0, "SHORT", "INR", 178652.4, 4962.57);
        int goalId = goalDao.insert(goal);
        assertTrue(goalId > 0);

        Goal fetched = goalDao.findById(goalId);
        assertNotNull(fetched);
        assertEquals("MacBook Pro", fetched.getItemName());
        assertEquals(150000.0, fetched.getCurrentPrice());
        assertEquals(2027, fetched.getTargetYear());
        assertEquals(6.0, fetched.getInflationRate());
        assertEquals("SHORT", fetched.getTermType());
        assertEquals("INR", fetched.getCurrencyCode());
        assertEquals("ACTIVE", fetched.getStatus());
    }

    @Test
    void testFindByUserIdAndStatus() throws SQLException {
        Goal g1 = new Goal(userId, "Goal 1", 50000.0, 2028, 5.0, "SHORT", "USD", 55000.0, 1500.0);
        Goal g2 = new Goal(userId, "Goal 2", 100000.0, 2030, 7.0, "LONG", "EUR", 120000.0, 2000.0);
        g2.setStatus("COMPLETED");

        int g1Id = goalDao.insert(g1);
        int g2Id = goalDao.insert(g2);
        // Ensure g2 has status COMPLETED
        goalDao.updateStatus(g2Id, "COMPLETED");

        List<Goal> allGoals = goalDao.findByUserId(userId);
        assertEquals(2, allGoals.size());

        List<Goal> activeGoals = goalDao.findByUserIdAndStatus(userId, "ACTIVE");
        assertEquals(1, activeGoals.size());
        assertEquals(g1Id, activeGoals.get(0).getId());

        List<Goal> completedGoals = goalDao.findByUserIdAndStatus(userId, "COMPLETED");
        assertEquals(1, completedGoals.size());
        assertEquals(g2Id, completedGoals.get(0).getId());
    }

    @Test
    void testUpdateGoal() throws SQLException {
        Goal goal = new Goal(userId, "Old Name", 50000.0, 2027, 5.0, "SHORT", "INR", 60000.0, 2000.0);
        int id = goalDao.insert(goal);

        goal.setItemName("New Name");
        goal.setCurrentPrice(60000.0);
        goal.setTargetYear(2028);
        goal.setInflationRate(7.0);
        goal.setFutureCost(75000.0);
        goal.setMonthlySaving(2500.0);
        goal.setStatus("ACTIVE");

        boolean updated = goalDao.update(goal);
        assertTrue(updated);

        Goal refreshed = goalDao.findById(id);
        assertEquals("New Name", refreshed.getItemName());
        assertEquals(60000.0, refreshed.getCurrentPrice());
        assertEquals(2028, refreshed.getTargetYear());
        assertEquals(75000.0, refreshed.getFutureCost());
    }

    @Test
    void testUpdateStatusAndArchive() throws SQLException {
        Goal goal = new Goal(userId, "Bike", 80000.0, 2027, 6.0, "SHORT", "INR", 90000.0, 3000.0);
        int id = goalDao.insert(goal);

        goalDao.updateStatus(id, "ARCHIVED");
        Goal archived = goalDao.findById(id);
        assertEquals("ARCHIVED", archived.getStatus());

        goalDao.updateStatus(id, "ACTIVE");
        Goal restored = goalDao.findById(id);
        assertEquals("ACTIVE", restored.getStatus());
    }

    @Test
    void testDeleteGoal() throws SQLException {
        Goal goal = new Goal(userId, "Camera", 70000.0, 2027, 5.0, "SHORT", "INR", 80000.0, 2500.0);
        int id = goalDao.insert(goal);

        boolean deleted = goalDao.delete(id);
        assertTrue(deleted);
        assertNull(goalDao.findById(id));
    }
}

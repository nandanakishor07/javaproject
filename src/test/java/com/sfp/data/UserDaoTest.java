package com.sfp.data;

import com.sfp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserDao using isolated in-memory SQLite database.
 */
class UserDaoTest {
    private DatabaseManager dbManager;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        // Use unique in-memory database per test run
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        dbManager.initSchema();
        userDao = new UserDao(dbManager);
    }

    @Test
    void testInsertAndFindById() throws SQLException {
        User user = new User("Rahul", 75000.0, 25.0, "INR", "LIGHT");
        int id = userDao.insert(user);
        assertTrue(id > 0, "Generated ID should be positive");

        User fetched = userDao.findById(id);
        assertNotNull(fetched, "User should be found in database");
        assertEquals("Rahul", fetched.getName());
        assertEquals(75000.0, fetched.getMonthlyIncome());
        assertEquals(25.0, fetched.getSavingsPercent());
        assertEquals("INR", fetched.getDefaultCurrency());
        assertEquals("LIGHT", fetched.getTheme());
    }

    @Test
    void testFindFirst() throws SQLException {
        assertNull(userDao.findFirst(), "Initially there should be no user");

        User user = new User("Alice", 60000.0, 20.0, "USD", "DARK");
        userDao.insert(user);

        User first = userDao.findFirst();
        assertNotNull(first);
        assertEquals("Alice", first.getName());
        assertEquals("USD", first.getDefaultCurrency());
        assertEquals("DARK", first.getTheme());
    }

    @Test
    void testUpdateUser() throws SQLException {
        User user = new User("Bob", 50000.0, 15.0, "EUR", "LIGHT");
        int id = userDao.insert(user);

        user.setName("Robert");
        user.setMonthlyIncome(55000.0);
        user.setSavingsPercent(30.0);
        user.setTheme("DARK");
        boolean updated = userDao.update(user);
        assertTrue(updated, "Update should succeed");

        User refreshed = userDao.findById(id);
        assertEquals("Robert", refreshed.getName());
        assertEquals(55000.0, refreshed.getMonthlyIncome());
        assertEquals(30.0, refreshed.getSavingsPercent());
        assertEquals("DARK", refreshed.getTheme());
    }

    @Test
    void testDeleteUser() throws SQLException {
        User user = new User("Charlie", 40000.0, 10.0, "GBP", "LIGHT");
        int id = userDao.insert(user);

        boolean deleted = userDao.delete(id);
        assertTrue(deleted);
        assertNull(userDao.findById(id));
    }
}

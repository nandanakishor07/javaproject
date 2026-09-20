package com.sfp.data;

import com.sfp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object managing persistence operations for User entities.
 */
public class UserDao {
    private final DatabaseManager dbManager;

    /**
     * Constructs a UserDao backed by the given DatabaseManager.
     */
    public UserDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new user record and returns the generated primary key.
     */
    public int insert(User user) throws SQLException {
        String sql = """
            INSERT INTO users (name, monthly_income, savings_percent, default_currency, theme)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setDouble(2, user.getMonthlyIncome());
            ps.setDouble(3, user.getSavingsPercent());
            ps.setString(4, user.getDefaultCurrency());
            ps.setString(5, user.getTheme());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Finds and returns a user by their unique primary key ID, or null if not found.
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds and returns the first user profile, or null if no user has onboarded yet.
     */
    public User findFirst() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY id ASC LIMIT 1";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        }
        return null;
    }

    /**
     * Updates an existing user record with new income, savings percent, default currency, and theme.
     */
    public boolean update(User user) throws SQLException {
        String sql = """
            UPDATE users 
            SET name = ?, monthly_income = ?, savings_percent = ?, default_currency = ?, theme = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setDouble(2, user.getMonthlyIncome());
            ps.setDouble(3, user.getSavingsPercent());
            ps.setString(4, user.getDefaultCurrency());
            ps.setString(5, user.getTheme());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a user by ID.
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Maps an active ResultSet row to a populated User instance.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("monthly_income"),
            rs.getDouble("savings_percent"),
            rs.getString("default_currency"),
            rs.getString("theme"),
            rs.getString("created_at")
        );
    }
}

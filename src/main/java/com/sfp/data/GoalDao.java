package com.sfp.data;

import com.sfp.model.Goal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object managing persistence operations for Goal entities.
 */
public class GoalDao {
    private final DatabaseManager dbManager;

    /**
     * Constructs a GoalDao backed by the given DatabaseManager.
     */
    public GoalDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new financial goal and returns the generated primary key.
     */
    public int insert(Goal goal) throws SQLException {
        String sql = """
            INSERT INTO goals (
                user_id, item_name, current_price, target_year, 
                inflation_rate, term_type, currency_code, future_cost, 
                monthly_saving, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, goal.getUserId());
            ps.setString(2, goal.getItemName());
            ps.setDouble(3, goal.getCurrentPrice());
            ps.setInt(4, goal.getTargetYear());
            ps.setDouble(5, goal.getInflationRate());
            ps.setString(6, goal.getTermType());
            ps.setString(7, goal.getCurrencyCode());
            ps.setDouble(8, goal.getFutureCost());
            ps.setDouble(9, goal.getMonthlySaving());
            ps.setString(10, goal.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    goal.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Updates an existing goal's core properties and updates the updated_at timestamp.
     */
    public boolean update(Goal goal) throws SQLException {
        String sql = """
            UPDATE goals 
            SET item_name = ?, current_price = ?, target_year = ?, 
                inflation_rate = ?, term_type = ?, currency_code = ?, 
                future_cost = ?, monthly_saving = ?, status = ?,
                updated_at = datetime('now')
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getItemName());
            ps.setDouble(2, goal.getCurrentPrice());
            ps.setInt(3, goal.getTargetYear());
            ps.setDouble(4, goal.getInflationRate());
            ps.setString(5, goal.getTermType());
            ps.setString(6, goal.getCurrencyCode());
            ps.setDouble(7, goal.getFutureCost());
            ps.setDouble(8, goal.getMonthlySaving());
            ps.setString(9, goal.getStatus());
            ps.setInt(10, goal.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates the status of a goal (e.g., ACTIVE, COMPLETED, or ARCHIVED).
     */
    public boolean updateStatus(int goalId, String status) throws SQLException {
        String sql = "UPDATE goals SET status = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, goalId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Permanently deletes a goal by ID, cascading removal to any related contributions.
     */
    public boolean delete(int goalId) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goalId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds and returns a goal by ID, or null if not found.
     */
    public Goal findById(int id) throws SQLException {
        String sql = "SELECT * FROM goals WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToGoal(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all goals owned by a specific user.
     */
    public List<Goal> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY target_year ASC, id DESC";
        List<Goal> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToGoal(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all goals for a user filtered by status (e.g., ACTIVE, COMPLETED, ARCHIVED).
     */
    public List<Goal> findByUserIdAndStatus(int userId, String status) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ? AND status = ? ORDER BY target_year ASC, id DESC";
        List<Goal> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToGoal(rs));
                }
            }
        }
        return list;
    }

    /**
     * Maps an active ResultSet row to a populated Goal instance.
     */
    private Goal mapRowToGoal(ResultSet rs) throws SQLException {
        return new Goal(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("item_name"),
            rs.getDouble("current_price"),
            rs.getInt("target_year"),
            rs.getDouble("inflation_rate"),
            rs.getString("term_type"),
            rs.getString("currency_code"),
            rs.getDouble("future_cost"),
            rs.getDouble("monthly_saving"),
            rs.getString("status"),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
}

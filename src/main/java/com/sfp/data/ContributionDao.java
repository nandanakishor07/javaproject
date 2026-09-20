package com.sfp.data;

import com.sfp.model.Contribution;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object managing persistence operations for Contribution records.
 */
public class ContributionDao {
    private final DatabaseManager dbManager;

    /**
     * Constructs a ContributionDao backed by the given DatabaseManager.
     */
    public ContributionDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new contribution towards a goal and returns the generated primary key.
     */
    public int insert(Contribution contribution) throws SQLException {
        String sql = """
            INSERT INTO contributions (goal_id, amount, contribution_date, note)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, contribution.getGoalId());
            ps.setDouble(2, contribution.getAmount());
            ps.setString(3, contribution.getContributionDate());
            ps.setString(4, contribution.getNote());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    contribution.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    /**
     * Deletes an individual contribution record by its primary key.
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM contributions WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all contributions for a given goal ordered newest first.
     */
    public List<Contribution> findByGoalId(int goalId) throws SQLException {
        String sql = "SELECT * FROM contributions WHERE goal_id = ? ORDER BY contribution_date DESC, id DESC";
        List<Contribution> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToContribution(rs));
                }
            }
        }
        return list;
    }

    /**
     * Computes the cumulative total amount saved for a specific goal.
     */
    public double getTotalContributed(int goalId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM contributions WHERE goal_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, goalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * Maps an active ResultSet row to a populated Contribution instance.
     */
    private Contribution mapRowToContribution(ResultSet rs) throws SQLException {
        return new Contribution(
            rs.getInt("id"),
            rs.getInt("goal_id"),
            rs.getDouble("amount"),
            rs.getString("contribution_date"),
            rs.getString("note"),
            rs.getString("created_at")
        );
    }
}

package com.sfp.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connections, ensures directory creation, and initializes database schema.
 */
public class DatabaseManager {
    private static final String DEFAULT_DB_PATH = "data/smart_finance_planner.db";
    private static final String DEFAULT_JDBC_URL = "jdbc:sqlite:" + DEFAULT_DB_PATH;
    private final String jdbcUrl;

    private final boolean isInMemory;
    private Connection inMemoryConnection;

    /**
     * Initializes DatabaseManager with default production SQLite file database.
     */
    public DatabaseManager() {
        this(DEFAULT_JDBC_URL);
    }

    /**
     * Initializes DatabaseManager with a custom JDBC URL (e.g., in-memory for testing).
     */
    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        this.isInMemory = jdbcUrl.contains(":memory:");
    }

    /**
     * Opens and returns a database connection with foreign key constraints enabled.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (isInMemory) {
            if (inMemoryConnection == null || inMemoryConnection.isClosed()) {
                inMemoryConnection = DriverManager.getConnection(jdbcUrl);
                enableForeignKeys(inMemoryConnection);
            }
            return createInMemoryProxy(inMemoryConnection);
        }
        ensureDataDirectoryExists();
        Connection connection = DriverManager.getConnection(jdbcUrl);
        enableForeignKeys(connection);
        return connection;
    }

    /**
     * Closes the active in-memory connection if one is maintained.
     */
    public synchronized void close() throws SQLException {
        if (inMemoryConnection != null && !inMemoryConnection.isClosed()) {
            inMemoryConnection.close();
            inMemoryConnection = null;
        }
    }

    /**
     * Wraps in-memory connection to suppress close() during DAO try-with-resources blocks.
     */
    private Connection createInMemoryProxy(Connection target) {
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    return null;
                }
                return method.invoke(target, args);
            }
        );
    }

    /**
     * Creates an in-memory SQLite connection for isolated unit/DAO tests.
     */
    public static DatabaseManager createInMemoryManager() {
        return new DatabaseManager("jdbc:sqlite::memory:");
    }

    /**
     * Creates the parent data directory if using the local file database.
     */
    private void ensureDataDirectoryExists() {
        if (jdbcUrl.startsWith("jdbc:sqlite:") && !jdbcUrl.contains(":memory:")) {
            String path = jdbcUrl.substring("jdbc:sqlite:".length());
            File dbFile = new File(path);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
    }

    /**
     * Explicitly enforces foreign key constraint enforcement in SQLite.
     */
    private void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    /**
     * Initializes tables, constraints, and indices if they do not already exist.
     */
    public void initSchema() throws SQLException {
        try (Connection conn = getConnection()) {
            initSchema(conn);
        }
    }

    /**
     * Executes DDL schema creation on the specified connection.
     */
    public static void initSchema(Connection conn) throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                name              TEXT NOT NULL,
                monthly_income    REAL NOT NULL CHECK (monthly_income > 0),
                savings_percent   REAL NOT NULL CHECK (savings_percent > 0 AND savings_percent <= 100),
                default_currency  TEXT NOT NULL DEFAULT 'INR',
                theme             TEXT NOT NULL DEFAULT 'LIGHT',
                created_at        TEXT NOT NULL DEFAULT (datetime('now'))
            );
        """;

        String createGoalsTable = """
            CREATE TABLE IF NOT EXISTS goals (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id           INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                item_name         TEXT NOT NULL,
                current_price     REAL NOT NULL CHECK (current_price > 0),
                target_year       INTEGER NOT NULL,
                inflation_rate    REAL NOT NULL CHECK (inflation_rate >= 0 AND inflation_rate <= 50),
                term_type         TEXT NOT NULL CHECK (term_type IN ('SHORT', 'LONG')),
                currency_code     TEXT NOT NULL DEFAULT 'INR',
                future_cost       REAL,
                monthly_saving    REAL,
                status            TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED')),
                created_at        TEXT NOT NULL DEFAULT (datetime('now')),
                updated_at        TEXT NOT NULL DEFAULT (datetime('now'))
            );
        """;

        String createContributionsTable = """
            CREATE TABLE IF NOT EXISTS contributions (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                goal_id           INTEGER NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
                amount            REAL NOT NULL CHECK (amount > 0),
                contribution_date TEXT NOT NULL,
                note              TEXT,
                created_at        TEXT NOT NULL DEFAULT (datetime('now'))
            );
        """;

        String createGoalUserIndex = "CREATE INDEX IF NOT EXISTS idx_goals_user ON goals(user_id);";
        String createContribGoalIndex = "CREATE INDEX IF NOT EXISTS idx_contrib_goal ON contributions(goal_id);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createGoalsTable);
            stmt.execute(createContributionsTable);
            stmt.execute(createGoalUserIndex);
            stmt.execute(createContribGoalIndex);
        }
    }
}

package com.sfp.gui;

import com.sfp.data.ContributionDao;
import com.sfp.data.DatabaseManager;
import com.sfp.data.GoalDao;
import com.sfp.data.UserDao;
import com.sfp.engine.InflationCalculator;
import com.sfp.engine.ProgressTracker;
import com.sfp.engine.SavingsPlanner;
import com.sfp.gui.components.Navbar;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Main application window coordinating navigation, active user session, and screen lifecycle.
 */
public class MainFrame extends JFrame {
    private final DatabaseManager dbManager;
    private final UserDao userDao;
    private final GoalDao goalDao;
    private final ContributionDao contributionDao;

    private final InflationCalculator inflationCalculator;
    private final SavingsPlanner savingsPlanner;
    private final ProgressTracker progressTracker;

    private User currentUser;

    private final Navbar navbar;
    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    private final OnboardingPanel onboardingPanel;
    private final DashboardPanel dashboardPanel;
    private final GoalFormPanel goalFormPanel;
    private final ResultPanel resultPanel;
    private final SavedGoalsPanel savedGoalsPanel;
    private final GoalDetailPanel goalDetailPanel;
    private final SettingsPanel settingsPanel;
    private final ArchivedGoalsPanel archivedGoalsPanel;

    private String currentScreenKey = "DASHBOARD";

    /**
     * Constructs MainFrame and initializes all panels, DAOs, and database dependencies.
     */
    public MainFrame() {
        setTitle("Smart Finance Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        // Core singletons
        dbManager = new DatabaseManager();
        try {
            dbManager.initSchema();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to initialize SQLite database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        userDao = new UserDao(dbManager);
        goalDao = new GoalDao(dbManager);
        contributionDao = new ContributionDao(dbManager);

        inflationCalculator = new InflationCalculator();
        savingsPlanner = new SavingsPlanner();
        progressTracker = new ProgressTracker();

        // Check if user exists
        try {
            currentUser = userDao.findFirst();
        } catch (SQLException e) {
            currentUser = null;
        }

        // Setup CardLayout
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        onboardingPanel = new OnboardingPanel(this, userDao);
        dashboardPanel = new DashboardPanel(this, goalDao, contributionDao, progressTracker);
        goalFormPanel = new GoalFormPanel(this, inflationCalculator, savingsPlanner);
        resultPanel = new ResultPanel(this, goalDao);
        savedGoalsPanel = new SavedGoalsPanel(this, goalDao, contributionDao, progressTracker);
        goalDetailPanel = new GoalDetailPanel(this, goalDao, contributionDao, progressTracker);
        settingsPanel = new SettingsPanel(this, userDao);
        archivedGoalsPanel = new ArchivedGoalsPanel(this, goalDao, contributionDao);

        cardsPanel.add(onboardingPanel, "ONBOARDING");
        cardsPanel.add(dashboardPanel, "DASHBOARD");
        cardsPanel.add(goalFormPanel, "ADD_GOAL");
        cardsPanel.add(resultPanel, "RESULT");
        cardsPanel.add(savedGoalsPanel, "SAVED_GOALS");
        cardsPanel.add(goalDetailPanel, "GOAL_DETAIL");
        cardsPanel.add(settingsPanel, "SETTINGS");
        cardsPanel.add(archivedGoalsPanel, "ARCHIVED_GOALS");

        // Top navigation bar
        navbar = new Navbar(this::navigateTo, this::handleThemeToggle);

        setLayout(new BorderLayout());
        add(navbar, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);

        // Routing: If no user, show onboarding; else dashboard
        if (currentUser == null) {
            navbar.setVisible(false);
            navigateTo("ONBOARDING");
        } else {
            AppTheme.initTheme(currentUser.getTheme());
            navbar.setVisible(true);
            navigateTo("DASHBOARD");
        }
    }

    /**
     * Navigates to a specific screen, refreshes its data, and updates navbar highlights.
     */
    public void navigateTo(String screenKey) {
        this.currentScreenKey = screenKey;
        cardLayout.show(cardsPanel, screenKey);

        if ("ONBOARDING".equals(screenKey)) {
            navbar.setVisible(false);
        } else {
            navbar.setVisible(true);
            navbar.setActiveScreen(screenKey);
        }

        // Trigger refresh on the target screen
        switch (screenKey) {
            case "DASHBOARD" -> dashboardPanel.refresh();
            case "SAVED_GOALS" -> savedGoalsPanel.refresh();
            case "SETTINGS" -> settingsPanel.refresh();
            case "ARCHIVED_GOALS" -> archivedGoalsPanel.refresh();
        }
    }

    /**
     * Prepares and opens the goal form for adding a new goal.
     */
    public void showAddGoalForm() {
        goalFormPanel.prepareForNewGoal();
        navigateTo("ADD_GOAL");
    }

    /**
     * Prepares and opens the goal form in edit mode.
     */
    public void showEditGoalForm(Goal goal) {
        goalFormPanel.prepareForEditGoal(goal);
        navigateTo("ADD_GOAL");
    }

    /**
     * Displays calculation results on the ResultPanel.
     */
    public void showResultScreen(Goal goal, double income, double targetSavingsPercent, 
                                 double percentOfIncome, boolean isFeasible, boolean isEdit) {
        resultPanel.setResultData(goal, income, targetSavingsPercent, percentOfIncome, isFeasible, isEdit);
        navigateTo("RESULT");
    }

    /**
     * Navigates to the GoalDetailPanel for contribution tracking on a specific goal.
     */
    public void showGoalDetail(Goal goal) {
        goalDetailPanel.loadGoal(goal);
        navigateTo("GOAL_DETAIL");
    }

    /**
     * Toggles theme between Light and Dark mode, updates DB, and repaints.
     */
    private void handleThemeToggle() {
        String newTheme = AppTheme.isDarkMode() ? "LIGHT" : "DARK";
        AppTheme.setTheme(newTheme);
        if (currentUser != null) {
            currentUser.setTheme(newTheme);
            try {
                userDao.update(currentUser);
            } catch (SQLException ignored) {
            }
        }
        navbar.updateThemeToggleText();
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Callback when user profile settings are updated.
     */
    public void onUserUpdated(User updatedUser) {
        this.currentUser = updatedUser;
        navbar.updateThemeToggleText();
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Sets the active user reference after onboarding.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        navbar.setVisible(true);
    }

    /**
     * Gets the currently active logged-in user profile.
     */
    public User getCurrentUser() {
        return currentUser;
    }
}

package com.sfp.gui;

import com.sfp.data.ContributionDao;
import com.sfp.data.GoalDao;
import com.sfp.engine.ProgressTracker;
import com.sfp.gui.components.GoalCard;
import com.sfp.gui.components.StatCard;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import com.sfp.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main dashboard screen displaying KPI statistics, urgent goals, and quick-add actions.
 */
public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final GoalDao goalDao;
    private final ContributionDao contributionDao;
    private final ProgressTracker progressTracker;

    private final StatCard activeGoalsCard;
    private final StatCard totalSavedCard;
    private final StatCard nearestDeadlineCard;
    private final JPanel goalsListPanel;
    private final JLabel greetingLabel;

    /**
     * Constructs DashboardPanel.
     */
    public DashboardPanel(MainFrame mainFrame, GoalDao goalDao, 
                          ContributionDao contributionDao, ProgressTracker progressTracker) {
        this.mainFrame = mainFrame;
        this.goalDao = goalDao;
        this.contributionDao = contributionDao;
        this.progressTracker = progressTracker;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        // Header and Greeting
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.setBorder(new EmptyBorder(24, 28, 12, 28));

        greetingLabel = new JLabel("Welcome back!");
        greetingLabel.setFont(AppTheme.FONT_TITLE);
        greetingLabel.setForeground(AppTheme.NAVY_PRIMARY);

        JButton addGoalBtn = AppTheme.createPrimaryButton("+ Add New Goal");
        addGoalBtn.addActionListener(e -> mainFrame.showAddGoalForm());

        topContainer.add(greetingLabel, BorderLayout.WEST);
        topContainer.add(addGoalBtn, BorderLayout.EAST);

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(0, 28, 16, 28));

        activeGoalsCard = new StatCard("ACTIVE GOALS", "0 Goals", "In progress");
        totalSavedCard = new StatCard("TOTAL SAVED", "₹0.00", "Across all active goals");
        nearestDeadlineCard = new StatCard("NEAREST DEADLINE", "None", "Add a goal to set a target");

        statsRow.add(activeGoalsCard);
        statsRow.add(totalSavedCard);
        statsRow.add(nearestDeadlineCard);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(topContainer, BorderLayout.NORTH);
        topWrapper.add(statsRow, BorderLayout.SOUTH);

        add(topWrapper, BorderLayout.NORTH);

        // Content Area: Recent Goals
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 28, 24, 28));

        JLabel sectionTitle = new JLabel("Priority & Upcoming Goals");
        sectionTitle.setFont(AppTheme.FONT_SECTION);
        sectionTitle.setForeground(AppTheme.TEXT_MUTED);
        contentPanel.add(sectionTitle, BorderLayout.NORTH);

        goalsListPanel = new JPanel();
        goalsListPanel.setLayout(new BoxLayout(goalsListPanel, BoxLayout.Y_AXIS));
        goalsListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(goalsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Refreshes dashboard data asynchronously without blocking the Event Dispatch Thread.
     */
    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user == null) {
            return;
        }

        greetingLabel.setText("Welcome back, " + user.getName() + " 👋");

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                List<Goal> activeGoals = goalDao.findByUserIdAndStatus(user.getId(), "ACTIVE");
                Map<Integer, Double> contributionMap = new HashMap<>();
                Map<String, Double> totalSavedByCurrency = new HashMap<>();

                for (Goal goal : activeGoals) {
                    double saved = contributionDao.getTotalContributed(goal.getId());
                    contributionMap.put(goal.getId(), saved);
                    totalSavedByCurrency.put(
                        goal.getCurrencyCode(),
                        totalSavedByCurrency.getOrDefault(goal.getCurrencyCode(), 0.0) + saved
                    );
                }

                // Sort upcoming by targetYear ascending
                activeGoals.sort(Comparator.comparingInt(Goal::getTargetYear));
                return new DashboardData(activeGoals, contributionMap, totalSavedByCurrency);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateUIWithData(data);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this, 
                        "Error loading dashboard: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Populates KPI cards and urgent goals list on the EDT.
     */
    private void updateUIWithData(DashboardData data) {
        activeGoalsCard.setValue(data.activeGoals.size() + " Goals");

        // Format saved totals grouped by currency
        if (data.totalSavedByCurrency.isEmpty()) {
            User user = mainFrame.getCurrentUser();
            String defaultCurr = user != null ? user.getDefaultCurrency() : "INR";
            totalSavedCard.setValue(CurrencyFormatter.format(0.0, defaultCurr));
        } else {
            StringBuilder sb = new StringBuilder();
            data.totalSavedByCurrency.forEach((curr, amount) -> {
                if (sb.length() > 0) sb.append("  |  ");
                sb.append(CurrencyFormatter.format(amount, curr));
            });
            totalSavedCard.setValue(sb.toString());
        }

        // Nearest deadline
        if (!data.activeGoals.isEmpty()) {
            Goal nearest = data.activeGoals.get(0);
            nearestDeadlineCard.setValue(nearest.getTargetYear() + " (" + nearest.getItemName() + ")");
            nearestDeadlineCard.setSubtitle(nearest.getTermType() + "-term plan");
        } else {
            nearestDeadlineCard.setValue("None");
            nearestDeadlineCard.setSubtitle("Plan a new purchase goal");
        }

        // Populate recent goals
        goalsListPanel.removeAll();
        if (data.activeGoals.isEmpty()) {
            goalsListPanel.add(createEmptyStatePanel());
        } else {
            int limit = Math.min(5, data.activeGoals.size());
            for (int i = 0; i < limit; i++) {
                Goal goal = data.activeGoals.get(i);
                double saved = data.contributionMap.getOrDefault(goal.getId(), 0.0);
                boolean onTrack = progressTracker.isOnTrack(goal, saved);

                GoalCard card = new GoalCard(
                    goal, saved, onTrack,
                    g -> mainFrame.showGoalDetail(g),
                    g -> mainFrame.showEditGoalForm(g),
                    g -> handleArchiveGoal(g)
                );
                goalsListPanel.add(card);
                goalsListPanel.add(Box.createVerticalStrut(12));
            }
        }

        goalsListPanel.revalidate();
        goalsListPanel.repaint();
    }

    /**
     * Archives a goal from the dashboard.
     */
    private void handleArchiveGoal(Goal goal) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to archive '" + goal.getItemName() + "'?\nYou can view or restore it under Archived Goals.",
            "Archive Goal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                goalDao.updateStatus(goal.getId(), "ARCHIVED");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to archive goal: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Friendly empty state card shown when no goals have been planned yet.
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        AppTheme.applyCardStyle(panel);
        panel.setMaximumSize(new Dimension(800, 200));

        JLabel icon = new JLabel("🚀");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("You haven't added any goals yet!");
        msg.setFont(AppTheme.FONT_SUBTITLE);
        msg.setForeground(AppTheme.getTextPrimary());
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Plan your first purchase goal with inflation adjustment and smart saving targets.");
        sub.setFont(AppTheme.FONT_BODY);
        sub.setForeground(AppTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton actionBtn = AppTheme.createPrimaryButton("+ Plan Your First Goal");
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionBtn.addActionListener(e -> mainFrame.showAddGoalForm());

        panel.add(Box.createVerticalStrut(10));
        panel.add(icon);
        panel.add(Box.createVerticalStrut(8));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(14));
        panel.add(actionBtn);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    /**
     * Internal data container for background dashboard loading.
     */
    private static class DashboardData {
        final List<Goal> activeGoals;
        final Map<Integer, Double> contributionMap;
        final Map<String, Double> totalSavedByCurrency;

        DashboardData(List<Goal> activeGoals, Map<Integer, Double> contributionMap, Map<String, Double> totalSavedByCurrency) {
            this.activeGoals = activeGoals;
            this.contributionMap = contributionMap;
            this.totalSavedByCurrency = totalSavedByCurrency;
        }
    }
}

package com.sfp.gui;

import com.sfp.data.ContributionDao;
import com.sfp.data.GoalDao;
import com.sfp.engine.ProgressTracker;
import com.sfp.gui.components.GoalCard;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Screen displaying all saved goals with real-time search, sorting, status filtering, and action handling.
 */
public class SavedGoalsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final GoalDao goalDao;
    private final ContributionDao contributionDao;
    private final ProgressTracker progressTracker;

    private final JTextField searchField;
    private final JComboBox<String> sortCombo;
    private final JComboBox<String> filterCombo;
    private final JPanel goalsContainer;

    private List<Goal> loadedGoals = new ArrayList<>();
    private Map<Integer, Double> contributionMap = new HashMap<>();

    /**
     * Constructs SavedGoalsPanel.
     */
    public SavedGoalsPanel(MainFrame mainFrame, GoalDao goalDao, 
                           ContributionDao contributionDao, ProgressTracker progressTracker) {
        this.mainFrame = mainFrame;
        this.goalDao = goalDao;
        this.contributionDao = contributionDao;
        this.progressTracker = progressTracker;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        // Header & Controls Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(14, 10));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(24, 28, 16, 28));

        JLabel titleLabel = new JLabel("Saved Financial Goals");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);

        JButton addBtn = AppTheme.createPrimaryButton("+ Add New Goal");
        addBtn.addActionListener(e -> mainFrame.showAddGoalForm());

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(addBtn, BorderLayout.EAST);

        // Filter & Search Controls
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsRow.setOpaque(false);

        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search by item name...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFiltersAndSort(); }
            public void removeUpdate(DocumentEvent e) { applyFiltersAndSort(); }
            public void changedUpdate(DocumentEvent e) { applyFiltersAndSort(); }
        });

        filterCombo = new JComboBox<>(new String[]{"Active Goals", "Completed Goals", "All Goals", "Archived Goals"});
        filterCombo.addActionListener(e -> applyFiltersAndSort());

        sortCombo = new JComboBox<>(new String[]{
            "Target Year (Earliest)", 
            "Progress % (Highest)", 
            "Future Cost (Highest)", 
            "Item Name (A-Z)"
        });
        sortCombo.addActionListener(e -> applyFiltersAndSort());

        controlsRow.add(new JLabel("Search:"));
        controlsRow.add(searchField);
        controlsRow.add(new JLabel("Status:"));
        controlsRow.add(filterCombo);
        controlsRow.add(new JLabel("Sort by:"));
        controlsRow.add(sortCombo);

        toolbar.add(titleRow, BorderLayout.NORTH);
        toolbar.add(controlsRow, BorderLayout.SOUTH);
        add(toolbar, BorderLayout.NORTH);

        // Goals List Scrollable Area
        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setOpaque(false);
        goalsContainer.setBorder(new EmptyBorder(0, 28, 24, 28));

        JScrollPane scrollPane = new JScrollPane(goalsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes goals from the database asynchronously.
     */
    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user == null) {
            return;
        }

        new SwingWorker<Map.Entry<List<Goal>, Map<Integer, Double>>, Void>() {
            @Override
            protected Map.Entry<List<Goal>, Map<Integer, Double>> doInBackground() throws Exception {
                List<Goal> goals = goalDao.findByUserId(user.getId());
                Map<Integer, Double> map = new HashMap<>();
                for (Goal g : goals) {
                    double saved = contributionDao.getTotalContributed(g.getId());
                    map.put(g.getId(), saved);

                    // Auto-update to COMPLETED if fully funded and still ACTIVE
                    if (saved >= g.getFutureCost() && "ACTIVE".equalsIgnoreCase(g.getStatus())) {
                        g.setStatus("COMPLETED");
                        goalDao.updateStatus(g.getId(), "COMPLETED");
                    }
                }
                return Map.entry(goals, map);
            }

            @Override
            protected void done() {
                try {
                    Map.Entry<List<Goal>, Map<Integer, Double>> result = get();
                    loadedGoals = result.getKey();
                    contributionMap = result.getValue();
                    applyFiltersAndSort();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SavedGoalsPanel.this, 
                        "Failed to load goals: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Filters and sorts loaded goals based on user-selected criteria.
     */
    private void applyFiltersAndSort() {
        if (loadedGoals == null) {
            return;
        }

        String query = searchField.getText().trim().toLowerCase();
        String selectedFilter = (String) filterCombo.getSelectedItem();
        String selectedSort = (String) sortCombo.getSelectedItem();

        // Filter
        List<Goal> filtered = loadedGoals.stream().filter(g -> {
            boolean matchesSearch = query.isEmpty() || g.getItemName().toLowerCase().contains(query);
            if (!matchesSearch) return false;

            if ("Active Goals".equals(selectedFilter)) {
                return "ACTIVE".equalsIgnoreCase(g.getStatus());
            } else if ("Completed Goals".equals(selectedFilter)) {
                return "COMPLETED".equalsIgnoreCase(g.getStatus());
            } else if ("Archived Goals".equals(selectedFilter)) {
                return "ARCHIVED".equalsIgnoreCase(g.getStatus());
            }
            return true; // All Goals
        }).collect(Collectors.toList());

        // Sort
        if ("Target Year (Earliest)".equals(selectedSort)) {
            filtered.sort(Comparator.comparingInt(Goal::getTargetYear));
        } else if ("Progress % (Highest)".equals(selectedSort)) {
            filtered.sort((g1, g2) -> {
                double p1 = progressTracker.getProgressPercent(g1, contributionMap.getOrDefault(g1.getId(), 0.0));
                double p2 = progressTracker.getProgressPercent(g2, contributionMap.getOrDefault(g2.getId(), 0.0));
                return Double.compare(p2, p1);
            });
        } else if ("Future Cost (Highest)".equals(selectedSort)) {
            filtered.sort((g1, g2) -> Double.compare(g2.getFutureCost(), g1.getFutureCost()));
        } else if ("Item Name (A-Z)".equals(selectedSort)) {
            filtered.sort(Comparator.comparing(Goal::getItemName, String.CASE_INSENSITIVE_ORDER));
        }

        // Render Cards
        goalsContainer.removeAll();
        if (filtered.isEmpty()) {
            goalsContainer.add(createEmptyFilterState());
        } else {
            for (Goal goal : filtered) {
                double saved = contributionMap.getOrDefault(goal.getId(), 0.0);
                boolean onTrack = progressTracker.isOnTrack(goal, saved);

                GoalCard card = new GoalCard(
                    goal, saved, onTrack,
                    g -> mainFrame.showGoalDetail(g),
                    g -> mainFrame.showEditGoalForm(g),
                    g -> handleArchiveGoal(g)
                );
                goalsContainer.add(card);
                goalsContainer.add(Box.createVerticalStrut(12));
            }
        }

        goalsContainer.revalidate();
        goalsContainer.repaint();
    }

    /**
     * Handles archiving a goal.
     */
    private void handleArchiveGoal(Goal goal) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Archive goal '" + goal.getItemName() + "'?\nIt will be moved to the Archived Goals tab.",
            "Confirm Archive", JOptionPane.YES_NO_OPTION);
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
     * Renders a friendly empty message when no search or filter results match.
     */
    private JPanel createEmptyFilterState() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        AppTheme.applyCardStyle(p);
        p.setMaximumSize(new Dimension(800, 160));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("No matching goals found");
        msg.setFont(AppTheme.FONT_SUBTITLE);
        msg.setForeground(AppTheme.getTextPrimary());
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Try adjusting your search keywords or switching status filters.");
        sub.setFont(AppTheme.FONT_BODY);
        sub.setForeground(AppTheme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalStrut(10));
        p.add(icon);
        p.add(Box.createVerticalStrut(6));
        p.add(msg);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        p.add(Box.createVerticalStrut(10));
        return p;
    }
}

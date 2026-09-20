package com.sfp.gui;

import com.sfp.data.ContributionDao;
import com.sfp.data.GoalDao;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import com.sfp.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Screen displaying archived goals with options to restore or permanently delete them.
 */
public class ArchivedGoalsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final GoalDao goalDao;
    private final ContributionDao contributionDao;

    private final JPanel listContainer;

    /**
     * Constructs ArchivedGoalsPanel.
     */
    public ArchivedGoalsPanel(MainFrame mainFrame, GoalDao goalDao, ContributionDao contributionDao) {
        this.mainFrame = mainFrame;
        this.goalDao = goalDao;
        this.contributionDao = contributionDao;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        // Header Toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(24, 28, 16, 28));

        JLabel titleLabel = new JLabel("Archived Goals");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);

        JLabel subtitle = new JLabel("Goals moved here are hidden from the active dashboard. You can restore or permanently delete them.");
        subtitle.setFont(AppTheme.FONT_BODY);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        headerPanel.add(titles, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // List Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBorder(new EmptyBorder(0, 28, 24, 28));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes archived goals from SQLite asynchronously.
     */
    public void refresh() {
        User user = mainFrame.getCurrentUser();
        if (user == null) {
            return;
        }

        new SwingWorker<List<Goal>, Void>() {
            @Override
            protected List<Goal> doInBackground() throws Exception {
                return goalDao.findByUserIdAndStatus(user.getId(), "ARCHIVED");
            }

            @Override
            protected void done() {
                try {
                    List<Goal> archived = get();
                    populateList(archived);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ArchivedGoalsPanel.this, 
                        "Failed to load archived goals: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Renders archived goal cards.
     */
    private void populateList(List<Goal> archived) {
        listContainer.removeAll();

        if (archived.isEmpty()) {
            listContainer.add(createEmptyState());
        } else {
            for (Goal goal : archived) {
                listContainer.add(createArchivedCard(goal));
                listContainer.add(Box.createVerticalStrut(12));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    /**
     * Creates a card with Restore and Delete Permanently actions.
     */
    private JPanel createArchivedCard(Goal goal) {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        AppTheme.applyCardStyle(card);
        card.setMaximumSize(new Dimension(850, 100));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel nameLbl = new JLabel(goal.getItemName());
        nameLbl.setFont(AppTheme.FONT_SUBTITLE);
        nameLbl.setForeground(AppTheme.getTextPrimary());

        String cost = CurrencyFormatter.format(goal.getFutureCost(), goal.getCurrencyCode());
        JLabel detailsLbl = new JLabel("Target: " + goal.getTargetYear() + "  |  Projected Cost: " + cost + "  |  Status: ARCHIVED");
        detailsLbl.setFont(AppTheme.FONT_BODY);
        detailsLbl.setForeground(AppTheme.TEXT_MUTED);

        left.add(nameLbl);
        left.add(detailsLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton restoreBtn = AppTheme.createPrimaryButton("Restore to Active");
        restoreBtn.addActionListener(e -> handleRestore(goal));

        JButton deleteBtn = AppTheme.createSecondaryButton("Delete Permanently");
        deleteBtn.setForeground(AppTheme.RED_ERROR);
        deleteBtn.addActionListener(e -> handleDeletePermanently(goal));

        right.add(restoreBtn);
        right.add(deleteBtn);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    /**
     * Restores an archived goal back to ACTIVE status.
     */
    private void handleRestore(Goal goal) {
        try {
            goalDao.updateStatus(goal.getId(), "ACTIVE");
            refresh();
            JOptionPane.showMessageDialog(this, "Goal '" + goal.getItemName() + "' restored to active goals!", "Restored", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to restore: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Permanently purges a goal and its contributions.
     */
    private void handleDeletePermanently(Goal goal) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete '" + goal.getItemName() + "'?\nThis action cannot be undone and deletes all contribution records.",
            "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                goalDao.delete(goal.getId());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createEmptyState() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        AppTheme.applyCardStyle(p);
        p.setMaximumSize(new Dimension(800, 160));

        JLabel icon = new JLabel("📦");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("No archived goals");
        msg.setFont(AppTheme.FONT_SUBTITLE);
        msg.setForeground(AppTheme.getTextPrimary());
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Goals you archive from your active dashboard will appear here safely.");
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

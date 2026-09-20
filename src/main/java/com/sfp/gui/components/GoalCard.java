package com.sfp.gui.components;

import com.sfp.model.Goal;
import com.sfp.util.AppTheme;
import com.sfp.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern goal card displaying financial goals with progress bars, status badges, and action buttons.
 */
public class GoalCard extends JPanel {

    /**
     * Constructs a GoalCard with progress metrics and action callbacks.
     */
    public GoalCard(Goal goal, double totalContributed, boolean isOnTrack, 
                    Consumer<Goal> onViewDetails, Consumer<Goal> onEdit, Consumer<Goal> onArchive) {
        setLayout(new BorderLayout(12, 10));
        AppTheme.applyCardStyle(this);

        // Header: Item Name, Term Badge, and Status Badge
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(goal.getItemName());
        titleLabel.setFont(AppTheme.FONT_SUBTITLE);
        titleLabel.setForeground(AppTheme.getTextPrimary());

        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgesPanel.setOpaque(false);

        JLabel termBadge = createBadge(goal.getTermType() + "-TERM", new Color(224, 231, 255), AppTheme.NAVY_PRIMARY);
        badgesPanel.add(termBadge);

        if ("COMPLETED".equalsIgnoreCase(goal.getStatus())) {
            badgesPanel.add(createBadge("✓ COMPLETED", AppTheme.GREEN_LIGHT, AppTheme.GREEN_ACCENT));
        } else if ("ARCHIVED".equalsIgnoreCase(goal.getStatus())) {
            badgesPanel.add(createBadge("ARCHIVED", new Color(243, 244, 246), AppTheme.TEXT_MUTED));
        } else {
            if (isOnTrack) {
                badgesPanel.add(createBadge("✓ ON TRACK", AppTheme.GREEN_LIGHT, AppTheme.GREEN_ACCENT));
            } else {
                badgesPanel.add(createBadge("⚠ BEHIND", AppTheme.AMBER_LIGHT, AppTheme.AMBER_WARNING));
            }
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badgesPanel, BorderLayout.EAST);

        // Center: Financial numbers & Progress bar
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        centerPanel.setOpaque(false);

        String costText = CurrencyFormatter.format(goal.getFutureCost(), goal.getCurrencyCode());
        String savingText = CurrencyFormatter.format(goal.getMonthlySaving(), goal.getCurrencyCode()) + " / month";
        JLabel targetLabel = new JLabel("Target: " + goal.getTargetYear() + "  |  Future Cost: " + costText + "  (" + savingText + ")");
        targetLabel.setFont(AppTheme.FONT_BODY);
        targetLabel.setForeground(AppTheme.TEXT_MUTED);

        double progressPercent = goal.getFutureCost() > 0 ? (totalContributed / goal.getFutureCost()) * 100.0 : 0.0;
        CustomProgressBar progressBar = new CustomProgressBar(progressPercent);
        if ("COMPLETED".equalsIgnoreCase(goal.getStatus()) || progressPercent >= 100.0) {
            progressBar.setFillColor(AppTheme.GREEN_ACCENT);
        } else if (!isOnTrack) {
            progressBar.setFillColor(AppTheme.AMBER_WARNING);
        }

        String savedText = CurrencyFormatter.format(totalContributed, goal.getCurrencyCode());
        JLabel progressTextLabel = new JLabel(savedText + " of " + costText + " saved (" + String.format("%.1f", progressPercent) + "%)");
        progressTextLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        progressTextLabel.setForeground(AppTheme.getTextPrimary());

        centerPanel.add(targetLabel);
        centerPanel.add(progressBar);
        centerPanel.add(progressTextLabel);

        // Footer: Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerPanel.setOpaque(false);

        if (onViewDetails != null) {
            JButton viewBtn = AppTheme.createPrimaryButton("View Details");
            viewBtn.addActionListener(e -> onViewDetails.accept(goal));
            footerPanel.add(viewBtn);
        }

        if (onEdit != null && !"ARCHIVED".equalsIgnoreCase(goal.getStatus())) {
            JButton editBtn = AppTheme.createSecondaryButton("Edit");
            editBtn.addActionListener(e -> onEdit.accept(goal));
            footerPanel.add(editBtn);
        }

        if (onArchive != null && !"ARCHIVED".equalsIgnoreCase(goal.getStatus())) {
            JButton archiveBtn = AppTheme.createSecondaryButton("Archive");
            archiveBtn.addActionListener(e -> onArchive.accept(goal));
            footerPanel.add(archiveBtn);
        }

        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Helper to render a small rounded status chip badge.
     */
    private static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(AppTheme.FONT_SMALL_BOLD);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return badge;
    }
}

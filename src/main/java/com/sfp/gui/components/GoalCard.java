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
public class GoalCard extends AppTheme.CardPanel {

    /**
     * Constructs a GoalCard with progress metrics and action callbacks.
     */
    public GoalCard(Goal goal, double totalContributed, boolean isOnTrack, 
                    Consumer<Goal> onViewDetails, Consumer<Goal> onEdit, Consumer<Goal> onArchive) {
        setLayout(new BorderLayout(12, 10));
        AppTheme.applyCardStyle(this);
        setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.EmptyBorder(4, 4, 4, 4),
                new javax.swing.border.EmptyBorder(12, 16, 12, 16)));
        setMaximumSize(new Dimension(AppTheme.CONTENT_MAX_WIDTH, 170));
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header: Item Name, Term Badge, and Status Badge
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(goal.getItemName());
        titleLabel.setFont(AppTheme.FONT_BODY_BOLD);
        titleLabel.setForeground(AppTheme.getTextPrimary());

        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgesPanel.setOpaque(false);

        JLabel termBadge = createBadge(goal.getTermType() + "-TERM",
                AppTheme.getAccentBackground(), AppTheme.getAccentColor());
        badgesPanel.add(termBadge);

        if ("COMPLETED".equalsIgnoreCase(goal.getStatus())) {
            badgesPanel.add(createBadge("COMPLETED", AppTheme.getSuccessBackground(), AppTheme.getSuccessColor()));
        } else if ("ARCHIVED".equalsIgnoreCase(goal.getStatus())) {
            badgesPanel.add(createBadge("ARCHIVED", AppTheme.getBorderColor(), AppTheme.getTextMuted()));
        } else {
            if (isOnTrack) {
                badgesPanel.add(createBadge("ON TRACK", AppTheme.getSuccessBackground(), AppTheme.getSuccessColor()));
            } else {
                badgesPanel.add(createBadge("BEHIND", AppTheme.getWarningBackground(), AppTheme.getWarningColor()));
            }
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badgesPanel, BorderLayout.EAST);

        // Center: Financial numbers & Progress bar
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        String costText = CurrencyFormatter.format(goal.getFutureCost(), goal.getCurrencyCode());
        String savingText = CurrencyFormatter.format(goal.getMonthlySaving(), goal.getCurrencyCode()) + " / month";
        JLabel targetLabel = new JLabel("Target: " + goal.getTargetYear() + "  |  Future Cost: " + costText + "  (" + savingText + ")");
        targetLabel.setFont(AppTheme.FONT_SMALL);
        targetLabel.setForeground(AppTheme.getTextMuted());
        targetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        double progressPercent = goal.getFutureCost() > 0 ? (totalContributed / goal.getFutureCost()) * 100.0 : 0.0;
        CustomProgressBar progressBar = new CustomProgressBar(progressPercent);
        progressBar.setShowLabel(false);
        progressBar.setFillColor(AppTheme.getAccentColor());

        String savedText = CurrencyFormatter.format(totalContributed, goal.getCurrencyCode());
        JLabel progressTextLabel = new JLabel(savedText + " of " + costText + " saved (" + String.format("%.1f", progressPercent) + "%)");
        progressTextLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        progressTextLabel.setForeground(AppTheme.getTextPrimary());

        JPanel progressRow = new JPanel(new BorderLayout(10, 0));
        progressRow.setOpaque(false);
        progressRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel percentLabel = new JLabel(String.format("%.1f%%", progressPercent));
        percentLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        percentLabel.setForeground(AppTheme.getTextMuted());
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(percentLabel, BorderLayout.EAST);

        centerPanel.add(targetLabel);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(progressRow);
        centerPanel.add(Box.createVerticalStrut(2));
        centerPanel.add(progressTextLabel);

        // Footer: Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

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
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        return badge;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (getComponentCount() > 0) {
            revalidate();
            repaint();
        }
    }
}

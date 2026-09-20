package com.sfp.gui;

import com.sfp.data.GoalDao;
import com.sfp.model.Goal;
import com.sfp.util.AppTheme;
import com.sfp.util.CurrencyFormatter;
import com.sfp.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Result screen highlighting the hero calculated numbers, feasibility status, and save actions.
 */
public class ResultPanel extends JPanel {
    private final MainFrame mainFrame;
    private final GoalDao goalDao;

    private Goal currentGoal;
    private boolean isEditMode;

    private final JLabel itemNameHeader;
    private final JLabel futureCostHero;
    private final JLabel monthlySavingHero;
    private final JLabel yearlySavingDetail;
    private final JLabel breakdownLabel;
    private final JPanel feasibilityBanner;
    private final JLabel feasibilityLabel;
    private final JButton saveButton;

    /**
     * Constructs ResultPanel.
     */
    public ResultPanel(MainFrame mainFrame, GoalDao goalDao) {
        this.mainFrame = mainFrame;
        this.goalDao = goalDao;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Center Card (Max width 640px)
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(640, 560));
        AppTheme.applyCardStyle(card);

        // Header
        itemNameHeader = new JLabel("Your Savings Projection");
        itemNameHeader.setFont(AppTheme.FONT_TITLE);
        itemNameHeader.setForeground(AppTheme.NAVY_PRIMARY);
        itemNameHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subHeader = new JLabel("Here is what you need to save to reach your purchase goal.");
        subHeader.setFont(AppTheme.FONT_BODY);
        subHeader.setForeground(AppTheme.TEXT_MUTED);
        subHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hero Numbers Container
        JPanel heroesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        heroesPanel.setOpaque(false);

        JPanel costBox = createHeroBox("ESTIMATED FUTURE COST", futureCostHero = new JLabel("₹0.00"));
        JPanel savingBox = createHeroBox("MONTHLY TARGET SAVING", monthlySavingHero = new JLabel("₹0.00"));
        heroesPanel.add(costBox);
        heroesPanel.add(savingBox);

        yearlySavingDetail = new JLabel("Equivalent to ₹0.00 / year");
        yearlySavingDetail.setFont(AppTheme.FONT_SMALL);
        yearlySavingDetail.setForeground(AppTheme.TEXT_MUTED);
        yearlySavingDetail.setAlignmentX(Component.CENTER_ALIGNMENT);

        breakdownLabel = new JLabel("Based on X% inflation over N years.");
        breakdownLabel.setFont(AppTheme.FONT_BODY);
        breakdownLabel.setForeground(AppTheme.getTextPrimary());
        breakdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Feasibility Banner
        feasibilityBanner = new JPanel(new BorderLayout());
        feasibilityBanner.setMaximumSize(new Dimension(580, 50));
        feasibilityLabel = new JLabel("", SwingConstants.CENTER);
        feasibilityLabel.setFont(AppTheme.FONT_BODY_BOLD);
        feasibilityBanner.add(feasibilityLabel, BorderLayout.CENTER);
        feasibilityBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Action Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonRow.setOpaque(false);

        JButton backBtn = AppTheme.createSecondaryButton("← Edit Inputs / Recalculate");
        backBtn.addActionListener(e -> {
            if (isEditMode) {
                mainFrame.showEditGoalForm(currentGoal);
            } else {
                mainFrame.navigateTo("ADD_GOAL");
            }
        });

        saveButton = AppTheme.createPrimaryButton("Save This Goal ✓");
        saveButton.addActionListener(e -> handleSaveGoal());

        buttonRow.add(backBtn);
        buttonRow.add(saveButton);

        card.add(itemNameHeader);
        card.add(Box.createVerticalStrut(6));
        card.add(subHeader);
        card.add(Box.createVerticalStrut(24));
        card.add(heroesPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(yearlySavingDetail);
        card.add(Box.createVerticalStrut(14));
        card.add(breakdownLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(feasibilityBanner);
        card.add(Box.createVerticalStrut(24));
        card.add(buttonRow);

        container.add(card);
        add(container, BorderLayout.CENTER);
    }

    /**
     * Updates and formats the result view with projection details.
     */
    public void setResultData(Goal goal, double monthlyIncome, double targetSavingsPercent, 
                              double percentOfIncome, boolean isFeasible, boolean isEdit) {
        this.currentGoal = goal;
        this.isEditMode = isEdit;

        itemNameHeader.setText(goal.getItemName() + " — Projection");

        String currencyCode = goal.getCurrencyCode();
        futureCostHero.setText(CurrencyFormatter.format(goal.getFutureCost(), currencyCode));
        monthlySavingHero.setText(CurrencyFormatter.format(goal.getMonthlySaving(), currencyCode));

        int years = Math.max(1, goal.getTargetYear() - DateUtil.currentYear());
        double yearly = goal.getFutureCost() / years;
        yearlySavingDetail.setText("Equivalent to " + CurrencyFormatter.format(yearly, currencyCode) + " per year over " + years + " years");

        breakdownLabel.setText(String.format(
            "Current price: %s  •  Inflation: %.1f%%  •  Timeline: %d years (%s-term)",
            CurrencyFormatter.format(goal.getCurrentPrice(), currencyCode),
            goal.getInflationRate(),
            years,
            goal.getTermType()
        ));

        // Format Feasibility Banner
        if (isFeasible) {
            feasibilityBanner.setBackground(AppTheme.GREEN_LIGHT);
            feasibilityBanner.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.GREEN_ACCENT, 1, true),
                new EmptyBorder(10, 16, 10, 16)
            ));
            feasibilityLabel.setForeground(AppTheme.GREEN_ACCENT);
            feasibilityLabel.setText(String.format(
                "✓ Fits comfortably within your plan (requires %.1f%% of income, target cap is %.1f%%)",
                percentOfIncome, targetSavingsPercent
            ));
        } else {
            feasibilityBanner.setBackground(AppTheme.AMBER_LIGHT);
            feasibilityBanner.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.AMBER_WARNING, 1, true),
                new EmptyBorder(10, 16, 10, 16)
            ));
            feasibilityLabel.setForeground(AppTheme.AMBER_WARNING);
            feasibilityLabel.setText(String.format(
                "⚠ High stretch: requires %.1f%% of income (target cap is %.1f%%) — consider extending target year",
                percentOfIncome, targetSavingsPercent
            ));
        }

        saveButton.setText(isEditMode ? "Save Changes ✓" : "Save This Goal ✓");
    }

    /**
     * Persists the goal into SQLite and navigates to Saved Goals view.
     */
    private void handleSaveGoal() {
        if (currentGoal == null) {
            return;
        }

        try {
            if (isEditMode) {
                goalDao.update(currentGoal);
            } else {
                int id = goalDao.insert(currentGoal);
                currentGoal.setId(id);
            }
            mainFrame.navigateTo("SAVED_GOALS");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save goal: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper to render a hero metric box with navy title and large bold currency value.
     */
    private JPanel createHeroBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        AppTheme.applyCardStyle(box);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_SMALL_BOLD);
        titleLbl.setForeground(AppTheme.TEXT_MUTED);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(AppTheme.FONT_HERO);
        valueLabel.setForeground(AppTheme.NAVY_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titleLbl);
        box.add(Box.createVerticalStrut(8));
        box.add(valueLabel);
        return box;
    }
}

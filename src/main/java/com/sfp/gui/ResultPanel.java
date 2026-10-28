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
        container.setBorder(new EmptyBorder(AppTheme.PAGE_PADDING, AppTheme.PAGE_PADDING,
                AppTheme.PAGE_PADDING, AppTheme.PAGE_PADDING));

        // Center Card (Max width 640px)
        JPanel card = AppTheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(840, 560));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header
        itemNameHeader = new JLabel("Your Savings Projection");
        itemNameHeader.setFont(AppTheme.FONT_TITLE);
        itemNameHeader.setForeground(AppTheme.getHeadingColor());
        itemNameHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemNameHeader.setHorizontalAlignment(SwingConstants.LEFT);
        itemNameHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                itemNameHeader.getPreferredSize().height));

        JLabel subHeader = new JLabel("Here is what you need to save to reach your purchase goal.");
        subHeader.setFont(AppTheme.FONT_BODY);
        subHeader.setForeground(AppTheme.getTextMuted());
        subHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        subHeader.setHorizontalAlignment(SwingConstants.LEFT);
        subHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, subHeader.getPreferredSize().height));

        // Hero Numbers Container
        JPanel heroesPanel = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_16, 0));
        heroesPanel.setOpaque(false);
        heroesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel costBox = createHeroBox("ESTIMATED FUTURE COST", futureCostHero = new JLabel("₹0.00"));
        JPanel savingBox = createHeroBox("MONTHLY TARGET SAVING", monthlySavingHero = new JLabel("₹0.00"));
        heroesPanel.add(costBox);
        heroesPanel.add(savingBox);

        yearlySavingDetail = new JLabel("Equivalent to ₹0.00 / year");
        yearlySavingDetail.setFont(AppTheme.FONT_SMALL);
        yearlySavingDetail.setForeground(AppTheme.getTextMuted());
        yearlySavingDetail.setAlignmentX(Component.LEFT_ALIGNMENT);

        breakdownLabel = new JLabel("Based on X% inflation over N years.");
        breakdownLabel.setFont(AppTheme.FONT_BODY);
        breakdownLabel.setForeground(AppTheme.getTextPrimary());
        breakdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Feasibility Banner
        feasibilityBanner = AppTheme.createRoundedPanel(new BorderLayout());
        feasibilityLabel = new JLabel("", SwingConstants.CENTER);
        feasibilityLabel.setFont(AppTheme.FONT_BODY_BOLD);
        feasibilityBanner.add(feasibilityLabel, BorderLayout.CENTER);
        feasibilityBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Action Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonRow.setOpaque(false);

        JButton backBtn = AppTheme.createSecondaryButton("Edit Inputs / Recalculate");
        backBtn.addActionListener(e -> {
            if (isEditMode) {
                mainFrame.showEditGoalForm(currentGoal);
            } else {
                mainFrame.navigateTo("ADD_GOAL");
            }
        });

        saveButton = AppTheme.createPrimaryButton("Save This Goal");
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
        String yearLabel = years == 1 ? "1 year" : years + " years";
        yearlySavingDetail.setText("Equivalent to " + CurrencyFormatter.format(yearly, currencyCode)
                + " per year over " + yearLabel);

        breakdownLabel.setText(String.format(
            "Current price: %s  |  Inflation: %.1f%%  |  Timeline: %s (%s-term)",
            CurrencyFormatter.format(goal.getCurrentPrice(), currencyCode),
            goal.getInflationRate(),
            yearLabel,
            goal.getTermType()
        ));

        if (isFeasible) {
            feasibilityBanner.setBackground(AppTheme.getSuccessBackground());
            feasibilityBanner.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.getSuccessColor(), 1, true),
                new EmptyBorder(AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12)
            ));
            setFeasibilityMessage(String.format(
                    "Achievable: needs %.1f%% of your income, within your %.1f%% limit.",
                    percentOfIncome, targetSavingsPercent), AppTheme.getSuccessColor());
        } else if (percentOfIncome > 100.0) {
            feasibilityBanner.setBackground(AppTheme.getWarningBackground());
            feasibilityBanner.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.getWarningColor(), 1, true),
                new EmptyBorder(AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12)
            ));
            setFeasibilityMessage(
                    "This goal needs more than your whole monthly income. Extend the target year, "
                            + "lower the price, or increase your income.",
                    AppTheme.getWarningColor());
        } else {
            feasibilityBanner.setBackground(AppTheme.getWarningBackground());
            feasibilityBanner.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.getWarningColor(), 1, true),
                new EmptyBorder(AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12, AppTheme.SPACE_12)
            ));
            setFeasibilityMessage(String.format(
                    "Needs %.1f%% of your income, above your %.1f%% limit. "
                            + "Raise your savings rate to at least %.1f%%, or extend the target year.",
                    percentOfIncome, targetSavingsPercent, percentOfIncome), AppTheme.getWarningColor());
        }

        saveButton.setText(isEditMode ? "Save Changes" : "Save This Goal");
    }

    private void setFeasibilityMessage(String message, Color color) {
        String htmlColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        feasibilityLabel.setText("<html><div style='width:420px; text-align:center; color:"
                + htmlColor + ";'>" + message + "</div></html>");
        Dimension preferredSize = feasibilityBanner.getPreferredSize();
        feasibilityBanner.setMaximumSize(new Dimension(580, preferredSize.height));
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
        JPanel box = AppTheme.createCardPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setPreferredSize(new Dimension(360, 140));
        box.setMinimumSize(new Dimension(0, 140));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_SMALL_BOLD);
        titleLbl.setForeground(AppTheme.getTextMuted());
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(AppTheme.FONT_HERO);
        valueLabel.setForeground(AppTheme.getHeadingColor());
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titleLbl);
        box.add(Box.createVerticalStrut(8));
        box.add(valueLabel);
        return box;
    }
}

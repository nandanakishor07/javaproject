package com.sfp.gui;

import com.sfp.engine.InflationCalculator;
import com.sfp.engine.SavingsPlanner;
import com.sfp.model.Currency;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import com.sfp.util.DateUtil;
import com.sfp.util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Form screen for adding a new goal or editing an existing goal with inline validation and calculations.
 */
public class GoalFormPanel extends JPanel {
    private final MainFrame mainFrame;
    private final InflationCalculator inflationCalculator;
    private final SavingsPlanner savingsPlanner;

    private Goal editingGoal; // null if adding new goal

    private final JLabel headerTitle;
    private final JTextField itemNameField;
    private final JTextField priceField;
    private final JComboBox<Currency> currencyCombo;
    private final JSpinner targetYearSpinner;
    private final JSpinner inflationSpinner;
    private final JRadioButton shortTermRadio;
    private final JRadioButton longTermRadio;
    private final JTextField incomeField;
    private final JSpinner savingsSpinner;

    // Inline error labels
    private final JLabel itemNameError;
    private final JLabel priceError;
    private final JLabel targetYearError;
    private final JLabel inflationError;
    private final JLabel incomeError;
    private final JLabel savingsError;

    /**
     * Constructs GoalFormPanel.
     */
    public GoalFormPanel(MainFrame mainFrame, InflationCalculator inflationCalculator, SavingsPlanner savingsPlanner) {
        this.mainFrame = mainFrame;
        this.inflationCalculator = inflationCalculator;
        this.savingsPlanner = savingsPlanner;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Center card with max width 650px
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(660, 680));
        AppTheme.applyCardStyle(card);

        headerTitle = new JLabel("Plan a New Purchase Goal");
        headerTitle.setFont(AppTheme.FONT_TITLE);
        headerTitle.setForeground(AppTheme.NAVY_PRIMARY);
        headerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your target item details to calculate inflation and savings targets.");
        subtitle.setFont(AppTheme.FONT_BODY);
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(headerTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // Input components
        itemNameField = new JTextField();
        priceField = new JTextField();
        currencyCombo = new JComboBox<>(Currency.values());

        int curYear = DateUtil.currentYear();
        targetYearSpinner = new JSpinner(new SpinnerNumberModel(curYear + 1, curYear + 1, curYear + 50, 1));
        inflationSpinner = new JSpinner(new SpinnerNumberModel(6.0, 0.0, 50.0, 0.5));

        shortTermRadio = new JRadioButton("Short-Term (≤ 3 yrs)");
        longTermRadio = new JRadioButton("Long-Term (> 3 yrs)");
        ButtonGroup termGroup = new ButtonGroup();
        termGroup.add(shortTermRadio);
        termGroup.add(longTermRadio);
        shortTermRadio.setSelected(true);

        // Auto-select term when target year changes
        targetYearSpinner.addChangeListener(e -> {
            int target = (int) targetYearSpinner.getValue();
            int diff = target - DateUtil.currentYear();
            if (diff <= 3) {
                shortTermRadio.setSelected(true);
            } else {
                longTermRadio.setSelected(true);
            }
        });

        incomeField = new JTextField();
        savingsSpinner = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 100.0, 1.0));

        // Error labels
        itemNameError = createErrorLabel();
        priceError = createErrorLabel();
        targetYearError = createErrorLabel();
        inflationError = createErrorLabel();
        incomeError = createErrorLabel();
        savingsError = createErrorLabel();

        // Add fields
        card.add(createFieldWithInlineError("Item / Purchase Name:", itemNameField, itemNameError));
        card.add(Box.createVerticalStrut(10));

        JPanel priceCurrRow = new JPanel(new GridLayout(1, 2, 12, 0));
        priceCurrRow.setOpaque(false);
        priceCurrRow.add(createFieldWithInlineError("Current Price Today:", priceField, priceError));
        priceCurrRow.add(createSimpleField("Currency:", currencyCombo));
        card.add(priceCurrRow);
        card.add(Box.createVerticalStrut(10));

        JPanel yearInfRow = new JPanel(new GridLayout(1, 2, 12, 0));
        yearInfRow.setOpaque(false);
        yearInfRow.add(createFieldWithInlineError("Target Purchase Year:", targetYearSpinner, targetYearError));
        yearInfRow.add(createFieldWithInlineError("Annual Inflation Rate (%):", inflationSpinner, inflationError));
        card.add(yearInfRow);
        card.add(Box.createVerticalStrut(10));

        // Goal Term Toggle
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        termPanel.setOpaque(false);
        termPanel.add(new JLabel("Goal Term:"));
        termPanel.add(shortTermRadio);
        termPanel.add(longTermRadio);
        card.add(termPanel);
        card.add(Box.createVerticalStrut(10));

        // Financial Context Row
        JPanel financeRow = new JPanel(new GridLayout(1, 2, 12, 0));
        financeRow.setOpaque(false);
        financeRow.add(createFieldWithInlineError("Monthly Income:", incomeField, incomeError));
        financeRow.add(createFieldWithInlineError("Target Savings Rate (%):", savingsSpinner, savingsError));
        card.add(financeRow);
        card.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = AppTheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> mainFrame.navigateTo("DASHBOARD"));

        JButton calcBtn = AppTheme.createPrimaryButton("Calculate Savings Plan →");
        calcBtn.addActionListener(e -> handleCalculate());

        buttonRow.add(cancelBtn);
        buttonRow.add(calcBtn);
        card.add(buttonRow);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        container.add(card);
        add(container, BorderLayout.CENTER);
    }

    /**
     * Initializes form fields for adding a brand new goal with sensible defaults.
     */
    public void prepareForNewGoal() {
        this.editingGoal = null;
        headerTitle.setText("Plan a New Purchase Goal");
        clearErrors();

        itemNameField.setText("");
        priceField.setText("");

        User user = mainFrame.getCurrentUser();
        if (user != null) {
            currencyCombo.setSelectedItem(Currency.fromCode(user.getDefaultCurrency()));
            incomeField.setText(String.valueOf(user.getMonthlyIncome()));
            savingsSpinner.setValue(user.getSavingsPercent());
        }

        int nextYear = DateUtil.currentYear() + 1;
        targetYearSpinner.setValue(nextYear);
        inflationSpinner.setValue(6.0);
        shortTermRadio.setSelected(true);
    }

    /**
     * Pre-populates form fields with existing goal attributes for editing.
     */
    public void prepareForEditGoal(Goal goal) {
        this.editingGoal = goal;
        headerTitle.setText("Edit Goal: " + goal.getItemName());
        clearErrors();

        itemNameField.setText(goal.getItemName());
        priceField.setText(String.valueOf(goal.getCurrentPrice()));
        currencyCombo.setSelectedItem(Currency.fromCode(goal.getCurrencyCode()));
        targetYearSpinner.setValue(goal.getTargetYear());
        inflationSpinner.setValue(goal.getInflationRate());

        if ("LONG".equalsIgnoreCase(goal.getTermType())) {
            longTermRadio.setSelected(true);
        } else {
            shortTermRadio.setSelected(true);
        }

        User user = mainFrame.getCurrentUser();
        if (user != null) {
            incomeField.setText(String.valueOf(user.getMonthlyIncome()));
            savingsSpinner.setValue(user.getSavingsPercent());
        }
    }

    /**
     * Validates all inputs using shared Validator, computes projection, and forwards to ResultPanel.
     */
    private void handleCalculate() {
        clearErrors();
        boolean valid = true;

        String name = itemNameField.getText().trim();
        String nameErr = Validator.validateItemName(name);
        if (nameErr != null) {
            itemNameError.setText(nameErr);
            valid = false;
        }

        double price = 0.0;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            String priceErr = Validator.validatePrice(price);
            if (priceErr != null) {
                priceError.setText(priceErr);
                valid = false;
            }
        } catch (NumberFormatException e) {
            priceError.setText("Price must be a valid positive number.");
            valid = false;
        }

        int targetYear = (int) targetYearSpinner.getValue();
        String yearErr = Validator.validateTargetYear(targetYear, DateUtil.currentYear());
        if (yearErr != null) {
            targetYearError.setText(yearErr);
            valid = false;
        }

        double inflation = ((Number) inflationSpinner.getValue()).doubleValue();
        String infErr = Validator.validateInflationRate(inflation);
        if (infErr != null) {
            inflationError.setText(infErr);
            valid = false;
        }

        double income = 0.0;
        try {
            income = Double.parseDouble(incomeField.getText().trim());
            String incErr = Validator.validateMonthlyIncome(income);
            if (incErr != null) {
                incomeError.setText(incErr);
                valid = false;
            }
        } catch (NumberFormatException e) {
            incomeError.setText("Income must be a valid positive number.");
            valid = false;
        }

        double savingsPercent = ((Number) savingsSpinner.getValue()).doubleValue();
        String savErr = Validator.validateSavingsPercent(savingsPercent);
        if (savErr != null) {
            savingsError.setText(savErr);
            valid = false;
        }

        if (!valid) {
            return;
        }

        int years = targetYear - DateUtil.currentYear();
        double rateDecimal = inflation / 100.0;
        double futureCost = inflationCalculator.futureValue(price, rateDecimal, years);
        double monthlySaving = savingsPlanner.monthlySaving(futureCost, years);
        double incomePercent = savingsPlanner.percentOfIncome(monthlySaving, income);
        boolean isFeasible = savingsPlanner.isFeasible(monthlySaving, income, savingsPercent);

        Currency selectedCurrency = (Currency) currencyCombo.getSelectedItem();
        String currencyCode = selectedCurrency != null ? selectedCurrency.getCode() : "INR";
        String termType = shortTermRadio.isSelected() ? "SHORT" : "LONG";

        User user = mainFrame.getCurrentUser();
        int userId = user != null ? user.getId() : 1;

        Goal targetGoal;
        if (editingGoal != null) {
            targetGoal = editingGoal;
            targetGoal.setItemName(name);
            targetGoal.setCurrentPrice(price);
            targetGoal.setTargetYear(targetYear);
            targetGoal.setInflationRate(inflation);
            targetGoal.setTermType(termType);
            targetGoal.setCurrencyCode(currencyCode);
            targetGoal.setFutureCost(futureCost);
            targetGoal.setMonthlySaving(monthlySaving);
        } else {
            targetGoal = new Goal(userId, name, price, targetYear, inflation, termType, currencyCode, futureCost, monthlySaving);
        }

        mainFrame.showResultScreen(targetGoal, income, savingsPercent, incomePercent, isFeasible, editingGoal != null);
    }

    private void clearErrors() {
        itemNameError.setText(" ");
        priceError.setText(" ");
        targetYearError.setText(" ");
        inflationError.setText(" ");
        incomeError.setText(" ");
        savingsError.setText(" ");
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.RED_ERROR);
        return lbl;
    }

    private JPanel createFieldWithInlineError(String labelText, JComponent input, JLabel errorLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_SMALL_BOLD);
        label.setForeground(AppTheme.getTextPrimary());

        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(errorLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSimpleField(String labelText, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_SMALL_BOLD);
        label.setForeground(AppTheme.getTextPrimary());

        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(new JLabel(" "), BorderLayout.SOUTH); // spacing placeholder
        return panel;
    }
}

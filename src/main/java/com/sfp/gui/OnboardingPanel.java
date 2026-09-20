package com.sfp.gui;

import com.sfp.data.UserDao;
import com.sfp.model.Currency;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import com.sfp.util.Validator;

import javax.swing.*;
import java.awt.*;

/**
 * First-run onboarding screen collecting initial user profile, income, savings percent, and currency.
 */
public class OnboardingPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserDao userDao;

    private final JTextField nameField;
    private final JTextField incomeField;
    private final JSpinner savingsSpinner;
    private final JComboBox<Currency> currencyCombo;
    private final JLabel errorLabel;

    /**
     * Constructs the OnboardingPanel.
     */
    public OnboardingPanel(MainFrame mainFrame, UserDao userDao) {
        this.mainFrame = mainFrame;
        this.userDao = userDao;

        setLayout(new GridBagLayout());
        setBackground(AppTheme.getSurfaceBackground());

        // Centered Card Container (Max width ~500px)
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 490));
        AppTheme.applyCardStyle(card);

        // Header
        JLabel iconLabel = new JLabel("🎯");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Welcome to Smart Finance");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Set up your financial profile to start planning goals.");
        subtitleLabel.setFont(AppTheme.FONT_BODY);
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form Fields
        nameField = new JTextField();
        incomeField = new JTextField();
        savingsSpinner = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 100.0, 1.0));
        currencyCombo = new JComboBox<>(Currency.values());

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        errorLabel.setForeground(AppTheme.RED_ERROR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = AppTheme.createPrimaryButton("Get Started →");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> handleGetStarted());

        card.add(Box.createVerticalStrut(10));
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(20));

        card.add(createFieldRow("Your Name:", nameField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Monthly Income:", incomeField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Target Savings Rate (%):", savingsSpinner));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Default Currency:", currencyCombo));
        card.add(Box.createVerticalStrut(16));

        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(startBtn);

        add(card);
    }

    /**
     * Creates a labeled input row with standard padding.
     */
    private JPanel createFieldRow(String labelText, JComponent inputComponent) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(420, 56));

        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_SMALL_BOLD);
        label.setForeground(AppTheme.getTextPrimary());

        row.add(label, BorderLayout.NORTH);
        row.add(inputComponent, BorderLayout.CENTER);
        return row;
    }

    /**
     * Validates onboarding input and persists initial user record.
     */
    private void handleGetStarted() {
        errorLabel.setText(" ");
        String name = nameField.getText().trim();
        String nameError = Validator.validateItemName(name);
        if (nameError != null) {
            errorLabel.setText("Name cannot be empty.");
            return;
        }

        double income;
        try {
            income = Double.parseDouble(incomeField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Monthly income must be a valid positive number.");
            return;
        }

        String incomeError = Validator.validateMonthlyIncome(income);
        if (incomeError != null) {
            errorLabel.setText(incomeError);
            return;
        }

        double savingsPercent = ((Number) savingsSpinner.getValue()).doubleValue();
        String savingsError = Validator.validateSavingsPercent(savingsPercent);
        if (savingsError != null) {
            errorLabel.setText(savingsError);
            return;
        }

        Currency currency = (Currency) currencyCombo.getSelectedItem();
        String currencyCode = currency != null ? currency.getCode() : "INR";

        try {
            User newUser = new User(name, income, savingsPercent, currencyCode, "LIGHT");
            int userId = userDao.insert(newUser);
            newUser.setId(userId);
            mainFrame.setCurrentUser(newUser);
            mainFrame.navigateTo("DASHBOARD");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to create profile: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

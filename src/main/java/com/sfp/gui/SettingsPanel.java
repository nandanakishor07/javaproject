package com.sfp.gui;

import com.sfp.data.UserDao;
import com.sfp.model.Currency;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import com.sfp.util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings screen for updating user financial profile, default currency, and appearance theme.
 */
public class SettingsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserDao userDao;

    private final JTextField nameField;
    private final JTextField incomeField;
    private final JSpinner savingsSpinner;
    private final JComboBox<Currency> currencyCombo;
    private final JComboBox<String> themeCombo;
    private final JLabel errorLabel;

    /**
     * Constructs SettingsPanel.
     */
    public SettingsPanel(MainFrame mainFrame, UserDao userDao) {
        this.mainFrame = mainFrame;
        this.userDao = userDao;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Center card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(560, 520));
        AppTheme.applyCardStyle(card);

        JLabel titleLabel = new JLabel("Application Preferences & Profile");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);

        JLabel subtitle = new JLabel("Adjust your financial baseline and default currency preferences.");
        subtitle.setFont(AppTheme.FONT_BODY);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20));

        nameField = new JTextField();
        incomeField = new JTextField();
        savingsSpinner = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 100.0, 1.0));
        currencyCombo = new JComboBox<>(Currency.values());
        themeCombo = new JComboBox<>(new String[]{"LIGHT", "DARK"});

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        errorLabel.setForeground(AppTheme.RED_ERROR);

        card.add(createFieldRow("Your Name:", nameField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Monthly Income:", incomeField));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Default Savings Rate (%):", savingsSpinner));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Default Currency:", currencyCombo));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldRow("Theme Appearance:", themeCombo));
        card.add(Box.createVerticalStrut(16));

        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        JButton cancelBtn = AppTheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> mainFrame.navigateTo("DASHBOARD"));

        JButton saveBtn = AppTheme.createPrimaryButton("Save Preferences ✓");
        saveBtn.addActionListener(e -> handleSaveSettings());

        buttonRow.add(cancelBtn);
        buttonRow.add(saveBtn);
        card.add(buttonRow);

        container.add(card);
        add(container, BorderLayout.CENTER);
    }

    /**
     * Refreshes fields with active user values.
     */
    public void refresh() {
        errorLabel.setText(" ");
        User user = mainFrame.getCurrentUser();
        if (user != null) {
            nameField.setText(user.getName());
            incomeField.setText(String.valueOf(user.getMonthlyIncome()));
            savingsSpinner.setValue(user.getSavingsPercent());
            currencyCombo.setSelectedItem(Currency.fromCode(user.getDefaultCurrency()));
            themeCombo.setSelectedItem(user.getTheme() != null ? user.getTheme().toUpperCase() : "LIGHT");
        }
    }

    /**
     * Validates and saves updated preferences to the SQLite database.
     */
    private void handleSaveSettings() {
        errorLabel.setText(" ");
        String name = nameField.getText().trim();
        String nameErr = Validator.validateItemName(name);
        if (nameErr != null) {
            errorLabel.setText(nameErr);
            return;
        }

        double income;
        try {
            income = Double.parseDouble(incomeField.getText().trim());
            String incErr = Validator.validateMonthlyIncome(income);
            if (incErr != null) {
                errorLabel.setText(incErr);
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Monthly income must be a valid positive number.");
            return;
        }

        double savingsPercent = ((Number) savingsSpinner.getValue()).doubleValue();
        String savErr = Validator.validateSavingsPercent(savingsPercent);
        if (savErr != null) {
            errorLabel.setText(savErr);
            return;
        }

        Currency currency = (Currency) currencyCombo.getSelectedItem();
        String currencyCode = currency != null ? currency.getCode() : "INR";
        String theme = (String) themeCombo.getSelectedItem();

        User user = mainFrame.getCurrentUser();
        if (user != null) {
            user.setName(name);
            user.setMonthlyIncome(income);
            user.setSavingsPercent(savingsPercent);
            user.setDefaultCurrency(currencyCode);
            user.setTheme(theme);

            try {
                userDao.update(user);
                AppTheme.setTheme(theme);
                mainFrame.onUserUpdated(user);
                JOptionPane.showMessageDialog(this, "Preferences saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.navigateTo("DASHBOARD");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update settings: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createFieldRow(String labelText, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(500, 56));

        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_SMALL_BOLD);
        label.setForeground(AppTheme.getTextPrimary());

        row.add(label, BorderLayout.NORTH);
        row.add(input, BorderLayout.CENTER);
        return row;
    }
}

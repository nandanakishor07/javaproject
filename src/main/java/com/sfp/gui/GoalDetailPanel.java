package com.sfp.gui;

import com.sfp.data.ContributionDao;
import com.sfp.data.GoalDao;
import com.sfp.engine.ProgressTracker;
import com.sfp.gui.components.CustomProgressBar;
import com.sfp.model.Contribution;
import com.sfp.model.Goal;
import com.sfp.util.AppTheme;
import com.sfp.util.CurrencyFormatter;
import com.sfp.util.DateUtil;
import com.sfp.util.Validator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Detailed view for an individual goal supporting contribution logging, progress metrics, and text export.
 */
public class GoalDetailPanel extends JPanel {
    private final MainFrame mainFrame;
    private final GoalDao goalDao;
    private final ContributionDao contributionDao;
    private final ProgressTracker progressTracker;

    private Goal currentGoal;
    private double totalContributed;
    private List<Contribution> contributions;

    private final JLabel titleLabel;
    private final JLabel subDetailsLabel;
    private final JLabel onTrackBadge;
    private final CustomProgressBar progressBar;
    private final JLabel progressSummaryLabel;
    private final JLabel remainingAmountLabel;
    private final JTable historyTable;
    private final DefaultTableModel tableModel;

    /**
     * Constructs GoalDetailPanel.
     */
    public GoalDetailPanel(MainFrame mainFrame, GoalDao goalDao, 
                           ContributionDao contributionDao, ProgressTracker progressTracker) {
        this.mainFrame = mainFrame;
        this.goalDao = goalDao;
        this.contributionDao = contributionDao;
        this.progressTracker = progressTracker;

        setLayout(new BorderLayout());
        setBackground(AppTheme.getSurfaceBackground());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Navigation Bar (Back and Export buttons)
        JPanel navRow = new JPanel(new BorderLayout());
        navRow.setOpaque(false);
        navRow.setMaximumSize(new Dimension(850, 40));

        JButton backBtn = AppTheme.createSecondaryButton("← Back to Goals");
        backBtn.addActionListener(e -> mainFrame.navigateTo("SAVED_GOALS"));

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton exportBtn = AppTheme.createSecondaryButton("📄 Export Summary (.txt)");
        exportBtn.addActionListener(e -> handleExportSummary());

        JButton addContribBtn = AppTheme.createPrimaryButton("+ Log Contribution");
        addContribBtn.addActionListener(e -> showAddContributionDialog());

        actionBtns.add(exportBtn);
        actionBtns.add(addContribBtn);

        navRow.add(backBtn, BorderLayout.WEST);
        navRow.add(actionBtns, BorderLayout.EAST);
        container.add(navRow);
        container.add(Box.createVerticalStrut(16));

        // Goal Overview Card
        JPanel overviewCard = new JPanel();
        overviewCard.setLayout(new BoxLayout(overviewCard, BoxLayout.Y_AXIS));
        overviewCard.setMaximumSize(new Dimension(850, 220));
        AppTheme.applyCardStyle(overviewCard);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        titleLabel = new JLabel("Goal Details");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);

        onTrackBadge = new JLabel(" ✓ ON TRACK ");
        onTrackBadge.setFont(AppTheme.FONT_SMALL_BOLD);
        onTrackBadge.setOpaque(true);
        onTrackBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        headerRow.add(titleLabel, BorderLayout.WEST);
        headerRow.add(onTrackBadge, BorderLayout.EAST);

        subDetailsLabel = new JLabel("Target: 2027  •  Inflation: 6%  •  Term: SHORT");
        subDetailsLabel.setFont(AppTheme.FONT_BODY);
        subDetailsLabel.setForeground(AppTheme.TEXT_MUTED);

        progressBar = new CustomProgressBar(0.0);
        progressBar.setPreferredSize(new Dimension(800, 22));

        JPanel numbersRow = new JPanel(new BorderLayout());
        numbersRow.setOpaque(false);

        progressSummaryLabel = new JLabel("₹0.00 of ₹0.00 saved (0.0%)");
        progressSummaryLabel.setFont(AppTheme.FONT_BODY_BOLD);
        progressSummaryLabel.setForeground(AppTheme.getTextPrimary());

        remainingAmountLabel = new JLabel("Remaining: ₹0.00");
        remainingAmountLabel.setFont(AppTheme.FONT_BODY_BOLD);
        remainingAmountLabel.setForeground(AppTheme.NAVY_PRIMARY);

        numbersRow.add(progressSummaryLabel, BorderLayout.WEST);
        numbersRow.add(remainingAmountLabel, BorderLayout.EAST);

        JLabel noteLabel = new JLabel("Manual savings log — track your progress over time without any bank connection.");
        noteLabel.setFont(AppTheme.FONT_SMALL);
        noteLabel.setForeground(AppTheme.TEXT_MUTED);

        overviewCard.add(headerRow);
        overviewCard.add(Box.createVerticalStrut(4));
        overviewCard.add(subDetailsLabel);
        overviewCard.add(Box.createVerticalStrut(14));
        overviewCard.add(progressBar);
        overviewCard.add(Box.createVerticalStrut(8));
        overviewCard.add(numbersRow);
        overviewCard.add(Box.createVerticalStrut(8));
        overviewCard.add(noteLabel);

        container.add(overviewCard);
        container.add(Box.createVerticalStrut(20));

        // Contribution History Section
        JPanel historyCard = new JPanel(new BorderLayout(0, 10));
        historyCard.setMaximumSize(new Dimension(850, 360));
        AppTheme.applyCardStyle(historyCard);

        JPanel historyHeader = new JPanel(new BorderLayout());
        historyHeader.setOpaque(false);

        JLabel historyTitle = new JLabel("Contribution History");
        historyTitle.setFont(AppTheme.FONT_SECTION);
        historyTitle.setForeground(AppTheme.NAVY_PRIMARY);

        JButton deleteEntryBtn = AppTheme.createSecondaryButton("Delete Selected Entry");
        deleteEntryBtn.addActionListener(e -> handleDeleteSelectedContribution());

        historyHeader.add(historyTitle, BorderLayout.WEST);
        historyHeader.add(deleteEntryBtn, BorderLayout.EAST);

        String[] columns = {"Date", "Amount", "Note / Memo"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(AppTheme.FONT_BODY);
        historyTable.getTableHeader().setFont(AppTheme.FONT_SMALL_BOLD);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(new LineBorder(AppTheme.getBorderColor(), 1));

        historyCard.add(historyHeader, BorderLayout.NORTH);
        historyCard.add(tableScroll, BorderLayout.CENTER);

        container.add(historyCard);

        JScrollPane mainScroll = new JScrollPane(container);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(12);

        add(mainScroll, BorderLayout.CENTER);
    }

    /**
     * Loads a goal into the detail view and refreshes contributions from SQLite.
     */
    public void loadGoal(Goal goal) {
        this.currentGoal = goal;
        refreshData();
    }

    /**
     * Refreshes contribution history and progress metrics asynchronously.
     */
    private void refreshData() {
        if (currentGoal == null) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                contributions = contributionDao.findByGoalId(currentGoal.getId());
                totalContributed = contributionDao.getTotalContributed(currentGoal.getId());

                // Check auto-complete status
                if (totalContributed >= currentGoal.getFutureCost() && "ACTIVE".equalsIgnoreCase(currentGoal.getStatus())) {
                    currentGoal.setStatus("COMPLETED");
                    goalDao.updateStatus(currentGoal.getId(), "COMPLETED");
                }
                return null;
            }

            @Override
            protected void done() {
                updateView();
            }
        }.execute();
    }

    /**
     * Updates UI components on the EDT with current goal and contribution state.
     */
    private void updateView() {
        String curr = currentGoal.getCurrencyCode();
        titleLabel.setText(currentGoal.getItemName() + " (" + currentGoal.getStatus() + ")");

        int years = Math.max(1, currentGoal.getTargetYear() - DateUtil.currentYear());
        subDetailsLabel.setText(String.format(
            "Target Year: %d (%d yrs remaining)  •  Inflation: %.1f%%  •  Required: %s / mo",
            currentGoal.getTargetYear(), years, currentGoal.getInflationRate(),
            CurrencyFormatter.format(currentGoal.getMonthlySaving(), curr)
        ));

        double percent = progressTracker.getProgressPercent(currentGoal, totalContributed);
        progressBar.setProgressPercent(percent);

        boolean onTrack = progressTracker.isOnTrack(currentGoal, totalContributed);
        if ("COMPLETED".equalsIgnoreCase(currentGoal.getStatus()) || percent >= 100.0) {
            onTrackBadge.setText(" ✓ COMPLETED ");
            onTrackBadge.setBackground(AppTheme.GREEN_LIGHT);
            onTrackBadge.setForeground(AppTheme.GREEN_ACCENT);
            progressBar.setFillColor(AppTheme.GREEN_ACCENT);
        } else if (onTrack) {
            onTrackBadge.setText(" ✓ ON TRACK ");
            onTrackBadge.setBackground(AppTheme.GREEN_LIGHT);
            onTrackBadge.setForeground(AppTheme.GREEN_ACCENT);
            progressBar.setFillColor(AppTheme.GREEN_ACCENT);
        } else {
            onTrackBadge.setText(" ⚠ BEHIND SCHEDULE ");
            onTrackBadge.setBackground(AppTheme.AMBER_LIGHT);
            onTrackBadge.setForeground(AppTheme.AMBER_WARNING);
            progressBar.setFillColor(AppTheme.AMBER_WARNING);
        }

        String savedStr = CurrencyFormatter.format(totalContributed, curr);
        String targetStr = CurrencyFormatter.format(currentGoal.getFutureCost(), curr);
        progressSummaryLabel.setText(savedStr + " of " + targetStr + " saved (" + String.format("%.1f", percent) + "%)");

        double remaining = progressTracker.getRemainingAmount(currentGoal, totalContributed);
        remainingAmountLabel.setText("Remaining Needed: " + CurrencyFormatter.format(remaining, curr));

        // Update Table
        tableModel.setRowCount(0);
        if (contributions != null) {
            for (Contribution c : contributions) {
                String formattedDate = DateUtil.formatDisplay(c.getContributionDate());
                String formattedAmount = CurrencyFormatter.format(c.getAmount(), curr);
                String note = c.getNote() != null ? c.getNote() : "";
                tableModel.addRow(new Object[]{formattedDate, formattedAmount, note});
            }
        }
    }

    /**
     * Opens a dialog allowing the user to record a manual savings contribution.
     */
    private void showAddContributionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Log Contribution", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(380, 290);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField(DateUtil.todayIso());
        JTextField noteField = new JTextField();
        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(AppTheme.FONT_SMALL);
        errorLbl.setForeground(AppTheme.RED_ERROR);

        panel.add(createDialogRow("Amount (" + currentGoal.getCurrencyCode() + "):", amountField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createDialogRow("Date (YYYY-MM-DD):", dateField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createDialogRow("Note / Memo (Optional):", noteField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(errorLbl);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelBtn = AppTheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = AppTheme.createPrimaryButton("Save Entry");
        saveBtn.addActionListener(e -> {
            errorLbl.setText(" ");
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                String amtErr = Validator.validateContributionAmount(amount);
                if (amtErr != null) {
                    errorLbl.setText(amtErr);
                    return;
                }
            } catch (NumberFormatException ex) {
                errorLbl.setText("Amount must be a positive number.");
                return;
            }

            String dateStr = dateField.getText().trim();
            String dateErr = Validator.validateContributionDateStr(dateStr);
            if (dateErr != null) {
                errorLbl.setText(dateErr);
                return;
            }

            try {
                Contribution contribution = new Contribution(currentGoal.getId(), amount, dateStr, noteField.getText().trim());
                contributionDao.insert(contribution);
                dialog.dispose();
                refreshData();
            } catch (Exception ex) {
                errorLbl.setText("Database error: " + ex.getMessage());
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Deletes the currently selected contribution entry from the history table.
     */
    private void handleDeleteSelectedContribution() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0 || contributions == null || selectedRow >= contributions.size()) {
            JOptionPane.showMessageDialog(this, "Please select an entry from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Contribution c = contributions.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete contribution of " + CurrencyFormatter.format(c.getAmount(), currentGoal.getCurrencyCode()) + " on " + c.getContributionDate() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contributionDao.delete(c.getId());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exports a comprehensive plain-text summary report (.txt) of the goal and its contributions.
     */
    private void handleExportSummary() {
        if (currentGoal == null) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Goal Summary Report");
        String sanitizedName = currentGoal.getItemName().replaceAll("[^a-zA-Z0-9_-]", "_");
        fileChooser.setSelectedFile(new File("Goal_Summary_" + sanitizedName + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write("=========================================================\n");
                writer.write("          SMART FINANCE PLANNER — GOAL SUMMARY\n");
                writer.write("=========================================================\n\n");
                writer.write("Item Name:            " + currentGoal.getItemName() + "\n");
                writer.write("Status:               " + currentGoal.getStatus() + "\n");
                writer.write("Currency:             " + currentGoal.getCurrencyCode() + "\n");
                writer.write("Current Today Price:  " + CurrencyFormatter.format(currentGoal.getCurrentPrice(), currentGoal.getCurrencyCode()) + "\n");
                writer.write("Target Purchase Year: " + currentGoal.getTargetYear() + "\n");
                writer.write("Inflation Rate:       " + currentGoal.getInflationRate() + "%\n");
                writer.write("Planning Term:        " + currentGoal.getTermType() + "-Term\n");
                writer.write("Projected Cost:       " + CurrencyFormatter.format(currentGoal.getFutureCost(), currentGoal.getCurrencyCode()) + "\n");
                writer.write("Target Monthly Saving:" + CurrencyFormatter.format(currentGoal.getMonthlySaving(), currentGoal.getCurrencyCode()) + "\n\n");

                double percent = progressTracker.getProgressPercent(currentGoal, totalContributed);
                double remaining = progressTracker.getRemainingAmount(currentGoal, totalContributed);
                boolean onTrack = progressTracker.isOnTrack(currentGoal, totalContributed);

                writer.write("PROGRESS STATUS:\n");
                writer.write("---------------------------------------------------------\n");
                writer.write("Total Saved So Far:   " + CurrencyFormatter.format(totalContributed, currentGoal.getCurrencyCode()) + "\n");
                writer.write("Remaining to Save:    " + CurrencyFormatter.format(remaining, currentGoal.getCurrencyCode()) + "\n");
                writer.write(String.format("Completion Progress:  %.1f%%\n", percent));
                writer.write("Pacing Indicator:     " + (onTrack ? "ON TRACK" : "BEHIND SCHEDULE") + "\n\n");

                writer.write("CONTRIBUTION HISTORY:\n");
                writer.write("---------------------------------------------------------\n");
                writer.write(String.format("%-14s | %-16s | %s\n", "Date", "Amount", "Note"));
                writer.write("---------------------------------------------------------\n");
                if (contributions != null && !contributions.isEmpty()) {
                    for (Contribution c : contributions) {
                        writer.write(String.format("%-14s | %-16s | %s\n",
                            c.getContributionDate(),
                            CurrencyFormatter.format(c.getAmount(), currentGoal.getCurrencyCode()),
                            c.getNote() != null ? c.getNote() : ""
                        ));
                    }
                } else {
                    writer.write("No contributions recorded yet.\n");
                }
                writer.write("=========================================================\n");
                writer.write("Generated on: " + DateUtil.todayIso() + " via Smart Finance Planner\n");

                JOptionPane.showMessageDialog(this, 
                    "Summary successfully exported to:\n" + fileToSave.getAbsolutePath(), 
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to export file: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createDialogRow(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_SMALL_BOLD);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }
}

package com.sfp.gui.components;

import com.sfp.util.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Top navigation bar featuring brand title, primary screen links, and theme toggle.
 */
public class Navbar extends JPanel {
    private final Map<String, JButton> navButtons = new HashMap<>();
    private final JButton themeToggleBtn;
    private final Consumer<String> onNavigate;
    private final Runnable onThemeToggle;

    /**
     * Constructs the Navbar with navigation and theme callbacks.
     */
    public Navbar(Consumer<String> onNavigate, Runnable onThemeToggle) {
        this.onNavigate = onNavigate;
        this.onThemeToggle = onThemeToggle;

        setLayout(new BorderLayout());
        setBackground(AppTheme.isDarkMode() ? AppTheme.CARD_BG_DARK : Color.WHITE);
        setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.getBorderColor()),
            new EmptyBorder(10, 20, 10, 20)
        ));

        // Brand section
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("💰");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel titleLabel = new JLabel("Smart Finance Planner");
        titleLabel.setFont(AppTheme.FONT_SUBTITLE);
        titleLabel.setForeground(AppTheme.NAVY_PRIMARY);

        brandPanel.add(logoLabel);
        brandPanel.add(titleLabel);

        // Navigation links
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        linksPanel.setOpaque(false);

        addNavLink(linksPanel, "Dashboard", "DASHBOARD");
        addNavLink(linksPanel, "Saved Goals", "SAVED_GOALS");
        addNavLink(linksPanel, "+ Add Goal", "ADD_GOAL");
        addNavLink(linksPanel, "Archived", "ARCHIVED_GOALS");
        addNavLink(linksPanel, "Settings", "SETTINGS");

        // Theme Toggle Button
        themeToggleBtn = AppTheme.createSecondaryButton(AppTheme.isDarkMode() ? "☀ Light" : "🌙 Dark");
        themeToggleBtn.addActionListener(e -> {
            if (onThemeToggle != null) {
                onThemeToggle.run();
                updateThemeToggleText();
            }
        });
        linksPanel.add(themeToggleBtn);

        add(brandPanel, BorderLayout.WEST);
        add(linksPanel, BorderLayout.EAST);
    }

    /**
     * Adds a navigation button to the links panel.
     */
    private void addNavLink(JPanel panel, String label, String screenKey) {
        JButton btn = new JButton(label);
        btn.setFont(AppTheme.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(AppTheme.TEXT_MUTED);

        btn.addActionListener(e -> {
            if (onNavigate != null) {
                onNavigate.accept(screenKey);
            }
        });

        navButtons.put(screenKey, btn);
        panel.add(btn);
    }

    /**
     * Highlights the active screen's navigation link.
     */
    public void setActiveScreen(String screenKey) {
        navButtons.forEach((key, btn) -> {
            if (key.equals(screenKey)) {
                btn.setForeground(AppTheme.NAVY_PRIMARY);
                btn.setFont(AppTheme.FONT_BODY_BOLD);
            } else {
                btn.setForeground(AppTheme.TEXT_MUTED);
                btn.setFont(AppTheme.FONT_BODY);
            }
        });
    }

    /**
     * Updates the theme toggle button label.
     */
    public void updateThemeToggleText() {
        themeToggleBtn.setText(AppTheme.isDarkMode() ? "☀ Light" : "🌙 Dark");
        setBackground(AppTheme.isDarkMode() ? AppTheme.CARD_BG_DARK : Color.WHITE);
        repaint();
    }
}

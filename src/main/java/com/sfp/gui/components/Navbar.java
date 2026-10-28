package com.sfp.gui.components;

import com.sfp.util.AppTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;
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
        setBackground(AppTheme.getNavbarBackground());
        setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.getBorderColor()),
            new EmptyBorder(10, 20, 10, 20)
        ));

        // Brand section
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("SF", SwingConstants.CENTER);
        logoLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setOpaque(true);
        logoLabel.setBackground(AppTheme.getPrimaryColor());
        logoLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel titleLabel = new JLabel("Smart Finance Planner");
        titleLabel.setFont(AppTheme.FONT_SUBTITLE);
        titleLabel.setForeground(AppTheme.getHeadingColor());

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
        themeToggleBtn = AppTheme.createSecondaryButton(AppTheme.isDarkMode() ? "Light" : "Dark");
        themeToggleBtn.setFont(AppTheme.FONT_SMALL_BOLD);
        themeToggleBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.getBorderColor(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
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
        AppTheme.styleNavigationButton(btn);

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
                btn.setForeground(AppTheme.getHeadingColor());
                btn.setFont(AppTheme.FONT_BODY_BOLD);
                btn.setBackground(AppTheme.getAccentBackground());
                btn.putClientProperty("sfp.navActive", Boolean.TRUE);
            } else {
                btn.setForeground(AppTheme.getTextMuted());
                btn.setFont(AppTheme.FONT_BODY);
                btn.setBackground(AppTheme.getNavbarBackground());
                btn.putClientProperty("sfp.navActive", Boolean.FALSE);
            }
        });
    }

    /**
     * Updates the theme toggle button label.
     */
    public void updateThemeToggleText() {
        themeToggleBtn.setText(AppTheme.isDarkMode() ? "Light" : "Dark");
        setBackground(AppTheme.getNavbarBackground());
        setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, AppTheme.getBorderColor()),
            new EmptyBorder(10, 20, 10, 20)
        ));
        navButtons.values().forEach(button -> {
            if (Boolean.TRUE.equals(button.getClientProperty("sfp.navActive"))) {
                button.setForeground(AppTheme.getHeadingColor());
                button.setBackground(AppTheme.getAccentBackground());
            } else {
                button.setForeground(AppTheme.getTextMuted());
                button.setBackground(AppTheme.getNavbarBackground());
            }
        });
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        for (int i = 4; i >= 1; i--) {
            g.setColor(new Color(15, 23, 42, AppTheme.isDarkMode() ? 3 * i : 2 * i));
            g.fill(new Rectangle2D.Float(0, getHeight() - i, getWidth(), 1));
        }
        g.dispose();
    }
}

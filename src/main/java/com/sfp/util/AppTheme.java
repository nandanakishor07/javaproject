package com.sfp.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Design system tokens, color constants, typography, and FlatLaf theme management.
 */
public class AppTheme {
    // Brand & Status Colors
    public static final Color NAVY_PRIMARY = Color.decode("#141B4D");
    public static final Color NAVY_HOVER = Color.decode("#1E286D");
    public static final Color GREEN_ACCENT = Color.decode("#2E7D4F");
    public static final Color GREEN_LIGHT = Color.decode("#E8F5E9");
    public static final Color AMBER_WARNING = Color.decode("#C77E1B");
    public static final Color AMBER_LIGHT = Color.decode("#FEF3C7");
    public static final Color RED_ERROR = Color.decode("#C0392B");
    public static final Color RED_LIGHT = Color.decode("#FEE2E2");

    // Surfaces & Backgrounds
    public static final Color SURFACE_LIGHT = Color.decode("#F7F8FA");
    public static final Color SURFACE_DARK = Color.decode("#1B1F2A");
    public static final Color CARD_BG_LIGHT = Color.decode("#FFFFFF");
    public static final Color CARD_BG_DARK = Color.decode("#242A38");

    // Typography Colors
    public static final Color TEXT_PRIMARY_LIGHT = Color.decode("#1A1A1A");
    public static final Color TEXT_PRIMARY_DARK = Color.decode("#EDEDED");
    public static final Color TEXT_MUTED = Color.decode("#6B7280");

    // Borders
    public static final Color CARD_BORDER_LIGHT = Color.decode("#E5E7EB");
    public static final Color CARD_BORDER_DARK = Color.decode("#2C3140");

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    private static boolean isDark = false;

    /**
     * Initializes the default FlatLaf theme based on the stored preference string ("LIGHT" or "DARK").
     */
    public static void initTheme(String themePreference) {
        if ("DARK".equalsIgnoreCase(themePreference)) {
            FlatDarkLaf.setup();
            isDark = true;
        } else {
            FlatLightLaf.setup();
            isDark = false;
        }
    }

    /**
     * Toggles between Light and Dark FlatLaf themes and refreshes UI trees.
     */
    public static void setTheme(String themeName) {
        if ("DARK".equalsIgnoreCase(themeName)) {
            FlatDarkLaf.setup();
            isDark = true;
        } else {
            FlatLightLaf.setup();
            isDark = false;
        }
        FlatLaf.updateUI();
    }

    /**
     * Returns true if dark theme is currently active.
     */
    public static boolean isDarkMode() {
        return isDark;
    }

    /**
     * Returns the appropriate card background color for the active theme.
     */
    public static Color getCardBackground() {
        return isDark ? CARD_BG_DARK : CARD_BG_LIGHT;
    }

    /**
     * Returns the appropriate surface background color for the active theme.
     */
    public static Color getSurfaceBackground() {
        return isDark ? SURFACE_DARK : SURFACE_LIGHT;
    }

    /**
     * Returns the appropriate border color for the active theme.
     */
    public static Color getBorderColor() {
        return isDark ? CARD_BORDER_DARK : CARD_BORDER_LIGHT;
    }

    /**
     * Returns the primary text color for the active theme.
     */
    public static Color getTextPrimary() {
        return isDark ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
    }

    /**
     * Creates a styled primary button with navy fill and white text.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(NAVY_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    /**
     * Creates a styled secondary/cancel button with outlined border.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(getBorderColor(), 1, true),
            BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    /**
     * Applies standard rounded card styling with generous padding.
     */
    public static void applyCardStyle(JPanel panel) {
        panel.setBackground(getCardBackground());
        Border rounded = new LineBorder(getBorderColor(), 1, true);
        Border padding = new EmptyBorder(20, 24, 20, 24);
        panel.setBorder(new CompoundBorder(rounded, padding));
    }
}

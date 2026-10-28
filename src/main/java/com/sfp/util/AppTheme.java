package com.sfp.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Shared design tokens, component styling, and FlatLaf theme management.
 */
public class AppTheme {
    public static final Color SURFACE_LIGHT = Color.decode("#F4F6FB");
    public static final Color CARD_BG_LIGHT = Color.decode("#FFFFFF");
    public static final Color CARD_BORDER_LIGHT = Color.decode("#E3E7EF");
    public static final Color TEXT_PRIMARY_LIGHT = Color.decode("#111827");
    public static final Color TEXT_MUTED_LIGHT = Color.decode("#6B7280");
    public static final Color NAVY_PRIMARY = Color.decode("#1B2559");
    public static final Color ACCENT_LIGHT = Color.decode("#4F46E5");
    public static final Color ACCENT_HOVER_LIGHT = Color.decode("#4338CA");
    public static final Color GREEN_ACCENT = Color.decode("#15803D");
    public static final Color GREEN_LIGHT = Color.decode("#DCFCE7");
    public static final Color AMBER_WARNING = Color.decode("#B45309");
    public static final Color AMBER_LIGHT = Color.decode("#FEF3C7");
    public static final Color RED_ERROR = Color.decode("#B91C1C");
    public static final Color RED_LIGHT = Color.decode("#FEE2E2");

    public static final Color SURFACE_DARK = Color.decode("#0F1424");
    public static final Color CARD_BG_DARK = Color.decode("#181F36");
    public static final Color CARD_BORDER_DARK = Color.decode("#2A3350");
    public static final Color TEXT_PRIMARY_DARK = Color.decode("#E8ECF8");
    public static final Color TEXT_MUTED_DARK = Color.decode("#9AA3BC");
    public static final Color PRIMARY_DARK = Color.decode("#6366F1");
    public static final Color ACCENT_HOVER_DARK = Color.decode("#818CF8");
    public static final Color HEADING_DARK = Color.decode("#C7D2FE");
    public static final Color GREEN_DARK = Color.decode("#4ADE80");
    public static final Color GREEN_LIGHT_DARK = Color.decode("#123126");
    public static final Color AMBER_DARK = Color.decode("#FBBF24");
    public static final Color AMBER_LIGHT_DARK = Color.decode("#382B16");
    public static final Color RED_DARK = Color.decode("#F87171");
    public static final Color RED_LIGHT_DARK = Color.decode("#3A202A");

    public static final int SPACE_4 = 4;
    public static final int SPACE_8 = 8;
    public static final int SPACE_12 = 12;
    public static final int SPACE_16 = 16;
    public static final int SPACE_24 = 24;
    public static final int SPACE_32 = 32;
    public static final int PAGE_PADDING = 32;
    public static final int CARD_RADIUS = 14;
    public static final int INPUT_HEIGHT = 40;
    public static final int CONTENT_MAX_WIDTH = 1000;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    private static final List<WeakReference<JButton>> STYLED_BUTTONS = new ArrayList<>();
    private static boolean isDark;

    /**
     * Initializes the default FlatLaf theme from the stored preference.
     */
    public static void initTheme(String themePreference) {
        applyTheme("DARK".equalsIgnoreCase(themePreference));
    }

    /**
     * Switches FlatLaf themes and refreshes the shared styled components.
     */
    public static void setTheme(String themeName) {
        applyTheme("DARK".equalsIgnoreCase(themeName));
        FlatLaf.updateUI();
        refreshStyledButtons();
    }

    private static void applyTheme(boolean dark) {
        if (dark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        isDark = dark;
        UIManager.put("defaultFont", FONT_BODY);
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", CARD_RADIUS);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Component.focusWidth", 2);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", getAccentColor());
        UIManager.put("TextField.margin", new Insets(8, 10, 8, 10));
        UIManager.put("TextField.placeholderForeground", getTextMuted());
        UIManager.put("Spinner.buttonStyle", "button");
        UIManager.put("ComboBox.padding", new Insets(7, 9, 7, 9));
    }

    public static boolean isDarkMode() {
        return isDark;
    }

    /** Returns the current theme's card surface color. */
    public static Color getCardBackground() {
        return isDark ? CARD_BG_DARK : CARD_BG_LIGHT;
    }

    /** Returns the current theme's page background color. */
    public static Color getSurfaceBackground() {
        return isDark ? SURFACE_DARK : SURFACE_LIGHT;
    }

    /** Returns the current theme's border color. */
    public static Color getBorderColor() {
        return isDark ? CARD_BORDER_DARK : CARD_BORDER_LIGHT;
    }

    /** Returns the current theme's primary text color. */
    public static Color getTextPrimary() {
        return isDark ? TEXT_PRIMARY_DARK : TEXT_PRIMARY_LIGHT;
    }

    /** Returns the current theme's muted text color. */
    public static Color getTextMuted() {
        return isDark ? TEXT_MUTED_DARK : TEXT_MUTED_LIGHT;
    }

    /** Returns the current theme's heading color. */
    public static Color getHeadingColor() {
        return isDark ? HEADING_DARK : NAVY_PRIMARY;
    }

    /** Returns the current theme's primary button fill color. */
    public static Color getPrimaryColor() {
        return isDark ? PRIMARY_DARK : ACCENT_LIGHT;
    }

    /** Returns the current theme's accessible accent color for text and icons. */
    public static Color getAccentColor() {
        return isDark ? ACCENT_HOVER_DARK : ACCENT_LIGHT;
    }

    /** Returns the current theme's hover color for accent controls. */
    public static Color getAccentHoverColor() {
        return isDark ? ACCENT_HOVER_DARK : ACCENT_HOVER_LIGHT;
    }

    /** Returns the current theme's tinted accent surface. */
    public static Color getAccentBackground() {
        return isDark ? Color.decode("#252D52") : Color.decode("#EEF2FF");
    }

    /** Returns the current theme's success text and icon color. */
    public static Color getSuccessColor() {
        return isDark ? GREEN_DARK : GREEN_ACCENT;
    }

    /** Returns the current theme's tinted success surface. */
    public static Color getSuccessBackground() {
        return isDark ? GREEN_LIGHT_DARK : GREEN_LIGHT;
    }

    /** Returns the current theme's warning text and icon color. */
    public static Color getWarningColor() {
        return isDark ? AMBER_DARK : AMBER_WARNING;
    }

    /** Returns the current theme's tinted warning surface. */
    public static Color getWarningBackground() {
        return isDark ? AMBER_LIGHT_DARK : AMBER_LIGHT;
    }

    /** Returns the current theme's error text and icon color. */
    public static Color getErrorColor() {
        return isDark ? RED_DARK : RED_ERROR;
    }

    /** Returns the current theme's tinted error surface. */
    public static Color getErrorBackground() {
        return isDark ? RED_LIGHT_DARK : RED_LIGHT;
    }

    /** Returns the current theme's navbar surface color. */
    public static Color getNavbarBackground() {
        return getCardBackground();
    }

    /**
     * Creates a rounded Java2D-painted card with standard padding.
     */
    public static CardPanel createCardPanel() {
        CardPanel panel = new CardPanel();
        applyCardStyle(panel);
        return panel;
    }

    /**
     * Centers a page section while allowing it to grow up to the shared content width.
     */
    public static JPanel centeredContent(JComponent content) {
        return new CenteredPanel(content);
    }

    /**
     * Creates a rounded Java2D panel for tinted status banners.
     */
    public static RoundedPanel createRoundedPanel(LayoutManager layout) {
        return new RoundedPanel(layout);
    }

    /**
     * Applies the shared rounded-card layout and padding.
     */
    public static void applyCardStyle(JPanel panel) {
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(SPACE_24, SPACE_24, SPACE_24, SPACE_24));
        if (!(panel instanceof CardPanel)) {
            panel.setBackground(getCardBackground());
            panel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(getBorderColor(), 1, true),
                    new EmptyBorder(SPACE_24, SPACE_24, SPACE_24, SPACE_24)));
        }
    }

    /**
     * Applies common rounded control geometry and height.
     */
    public static void styleInput(JComponent component) {
        component.putClientProperty("JComponent.roundRect", true);
        component.setFont(FONT_BODY);
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferred.width, INPUT_HEIGHT));
        component.setMinimumSize(new Dimension(0, INPUT_HEIGHT));
    }

    /**
     * Applies or removes the shared red input error outline.
     */
    public static void setInputError(JComponent component, boolean invalid) {
        Border normalBorder = (Border) component.getClientProperty("sfp.normalBorder");
        if (normalBorder == null) {
            normalBorder = component.getBorder();
            component.putClientProperty("sfp.normalBorder", normalBorder);
        }
        component.setBorder(invalid
                ? BorderFactory.createCompoundBorder(
                        new LineBorder(getErrorColor(), 1, true),
                        BorderFactory.createEmptyBorder(7, 9, 7, 9))
                : normalBorder);
    }

    /**
     * Creates a primary accent button with accessible focus and subtle hover transitions.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, SPACE_16, 10, SPACE_16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.focusWidth", 2);
        registerButton(button, "primary");
        return button;
    }

    /**
     * Creates a transparent secondary button with a theme-aware border and hover tint.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY);
        button.setForeground(getTextPrimary());
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.focusWidth", 2);
        registerButton(button, "secondary");
        return button;
    }

    /**
     * Styles a navigation link with a compact pill and theme-aware hover tint.
     */
    public static void styleNavigationButton(JButton button) {
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("sfp.navActive", Boolean.FALSE);
        button.setForeground(getTextMuted());
        button.setBackground(getCardBackground());
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                if (!Boolean.TRUE.equals(button.getClientProperty("sfp.navActive"))) {
                    button.setBackground(getAccentBackground());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                if (!Boolean.TRUE.equals(button.getClientProperty("sfp.navActive"))) {
                    button.setBackground(getCardBackground());
                }
            }
        });
    }

    /**
     * Creates a compact destructive outline button.
     */
    public static JButton createDangerButton(String text) {
        JButton button = createSecondaryButton(text);
        button.setForeground(getErrorColor());
        button.putClientProperty("sfp.buttonRole", "danger");
        return button;
    }

    private static void registerButton(JButton button, String role) {
        button.putClientProperty("sfp.buttonRole", role);
        STYLED_BUTTONS.add(new WeakReference<>(button));
        button.setBackground(buttonBaseColor(role));
        button.setBorder(buttonBorder(role));
        installButtonTransition(button);
    }

    private static void installButtonTransition(JButton button) {
        final Color[] start = {button.getBackground()};
        final Color[] target = {button.getBackground()};
        final long[] started = {0L};
        ActionListener tick = event -> {
            float fraction = Math.min(1f, (System.nanoTime() - started[0]) / 140_000_000f);
            button.setBackground(blend(start[0], target[0], fraction));
            if ("primary".equals(button.getClientProperty("sfp.buttonRole"))) {
                boolean lightText = !isDark || button.getModel().isPressed();
                button.setForeground(lightText ? Color.WHITE : TEXT_PRIMARY_LIGHT);
            }
            if (fraction >= 1f) {
                ((javax.swing.Timer) event.getSource()).stop();
            }
        };
        javax.swing.Timer timer = new javax.swing.Timer(16, tick);
        button.getModel().addChangeListener(event -> {
            Color next = buttonTargetColor(button);
            if (!next.equals(target[0])) {
                start[0] = button.getBackground();
                target[0] = next;
                started[0] = System.nanoTime();
                timer.restart();
            }
        });
    }

    private static Color buttonTargetColor(JButton button) {
        ButtonModel model = button.getModel();
        String role = (String) button.getClientProperty("sfp.buttonRole");
        if (model.isPressed() && model.isArmed()) {
            return isDark ? new Color(79, 70, 229) : ACCENT_HOVER_LIGHT;
        }
        if (model.isRollover()) {
            return "primary".equals(role) ? getAccentHoverColor() : getBorderColor();
        }
        return buttonBaseColor(role);
    }

    private static Color buttonBaseColor(String role) {
        return "primary".equals(role) ? getPrimaryColor() : getCardBackground();
    }

    private static Border buttonBorder(String role) {
        if ("primary".equals(role)) {
            return new LineBorder(getPrimaryColor(), 1, true);
        }
        if ("danger".equals(role)) {
            return new LineBorder(getErrorColor(), 1, true);
        }
        return new LineBorder(getBorderColor(), 1, true);
    }

    private static Color blend(Color from, Color to, float fraction) {
        float inverse = 1f - fraction;
        return new Color(
                Math.round(from.getRed() * inverse + to.getRed() * fraction),
                Math.round(from.getGreen() * inverse + to.getGreen() * fraction),
                Math.round(from.getBlue() * inverse + to.getBlue() * fraction));
    }

    private static void refreshStyledButtons() {
        Iterator<WeakReference<JButton>> iterator = STYLED_BUTTONS.iterator();
        while (iterator.hasNext()) {
            JButton button = iterator.next().get();
            if (button == null) {
                iterator.remove();
                continue;
            }
            String role = (String) button.getClientProperty("sfp.buttonRole");
            if (role == null) {
                continue;
            }
            button.setBackground(buttonBaseColor(role));
            button.setBorder(buttonBorder(role));
            if ("primary".equals(role)) {
                button.setForeground(Color.WHITE);
            }
            if ("danger".equals(role)) {
                button.setForeground(getErrorColor());
            }
        }
    }

    /**
     * Rounded card surface painted with a soft shadow in Java2D.
     */
    public static class CardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int inset = 5;
            for (int i = 4; i >= 1; i--) {
                g.setColor(new Color(15, 23, 42, isDark ? 16 : 9));
                g.fill(new RoundRectangle2D.Float(inset - i / 2f, inset - i / 2f,
                        getWidth() - inset * 2 + i, getHeight() - inset * 2 + i,
                        CARD_RADIUS * 2, CARD_RADIUS * 2));
            }
            g.setColor(getCardBackground());
            g.fill(new RoundRectangle2D.Float(inset, inset, getWidth() - inset * 2,
                    getHeight() - inset * 2, CARD_RADIUS * 2, CARD_RADIUS * 2));
            g.setStroke(new BasicStroke(1f));
            g.setColor(getBorderColor());
            g.draw(new RoundRectangle2D.Float(inset, inset, getWidth() - inset * 2 - 1,
                    getHeight() - inset * 2 - 1, CARD_RADIUS * 2, CARD_RADIUS * 2));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /**
     * Rounded panel with a theme-aware fill, used for status surfaces.
     */
    public static class RoundedPanel extends JPanel {
        public RoundedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CARD_RADIUS * 2, CARD_RADIUS * 2));
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class CenteredPanel extends JPanel implements Scrollable {
        private final JComponent content;

        private CenteredPanel(JComponent content) {
            this.content = content;
            setLayout(null);
            setOpaque(false);
            add(content);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferred = content.getPreferredSize();
            return new Dimension(Math.min(preferred.width, maxContentWidth()), preferred.height);
        }

        @Override
        public void doLayout() {
            int width = Math.min(getWidth(), maxContentWidth());
            content.setBounds((getWidth() - width) / 2, 0, width, getHeight());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return SPACE_16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(SPACE_16, orientation == SwingConstants.VERTICAL
                    ? visibleRect.height - SPACE_16 : visibleRect.width - SPACE_16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        private int maxContentWidth() {
            int maximum = content.getMaximumSize().width;
            return maximum > 0 && maximum < Integer.MAX_VALUE
                    ? Math.min(maximum, CONTENT_MAX_WIDTH)
                    : CONTENT_MAX_WIDTH;
        }
    }
}

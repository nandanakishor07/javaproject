package com.sfp.gui.components;

import com.sfp.util.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern custom-painted rounded progress bar displaying percentage and progress fill.
 */
public class CustomProgressBar extends JComponent {
    private double progressPercent; // 0 to 100+
    private Color fillColor = AppTheme.GREEN_ACCENT;
    private Color trackColor = new Color(229, 231, 235);
    private boolean showLabel = true;

    /**
     * Creates a custom progress bar with 0% initial progress.
     */
    public CustomProgressBar() {
        this(0.0);
    }

    /**
     * Creates a custom progress bar with initial progress percentage.
     */
    public CustomProgressBar(double progressPercent) {
        this.progressPercent = Math.max(0.0, progressPercent);
        setPreferredSize(new Dimension(200, 20));
        setMinimumSize(new Dimension(100, 16));
    }

    /**
     * Updates the progress percentage and triggers repaint.
     */
    public void setProgressPercent(double percent) {
        this.progressPercent = Math.max(0.0, percent);
        repaint();
    }

    /**
     * Gets the current progress percentage.
     */
    public double getProgressPercent() {
        return progressPercent;
    }

    /**
     * Sets the fill color of the progress bar.
     */
    public void setFillColor(Color color) {
        this.fillColor = color;
        repaint();
    }

    /**
     * Toggles visibility of the centered percentage label.
     */
    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = height; // pill shape

        // Background track
        Color track = AppTheme.isDarkMode() ? new Color(44, 49, 64) : trackColor;
        g2.setColor(track);
        g2.fillRoundRect(0, 0, width, height, arc, arc);

        // Filled progress
        double clampedPercent = Math.min(100.0, progressPercent);
        int fillWidth = (int) Math.round((clampedPercent / 100.0) * width);
        if (fillWidth > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, fillWidth, height, arc, arc);
        }

        // Percentage text
        if (showLabel) {
            String text = String.format("%.1f%%", progressPercent);
            g2.setFont(AppTheme.FONT_SMALL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            int textX = (width - textWidth) / 2;
            int textY = (height + textHeight) / 2 - 2;

            // Subtle contrast text color
            g2.setColor(AppTheme.getTextPrimary());
            g2.drawString(text, textX, textY);
        }

        g2.dispose();
    }
}

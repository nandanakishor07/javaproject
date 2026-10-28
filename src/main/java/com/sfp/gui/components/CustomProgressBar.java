package com.sfp.gui.components;

import com.sfp.util.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern custom-painted rounded progress bar displaying percentage and progress fill.
 */
public class CustomProgressBar extends JComponent {
    private double progressPercent; // 0 to 100+
    private Color fillColor;
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
        setPreferredSize(new Dimension(200, 10));
        setMinimumSize(new Dimension(60, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
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
        int height = 10;
        int y = Math.max(0, (getHeight() - height) / 2);
        int arc = height;

        // Background track
        g2.setColor(AppTheme.getBorderColor());
        g2.fillRoundRect(0, y, width, height, arc, arc);

        // Filled progress
        double clampedPercent = Math.min(100.0, progressPercent);
        int fillWidth = (int) Math.round((clampedPercent / 100.0) * width);
        if (fillWidth > 0) {
            g2.setColor(fillColor != null ? fillColor : AppTheme.getAccentColor());
            g2.fillRoundRect(0, y, fillWidth, height, arc, arc);
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
            g2.drawString(text, textX, Math.max(fm.getAscent(), textY));
        }

        g2.dispose();
    }
}

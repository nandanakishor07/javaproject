package com.sfp.gui.components;

import com.sfp.util.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Modern KPI dashboard card displaying a title, hero metric, and auxiliary detail.
 */
public class StatCard extends AppTheme.CardPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;

    /**
     * Constructs a StatCard with title, value, and subtitle text.
     */
    public StatCard(String title, String value, String subtitle) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        AppTheme.applyCardStyle(this);

        titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        titleLabel.setForeground(AppTheme.getTextMuted());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel = new JLabel(value);
        valueLabel.setFont(value.length() > 16 ? AppTheme.FONT_SUBTITLE : AppTheme.FONT_HERO);
        valueLabel.setForeground(AppTheme.getHeadingColor());
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(AppTheme.FONT_SMALL);
        subtitleLabel.setForeground(AppTheme.getTextMuted());
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(titleLabel);
        add(Box.createVerticalStrut(6));
        add(valueLabel);
        add(Box.createVerticalStrut(4));
        add(subtitleLabel);
    }

    /**
     * Updates the main metric value displayed in the card.
     */
    public void setValue(String value) {
        valueLabel.setText(value);
        valueLabel.setFont(value.length() > 16 ? AppTheme.FONT_SUBTITLE : AppTheme.FONT_HERO);
    }

    /**
     * Updates the subtitle detail text displayed in the card.
     */
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (valueLabel != null) {
            valueLabel.setForeground(AppTheme.getHeadingColor());
            valueLabel.setFont(valueLabel.getText().length() > 16
                    ? AppTheme.FONT_SUBTITLE : AppTheme.FONT_HERO);
            titleLabel.setForeground(AppTheme.getTextMuted());
            subtitleLabel.setForeground(AppTheme.getTextMuted());
        }
    }
}

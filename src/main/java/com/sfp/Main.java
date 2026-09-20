package com.sfp;

import com.sfp.gui.MainFrame;
import com.sfp.util.AppTheme;

import javax.swing.*;

/**
 * Application entry point for the Smart Finance Planner desktop application.
 */
public class Main {

    /**
     * Initializes FlatLaf appearance and launches the GUI on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        // Initialize default Light theme initially
        AppTheme.initTheme("LIGHT");

        // Run UI creation on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

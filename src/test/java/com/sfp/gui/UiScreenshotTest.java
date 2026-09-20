package com.sfp.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.sfp.data.ContributionDao;
import com.sfp.data.DatabaseManager;
import com.sfp.data.GoalDao;
import com.sfp.data.UserDao;
import com.sfp.engine.InflationCalculator;
import com.sfp.engine.ProgressTracker;
import com.sfp.engine.SavingsPlanner;
import com.sfp.model.Contribution;
import com.sfp.model.Goal;
import com.sfp.model.User;
import com.sfp.util.AppTheme;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;

/**
 * Utility test that instantiates all Swing screens, populates them with sample data,
 * and renders high-resolution PNG captures for visual verification and report generation.
 */
public class UiScreenshotTest {

    @Test
    public void generateScreenshots() throws Exception {
        File dir = new File("target/screenshots");
        dir.mkdirs();

        // 1. Onboarding Screen
        FlatLightLaf.setup();
        DatabaseManager emptyDb = new DatabaseManager("jdbc:sqlite::memory:");
        emptyDb.initSchema();
        UserDao emptyUserDao = new UserDao(emptyDb);
        OnboardingPanel onboardingPanel = new OnboardingPanel(null, emptyUserDao);
        saveComponentImage(onboardingPanel, 1100, 740, new File(dir, "01_onboarding.png"));

        // Setup populated DB
        DatabaseManager db = new DatabaseManager("jdbc:sqlite::memory:");
        db.initSchema();
        UserDao userDao = new UserDao(db);
        GoalDao goalDao = new GoalDao(db);
        ContributionDao contributionDao = new ContributionDao(db);

        User user = new User("Rahul Sharma", 75000.0, 20.0, "INR", "LIGHT");
        int userId = userDao.insert(user);
        user.setId(userId);

        Goal g1 = new Goal(userId, "MacBook Pro M3", 150000.0, 2027, 6.0, "SHORT", "INR", 178652.4, 4962.57);
        int g1Id = goalDao.insert(g1);
        g1.setId(g1Id);

        Goal g2 = new Goal(userId, "Electric Scooter", 120000.0, 2028, 5.0, "SHORT", "INR", 138915.0, 2894.0);
        int g2Id = goalDao.insert(g2);
        g2.setId(g2Id);

        Goal g3 = new Goal(userId, "Europe Vacation", 3500.0, 2027, 4.0, "SHORT", "EUR", 3785.6, 105.15);
        int g3Id = goalDao.insert(g3);
        g3.setId(g3Id);

        Goal g4 = new Goal(userId, "Old Gaming Rig", 80000.0, 2025, 5.0, "SHORT", "INR", 84000.0, 7000.0);
        g4.setStatus("ARCHIVED");
        int g4Id = goalDao.insert(g4);
        g4.setId(g4Id);

        // Add contributions
        contributionDao.insert(new Contribution(g1Id, 25000.0, "2026-01-15", "Salary savings"));
        contributionDao.insert(new Contribution(g1Id, 30000.0, "2026-02-15", "Performance bonus"));
        contributionDao.insert(new Contribution(g2Id, 15000.0, "2026-01-20", "Initial deposit"));

        InflationCalculator calc = new InflationCalculator();
        SavingsPlanner planner = new SavingsPlanner();
        ProgressTracker tracker = new ProgressTracker();

        // 2. Dashboard Panel
        JFrame mockFrame = new JFrame();
        mockFrame.setSize(1100, 740);

        DashboardPanel dashboard = new DashboardPanel(createMockMainFrame(user, db), goalDao, contributionDao, tracker);
        dashboard.refresh();
        // Give worker thread a moment
        Thread.sleep(300);
        saveComponentImage(dashboard, 1100, 740, new File(dir, "02_dashboard.png"));

        // 3. Add / Edit Goal Panel
        GoalFormPanel formPanel = new GoalFormPanel(createMockMainFrame(user, db), calc, planner);
        formPanel.prepareForNewGoal();
        saveComponentImage(formPanel, 1100, 740, new File(dir, "03_add_goal.png"));

        // 4. Result Screen Panel
        ResultPanel resultPanel = new ResultPanel(createMockMainFrame(user, db), goalDao);
        resultPanel.setResultData(g1, 75000.0, 20.0, 6.6, true, false);
        saveComponentImage(resultPanel, 1100, 740, new File(dir, "04_result_screen.png"));

        // 5. Saved Goals Panel
        SavedGoalsPanel savedGoals = new SavedGoalsPanel(createMockMainFrame(user, db), goalDao, contributionDao, tracker);
        savedGoals.refresh();
        Thread.sleep(300);
        saveComponentImage(savedGoals, 1100, 740, new File(dir, "05_saved_goals.png"));

        // 6. Goal Detail Panel
        GoalDetailPanel detailPanel = new GoalDetailPanel(createMockMainFrame(user, db), goalDao, contributionDao, tracker);
        detailPanel.loadGoal(g1);
        Thread.sleep(300);
        saveComponentImage(detailPanel, 1100, 740, new File(dir, "06_goal_detail.png"));

        // 7. Settings Panel
        SettingsPanel settingsPanel = new SettingsPanel(createMockMainFrame(user, db), userDao);
        settingsPanel.refresh();
        saveComponentImage(settingsPanel, 1100, 740, new File(dir, "07_settings.png"));

        // 8. Archived Goals Panel
        ArchivedGoalsPanel archivedPanel = new ArchivedGoalsPanel(createMockMainFrame(user, db), goalDao, contributionDao);
        archivedPanel.refresh();
        Thread.sleep(300);
        saveComponentImage(archivedPanel, 1100, 740, new File(dir, "08_archived_goals.png"));

        // 9. Dark Mode Dashboard
        FlatDarkLaf.setup();
        AppTheme.setTheme("DARK");
        DashboardPanel darkDashboard = new DashboardPanel(createMockMainFrame(user, db), goalDao, contributionDao, tracker);
        darkDashboard.refresh();
        Thread.sleep(300);
        saveComponentImage(darkDashboard, 1100, 740, new File(dir, "09_dark_dashboard.png"));

        // Reset theme to Light
        FlatLightLaf.setup();
        AppTheme.setTheme("LIGHT");
    }

    private MainFrame createMockMainFrame(User user, DatabaseManager db) {
        return new MainFrame() {
            @Override
            public User getCurrentUser() {
                return user;
            }
        };
    }

    private void saveComponentImage(Component comp, int width, int height, File file) throws Exception {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(width, height);
        frame.getContentPane().add(comp);
        frame.addNotify();
        frame.validate();
        comp.setSize(width, height);
        comp.doLayout();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        comp.paint(g2);
        g2.dispose();

        ImageIO.write(image, "PNG", file);
        frame.dispose();
    }
}

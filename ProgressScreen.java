package ui;

import models.Task;
import models.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProgressScreen extends JFrame {
    private final User user;
    private final Color backgroundColor = new Color(173, 216, 230); // Light blue background
    private final Font headerFont = new Font("Monospaced", Font.BOLD, 18);
    private final Font labelFont = new Font("Monospaced", Font.BOLD, 14);
    private final Color accentColor = new Color(199, 21, 133); // Deep pink accent

    public ProgressScreen() {
        // Get the current user from MainMenu
        this.user = MainMenu.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(null, "Error: No user logged in!");
            dispose();
            return;
        }

        setTitle("📊 Study Progress for " + user.getUsername());
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(backgroundColor);

        // Create tabbed pane for different progress views
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(labelFont);

        // Add panels to tabbed pane
        tabbedPane.addTab("📈 Dashboard", createDashboardPanel());
        tabbedPane.addTab("📋 Task History", createTaskHistoryPanel());
        tabbedPane.addTab("🎯 Goals & Achievements", createGoalsPanel());
        tabbedPane.addTab("📊 Study Analytics", createAnalyticsPanel());

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // User stats panel (top)
        JPanel statsPanel = createStatsPanel();

        // Progress bars panel (middle)
        JPanel progressPanel = createProgressBarsPanel();

        // Recent activity panel (bottom)
        JPanel recentActivityPanel = createRecentActivityPanel();

        // Add all panels to main panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(backgroundColor);
        centerPanel.add(statsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(progressPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(recentActivityPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(backgroundColor);

        // Calculate key metrics
        int totalTasks = user.getTasks().size();
        long completedTasks = user.getTasks().stream().filter(Task::isCompleted).count();
        int completionRate = totalTasks > 0 ? (int)((completedTasks * 100) / totalTasks) : 0;
        int totalXP = user.getXp();
        int totalCoins = user.getCoins();

        // Create stat cards
        panel.add(createStatCard("Tasks Completed", String.valueOf(completedTasks), "✅"));
        panel.add(createStatCard("Completion Rate", completionRate + "%", "📊"));
        panel.add(createStatCard("Total XP Earned", String.valueOf(totalXP), "⭐"));
        panel.add(createStatCard("Total Coins Earned", String.valueOf(totalCoins), "💰"));

        return panel;
    }

    private JPanel createStatCard(String title, String value, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 28));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);

        return card;
    }

    private JPanel createProgressBarsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Level Progress", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Calculate XP needed for next level
        int currentLevel = user.getLevel();
        int currentXP = user.getXp();
        int xpForCurrentLevel = calculateXPForLevel(currentLevel);
        int xpForNextLevel = calculateXPForLevel(currentLevel + 1);
        int xpNeeded = xpForNextLevel - xpForCurrentLevel;
        int xpProgress = currentXP - xpForCurrentLevel;
        int progressPercent = (int)(((double)xpProgress / xpNeeded) * 100);
        if (progressPercent < 0) progressPercent = 0;
        if (progressPercent > 100) progressPercent = 100;

        // Level progress
        JLabel levelLabel = new JLabel("Level " + currentLevel + " → Level " + (currentLevel + 1));
        levelLabel.setFont(labelFont);

        JProgressBar levelProgress = new JProgressBar(0, 100);
        levelProgress.setValue(progressPercent);
        levelProgress.setStringPainted(true);
        levelProgress.setString(xpProgress + "/" + xpNeeded + " XP");
        levelProgress.setPreferredSize(new Dimension(600, 25));
        levelProgress.setBackground(Color.WHITE);
        levelProgress.setForeground(new Color(75, 0, 130)); // Indigo

        // Task completion progress
        int totalTasks = user.getTasks().size();
        long completedTasks = user.getTasks().stream().filter(Task::isCompleted).count();
        int taskProgressPercent = totalTasks > 0 ? (int)((completedTasks * 100) / totalTasks) : 0;

        JLabel taskLabel = new JLabel("Task Completion");
        taskLabel.setFont(labelFont);

        JProgressBar taskProgress = new JProgressBar(0, 100);
        taskProgress.setValue(taskProgressPercent);
        taskProgress.setStringPainted(true);
        taskProgress.setString(completedTasks + "/" + totalTasks + " Tasks");
        taskProgress.setPreferredSize(new Dimension(600, 25));
        taskProgress.setBackground(Color.WHITE);
        taskProgress.setForeground(new Color(46, 139, 87)); // Sea green

        // Add components to panel
        panel.add(levelLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(levelProgress);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(taskLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(taskProgress);

        return panel;
    }

    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Recent Activity", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Get completed tasks
        List<Task> completedTasks = user.getTasks().stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());

        // Sort by completion date if available
        List<Task> recentTasks = completedTasks.stream()
                .filter(task -> task.getCompletionDate() != null)
                .sorted(Comparator.comparing(Task::getCompletionDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // If no tasks with dates, just take the first 5 completed tasks
        if (recentTasks.isEmpty() && !completedTasks.isEmpty()) {
            recentTasks = completedTasks.stream().limit(5).collect(Collectors.toList());
        }

        DefaultListModel<String> activityModel = new DefaultListModel<>();

        if (recentTasks.isEmpty()) {
            activityModel.addElement("No completed tasks yet. Complete a task to see it here!");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            for (Task task : recentTasks) {
                String dateStr = task.getCompletionDate() != null ?
                        task.getCompletionDate().format(formatter) : "Recently";
                activityModel.addElement("✅ " + dateStr + " - " + task.getDescription() +
                        " (+" + task.getXpReward() + " XP, +" + task.getCoinReward() + " Coins)");
            }
        }

        JList<String> activityList = new JList<>(activityModel);
        activityList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        activityList.setBackground(Color.WHITE);

        panel.add(new JScrollPane(activityList), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTaskHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create column names and table model for tasks
        String[] columnNames = {"Description", "Difficulty", "XP", "Coins", "Status", "Completion Date"};
        Object[][] data = new Object[user.getTasks().size()][6];

        // Fill data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        int i = 0;
        for (Task task : user.getTasks()) {
            data[i][0] = task.getDescription();
            data[i][1] = task.getDifficulty();
            data[i][2] = task.getXpReward();
            data[i][3] = task.getCoinReward();
            data[i][4] = task.isCompleted() ? "Completed" : "Pending";
            data[i][5] = task.getCompletionDate() != null ?
                    task.getCompletionDate().format(formatter) : "-";
            i++;
        }

        JTable taskTable = new JTable(data, columnNames);
        taskTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        taskTable.getTableHeader().setFont(labelFont);
        taskTable.setRowHeight(25);
        taskTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(accentColor, 2));

        // Add title label
        JLabel titleLabel = new JLabel("Complete Task History");
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(accentColor);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Achievement panel with milestones
        JPanel achievementsPanel = createAchievementsPanel();

        // Future goals panel
        JPanel goalsPanel = createGoalsInputPanel();

        // Add both panels to main panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(backgroundColor);
        centerPanel.add(achievementsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(goalsPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAchievementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Achievements", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Get the count of completed tasks
        long completedTasks = user.getTasks().stream().filter(Task::isCompleted).count();

        // Define achievement milestones
        String[][] achievements = {
            {"🔰 Beginner", "Complete 1 task", completedTasks >= 1 ? "Unlocked" : "Locked"},
            {"🥉 Bronze Scholar", "Complete 5 tasks", completedTasks >= 5 ? "Unlocked" : "Locked"},
            {"🥈 Silver Scholar", "Complete 10 tasks", completedTasks >= 10 ? "Unlocked" : "Locked"},
            {"🥇 Gold Scholar", "Complete 25 tasks", completedTasks >= 25 ? "Unlocked" : "Locked"},
            {"💎 Diamond Scholar", "Complete 50 tasks", completedTasks >= 50 ? "Unlocked" : "Locked"},
            {"🏆 Study Champion", "Complete 100 tasks", completedTasks >= 100 ? "Unlocked" : "Locked"}
        };

        // Create a table for achievements
        String[] columnNames = {"Title", "Requirement", "Status"};
        JTable achievementTable = new JTable(achievements, columnNames);
        achievementTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        achievementTable.getTableHeader().setFont(labelFont);
        achievementTable.setRowHeight(30);
        achievementTable.setEnabled(false); // Make it non-editable

        panel.add(new JScrollPane(achievementTable));

        return panel;
    }

    private JPanel createGoalsInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Set Study Goals", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Form panel for setting goals
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(backgroundColor);

        JLabel dailyTasksLabel = new JLabel("Daily Tasks Goal:");
        dailyTasksLabel.setFont(labelFont);
        JSpinner dailyTasksSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));

        JLabel weeklyXPLabel = new JLabel("Weekly XP Goal:");
        weeklyXPLabel.setFont(labelFont);
        JSpinner weeklyXPSpinner = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 100));

        JLabel targetLevelLabel = new JLabel("Target Level:");
        targetLevelLabel.setFont(labelFont);
        JSpinner targetLevelSpinner = new JSpinner(new SpinnerNumberModel(user.getLevel() + 1, user.getLevel() + 1, 100, 1));

        formPanel.add(dailyTasksLabel);
        formPanel.add(dailyTasksSpinner);
        formPanel.add(weeklyXPLabel);
        formPanel.add(weeklyXPSpinner);
        formPanel.add(targetLevelLabel);
        formPanel.add(targetLevelSpinner);

        // Button to save goals
        JButton saveGoalsButton = new JButton("Save Goals");
        saveGoalsButton.setFont(labelFont);
        saveGoalsButton.setBackground(accentColor);
        saveGoalsButton.setForeground(Color.WHITE);
        saveGoalsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveGoalsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Goals saved successfully!\n" +
                "Daily Tasks: " + dailyTasksSpinner.getValue() + "\n" +
                "Weekly XP: " + weeklyXPSpinner.getValue() + "\n" +
                "Target Level: " + targetLevelSpinner.getValue(),
                "Goals Saved", JOptionPane.INFORMATION_MESSAGE);
            // Note: In a full implementation, these would be saved to the user profile
        });

        // Add components to panel
        panel.add(formPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(saveGoalsButton);

        return panel;
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Task completion by difficulty
        JPanel difficultyPanel = createDifficultyAnalyticsPanel();

        // XP and Coins earned over time
        JPanel earningsPanel = createEarningsPanel();

        // Study consistency panel
        JPanel consistencyPanel = createConsistencyPanel();

        // Add all panels
        panel.add(difficultyPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(earningsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(consistencyPanel);

        return panel;
    }

    private JPanel createDifficultyAnalyticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Task Completion by Difficulty", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Count tasks by difficulty
        Map<Task.Difficulty, Integer> difficultyCount = new HashMap<>();
        for (Task.Difficulty diff : Task.Difficulty.values()) {
            difficultyCount.put(diff, 0);
        }

        for (Task task : user.getTasks()) {
            if (task.isCompleted()) {
                Task.Difficulty diff = task.getDifficulty();
                difficultyCount.put(diff, difficultyCount.get(diff) + 1);
            }
        }

        // Create labels for each difficulty
        JPanel statsPanel = new JPanel(new GridLayout(1, Task.Difficulty.values().length, 10, 0));
        statsPanel.setBackground(backgroundColor);

        Color[] colors = {
            new Color(50, 205, 50),  // EASY - Green
            new Color(255, 165, 0),  // MEDIUM - Orange
            new Color(220, 20, 60)   // HARD - Red
        };

        int i = 0;
        for (Task.Difficulty diff : Task.Difficulty.values()) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createLineBorder(colors[i], 3));

            JLabel titleLabel = new JLabel(diff.toString());
            titleLabel.setFont(labelFont);
            titleLabel.setForeground(colors[i]);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel countLabel = new JLabel(difficultyCount.get(diff).toString());
            countLabel.setFont(new Font("Monospaced", Font.BOLD, 36));
            countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tasksLabel = new JLabel("tasks");
            tasksLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
            tasksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(titleLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(countLabel);
            card.add(tasksLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));

            statsPanel.add(card);
            i++;
        }

        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEarningsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Earnings Summary", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Calculate total XP and coins by difficulty
        Map<Task.Difficulty, Integer> xpByDifficulty = new HashMap<>();
        Map<Task.Difficulty, Integer> coinsByDifficulty = new HashMap<>();

        for (Task.Difficulty diff : Task.Difficulty.values()) {
            xpByDifficulty.put(diff, 0);
            coinsByDifficulty.put(diff, 0);
        }

        for (Task task : user.getTasks()) {
            if (task.isCompleted()) {
                Task.Difficulty diff = task.getDifficulty();
                xpByDifficulty.put(diff, xpByDifficulty.get(diff) + task.getXpReward());
                coinsByDifficulty.put(diff, coinsByDifficulty.get(diff) + task.getCoinReward());
            }
        }

        // Create data for the table
        String[] columnNames = {"Difficulty", "XP Earned", "Coins Earned"};
        Object[][] data = new Object[Task.Difficulty.values().length + 1][3];

        int i = 0;
        int totalXP = 0;
        int totalCoins = 0;

        for (Task.Difficulty diff : Task.Difficulty.values()) {
            data[i][0] = diff.toString();
            data[i][1] = xpByDifficulty.get(diff);
            data[i][2] = coinsByDifficulty.get(diff);

            totalXP += xpByDifficulty.get(diff);
            totalCoins += coinsByDifficulty.get(diff);
            i++;
        }

        // Add total row
        data[i][0] = "TOTAL";
        data[i][1] = totalXP;
        data[i][2] = totalCoins;

        // Create table
        JTable earningsTable = new JTable(data, columnNames);
        earningsTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        earningsTable.getTableHeader().setFont(labelFont);
        earningsTable.setRowHeight(30);
        earningsTable.setEnabled(false); // Make it non-editable

        // Make the total row bold
        earningsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (row == Task.Difficulty.values().length) {
                    c.setFont(new Font("Monospaced", Font.BOLD, 14));
                    c.setBackground(new Color(240, 240, 240));
                } else {
                    c.setFont(new Font("Monospaced", Font.PLAIN, 14));
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        panel.add(new JScrollPane(earningsTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createConsistencyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor),
                "Study Consistency", TitledBorder.LEFT, TitledBorder.TOP, labelFont, accentColor),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // In a real implementation, this would analyze task completion dates
        // to show study consistency over time

        // For now, just show a placeholder message
        JLabel placeholderLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "Study consistency tracking will show your daily and weekly study patterns.<br><br>" +
            "This feature would display a calendar heatmap showing days when you completed tasks.<br>" +
            "It would also calculate your study streaks and average tasks per day.<br><br>" +
            "Complete more tasks to see your study patterns emerge!" +
            "</div></html>"
        );
        placeholderLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(placeholderLabel, BorderLayout.CENTER);

        return panel;
    }

    // Helper method to calculate XP needed for a given level
    private int calculateXPForLevel(int level) {
        return (level * level * 100);
    }
}

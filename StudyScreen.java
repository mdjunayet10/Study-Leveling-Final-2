package ui;

import models.Task;
import util.DataManager;
import util.TaskSelector;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudyScreen extends JFrame {
    private final DefaultListModel<Task> taskListModel = new DefaultListModel<>();
    private final DefaultListModel<Task> recommendedListModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(taskListModel);
    private final JList<Task> recommendedList = new JList<>(recommendedListModel);
    private final MainMenu mainMenu;
    private final models.User user;

    public StudyScreen(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        this.user = mainMenu.getUser();

        setTitle("📘 Study Tasks");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(173, 216, 230)); // Match main menu background

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        // === Recommended Tasks ===
        JLabel recommendedLabel = new JLabel("📌 Recommended Tasks:");
        styleTopLabel(recommendedLabel);
        JPanel recommendedPanel = new JPanel(new BorderLayout());
        recommendedPanel.setOpaque(false);
        recommendedPanel.add(recommendedLabel, BorderLayout.NORTH);
        recommendedPanel.add(new JScrollPane(recommendedList), BorderLayout.CENTER);
        mainPanel.add(recommendedPanel);

        // === Input Panel ===
        JTextField taskField = new JTextField();
        JTextField xpField = new JTextField("50", 3);
        JTextField coinField = new JTextField("20", 3);
        JComboBox<Task.Difficulty> difficultyBox = new JComboBox<>(Task.Difficulty.values());
        JButton addTask = createStyledButton("Add Task");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.add(taskField, BorderLayout.CENTER);
        inputPanel.add(addTask, BorderLayout.EAST);

        JPanel rewardPanel = new JPanel(new FlowLayout());
        rewardPanel.setOpaque(false);
        rewardPanel.add(new JLabel("⭐ XP:"));
        rewardPanel.add(xpField);
        rewardPanel.add(new JLabel("💰 Coins:"));
        rewardPanel.add(coinField);
        rewardPanel.add(new JLabel("⚙ Difficulty:"));
        rewardPanel.add(difficultyBox);

        mainPanel.add(inputPanel);
        mainPanel.add(rewardPanel);

        // === All Tasks ===
        JLabel allTasksLabel = new JLabel("📋 All Tasks:");
        styleTopLabel(allTasksLabel);
        JPanel allTasksPanel = new JPanel(new BorderLayout());
        allTasksPanel.setOpaque(false);
        allTasksPanel.add(allTasksLabel, BorderLayout.NORTH);
        allTasksPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
        mainPanel.add(allTasksPanel);

        // === Buttons ===
        JButton completeTask = createStyledButton("✔ Complete Task");
        JButton deleteTask = createStyledButton("🗑 Delete Task");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(completeTask);
        buttonPanel.add(deleteTask);
        mainPanel.add(buttonPanel);

        add(mainPanel);

        // === Listeners ===
        addTask.addActionListener(e -> {
            try {
                String desc = taskField.getText().trim();
                int xp = Integer.parseInt(xpField.getText());
                int coins = Integer.parseInt(coinField.getText());
                Task.Difficulty difficulty = (Task.Difficulty) difficultyBox.getSelectedItem();
                if (!desc.isEmpty() && difficulty != null) {
                    Task task = new Task(desc, xp, coins, difficulty);
                    user.getTasks().add(task);
                    taskListModel.addElement(task);
                    taskField.setText("");
                    updateRecommendedList();
                    DataManager.saveUser(user);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "XP and coins must be numbers.");
            }
        });

        completeTask.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null && !task.isCompleted()) {
                task.setCompleted(true);
                user.addXP(task.getXpReward());
                user.addCoins(task.getCoinReward());
                user.incrementCompletedTasksCounter(); // Increment the completed tasks counter

                JOptionPane.showMessageDialog(this,
                        "Task Completed! + " + task.getXpReward() + " XP, + " + task.getCoinReward() + " Coins");
                taskList.repaint();
                recommendedList.repaint();
                mainMenu.refreshStats();
                updateRecommendedList();
                DataManager.saveUser(user);

                // Upload stats to Firebase leaderboard whenever a task is completed
                util.FirebaseManager.uploadUserStats(user);
            }
        });

        deleteTask.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                user.getTasks().remove(task);
                taskListModel.removeElement(task);
                updateRecommendedList();
                DataManager.saveUser(user);
            }
        });

        user.getTasks().forEach(taskListModel::addElement);
        updateRecommendedList();

        setVisible(true);
    }

    private void updateRecommendedList() {
        recommendedListModel.clear();

        // Use the new prioritizeTasks method to get tasks sorted by priority
        List<Task> prioritizedTasks = TaskSelector.prioritizeTasks(user.getTasks());

        // Add prioritized tasks to the recommended list
        for (Task task : prioritizedTasks) {
            recommendedListModel.addElement(task);
        }
    }

    private void styleTopLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(199, 21, 133)); // Same as MainMenu
        label.setFont(new Font("Monospaced", Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(147, 112, 219)); // Purple like MainMenu
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }
}

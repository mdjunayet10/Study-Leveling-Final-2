//ui->MultiplayerStudyScreen

package ui;

import models.Task;
import models.User;
import util.DataManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplayerStudyScreen extends JFrame {
    private final List<User> users;
    private final Map<String, User> originalUserData = new HashMap<>(); // Store original user data snapshots
    private final MainMenu mainMenu; // Reference to the main menu for direct updates

    public MultiplayerStudyScreen(List<User> users, MainMenu mainMenu) {
        this.users = users;
        this.mainMenu = mainMenu;

        // Save a snapshot of each user's initial state for comparison when closing
        for (User user : users) {
            if (DataManager.userExists(user.getUsername())) {
                // Store the original user data from disk before any multiplayer changes
                originalUserData.put(user.getUsername(), DataManager.loadUser(user.getUsername()));
                System.out.println("Stored original state for " + user.getUsername());

                // Clear all existing tasks for this multiplayer session
                user.getTasks().clear();
                System.out.println("Cleared existing tasks for fresh multiplayer session: " + user.getUsername());
            }
        }

        setTitle("🤝 Multiplayer Study Mode");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(173, 216, 230)); // Light blue

        // Add window listener to sync progress with main accounts when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                syncUsersWithMainAccounts();
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        for (User user : users) {
            JPanel panel = createUserPanel(user);
            tabbedPane.addTab("👤 " + user.getUsername(), panel);
        }

        JButton leaderboardBtn = createStyledButton("🏆 Leaderboard");
        leaderboardBtn.addActionListener(e -> new LeaderboardScreen(users));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(leaderboardBtn);

        setLayout(new BorderLayout(10, 10));
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createUserPanel(User user) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(173, 216, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === Task List ===
        DefaultListModel<Task> model = new DefaultListModel<>();
        JList<Task> taskList = new JList<>(model);
        user.getTasks().forEach(model::addElement);
        taskList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Tasks"));

        // === Input Fields ===
        JTextField taskField = new JTextField(20);
        JTextField xpField = new JTextField("50", 4);
        JTextField coinField = new JTextField("20", 4);
        JComboBox<Task.Difficulty> difficultyBox = new JComboBox<>(Task.Difficulty.values());

        JButton add = createStyledButton("➕ Add Task");
        JButton complete = createStyledButton("✅ Complete Task");
        JButton delete = createStyledButton("🗑️ Delete Task");  // New delete task button

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createTitledBorder("📝 Add New Task"));
        inputPanel.add(taskField);
        inputPanel.add(new JLabel("⭐ XP:"));
        inputPanel.add(xpField);
        inputPanel.add(new JLabel("💰 Coins:"));
        inputPanel.add(coinField);
        inputPanel.add(new JLabel("⚙ Difficulty:"));
        inputPanel.add(difficultyBox);
        inputPanel.add(add);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(complete);
        buttonPanel.add(delete); // Add delete button to the panel

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);

        // === Event Handlers ===
        add.addActionListener(e -> {
            String desc = taskField.getText().trim();
            if (!desc.isEmpty()) {
                try {
                    int xp = Integer.parseInt(xpField.getText());
                    int coins = Integer.parseInt(coinField.getText());
                    Task.Difficulty diff = (Task.Difficulty) difficultyBox.getSelectedItem();
                    Task task = new Task(desc, xp, coins, diff);
                    user.getTasks().add(task);
                    model.addElement(task);
                    taskField.setText("");
                    DataManager.saveUser(user);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "XP and Coins must be numbers.");
                }
            }
        });

        complete.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null && !task.isCompleted()) {
                task.setCompleted(true);
                user.addXP(task.getXpReward());
                user.addCoins(task.getCoinReward());
                user.incrementCompletedTasksCounter(); // Increment the completed tasks counter
                JOptionPane.showMessageDialog(this,
                        user.getUsername() + " completed a task! + " +
                                task.getXpReward() + " XP, + " + task.getCoinReward() + " Coins!");
                taskList.repaint();
                DataManager.saveUser(user);

                // Upload stats to Firebase leaderboard whenever a task is completed
                util.FirebaseManager.uploadUserStats(user);
            }
        });

        // New event handler for delete button
        delete.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                user.getTasks().remove(task);
                model.removeElement(task);
                DataManager.saveUser(user);
            }
        });

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(147, 112, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }

    private void syncUsersWithMainAccounts() {
        for (User user : users) {
            // Check if this is a real user (not a guest) by verifying if they exist in storage
            if (DataManager.userExists(user.getUsername())) {
                // Skip users that don't have original data
                if (!originalUserData.containsKey(user.getUsername())) {
                    System.out.println("No original data for " + user.getUsername() + ", skipping sync");
                    continue;
                }

                System.out.println("Syncing multiplayer progress for user: " + user.getUsername());

                // Get the original user data from our stored snapshot
                User originalUser = originalUserData.get(user.getUsername());

                // Calculate what was gained during this multiplayer session
                int currentXP = user.getXp();
                int originalXP = originalUser.getXp();
                int xpGained = currentXP - originalXP;

                int currentCoins = user.getCoins();
                int originalCoins = originalUser.getCoins();
                int coinsGained = currentCoins - originalCoins;

                // Count how many tasks were completed in this multiplayer session
                int completedTasksInSession = 0;
                for (Task task : user.getTasks()) {
                    if (task.isCompleted()) {
                        completedTasksInSession++;
                    }
                }
                System.out.println("Tasks completed in this session: " + completedTasksInSession);

                System.out.println("Original state - XP: " + originalXP + ", Coins: " + originalCoins);
                System.out.println("Current state - XP: " + currentXP + ", Coins: " + currentCoins);
                System.out.println("Session gains - XP: " + xpGained + ", Coins: " + coinsGained);

                // Only proceed with sync if there were actual gains or completed tasks
                if (xpGained <= 0 && coinsGained <= 0 && completedTasksInSession == 0) {
                    System.out.println("No gains to sync for " + user.getUsername());
                    continue;
                }

                // Check if this user is the currently logged-in user from the main menu
                boolean isMainMenuUser = false;
                if (mainMenu != null && mainMenu.getUser().getUsername().equals(user.getUsername())) {
                    isMainMenuUser = true;
                    System.out.println("This is the main menu user - will update UI directly");

                    // Update the main menu user directly (this will update the UI immediately)
                    // Note: We don't need to add XP and coins again because they were already added
                    // when the user completed the tasks during the multiplayer session
                    // The multiplayer user object already has the updated values
                    // mainMenu.updateUserFromMultiplayer(xpGained, coinsGained);

                    // Instead, we'll directly set the values from the multiplayer session
                    User mainUser = mainMenu.getUser();
                    mainUser.setXp(user.getXp());
                    mainUser.setLevel(user.getLevel());
                    mainUser.setCoins(user.getCoins());

                    // Refresh the main menu UI to show updated stats immediately
                    mainMenu.refreshStats();

                    // Update the total completed tasks counter for each completed task in this session
                    // We need to do this because we previously cleared the tasks when starting the session
                    for (int i = 0; i < completedTasksInSession; i++) {
                        mainUser.incrementCompletedTasksCounter();
                    }

                    // Transfer newly completed tasks from multiplayer session to main account
                    for (Task task : user.getTasks()) {
                        if (task.isCompleted()) {
                            // Skip tasks that were already completed in the original state
                            boolean wasCompletedBefore = false;
                            for (Task originalTask : originalUser.getTasks()) {
                                if (originalTask.getDescription().equals(task.getDescription()) &&
                                    originalTask.isCompleted()) {
                                    wasCompletedBefore = true;
                                    break;
                                }
                            }

                            if (wasCompletedBefore) {
                                continue;
                            }

                            // Add completed task to main user if it doesn't exist
                            boolean taskExistsInMain = false;
                            for (Task mainTask : mainUser.getTasks()) {
                                if (mainTask.getDescription().equals(task.getDescription())) {
                                    taskExistsInMain = true;
                                    // Mark as completed if not already
                                    if (!mainTask.isCompleted()) {
                                        mainTask.setCompleted(true);
                                    }
                                    break;
                                }
                            }

                            if (!taskExistsInMain) {
                                Task newTask = new Task(
                                    task.getDescription(),
                                    task.getXpReward(),
                                    task.getCoinReward(),
                                    task.getDifficulty()
                                );
                                newTask.setCompleted(true);
                                mainUser.getTasks().add(newTask);
                            }
                        }
                    }

                    // Upload stats to Firebase to update the global leaderboard
                    util.FirebaseManager.uploadUserStats(mainUser);

                    // Show message about progress saved
                    String progressMessage = "Progress for " + user.getUsername() + " has been saved to main account!";
                    if (xpGained > 0) {
                        progressMessage += "\nXP gained: " + xpGained;
                    }
                    if (coinsGained > 0) {
                        progressMessage += "\nCoins gained: " + coinsGained;
                    }

                    JOptionPane.showMessageDialog(this,
                            progressMessage,
                            "Progress Saved", JOptionPane.INFORMATION_MESSAGE);

                    continue; // Skip the standard sync process for this user
                }

                // Standard sync process for non-main menu users
                User mainUser = DataManager.loadUser(user.getUsername());
                if (mainUser != null) {
                    // Store state before applying changes for confirmation message
                    int beforeXP = mainUser.getXp();
                    int beforeLevel = mainUser.getLevel();
                    int beforeCoins = mainUser.getCoins();

                    System.out.println("Main account before sync - XP: " + beforeXP + ", Coins: " + beforeCoins);

                    // Apply the gains directly to the main account
                    if (xpGained > 0) {
                        mainUser.addXP(xpGained);
                    }

                    if (coinsGained > 0) {
                        mainUser.addCoins(coinsGained);
                    }

                    System.out.println("Main account after sync - XP: " + mainUser.getXp() + ", Coins: " + mainUser.getCoins());

                    // Update the total completed tasks counter for each completed task in this session
                    // We need to do this because we previously cleared the tasks when starting the session
                    for (int i = 0; i < completedTasksInSession; i++) {
                        mainUser.incrementCompletedTasksCounter();
                    }

                    // Transfer newly completed tasks from multiplayer session to main account
                    for (Task task : user.getTasks()) {
                        if (task.isCompleted()) {
                            // Skip tasks that were already completed in the original state
                            boolean wasCompletedBefore = false;
                            for (Task originalTask : originalUser.getTasks()) {
                                if (originalTask.getDescription().equals(task.getDescription()) &&
                                    originalTask.isCompleted()) {
                                    wasCompletedBefore = true;
                                    break;
                                }
                            }

                            if (wasCompletedBefore) {
                                System.out.println("Task was already completed before: " + task.getDescription());
                                continue;
                            }

                            // Now check if task exists in main account and update it
                            boolean taskExistsInMain = false;
                            for (Task mainTask : mainUser.getTasks()) {
                                if (mainTask.getDescription().equals(task.getDescription())) {
                                    taskExistsInMain = true;
                                    // Mark as completed if not already
                                    if (!mainTask.isCompleted()) {
                                        mainTask.setCompleted(true);
                                    }
                                    break;
                                }
                            }

                            // Add task to main account if it doesn't exist
                            if (!taskExistsInMain) {
                                Task newTask = new Task(
                                    task.getDescription(),
                                    task.getXpReward(),
                                    task.getCoinReward(),
                                    task.getDifficulty()
                                );
                                newTask.setCompleted(true);
                                mainUser.getTasks().add(newTask);
                                System.out.println("Added new completed task to main account: " + task.getDescription());
                            }
                        }
                    }

                    // Save the updated main user data
                    DataManager.saveUser(mainUser);

                    // Upload stats to Firebase to update the global leaderboard
                    util.FirebaseManager.uploadUserStats(mainUser);

                    // Calculate actual gains for display
                    int actualXPGained = mainUser.getXp() - beforeXP;
                    int levelsGained = mainUser.getLevel() - beforeLevel;
                    int actualCoinsGained = mainUser.getCoins() - beforeCoins;

                    // Show message about progress saved
                    String progressMessage = "Progress for " + user.getUsername() + " has been saved to main account!";
                    if (actualXPGained > 0) {
                        progressMessage += "\nXP gained: " + actualXPGained;
                    }
                    if (levelsGained > 0) {
                        progressMessage += "\nLevels gained: " + levelsGained;
                    }
                    if (actualCoinsGained > 0) {
                        progressMessage += "\nCoins gained: " + actualCoinsGained;
                    }
                    if (completedTasksInSession > 0) {
                        progressMessage += "\nTasks completed: " + completedTasksInSession;
                    }

                    JOptionPane.showMessageDialog(this,
                            progressMessage,
                            "Progress Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // This is a guest user, just save their current state
                DataManager.saveUser(user);
            }
        }
    }
}
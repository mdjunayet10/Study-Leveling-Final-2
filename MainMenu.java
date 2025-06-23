//ui->MainMenu

package ui;

import models.User;
import util.FirebaseManager;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    private User user;
    private static User currentUser;
    private static MainMenu currentInstance;

    private JLabel levelLabel;
    private JLabel xpLabel;
    private JLabel coinLabel;

    public MainMenu(User user) {
        this.user = user;
        MainMenu.currentUser = user;
        MainMenu.currentInstance = this;

        setTitle("📘 Study Leveling");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(173, 216, 230));

        JLabel userLabel = new JLabel("👤 USER: " + user.getUsername());
        userLabel.setBounds(30, 20, 200, 30);
        styleTopLabel(userLabel);

        levelLabel = new JLabel("🧪 LEVEL: " + user.getLevel());
        levelLabel.setBounds(570, 20, 180, 30);
        styleTopLabel(levelLabel);

        xpLabel = new JLabel("⭐ XP: " + user.getXp());
        xpLabel.setBounds(570, 60, 180, 30);
        styleTopLabel(xpLabel);

        coinLabel = new JLabel("💰 COINS: " + user.getCoins());
        coinLabel.setBounds(570, 100, 180, 30);
        styleTopLabel(coinLabel);

        JPanel centralPanel = new JPanel();
        centralPanel.setBounds(200, 150, 400, 420);
        centralPanel.setBackground(Color.BLACK);
        centralPanel.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel title = new JLabel("STUDY LEVELING");
        title.setForeground(Color.YELLOW);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton startButton = createStyledButton("START");
        JButton rewardsButton = createStyledButton("REWARDS");
        JButton progressButton = createStyledButton("PROGRESS");
        JButton multiplayerButton = createStyledButton("MULTIPLAYER MODE");
        JButton leaderboardButton = createStyledButton("GLOBAL LEADERBOARD");

        startButton.addActionListener(e -> new StudyScreen(this));
        rewardsButton.addActionListener(e -> new RewardScreen(user, this));
        progressButton.addActionListener(e -> new ProgressScreen());
        multiplayerButton.addActionListener(e -> openMultiplayerMode());
        leaderboardButton.addActionListener(e -> new GlobalLeaderboardScreen());

        centralPanel.add(title);
        centralPanel.add(startButton);
        centralPanel.add(rewardsButton);
        centralPanel.add(progressButton);
        centralPanel.add(multiplayerButton);
        centralPanel.add(leaderboardButton);

        add(userLabel);
        add(levelLabel);
        add(xpLabel);
        add(coinLabel);
        add(centralPanel);

        setVisible(true);

        // Upload user stats to Firebase
        FirebaseManager.uploadUserStats(user);
    }
    public User getUser() {
        return user;
    }

    public void refreshStats() {
        levelLabel.setText("🧪 LEVEL: " + user.getLevel());
        xpLabel.setText("⭐ XP: " + user.getXp());
        coinLabel.setText("💰 COINS: " + user.getCoins());
    }

    private void styleTopLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(199, 21, 133));
        label.setFont(new Font("Monospaced", Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(147, 112, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }

    private void openMultiplayerMode() {
        String[] options = {"2", "3", "4"};
        String input = (String) JOptionPane.showInputDialog(
                this,
                "Select number of players:",
                "Multiplayer Mode",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                "2"
        );
        if (input != null) {
            int numPlayers = Integer.parseInt(input);
            new MultiplayerLoginScreen(numPlayers);
        }
    }

    // Static methods to access current user and instance
    public static User getCurrentUser() {
        return currentUser;
    }

    public static MainMenu getCurrentInstance() {
        return currentInstance;
    }

    // Method to update user data from multiplayer mode
    public void updateUserFromMultiplayer(int xpGained, int coinsGained) {
        // Add the gained XP and coins to the main user
        if (xpGained > 0) {
            user.addXP(xpGained);
        }

        if (coinsGained > 0) {
            user.addCoins(coinsGained);
        }

        // Update the UI
        refreshStats();

        // Save the updated user data
        util.DataManager.saveUser(user);

        // Upload updated stats to Firebase
        FirebaseManager.uploadUserStats(user);
    }
}
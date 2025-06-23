//ui->MultiplayerLoginScreen
package ui;

import models.User;
import util.DataManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerLoginScreen extends JFrame {
    private final int maxPlayers;
    private int currentPlayer = 1;
    private final List<User> loggedInUsers = new ArrayList<>();

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ");

    public MultiplayerLoginScreen(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        setupUI();
        promptNextPlayer();
    }

    private void setupUI() {
        setTitle("🤝 Multiplayer Login - Study Leveling");
        setSize(500, 380);  // Further increased width and height
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(173, 216, 230));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);  // Increased horizontal insets
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("🎮 Player Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(199, 21, 133));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a panel just for the title to ensure it's centered
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titlePanel, gbc);

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;

        // Username
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("👤 Username:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("🔑 Password:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // Buttons
        JButton signInButton = createStyledButton("🔓 Sign In");
        JButton guestButton = createStyledButton("🎭 Continue as Guest");

        signInButton.addActionListener(e -> signIn());
        guestButton.addActionListener(e -> promptGuestUsername());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(signInButton);
        buttonPanel.add(guestButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Message label
        messageLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        messageLabel.setForeground(Color.RED);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 0, 10);
        add(messageLabel, gbc);

        setVisible(true);
    }

    private void promptNextPlayer() {
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText("🎯 Player " + currentPlayer + ": Sign in or join as guest");
    }

    private void signIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("⚠️ Please enter both username and password.");
            return;
        }

        if (!DataManager.userExists(username)) {
            messageLabel.setText("❌ User not found. Sign up first.");
            return;
        }

        if (!DataManager.verifyPassword(username, password)) {
            messageLabel.setText("❌ Incorrect password.");
            return;
        }

        User user = DataManager.loadUser(username);
        if (user == null) {
            messageLabel.setText("⚠️ Failed to load user data.");
            return;
        }

        loggedInUsers.add(user);
        nextOrLaunch();
    }

    private void promptGuestUsername() {
        String guestName = JOptionPane.showInputDialog(
                this,
                "Enter a name for Guest Player " + currentPlayer + ":",
                "Guest Login",
                JOptionPane.PLAIN_MESSAGE
        );

        if (guestName != null && !guestName.trim().isEmpty()) {
            continueAsGuest(guestName.trim());
        } else {
            messageLabel.setText("⚠️ Guest name cannot be empty.");
        }
    }

    private void continueAsGuest(String name) {
        User guest = new User(name);
        loggedInUsers.add(guest);
        nextOrLaunch();
    }

    private void nextOrLaunch() {
        if (currentPlayer < maxPlayers) {
            currentPlayer++;
            promptNextPlayer();
        } else {
            dispose();
            // Pass the parent MainMenu instance to MultiplayerStudyScreen if the first user is from MainMenu
            MainMenu mainMenu = null;
            for (User user : loggedInUsers) {
                if (user.getUsername().equals(MainMenu.getCurrentUser().getUsername())) {
                    mainMenu = MainMenu.getCurrentInstance();
                    break;
                }
            }
            new MultiplayerStudyScreen(loggedInUsers, mainMenu);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(147, 112, 219));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }
}
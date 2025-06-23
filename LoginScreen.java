//ui->LoginScreen
package ui;

import models.User;
import util.DataManager;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginScreen() {
        setTitle("🔐 Study Leveling - Login");
        setSize(420, 280);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(173, 216, 230)); // Light blue

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);

        // Title
        JLabel titleLabel = new JLabel("📘 Study Leveling Login");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(new Color(60, 60, 60));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // Username label & field
        JLabel usernameLabel = new JLabel("👤 Username:");
        usernameLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);

        // Password label & field
        JLabel passwordLabel = new JLabel("🔑 Password:");
        passwordLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // Message Label
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        messageLabel.setForeground(Color.RED);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(messageLabel, gbc);

        // Buttons
        JButton signInButton = createStyledButton("🚪 Sign In");
        JButton signUpButton = createStyledButton("🆕 Sign Up");

        signInButton.addActionListener(e -> signIn());
        signUpButton.addActionListener(e -> signUp());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(173, 216, 230));
        buttonPanel.add(signInButton);
        buttonPanel.add(signUpButton);

        gbc.gridy = 4;
        add(buttonPanel, gbc);

        setVisible(true);
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

    private void signIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setMessage("❗ Enter both username and password!", Color.RED);
            return;
        }

        if (!DataManager.userExists(username)) {
            setMessage("❗ User not found. Please sign up.", Color.RED);
            return;
        }

        if (!DataManager.verifyPassword(username, password)) {
            setMessage("❗ Incorrect password!", Color.RED);
            return;
        }

        User user = DataManager.loadUser(username);
        if (user == null) {
            setMessage("⚠ Could not load user data.", Color.RED);
            return;
        }

        setMessage("✅ Welcome back, " + username + "!", new Color(0, 128, 0));
        openMainMenu(user);
    }

    private void signUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setMessage("❗ Username and password required!", Color.RED);
            return;
        }

        if (DataManager.userExists(username)) {
            setMessage("❗ Username already taken.", Color.RED);
            return;
        }

        User newUser = new User(username);
        DataManager.saveUser(newUser);
        DataManager.savePassword(username, password);

        setMessage("🎉 Account created!", new Color(0, 128, 0));
        openMainMenu(newUser);
    }

    private void setMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    private void openMainMenu(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> new MainMenu(user));

    }
}
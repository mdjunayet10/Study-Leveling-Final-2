//ui->RewardScreen
package ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import models.User;
import models.Reward;

public class RewardScreen extends JFrame {
    private User user;
    private JLabel coinsLabel;
    private MainMenu mainMenu;

    public RewardScreen(User user, MainMenu mainMenu) {
        this.user = user;
        this.mainMenu = mainMenu;

        setTitle("Break Rewards");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        List<Reward> rewards = Arrays.asList(
                new Reward("NETFLIX 30 MINS", 150),
                new Reward("VIDEO GAMES", 250),
                new Reward("GO OUT", 150),
                new Reward("1 HOUR BREAK", 300)
        );

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(147, 184, 239));

        JPanel rewardsPanel = new JPanel(new GridLayout(0, 1, 20, 20));
        rewardsPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        rewardsPanel.setBackground(new Color(147, 184, 239));

        for (Reward reward : rewards) {
            rewardsPanel.add(createRewardPanel(reward));
        }

        coinsLabel = new JLabel("Coins: " + user.getCoins());
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        coinsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coinsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(coinsLabel, BorderLayout.NORTH);
        mainPanel.add(rewardsPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createRewardPanel(Reward reward) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 222, 102));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setPreferredSize(new Dimension(300, 60));

        JLabel label = new JLabel(reward.toString());
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> {
            if (user.spendCoins(reward.getCost())) {
                JOptionPane.showMessageDialog(null, "You redeemed: " + reward.getName());
                coinsLabel.setText("Coins: " + user.getCoins());
                mainMenu.refreshStats();  // Updates MainMenu
            } else {
                JOptionPane.showMessageDialog(null, "Not enough coins!");
            }
        });

        panel.add(label, BorderLayout.CENTER);
        panel.add(buyButton, BorderLayout.EAST);

        return panel;
    }
}

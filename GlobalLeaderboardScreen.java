package ui;//ui->GlobalLeaderboardScreen
import com.google.firebase.database.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalLeaderboardScreen extends JFrame {

    public GlobalLeaderboardScreen() {
        setTitle("🌍 Global Leaderboard");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(173, 216, 230)); // Light blue
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("🌍 Global Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(199, 21, 133)); // Dark pink
        titleLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        titleLabel.setPreferredSize(new Dimension(700, 50));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"🏅 Rank", "👤 Username", "🧪 Level", "⭐ XP", "✅ Tasks"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table read-only
            }
        };

        JTable leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(28);
        leaderboardTable.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 15));
        leaderboardTable.getTableHeader().setBackground(new Color(147, 112, 219));
        leaderboardTable.getTableHeader().setForeground(Color.WHITE);

        // Center-align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📊 Rankings"));
        add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = createStyledButton("❌ Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadLeaderboardData(tableModel);

        setVisible(true);
    }

    private void loadLeaderboardData(DefaultTableModel tableModel) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("leaderboard");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<UserData> users = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String username = child.getKey();
                    Integer level = child.child("level").getValue(Integer.class);
                    Integer xp = child.child("xp").getValue(Integer.class);
                    Integer completedTasks = child.child("completedTasks").getValue(Integer.class);

                    if (username != null && level != null && xp != null && completedTasks != null) {
                        users.add(new UserData(username, level, xp, completedTasks));
                    }
                }

                // Sort by level, then XP
                users.sort((u1, u2) -> {
                    if (u2.getLevel() != u1.getLevel()) {
                        return Integer.compare(u2.getLevel(), u1.getLevel());
                    } else {
                        return Integer.compare(u2.getXp(), u1.getXp());
                    }
                });

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); // clear table
                    int rank = 1;
                    for (UserData user : users) {
                        tableModel.addRow(new Object[]{
                                rank++, user.getUsername(), user.getLevel(), user.getXp(), user.getCompletedTasks()
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                JOptionPane.showMessageDialog(GlobalLeaderboardScreen.this,
                        "Failed to load leaderboard data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
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

    // Helper class
    private static class UserData {
        private final String username;
        private final int level;
        private final int xp;
        private final int completedTasks;

        public UserData(String username, int level, int xp, int completedTasks) {
            this.username = username;
            this.level = level;
            this.xp = xp;
            this.completedTasks = completedTasks;
        }

        public String getUsername() { return username; }
        public int getLevel() { return level; }
        public int getXp() { return xp; }
        public int getCompletedTasks() { return completedTasks; }
    }
}
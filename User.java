package models;

import java.util.ArrayList;

public class User {
    private String username;
    private int xp;
    private int level;
    private int coins;
    private ArrayList<Task> tasks = new ArrayList<>();
    private int totalCompletedTasks = 0; // Track total completed tasks, even if deleted later

    public User(String username) {
        this.username = username;
        this.xp = 0;
        this.level = 1;
        this.coins = 0;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public int getCoins() {
        return coins;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public int getTotalCompletedTasks() {
        return totalCompletedTasks;
    }

    // Add XP and handle leveling up
    public void addXP(int amount) {
        xp += amount;
        while (xp >= xpNeeded(level)) {
            xp -= xpNeeded(level);
            level++;
            coins += 50; // bonus on level-up
        }
    }

    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        } else {
            return false;
        }
    }

    // Add coins safely
    public void addCoins(int amount) {
        coins += amount;
    }

    // XP required for next level
    public int xpNeeded(int level) {
        return 100 * (int) Math.pow(1.5, level - 1);
    }

    // Setter methods for synchronization
    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    // Initial state tracking for multiplayer mode
    private int initialXp = 0;
    private int initialLevel = 0;
    private int initialCoins = 0;
    private boolean trackingInitialized = false;

    public void initializeTracking() {
        this.initialXp = this.xp;
        this.initialLevel = this.level;
        this.initialCoins = this.coins;
        this.trackingInitialized = true;
        System.out.println("Initialized tracking for " + username + ": XP=" + initialXp + ", Coins=" + initialCoins);
    }

    public void resetTracking() {
        this.trackingInitialized = false;
    }

    public int getInitialXp() {
        return initialXp;
    }

    public int getInitialLevel() {
        return initialLevel;
    }

    public int getInitialCoins() {
        return initialCoins;
    }

    public boolean isTrackingInitialized() {
        return trackingInitialized;
    }

    // Calculate XP and coins gained during multiplayer session
    public int getXpGainedInSession() {
        return trackingInitialized ? (xp - initialXp) : 0;
    }

    public int getCoinsGainedInSession() {
        return trackingInitialized ? (coins - initialCoins) : 0;
    }

    // Method to increment the total completed tasks counter
    public void incrementCompletedTasksCounter() {
        totalCompletedTasks++;
    }
}
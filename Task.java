package models;

public class Task {
    private String description;
    private int xpReward;
    private int coinReward;
    private Difficulty difficulty;
    private boolean completed;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    // No-arg constructor for Gson
    public Task() {
        this.description = "";
        this.xpReward = 0;
        this.coinReward = 0;
        this.difficulty = Difficulty.EASY;
        this.completed = false;
    }

    public Task(String description, int xp, int coins, Difficulty difficulty) {
        this.description = description;
        this.xpReward = xp;
        this.coinReward = coins;
        this.difficulty = (difficulty != null) ? difficulty : Difficulty.EASY;
        this.completed = false;
    }

    public String getDescription() {
        return description;
    }

    public int getXpReward() {
        return xpReward;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public Difficulty getDifficulty() {
        return (difficulty != null) ? difficulty : Difficulty.EASY;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return (completed ? "[✓] " : "[ ] ") + description +
                " (XP: " + xpReward + ", Coins: " + coinReward + ", Difficulty: " + getDifficulty()+")";
    }
}
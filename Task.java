package models;

import java.time.LocalDate;

public class Task {
    private String description;
    private int xpReward;
    private int coinReward;
    private Difficulty difficulty;
    private boolean completed;
    private LocalDate completionDate;

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
        this.completionDate = null;
    }

    public Task(String description, int xp, int coins, Difficulty difficulty) {
        this.description = description;
        this.xpReward = xp;
        this.coinReward = coins;
        this.difficulty = (difficulty != null) ? difficulty : Difficulty.EASY;
        this.completed = false;
        this.completionDate = null;
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

        // Only set completion date if it's being marked as completed and doesn't already have a date
        if (completed && this.completionDate == null) {
            this.completionDate = LocalDate.now();
        }
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public String toString() {
        String status = completed ? "✓" : "○";
        return status + " " + description + " [" + difficulty + "] ⭐" + xpReward + " 💰" + coinReward;
    }
}

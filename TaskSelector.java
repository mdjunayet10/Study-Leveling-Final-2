//util->TaskSelector
package util;

import models.Task;
import java.util.*;

public class TaskSelector {

    public static int getEffort(Task.Difficulty difficulty) {
        if (difficulty == null) return 0; // Prevent crash
        switch (difficulty) {
            case EASY: return 2;
            case MEDIUM: return 5;
            case HARD: return 8;
            default: return 0;
        }
    }

    public static int getMaxEffortForLevel(int level) {
        return 10 + (level - 1) * 5;
    }

    // Original method for selecting optimal tasks based on effort
    public static List<Task> selectOptimalTasks(List<Task> tasks, int level) {
        int maxEffort = getMaxEffortForLevel(level);
        int n = tasks.size();

        int[] effort = new int[n];
        int[] value = new int[n];
        for (int i = 0; i < n; i++) {
            Task task = tasks.get(i);
            effort[i] = getEffort(task.getDifficulty());
            value[i] = task.getXpReward() + task.getCoinReward();
        }

        boolean[][] dp = new boolean[n + 1][maxEffort + 1];
        dp[0][0] = true;
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= maxEffort; j++) {
                if (j >= effort[i - 1]) {
                    dp[i][j] = dp[i - 1][j] || dp[i - 1][j - effort[i - 1]];
                } else {
                    dp[i][j] = dp[i - 1][j];
                }
            }
        }

        int bestValue = -1;
        List<Task> bestSet = new ArrayList<>();

        for (int j = maxEffort; j >= 0; j--) {
            if (dp[n][j]) {
                List<Task> chosen = new ArrayList<>();
                int w = j;
                for (int i = n; i >= 1; i--) {
                    if (w >= effort[i - 1] && dp[i - 1][w - effort[i - 1]]) {
                        chosen.add(tasks.get(i - 1));
                        w -= effort[i - 1];
                    }
                }

                int totalValue = chosen.stream()
                        .mapToInt(t -> t.getXpReward() + t.getCoinReward())
                        .sum();
                long hardCount = chosen.stream()
                        .filter(t -> t.getDifficulty() == Task.Difficulty.HARD)
                        .count();

                if (totalValue > bestValue ||
                        (totalValue == bestValue &&
                                hardCount > bestSet.stream().filter(t -> t.getDifficulty() == Task.Difficulty.HARD).count())) {
                    bestValue = totalValue;
                    bestSet = chosen;
                }
            }
        }

        return bestSet;
    }

    /**
     * Prioritizes tasks based on XP and Coins using 0/1 Knapsack algorithm.
     * Higher XP and Coins will have higher priority.
     * @param tasks List of tasks to prioritize
     * @return List of tasks sorted by priority (highest priority first)
     */
    public static List<Task> prioritizeTasks(List<Task> tasks) {
        // Filter out completed tasks
        List<Task> incompleteTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                incompleteTasks.add(task);
            }
        }

        // If no incomplete tasks, return empty list
        if (incompleteTasks.isEmpty()) {
            return incompleteTasks;
        }

        // Create a copy of tasks with their priorities
        List<TaskPriority> taskPriorities = new ArrayList<>();
        for (Task task : incompleteTasks) {
            int priority = task.getXpReward() + task.getCoinReward();
            taskPriorities.add(new TaskPriority(task, priority));
        }

        // Sort tasks by priority (highest first)
        Collections.sort(taskPriorities, (a, b) -> Integer.compare(b.priority, a.priority));

        // Extract tasks in priority order
        List<Task> prioritizedTasks = new ArrayList<>();
        for (TaskPriority tp : taskPriorities) {
            prioritizedTasks.add(tp.task);
        }

        return prioritizedTasks;
    }

    // Helper class to store task with its priority
    private static class TaskPriority {
        Task task;
        int priority;

        TaskPriority(Task task, int priority) {
            this.task = task;
            this.priority = priority;
        }
    }
}
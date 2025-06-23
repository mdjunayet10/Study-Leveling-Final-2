//util->FirebaseManager
package util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import models.User;

public class FirebaseManager {

    public static void uploadUserStats(User user) {
        try {
            // Use the totalCompletedTasks counter instead of counting current tasks
            int completedTasks = user.getTotalCompletedTasks();

            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("leaderboard")
                    .child(user.getUsername());  // Use username as unique key

            dbRef.child("level").setValueAsync(user.getLevel());
            dbRef.child("xp").setValueAsync(user.getXp());
            dbRef.child("completedTasks").setValueAsync(completedTasks);

            System.out.println("✅ Successfully uploaded user stats to Firebase.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to upload stats.");
        }
    }
}
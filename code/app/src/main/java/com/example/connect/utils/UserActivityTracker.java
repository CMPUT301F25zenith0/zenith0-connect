package com.example.connect.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for tracking user activity status in the app.
 * Updates Firestore with user's active status and last active timestamp.
 * 
 * @author Aakansh Chatterjee
 * @version 1.0
 */
public class UserActivityTracker {
    private static final String TAG = "UserActivityTracker";
    private static final String COLLECTION_ACCOUNTS = "accounts";
    private static final String FIELD_IS_ACTIVE = "is_active";
    private static final String FIELD_LAST_ACTIVE = "last_active_timestamp";
    
    // Time threshold in milliseconds (5 minutes) - users inactive for longer are considered offline
    private static final long INACTIVITY_THRESHOLD = 5 * 60 * 1000; // 5 minutes

    /**
     * Marks the current user as active in Firestore.
     * Should be called when user opens the app or resumes an activity.
     */
    public static void markUserActive() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user, skipping activity update");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTimestamp = System.currentTimeMillis();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(FIELD_IS_ACTIVE, true);
        updateData.put(FIELD_LAST_ACTIVE, currentTimestamp);

        db.collection(COLLECTION_ACCOUNTS).document(currentUser.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User marked as active: " + currentUser.getUid());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark user as active: " + e.getMessage(), e);
                });
    }

    /**
     * Marks the current user as inactive in Firestore.
     * Should be called when user closes the app or pauses an activity.
     */
    public static void markUserInactive() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No authenticated user, skipping inactivity update");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(FIELD_IS_ACTIVE, false);

        db.collection(COLLECTION_ACCOUNTS).document(currentUser.getUid())
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User marked as inactive: " + currentUser.getUid());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark user as inactive: " + e.getMessage(), e);
                });
    }

    /**
     * Checks if a user should be considered active based on their last active timestamp.
     * A user is considered active if:
     * - is_active field is true AND
     * - last_active_timestamp is within the inactivity threshold
     * 
     * @param isActive The is_active field value from Firestore
     * @param lastActiveTimestamp The last_active_timestamp field value from Firestore
     * @return true if user should be considered active, false otherwise
     */
    public static boolean isUserCurrentlyActive(Boolean isActive, Long lastActiveTimestamp) {
        if (isActive == null || !isActive) {
            return false;
        }

        if (lastActiveTimestamp == null) {
            return false;
        }

        long timeSinceLastActive = System.currentTimeMillis() - lastActiveTimestamp;
        return timeSinceLastActive <= INACTIVITY_THRESHOLD;
    }

}


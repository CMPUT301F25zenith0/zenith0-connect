package com.example.connect.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationHelper writes notifications to user accounts
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private final FirebaseFirestore db;

    public NotificationHelper() {
        this.db = FirebaseFirestore.getInstance();
        Log.d(TAG, "NotificationHelper initialized, Firestore instance created");
    }

    /**
     * Notify users who were chosen for an event
     */
    public void notifyChosenEntrants(String eventId, List<String> chosenEntrantIds,
                                     String eventName, NotificationCallback callback) {
        Log.d(TAG, "notifyChosenEntrants called | eventId=" + eventId +
                " | entrants=" + chosenEntrantIds.size() + " | eventName=" + eventName);

        if (chosenEntrantIds.isEmpty()) {
            Log.w(TAG, "No entrants provided for chosen notification");
            callback.onFailure("No entrants to notify");
            return;
        }

        String title = "Congratulations! 🎉";
        String body = "You have been selected for " + eventName + "!";
        String type = "chosen";

        sendNotificationsToUsers(chosenEntrantIds, title, body, type, eventId, eventName, callback);
    }

    /**
     * Notify users who were NOT chosen for an event
     */
    public void notifyNotChosenEntrants(String eventId, List<String> notChosenEntrantIds,
                                        String eventName, NotificationCallback callback) {
        Log.d(TAG, "notifyNotChosenEntrants called | eventId=" + eventId +
                " | entrants=" + notChosenEntrantIds.size() + " | eventName=" + eventName);

        if (notChosenEntrantIds.isEmpty()) {
            Log.w(TAG, "No entrants provided for not-chosen notification");
            callback.onFailure("No entrants to notify");
            return;
        }

        String title = "Event Update";
        String body = "Thank you for your interest in " + eventName +
                ". Unfortunately, you were not selected this time.";
        String type = "not_chosen";

        sendNotificationsToUsers(notChosenEntrantIds, title, body, type, eventId, eventName, callback);
    }

    /**
     * Notify all users in the waiting list for an event
     */
    public void notifyAllWaitingListEntrants(String eventId, List<String> waitingListIds,
                                             String eventName, NotificationCallback callback) {
        Log.d(TAG, "notifyAllWaitingListEntrants called | eventId=" + eventId +
                " | entrants=" + waitingListIds.size() + " | eventName=" + eventName);

        if (waitingListIds.isEmpty()) {
            Log.w(TAG, "No entrants provided for waiting list notification");
            callback.onFailure("No entrants to notify");
            return;
        }

        String title = eventName + " - Update";
        String body = "Thank you for your interest in " + eventName +
                ". You have been placed in the waiting list for this event.";
        String type = "waiting_list_announcement";

        sendNotificationsToUsers(waitingListIds, title, body, type, eventId, eventName, callback);
    }

    /**
     * Sends notifications to the specified users.
     *
     * New DB structure:
     * - User documents stored in: /users/{userId}
     * - Notifications stored in: /users/{userId}/notifications/{notifId}
     *
     * Logic:
     * 1. Fetch each user's notification preference.
     * 2. If enabled → save notification under their notifications collection.
     * 3. Count how many succeeded / skipped.
     */
    private void sendNotificationsToUsers(List<String> userIds, String title, String body,
                                          String type, String eventId, String eventName,
                                          NotificationCallback callback) {
        if (userIds.isEmpty()) {
            callback.onFailure("No users to notify");
            return;
        }

        final int totalUsers = userIds.size();
        final int[] processedCount = {0};
        final int[] notifiedCount = {0};
        final int[] skippedCount = {0};

        for (String userId : userIds) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled") != null
                                ? documentSnapshot.getBoolean("notificationsEnabled")
                                : true;

                        if (!notificationsEnabled) {
                            skippedCount[0]++;
                            processedCount[0]++;
                            if (processedCount[0] == totalUsers) {
                                callback.onSuccess("Notifications sent to " + notifiedCount[0] +
                                        ", skipped " + skippedCount[0]);
                            }
                            return;
                        }

                        Map<String, Object> notificationData = new HashMap<>();
                        notificationData.put("title", title);
                        notificationData.put("body", body);
                        notificationData.put("type", type);
                        notificationData.put("eventId", eventId);
                        notificationData.put("eventName", eventName);
                        notificationData.put("timestamp", FieldValue.serverTimestamp());
                        notificationData.put("read", false);

                        db.collection("users").document(userId)
                                .collection("notifications")
                                .add(notificationData)
                                .addOnSuccessListener(docRef -> {
                                    notifiedCount[0]++;
                                    processedCount[0]++;
                                    if (processedCount[0] == totalUsers) {
                                        callback.onSuccess("Notifications sent to " + notifiedCount[0] +
                                                ", skipped " + skippedCount[0]);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    processedCount[0]++;
                                    if (processedCount[0] == totalUsers) {
                                        callback.onSuccess("Notifications sent to " + notifiedCount[0] +
                                                ", skipped " + skippedCount[0]);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        processedCount[0]++;
                        if (processedCount[0] == totalUsers) {
                            callback.onSuccess("Notifications sent to " + notifiedCount[0] +
                                    ", skipped " + skippedCount[0] + " (some errors)");
                        }
                    });
        }
    }

    public interface NotificationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}

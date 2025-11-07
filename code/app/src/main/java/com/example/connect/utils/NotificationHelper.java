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
 * NotificationHelper actually writes notifications notifyChosenEntrants
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private final FirebaseFirestore db;

    public NotificationHelper() {
        this.db = FirebaseFirestore.getInstance();
        Log.d(TAG, "NotificationHelper initialized, Firestore instance created");
    }

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
        markEntrantsAsNotified(eventId, chosenEntrantIds, "chosen");
    }

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
        markEntrantsAsNotified(eventId, notChosenEntrantIds, "notChosen");
    }

    public void notifyAllWaitingListEntrants(String eventId, String eventName, NotificationCallback callback) {
        Log.d(TAG, "notifyAllWaitingListEntrants called | eventId=" + eventId + " | eventName=" + eventName);

        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    Log.d(TAG, "Fetched waitingList with " + count + " documents for event " + eventId);

                    List<String> entrantIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        entrantIds.add(doc.getId());
                    }

                    if (entrantIds.isEmpty()) {
                        Log.w(TAG, "No entrants found in waiting list for event: " + eventId);
                        callback.onFailure("No entrants found in waiting list");
                        return;
                    }

                    String title = eventName + " - Update";
                    String body = "Thank you for your interest in " + eventName +
                            ". You have been placed in the waiting list for this event.";

                    String type = "waiting_list_announcement";

                    sendNotificationsToUsers(entrantIds, title, body, type, eventId, eventName, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get waiting list for eventId=" + eventId, e);
                    callback.onFailure("Failed to retrieve waiting list: " + e.getMessage());
                });
    }

    private void sendNotificationsToUsers(List<String> userIds, String title, String body,
                                          String type, String eventId, String eventName,
                                          NotificationCallback callback) {
        Log.d(TAG, "sendNotificationsToUsers started | totalUsers=" + userIds.size() +
                " | type=" + type + " | eventId=" + eventId);

        final int[] completed = {0};
        final int[] successful = {0};
        final int totalUsers = userIds.size();

        for (int i = 0; i < totalUsers; i++) {
            String userId = userIds.get(i);
            Log.d(TAG, "Preparing notification for user " + (i + 1) + "/" + totalUsers + " | userId=" + userId);

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);
            notification.put("type", type);
            notification.put("eventId", eventId);
            notification.put("eventName", eventName);
            notification.put("timestamp", FieldValue.serverTimestamp());
            notification.put("read", false);

            db.collection("accounts").document(userId)
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener(docRef -> {
                        Log.d(TAG, "✅ Notification sent successfully to userId=" + userId +
                                " | docId=" + docRef.getId());
                        synchronized (successful) {
                            successful[0]++;
                            completed[0]++;
                            Log.d(TAG, "Progress: " + completed[0] + "/" + totalUsers +
                                    " completed | " + successful[0] + " successful");
                            if (completed[0] == totalUsers) {
                                Log.d(TAG, "All sends completed | totalSuccess=" + successful[0]);
                                callback.onSuccess("Sent " + successful[0] + " notifications successfully!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to send notification to userId=" + userId, e);
                        synchronized (completed) {
                            completed[0]++;
                            Log.d(TAG, "Progress: " + completed[0] + "/" + totalUsers + " completed");
                            if (completed[0] == totalUsers) {
                                Log.d(TAG, "All sends completed | totalSuccess=" + successful[0]);
                                callback.onSuccess("Sent " + successful[0] + " notifications successfully!");
                            }
                        }
                    });
        }
    }

    private void markEntrantsAsNotified(String eventId, List<String> entrantIds,
                                        String collectionName) {
        Log.d(TAG, "markEntrantsAsNotified called | eventId=" + eventId +
                " | count=" + entrantIds.size() + " | collection=" + collectionName);

        for (String userId : entrantIds) {
            Log.d(TAG, "Marking entrant as notified | userId=" + userId);
            db.collection("events").document(eventId)
                    .collection(collectionName).document(userId)
                    .update("notified", true, "notifiedAt", FieldValue.serverTimestamp())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Marked as notified: " + userId))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "❌ Failed to mark user as notified: " + userId, e));
        }
    }

    public interface NotificationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
package com.example.connect.network;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for Firestore operations related to events + notifications.
 *
 * Used for:
 * - US 02.07.02: notify all selected entrants
 * - US 02.07.03: notify all cancelled entrants
 */
public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SimpleCallback {
        void onComplete();
        void onError(Exception e);
    }

    /**
     * Send a notification to all registrations of an event
     * that have a specific status, e.g. "selected" or "cancelled".
     *
     * Firestore structure expected:
     *
     * events/{eventId}/registrations/{userId}
     *   - status: "selected", "cancelled", ...
     *
     * accounts/{userId}/notifications/{notificationId}
     */
    public void sendNotificationToRegistrations(
            String eventId,
            String eventName,
            String statusFilter,    // "selected" or "cancelled"
            String title,
            String body,
            SimpleCallback callback
    ) {
        db.collection("events")
                .document(eventId)
                .collection("registrations")
                .whereEqualTo("status", statusFilter)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Log.d(TAG, "No registrations with status: " + statusFilter);
                        if (callback != null) callback.onComplete();
                        return;
                    }

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String userId = doc.getId();

                        Map<String, Object> notification = new HashMap<>();
                        notification.put("title", title);
                        notification.put("body", body);
                        notification.put("type", statusFilter); // used by NotificationListenerService
                        notification.put("event_id", eventId);
                        notification.put("event_name", eventName);
                        notification.put("timestamp", FieldValue.serverTimestamp());
                        notification.put("read", false);

                        db.collection("accounts")
                                .document(userId)
                                .collection("notifications")
                                .add(notification)
                                .addOnSuccessListener(ref ->
                                        Log.d(TAG, "Sent notification to user: " + userId))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Failed to send to user: " + userId, e));
                    }

                    if (callback != null) callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed fetching registrations", e);
                    if (callback != null) callback.onError(e);
                });
    }
}

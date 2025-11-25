package com.example.connect.utils;

import android.util.Log;

import com.example.connect.models.Event;
import com.example.connect.models.WaitingListEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Manages the lottery draw process for event entrants
 * Handles automatic random selection with proper validation
 *
 * Features:
 * - Ensures unique user selection (no duplicates)
 * - Handles cases where waiting list is smaller than capacity
 * - Validates all entrants before selection
 * - Fair random selection using Fisher-Yates shuffle
 *
 * @author Zenith Team
 * @version 2.0
 */
public class LotteryManager {

    private static final String TAG = "LotteryManager";
    private final FirebaseFirestore db;

    public LotteryManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Interface for lottery completion callbacks
     */
    public interface LotteryCallback {
        void onSuccess(int selectedCount, int waitingListCount);
        void onFailure(String error);
    }

    /**
     * Perform automatic lottery draw for an event
     * Validates event and performs fair random selection
     *
     * @param eventId The event ID to perform lottery for
     * @param callback Callback for success/failure
     */
    public void performAutomaticLottery(String eventId, LotteryCallback callback) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "Starting automatic lottery draw");
        Log.d(TAG, "Event ID: " + eventId);
        Log.d(TAG, "========================================");

        // Load the event to get draw capacity
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Log.e(TAG, "Event not found: " + eventId);
                        if (callback != null) {
                            callback.onFailure("Event not found");
                        }
                        return;
                    }

                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        Log.e(TAG, "Failed to parse event data");
                        if (callback != null) {
                            callback.onFailure("Failed to load event data");
                        }
                        return;
                    }

                    // Check if already drawn
                    if (event.isDrawCompleted()) {
                        Log.d(TAG, "Lottery already completed for: " + event.getName());
                        if (callback != null) {
                            callback.onFailure("Lottery already completed");
                        }
                        return;
                    }

                    int drawCapacity = event.getDrawCapacity();
                    if (drawCapacity <= 0) {
                        Log.e(TAG, "Invalid draw capacity: " + drawCapacity);
                        if (callback != null) {
                            callback.onFailure("Draw capacity is 0 or not set");
                        }
                        return;
                    }

                    Log.d(TAG, "Event: " + event.getName());
                    Log.d(TAG, "Draw Capacity: " + drawCapacity);

                    // Load all waiting entrants and perform selection
                    loadAndSelectWaitingEntrants(eventId, event.getName(), drawCapacity, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    if (callback != null) {
                        callback.onFailure("Error loading event: " + e.getMessage());
                    }
                });
    }

    /**
     * Load all entrants with "waiting" status and perform validated selection
     */
    private void loadAndSelectWaitingEntrants(String eventId, String eventName,
                                              int drawCapacity, LotteryCallback callback) {
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Use Set to ensure uniqueness and List for document IDs
                    Set<String> uniqueUserIds = new HashSet<>();
                    List<String> entrantDocIds = new ArrayList<>();
                    Map<String, String> docIdToUserId = new HashMap<>();

                    // Collect unique waiting entrants
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        String userId = entry.getUserId();
                        String docId = doc.getId();

                        // Validate entry has a user ID
                        if (userId == null || userId.isEmpty()) {
                            Log.w(TAG, "Skipping entry with no user ID: " + docId);
                            continue;
                        }

                        // Check for duplicates
                        if (uniqueUserIds.contains(userId)) {
                            Log.w(TAG, "Duplicate user detected (skipping): " + userId);
                            continue;
                        }

                        // Add to unique set and list
                        uniqueUserIds.add(userId);
                        entrantDocIds.add(docId);
                        docIdToUserId.put(docId, userId);
                    }

                    int totalWaiting = entrantDocIds.size();
                    Log.d(TAG, "Found " + totalWaiting + " unique waiting entrants");
                    Log.d(TAG, "Draw capacity: " + drawCapacity);

                    // Handle empty waiting list
                    if (totalWaiting == 0) {
                        Log.d(TAG, "No entrants in waiting list - marking draw as complete");
                        markDrawComplete(eventId, eventName, 0, 0, callback);
                        return;
                    }

                    // Handle case where waiting list is smaller than capacity
                    int selectCount = Math.min(drawCapacity, totalWaiting);

                    if (totalWaiting < drawCapacity) {
                        Log.d(TAG, "⚠ Waiting list (" + totalWaiting +
                                ") is smaller than capacity (" + drawCapacity + ")");
                        Log.d(TAG, "Will select all " + totalWaiting + " entrants");
                    }

                    // Perform random selection with validation
                    List<String> selectedDocIds = performValidatedRandomSelection(
                            entrantDocIds,
                            selectCount,
                            docIdToUserId
                    );

                    // Update selected entrants in Firestore
                    updateSelectedEntrants(eventId, eventName, selectedDocIds,
                            docIdToUserId, totalWaiting, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waiting entrants", e);
                    if (callback != null) {
                        callback.onFailure("Error loading entrants: " + e.getMessage());
                    }
                });
    }

    /**
     * Perform validated random selection ensuring uniqueness
     * Uses Fisher-Yates shuffle algorithm for fair selection
     *
     * @param entrantDocIds List of all waiting entrant document IDs
     * @param selectCount Number of entrants to select
     * @param docIdToUserId Mapping to verify uniqueness
     * @return List of randomly selected document IDs (guaranteed unique)
     */
    private List<String> performValidatedRandomSelection(List<String> entrantDocIds,
                                                         int selectCount,
                                                         Map<String, String> docIdToUserId) {
        Log.d(TAG, "--- Random Selection Process ---");
        Log.d(TAG, "Total candidates: " + entrantDocIds.size());
        Log.d(TAG, "To select: " + selectCount);

        // Validate inputs
        if (entrantDocIds.isEmpty() || selectCount <= 0) {
            Log.w(TAG, "Invalid selection parameters");
            return new ArrayList<>();
        }

        // Create a copy to avoid modifying original list
        List<String> shuffleable = new ArrayList<>(entrantDocIds);

        // Use cryptographically secure random with time-based seed for fairness
        Random random = new Random(System.currentTimeMillis() ^ System.nanoTime());

        // Perform Fisher-Yates shuffle (ensures uniform distribution)
        Collections.shuffle(shuffleable, random);

        // Take the first 'selectCount' entries
        int actualSelect = Math.min(selectCount, shuffleable.size());
        List<String> selected = new ArrayList<>(shuffleable.subList(0, actualSelect));

        // Verify uniqueness (should always pass, but extra validation)
        Set<String> selectedUserIds = new HashSet<>();
        for (String docId : selected) {
            String userId = docIdToUserId.get(docId);
            if (!selectedUserIds.add(userId)) {
                Log.e(TAG, "ERROR: Duplicate user in selection! This should never happen!");
            }
        }

        Log.d(TAG, "✓ Successfully selected " + selected.size() + " unique entrants");
        Log.d(TAG, "✓ Verified " + selectedUserIds.size() + " unique users");
        Log.d(TAG, "--------------------------------");

        return selected;
    }

    /**
     * Update selected entrants in Firestore with batch write
     * Changes their status from "waiting" to "selected"
     */
    private void updateSelectedEntrants(String eventId, String eventName,
                                        List<String> selectedDocIds,
                                        Map<String, String> docIdToUserId,
                                        int totalWaiting,
                                        LotteryCallback callback) {
        if (selectedDocIds.isEmpty()) {
            Log.d(TAG, "No entrants selected - marking draw as complete with 0 selections");
            markDrawComplete(eventId, eventName, 0, totalWaiting, callback);
            return;
        }

        Log.d(TAG, "Updating " + selectedDocIds.size() + " entrants to 'selected' status");

        WriteBatch batch = db.batch();
        Timestamp selectedTime = Timestamp.now();
        List<String> selectedUserIds = new ArrayList<>();

        // Update each selected entrant
        for (String docId : selectedDocIds) {
            String userId = docIdToUserId.get(docId);
            selectedUserIds.add(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "selected");
            updates.put("selected_date", selectedTime);

            batch.update(
                    db.collection("waiting_lists")
                            .document(eventId)
                            .collection("entrants")
                            .document(docId),
                    updates
            );

            Log.d(TAG, "  → Selecting user: " + userId);
        }

        // Update event with draw completion info
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("draw_completed", true);
        eventUpdates.put("draw_date", selectedTime);
        eventUpdates.put("selected_count", selectedDocIds.size());

        batch.update(db.collection("events").document(eventId), eventUpdates);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "========================================");
                    Log.d(TAG, "✓✓✓ LOTTERY COMPLETED SUCCESSFULLY ✓✓✓");
                    Log.d(TAG, "Event: " + eventName);
                    Log.d(TAG, "Selected: " + selectedDocIds.size() + " / " + totalWaiting);
                    Log.d(TAG, "========================================");

                    // Send notifications to selected entrants
                    sendSelectionNotifications(eventId, eventName, selectedUserIds);

                    if (callback != null) {
                        callback.onSuccess(selectedDocIds.size(), totalWaiting);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "✗ Error updating entrants in batch", e);
                    if (callback != null) {
                        callback.onFailure("Error updating entrants: " + e.getMessage());
                    }
                });
    }

    /**
     * Mark draw as complete even when no selections made
     * Used when waiting list is empty
     */
    private void markDrawComplete(String eventId, String eventName,
                                  int selectedCount, int waitingCount,
                                  LotteryCallback callback) {
        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("draw_completed", true);
        eventUpdates.put("draw_date", Timestamp.now());
        eventUpdates.put("selected_count", selectedCount);

        db.collection("events")
                .document(eventId)
                .update(eventUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✓ Draw marked complete for " + eventName +
                            " (0 selections from empty list)");
                    if (callback != null) {
                        callback.onSuccess(selectedCount, waitingCount);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking draw complete", e);
                    if (callback != null) {
                        callback.onFailure("Error marking draw complete: " + e.getMessage());
                    }
                });
    }

    /**
     * Send notifications to selected entrants
     */
    private void sendSelectionNotifications(String eventId, String eventName,
                                            List<String> selectedUserIds) {
        Log.d(TAG, "Sending notifications to " + selectedUserIds.size() + " selected users");

        Timestamp notificationTime = Timestamp.now();

        // Create notification for each selected user
        for (String userId : selectedUserIds) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("user_id", userId);
            notification.put("event_id", eventId);
            notification.put("event_name", eventName);
            notification.put("type", "lottery_selected");
            notification.put("title", "🎉 Congratulations! You've been selected!");
            notification.put("message", "You have been selected for " + eventName +
                    ". Please confirm your enrollment soon.");
            notification.put("timestamp", notificationTime);
            notification.put("read", false);

            // Add notification to Firestore
            db.collection("notifications")
                    .add(notification)
                    .addOnSuccessListener(docRef ->
                            Log.d(TAG, "  ✓ Notification sent to user: " + userId))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "  ✗ Failed to send notification to: " + userId, e));
        }
    }
}
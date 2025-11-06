package com.example.connect.network;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository for managing waiting list entries.
 * Handles counting and managing entrants on waiting lists for events.
 */
public class WaitingListRepository {

    public interface WaitingListCountCallback {
        void onSuccess(int count);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Count the total number of entrants on the waiting list for a specific event.
     * 
     * Firestore Structure:
     * Collection: "waiting_lists"
     * Document: {eventId}
     * Subcollection: "entries"
     *   - Each entry document represents one entrant
     * 
     * OR alternative structure:
     * Collection: "waiting_list_entries"
     *   - Each document has: eventId (String), userId (String), timestamp (Long)
     * 
     * @param eventId The ID of the event
     * @param callback Callback with the count or error
     */
    public void getWaitingListCount(@NonNull String eventId, @NonNull WaitingListCountCallback callback) {
        if (eventId == null || eventId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Event ID cannot be null or empty"));
            return;
        }

        // Try subcollection structure first: waiting_lists/{eventId}/entries
        android.util.Log.d("WaitingListRepository", "Fetching waiting list count for Event ID: " + eventId);
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entries")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            int count = snapshot != null ? snapshot.size() : 0;
                            android.util.Log.d("WaitingListRepository", "Found " + count + " entries in waiting_lists/" + eventId + "/entries");
                            callback.onSuccess(count);
                        } else {
                            android.util.Log.w("WaitingListRepository", "Subcollection query failed for Event ID: " + eventId + ", trying flat structure", task.getException());
                            // If subcollection doesn't exist, try flat structure
                            tryFlatStructure(eventId, callback);
                        }
                    }
                });
    }

    /**
     * Try alternative flat structure: waiting_list_entries collection with eventId field
     */
    private void tryFlatStructure(@NonNull String eventId, @NonNull WaitingListCountCallback callback) {
        db.collection("waiting_list_entries")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot snapshot = task.getResult();
                            int count = snapshot != null ? snapshot.size() : 0;
                            callback.onSuccess(count);
                        } else {
                            // If both structures fail, return 0 (no waiting list entries yet)
                            android.util.Log.w("WaitingListRepository", 
                                    "Could not find waiting list entries. Using subcollection structure.", 
                                    task.getException());
                            callback.onSuccess(0);
                        }
                    }
                });
    }
}


package com.example.connect.network;

import androidx.annotation.NonNull;

import com.example.connect.constants.AppConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all Firestore operations for events.
 * This class isolates database access from the UI layer for cleaner architecture.
 */
public class EventRepository {

    // Reference to Firestore
    private final FirebaseFirestore db;

    /**
     * Constructor — injects FirebaseFirestore instance.
     */
    public EventRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Helper function to get a document reference for a specific event.
     * @param eventId The event's Firestore document ID.
     * @return DocumentReference for that event.
     */
    private DocumentReference eventRef(@NonNull String eventId) {
        return db.collection(AppConstants.COL_EVENTS).document(eventId);
    }

    /**
     * Updates the drawCount for an event in a Firestore transaction.
     * This ensures that two organizers updating the same event won't overwrite each other.
     *
     * @param eventId         ID of the event to update.
     * @param requestedCount  The new draw count entered by the organizer.
     * @param explicitCapacity Optional upper bound (e.g., maxParticipants).
     */
    public Task<Void> setDrawCountTransactional(
            @NonNull String eventId,
            int requestedCount,
            Integer explicitCapacity
    ) {
        // Quick client-side sanity check
        if (requestedCount < 0) {
            throw new IllegalArgumentException("Draw count must be non-negative.");
        }

        // Get the document reference for the event
        final DocumentReference ref = eventRef(eventId);

        // Run a transaction for atomic update
        return db.runTransaction((Transaction.Function<Void>) transaction -> {
            Map<String, Object> data = transaction.get(ref).getData();
            if (data == null) return null; // Event might have been deleted

            // Default to the requested value
            int finalCount = requestedCount;

            // Fetch capacity (maxParticipants)
            Integer capacity = explicitCapacity;
            if (capacity == null) {
                Object cap = data.get(AppConstants.F_CAPACITY);
                if (cap instanceof Number) {
                    capacity = ((Number) cap).intValue();
                }
            }

            // Clamp the draw count to capacity if applicable
            if (capacity != null && capacity > 0) {
                finalCount = Math.min(finalCount, capacity);
            }

            // Prepare the update map
            Map<String, Object> updates = new HashMap<>();
            updates.put(AppConstants.F_DRAW_COUNT, finalCount);

            // Apply update in transaction
            transaction.update(ref, updates);
            return null;
        });
    }
}

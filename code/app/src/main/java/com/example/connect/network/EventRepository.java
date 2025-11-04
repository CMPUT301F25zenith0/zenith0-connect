package com.example.connect.network;

import androidx.annotation.NonNull;

import com.example.connect.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for reading events to display in the Event List.
 *
 * Firestore structure (minimal):
 *   Collection: "events"
 *   Document fields: name (String), date (String yyyy-MM-dd),
 *                    regOpens (String yyyy-MM-dd), regCloses (String yyyy-MM-dd),
 *                    maxParticipants (Number), posterUrl (String, optional)
 */
public class EventRepository {

    public interface EventsCallback {
        void onSuccess(List<Event> events);
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Fetch events ordered by regCloses ascending and filter joinable client-side.
     * (You can move the joinable filter server-side later.)
     */
    public void fetchJoinableEvents(@NonNull EventsCallback cb) {
        try {
            // Try with orderBy first, but catch if index doesn't exist
            Query q = db.collection("events").orderBy("regCloses", Query.Direction.ASCENDING);

            q.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    android.util.Log.e("EventRepository", "Query failed, trying without orderBy", e);
                    
                    // If orderBy fails (likely missing index), try without it
                    db.collection("events").get().addOnCompleteListener(task2 -> {
                        if (!task2.isSuccessful()) {
                            cb.onError(task2.getException());
                            return;
                        }
                        processResults(task2.getResult(), cb);
                    });
                    return;
                }
                processResults(task.getResult(), cb);
            });
        } catch (Exception e) {
            android.util.Log.e("EventRepository", "Error in fetchJoinableEvents", e);
            cb.onError(e);
        }
    }
    
    private void processResults(com.google.firebase.firestore.QuerySnapshot snapshot, EventsCallback cb) {
        try {
            List<Event> results = new ArrayList<>();
            if (snapshot != null && snapshot.getDocuments() != null) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    try {
                        Event e = doc.toObject(Event.class);
                        if (e == null) continue;
                        e.setId(doc.getId());
                        if (e.isJoinableToday()) {
                            results.add(e);
                        }
                    } catch (Exception ex) {
                        android.util.Log.w("EventRepository", "Error processing document " + doc.getId(), ex);
                    }
                }
            }
            cb.onSuccess(results);
        } catch (Exception e) {
            android.util.Log.e("EventRepository", "Error processing results", e);
            cb.onError(e);
        }
    }

    /** (Optional) raw list without filtering */
    public Task<com.google.firebase.firestore.QuerySnapshot> fetchAll() {
        return db.collection("events").get();
    }

}

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
        Query q = db.collection("events").orderBy("regCloses", Query.Direction.ASCENDING);

        q.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                cb.onError(task.getException());
                return;
            }
            List<Event> results = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e == null) continue;
                e.setId(doc.getId());
                if (e.isJoinableToday()) {
                    results.add(e);
                }
            }
            cb.onSuccess(results);
        });
    }

    /** (Optional) raw list without filtering */
    public Task<com.google.firebase.firestore.QuerySnapshot> fetchAll() {
        return db.collection("events").get();
    }
}

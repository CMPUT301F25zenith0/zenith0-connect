package com.example.connect.network;

import androidx.annotation.NonNull;

import com.example.connect.models.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                        // Manually read fields to handle both Timestamp and String types
                        Event e = new Event();
                        e.setId(doc.getId());
                        
                        // Read basic fields
                        e.setName(doc.getString("name"));
                        e.setMaxParticipants(doc.getLong("maxParticipants") != null ? doc.getLong("maxParticipants").intValue() : 0);
                        e.setPosterUrl(doc.getString("posterUrl"));
                        e.setTime(doc.getString("time"));
                        e.setLocation(doc.getString("location"));
                        e.setPrice(doc.getString("price"));
                        
                        // Handle date field - can be Timestamp or String
                        Object dateObj = doc.get("date");
                        if (dateObj instanceof Timestamp) {
                            // Convert Timestamp to ISO string format
                            Date date = ((Timestamp) dateObj).toDate();
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            e.setDate(sdf.format(date));
                        } else if (dateObj instanceof String) {
                            e.setDate((String) dateObj);
                        }
                        
                        // Always explicitly read regOpens and regCloses from Firestore document
                        // This handles both Timestamp and String formats
                        Object regOpensObj = doc.get("regOpens");
                        android.util.Log.d("EventRepository", "Processing event " + doc.getId() + ", regOpens type: " + 
                            (regOpensObj != null ? regOpensObj.getClass().getSimpleName() : "null"));
                        
                        if (regOpensObj instanceof Timestamp) {
                            e.setRegOpensTimestamp((Timestamp) regOpensObj);
                            android.util.Log.d("EventRepository", "Set regOpens from Timestamp: " + regOpensObj);
                        } else if (regOpensObj instanceof String) {
                            // Convert string to Timestamp
                            e.setRegOpens((String) regOpensObj);
                            android.util.Log.d("EventRepository", "Set regOpens from String: " + regOpensObj);
                        } else if (regOpensObj == null) {
                            android.util.Log.w("EventRepository", "regOpens is null for event " + doc.getId());
                        } else {
                            // Handle unexpected type
                            android.util.Log.w("EventRepository", "regOpens has unexpected type: " + 
                                regOpensObj.getClass().getName() + " for event " + doc.getId() + ", value: " + regOpensObj);
                            // Try to convert to string and parse
                            try {
                                e.setRegOpens(regOpensObj.toString());
                            } catch (Exception ex) {
                                android.util.Log.e("EventRepository", "Failed to convert regOpens", ex);
                            }
                        }
                        
                        Object regClosesObj = doc.get("regCloses");
                        android.util.Log.d("EventRepository", "Processing event " + doc.getId() + ", regCloses type: " + 
                            (regClosesObj != null ? regClosesObj.getClass().getSimpleName() : "null"));
                        
                        if (regClosesObj instanceof Timestamp) {
                            e.setRegClosesTimestamp((Timestamp) regClosesObj);
                            android.util.Log.d("EventRepository", "Set regCloses from Timestamp: " + regClosesObj);
                        } else if (regClosesObj instanceof String) {
                            // Convert string to Timestamp
                            e.setRegCloses((String) regClosesObj);
                            android.util.Log.d("EventRepository", "Set regCloses from String: " + regClosesObj);
                        } else if (regClosesObj == null) {
                            android.util.Log.w("EventRepository", "regCloses is null for event " + doc.getId());
                        } else {
                            // Handle unexpected type
                            android.util.Log.w("EventRepository", "regCloses has unexpected type: " + 
                                regClosesObj.getClass().getName() + " for event " + doc.getId() + ", value: " + regClosesObj);
                            // Try to convert to string and parse
                            try {
                                e.setRegCloses(regClosesObj.toString());
                            } catch (Exception ex) {
                                android.util.Log.e("EventRepository", "Failed to convert regCloses", ex);
                            }
                        }
                        
                        // Check if event is joinable
                        boolean isJoinable = e.isJoinableToday();
                        android.util.Log.d("EventRepository", "Event " + doc.getId() + " (" + e.getName() + ") isJoinable: " + isJoinable);
                        
                        // TEMPORARILY: Show all events to debug - remove this filter later
                        // Always add the event for now to see if it's being read correctly

                        android.util.Log.d("EventRepository", "Added event " + doc.getId() + " (" + e.getName() + ") to results (temporarily showing all)");


                        if (e.isJoinableToday()) {
                            results.add(e);
                        }
                        // }
                    } catch (Exception ex) {
                        android.util.Log.e("EventRepository", "Error processing document " + doc.getId(), ex);
                    }
                }
            }
            android.util.Log.d("EventRepository", "Total joinable events found: " + results.size());
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

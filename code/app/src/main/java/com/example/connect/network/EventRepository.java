package com.example.connect.network;

import android.util.Log;

import com.example.connect.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing Event data from Firestore
 */
public class EventRepository {

    private static final String TAG = "EventRepository";
    private static final String COLLECTION_EVENTS = "events";

    private final FirebaseFirestore db;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetch all events from Firestore
     */
    public void getAllEvents(EventCallback callback) {
        db.collection(COLLECTION_EVENTS)
                // Remove or comment out the orderBy temporarily to test
                // .orderBy("dateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    Log.d("EventRepository", "Documents retrieved: " + queryDocumentSnapshots.size());

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            Log.d("EventRepository", "Event loaded: " + event.getName());
                            events.add(event);
                        }
                    }
                    Log.d("EventRepository", "Total events after parsing: " + events.size());
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventRepository", "Error fetching events", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Search events by name
     */
    public void searchEventsByName(String searchQuery, EventCallback callback) {
        db.collection(COLLECTION_EVENTS)
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching events", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Filter events by category
     */
    public void getEventsByCategory(String category, EventCallback callback) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("category", category)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering events by category", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Filter events by location
     */
    public void getEventsByLocation(String location, EventCallback callback) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("location", location)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering events by location", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Filter events by date range
     */
    public void getEventsByDateRange(String startDate, String endDate, EventCallback callback) {
        db.collection(COLLECTION_EVENTS)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering events by date", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Callback interface for event operations
     */
    public interface EventCallback {
        void onSuccess(List<Event> events);
        void onFailure(Exception e);
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for admin to browse and remove events.
 * Displays a list of all events with their basic information.
 * Supports searching/filtering events by name, location, or category.
 *
 * Implements:
 * - US 03.04.01: Admin browses events
 * - US 03.01.01: Admin removes events
 *
 * @author Zenith Team
 * @version 1.0
 */
public class AdminEventListActivity extends AppCompatActivity {

    /** RecyclerView to display the list of events */
    private RecyclerView recyclerViewEvents;
    
    /** Search input field for filtering events */
    private TextInputEditText etSearch;
    
    /** Adapter for managing event items in the RecyclerView */
    private AdminEventAdapter eventAdapter;
    
    /** Complete list of all events loaded from Firestore */
    private List<Event> allEvents;
    
    /** Filtered list of events based on search query */
    private List<Event> filteredEvents;
    
    /** Firebase Firestore database instance */
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the layout, initializes Firebase Firestore,
     * and sets up all UI components and event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_list);

        db = FirebaseFirestore.getInstance();
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadEvents();
    }

    /**
     * Initializes all UI components from the layout.
     * Sets up the RecyclerView, search field, and toolbar with back navigation.
     */
    private void initViews() {
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        etSearch = findViewById(R.id.etSearchEvents);

        // Setup toolbar back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    /**
     * Sets up the RecyclerView with the event adapter.
     * Configures the adapter with click listeners for viewing and deleting events.
     * Uses LinearLayoutManager for vertical scrolling and optimizes for performance.
     */
    private void setupRecyclerView() {
        eventAdapter = new AdminEventAdapter(filteredEvents, new AdminEventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(Event event) {
                // Handle event click - navigate to event details
                Intent intent = new Intent(AdminEventListActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onEventDelete(Event event) {
                // Show confirmation dialog before deleting
                confirmDeleteEvent(event);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEvents.setLayoutManager(layoutManager);
        recyclerViewEvents.setAdapter(eventAdapter);

        // Optimize RecyclerView for smooth scrolling
        recyclerViewEvents.setHasFixedSize(true);
        recyclerViewEvents.setItemAnimator(null); // Disable animations for smoother scrolling
    }

    /**
     * Sets up the search functionality to filter events in real-time.
     * Listens for text changes in the search field and filters the event list accordingly.
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all events from Firestore database.
     * Retrieves all event documents, converts them to Event objects,
     * and updates the UI with the loaded events.
     * Displays an error message if the loading fails.
     * 
     * Implements US 03.04.01: Admin browses events
     */
    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Convert document to Event object
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(document.getId());
                            allEvents.add(event);
                        }
                    }

                    // Update filtered list and adapter
                    filteredEvents.clear();
                    filteredEvents.addAll(allEvents);
                    eventAdapter.notifyDataSetChanged();

                    Log.d("AdminEventList", "Loaded " + allEvents.size() + " events");

                    if (allEvents.isEmpty()) {
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminEventList", "Error loading events", e);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Filters events based on search query.
     * Searches in event name, location, and category fields (case-insensitive).
     * If query is empty, shows all events. Updates the RecyclerView adapter
     * after filtering.
     *
     * @param query The search query string to filter events by
     */
    private void filterEvents(String query) {
        filteredEvents.clear();

        if (query.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : allEvents) {
                boolean matchesName = event.getName() != null &&
                        event.getName().toLowerCase().contains(lowerQuery);
                boolean matchesLocation = event.getLocation() != null &&
                        event.getLocation().toLowerCase().contains(lowerQuery);
                boolean matchesCategory = event.getCategory() != null &&
                        event.getCategory().toLowerCase().contains(lowerQuery);

                if (matchesName || matchesLocation || matchesCategory) {
                    filteredEvents.add(event);
                }
            }
        }

        eventAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a confirmation dialog before deleting an event.
     * Displays the event name and warns about permanent deletion of all related data.
     * User can confirm or cancel the deletion.
     *
     * @param event The event to delete
     */
    private void confirmDeleteEvent(Event event) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getName() + "\"? " +
                        "This will permanently remove the event, all participants, waiting lists, " +
                        "and other related information. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes an event and all related data from Firestore.
     * Performs cascade deletion of:
     * - Event document from events collection
     * - Waiting list document from waiting_lists collection
     * - All subcollections (waitingList subcollection)
     * 
     * Implements US 03.01.01: Admin removes events
     * 
     * Updates the UI after successful deletion and shows appropriate error messages
     * if deletion fails.
     *
     * @param event The event to delete
     */
    private void deleteEvent(Event event) {
        String eventId = event.getEventId();
        Log.d("AdminEventList", "Delete button clicked for event: " + event.getName());
        Log.d("AdminEventList", "Event ID: " + eventId);

        if (eventId == null || eventId.isEmpty()) {
            Log.e("AdminEventList", "❌ Invalid event ID - cannot delete");
            Toast.makeText(this, "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AdminEventList", "✅ Starting deletion for eventId: " + eventId);
        Toast.makeText(this, "Deleting event...", Toast.LENGTH_SHORT).show();

        // Delete waiting_lists document for this event if it exists (non-blocking)
        db.collection("waiting_lists").document(eventId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AdminEventList", "Deleted waiting_lists document: " + eventId);
                    } else {
                        Log.w("AdminEventList", "Waiting list document may not exist: " + eventId);
                    }
                });

        // Delete event subcollections first (waitingList, participants, etc.)
        deleteEventSubcollections(eventId, () -> {
            // Then delete the event document itself
            db.collection("events").document(eventId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AdminEventList", "✅ Event document deleted from Firestore: " + eventId);
                        
                        // Verify deletion
                        db.collection("events").document(eventId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        Log.d("AdminEventList", "✅ Verified: Event document successfully deleted");
                                    } else {
                                        Log.w("AdminEventList", "⚠️ Warning: Event document still exists after deletion");
                                    }
                                    
                                    // Remove from local lists and refresh UI
                                    runOnUiThread(() -> {
                                        removeFromLocalLists(eventId);
                                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminEventList", "Error verifying deletion", e);
                                    // Still proceed with UI update
                                    runOnUiThread(() -> {
                                        removeFromLocalLists(eventId);
                                        Toast.makeText(this, "Event deleted (verification failed)", Toast.LENGTH_SHORT).show();
                                    });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminEventList", "❌ Error deleting event document: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    });
        });
    }

    /**
     * Deletes all subcollections of an event (waitingList, participants, etc.).
     * Recursively deletes all documents in the event's subcollections before
     * deleting the main event document. This ensures proper cleanup of related data.
     *
     * @param eventId The event ID whose subcollections should be deleted
     * @param onComplete Callback to execute when all subcollections are deleted
     */
    private void deleteEventSubcollections(String eventId, Runnable onComplete) {
        // Delete waitingList subcollection
        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] deleteCount = {0};
                    final int totalDocs = queryDocumentSnapshots.size();

                    if (totalDocs == 0) {
                        onComplete.run();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference()
                                .delete()
                                .addOnCompleteListener(task -> {
                                    deleteCount[0]++;
                                    if (deleteCount[0] >= totalDocs) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminEventList", "Error deleting event subcollections", e);
                    onComplete.run();
                });
    }

    /**
     * Removes event from local lists and updates UI.
     * Removes the deleted event from both allEvents and filteredEvents lists,
     * then notifies the adapter to refresh the RecyclerView display.
     *
     * @param eventId The ID of the event to remove from local lists
     */
    private void removeFromLocalLists(String eventId) {
        int initialSize = allEvents.size();
        allEvents.removeIf(event -> event.getEventId() != null && event.getEventId().equals(eventId));
        filteredEvents.removeIf(event -> event.getEventId() != null && event.getEventId().equals(eventId));

        int removedCount = initialSize - allEvents.size();
        Log.d("AdminEventList", "Removed " + removedCount + " event(s) from local lists");

        eventAdapter.notifyDataSetChanged();
    }
}


package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;


/**
 * Activity for administrators to view and manage all events in the system.
 *
 * <p>This activity displays a searchable list of all events with the ability to:
 * <ul>
 *   <li>View all events in the system</li>
 *   <li>Search events by name or organizer ID</li>
 *   <li>View detailed information about a specific event</li>
 *   <li>Delete events (including their associated waitlists)</li>
 * </ul>
 *
 * @author Vansh Taneja, Sai Vashnavi Jattu
 * @version 2.0
 */

public class AdminEventListActivity extends AppCompatActivity {

    // Ui Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AdminEventAdapter adapter;
    private FirebaseFirestore db;
    private TextInputEditText searchInput;
    private View searchLayout;
    private final List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            loadEvents();
        } catch (Exception e) {
            Log.e("AdminEventList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Close activity to prevent stuck state
        }
    }

    /**
     * Initializes all UI components including the toolbar, search bar, and empty state view.
     * Sets up the search functionality with a text watcher for real-time filtering.
     */
    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Manage Events");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        searchLayout = findViewById(R.id.search_layout);
        searchInput = findViewById(R.id.search_input);

        if (searchLayout != null) {
            searchLayout.setVisibility(View.VISIBLE);
        }

        if (searchInput != null) {
            searchInput.setHint("Search events");
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    /**
     * Sets up the RecyclerView with an adapter and layout manager.
     * Configures click handlers for delete and detail view actions.
     */
    private void setupRecyclerView() {
        adapter = new AdminEventAdapter(this::deleteEvent, this::openEventDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Opens the detail view for a specific event.
     *
     * @param event The event to view in detail
     */
    private void openEventDetails(Event event) {
        Intent intent = new Intent(this, AdminEventDetailActivity.class);
        intent.putExtra(AdminEventDetailActivity.EXTRA_EVENT_ID, event.getEventId());
        startActivity(intent);
    }

    /**
     * Loads all events from Firestore and displays them in the RecyclerView.
     * Shows a progress indicator while loading and displays an empty state message
     * if no events are found.
     */
    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        events.add(event);
                    }

                    allEvents.clear();
                    allEvents.addAll(events);
                    applyCurrentFilter();

                    if (allEvents.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminEventList", "Error loading events", e);
                });
    }

    /**
     * Applies the current search filter to the event list.
     * Used after loading events to maintain the search state.
     */
    private void applyCurrentFilter() {
        String query = searchInput != null && searchInput.getText() != null
                ? searchInput.getText().toString()
                : "";
        filterEvents(query);
    }

    /**
     * Filters the event list based on a search query.
     * Searches through event names and organizer IDs (case-insensitive).
     * Updates the RecyclerView and empty state visibility based on results.
     *
     * @param query The search query to filter by
     */
    private void filterEvents(String query) {
        if (adapter == null) return;

        if (query == null) {
            query = "";
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Event> filtered = new ArrayList<>();
        if (lowerQuery.isEmpty()) {
            filtered.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                String name = event.getName() != null ? event.getName().toLowerCase() : "";
                String organizer = event.getOrganizerId() != null ? event.getOrganizerId().toLowerCase() : "";
                if (name.contains(lowerQuery) || organizer.contains(lowerQuery)) {
                    filtered.add(event);
                }
            }
        }

        adapter.setEvents(filtered);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Deletes an event and its associated waitlist from Firestore.
     * The waitlist is deleted first to maintain data integrity, followed by the event itself.
     * Refreshes the event list upon successful deletion.
     *
     * @param event The event to delete
     */
    private void deleteEvent(Event event) {
        if (event.getEventId() == null) {
            Toast.makeText(this, "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete the waitlist subcollection documents first
        deleteWaitlistForEvent(event.getEventId())
                .addOnSuccessListener(aVoid -> {
                    // If waitlist deletion is successful, delete the event document
                    db.collection("events").document(event.getEventId())
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Event and waitlist deleted successfully", Toast.LENGTH_SHORT).show();
                                loadEvents(); // Refresh list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("AdminEventList", "Error deleting event document: " + event.getEventId(), e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminEventList", "Error deleting waitlist for event: " + event.getEventId(), e);
                });
    }

    /**
     * Deletes the document from the top-level 'waiting_lists' collection
     * where the document ID matches the eventId.
     *
     * @param eventId The ID of the event, which is assumed to be the document ID
     * of the corresponding waiting list entry.
     * @return Successfull or Failed completion
     */
    private com.google.android.gms.tasks.Task<Void> deleteWaitlistForEvent(String eventId) {
        // 1. Access the top-level collection 'waiting_lists'
        // 2. Target the specific document using the eventId as the document ID
        return db.collection("waiting_lists").document(eventId)
                .delete(); // Delete the document directly
    }
}

package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.OrganizerEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity serving as the main dashboard for event organizers
 * <p>
 * This activity provides organizers with a comprehensive interface to manage their events:
 * <ul>
 *   <li>View all events organized by the user</li>
 *   <li>Create new events</li>
 *   <li>Edit existing events</li>
 *   <li>Filter events by status (all, open, closed, drawn)</li>
 *   <li>Navigate to messages, map, and profile sections</li>
 *   <li>Access event details and management features</li>
 * </ul>
 * </p>
 * <p>
 * The dashboard uses a RecyclerView to display events and provides filter tabs
 * to quickly switch between different event states.
 * </p>
 *
 * @author Digaant Chokkra
 * @version 3.0
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";

    // UI Components
    private MaterialButton btnNewEvent;
    private MaterialButton btnTotalEvents, btnOpen, btnClosed, btnDrawn;
    private RecyclerView recyclerViewEvents;
    private MaterialButton btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    // Data
    private OrganizerEventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private String currentFilter = "all"; // Track current filter

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the organizer dashboard by:
     * <ol>
     *   <li>Setting up Firebase</li>
     *   <li>Setting up all UI components</li>
     *   <li>Configuring click listeners for navigation and filtering</li>
     *   <li>Setting up the RecyclerView with adapter</li>
     *   <li>Loading events from Firestore</li>
     *   <li>Applying the default "Total Events" filter</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        initializeViews();
        setupClickListeners();
        setupRecyclerView();

        // Load events
        loadOrganizerEvents();

        // Set default filter to Total Events
        selectFilter(btnTotalEvents, "all");
    }

    /**
     * Initializes all view components from the layout.
     * <p>
     * Finds and assigns references to:
     * <ul>
     *   <li>Action buttons (new event)</li>
     *   <li>Filter tab buttons</li>
     *   <li>RecyclerView for events</li>
     *   <li>Bottom navigation buttons</li>
     * </ul>
     * </p>
     */
    private void initializeViews() {
        // Top buttons
        btnNewEvent = findViewById(R.id.btnNewEvent);

        // Filter tabs
        btnTotalEvents = findViewById(R.id.btnTotalEvents);
        btnOpen = findViewById(R.id.btnOpen);
        btnClosed = findViewById(R.id.btnClosed);
        btnDrawn = findViewById(R.id.btnDrawn);

        // RecyclerView
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);

        // Bottom navigation
        btnNavDashboard = findViewById(R.id.btnNavDashboard);
        btnNavMessage = findViewById(R.id.btnNavMessage);
        btnNavMap = findViewById(R.id.btnNavMap);
        btnNavProfile = findViewById(R.id.btnNavProfile);
    }

    /**
     * Configures click listeners for all interactive UI components.
     */
    private void setupClickListeners() {
        // Navigate to CreateEvent
        btnNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerActivity.this, CreateEvent.class);
            startActivity(intent);
        });

        // Filter tabs
        btnTotalEvents.setOnClickListener(v -> selectFilter(btnTotalEvents, "all"));
        btnOpen.setOnClickListener(v -> selectFilter(btnOpen, "open"));
        btnClosed.setOnClickListener(v -> selectFilter(btnClosed, "closed"));
        btnDrawn.setOnClickListener(v -> selectFilter(btnDrawn, "drawn"));

        // Bottom navigation
        btnNavDashboard.setOnClickListener(v -> {
            // Already on dashboard
            Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
        });

        btnNavMessage.setOnClickListener(v -> {
            // Navigate to notification manager
            Intent organizerNotifsIntent = new Intent(OrganizerActivity.this, OrganizerMessagesActivity.class);
            startActivity(organizerNotifsIntent);
        });

        btnNavMap.setOnClickListener(v -> {
            // TODO: Navigate to Map
            Toast.makeText(this, "Map - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(OrganizerActivity.this, ProfileActivity.class);
            profileIntent.putExtra("from_organizer", true); // Mark that it's opened from organizer view
            startActivity(profileIntent);
            // Don't finish() here - let user navigate back if needed
        });
    }

    /**
     * Configures the RecyclerView for displaying events with OrganizerEventAdapter.
     * <p>
     * Sets up a LinearLayoutManager for vertical scrolling of events and
     * connects the adapter with event action listeners.
     * </p>
     */
    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEvents.setLayoutManager(layoutManager);

        // Setup adapter with listeners
        adapter = new OrganizerEventAdapter(new OrganizerEventAdapter.OrganizerEventListener() {
            @Override
            public void onEditEvent(Event event) {
                // Navigate to CreateEvent activity in edit mode
                Intent intent = new Intent(OrganizerActivity.this, CreateEvent.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                intent.putExtra("EDIT_MODE", true);
                startActivity(intent);
            }

            @Override
            public void onViewDetails(Event event) {
                // Navigate to EventDetails activity
                Intent intent = new Intent(OrganizerActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onManageDraw(Event event) {
                // Navigate to manage draw activity
                Toast.makeText(OrganizerActivity.this,
                        "Manage Draw: " + event.getName(),
                        Toast.LENGTH_SHORT).show();

                // TODO: Implement manage draw functionality
                // Intent intent = new Intent(OrganizerActivity.this, ManageDrawActivity.class);
                // intent.putExtra("EVENT_ID", event.getEventId());
                // startActivity(intent);
            }

            @Override
            public void onExportCSV(Event event) {
                // Export event data to CSV
                Toast.makeText(OrganizerActivity.this,
                        "Export CSV: " + event.getName(),
                        Toast.LENGTH_SHORT).show();

                // TODO: Implement CSV export functionality
            }

            @Override
            public void onImageClick(Event event) {
                // Allow organizer to change/add event image
                Toast.makeText(OrganizerActivity.this,
                        "Change Image: " + event.getName(),
                        Toast.LENGTH_SHORT).show();

                // TODO: Implement image selection/update
                // You could navigate to CreateEvent in edit mode
                // or create a separate image update activity
            }
        });

        recyclerViewEvents.setAdapter(adapter);
    }

    /**
     * Load all events created by the current organizer from Firestore.
     * Queries events collection filtering by organizer_id.
     */
    private void loadOrganizerEvents() {
        db.collection("events")
                .whereEqualTo("organizer_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        allEvents.add(event);
                    }

                    Log.d(TAG, "Loaded " + allEvents.size() + " events for organizer: " + currentUserId);

                    // Apply current filter
                    filterEvents(currentFilter);

                    if (allEvents.isEmpty()) {
                        Toast.makeText(this, "No events found. Create your first event!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this,
                            "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Applies a filter to the event list and updates the UI accordingly.
     * <p>
     * This method:
     * <ol>
     *   <li>Resets all filter buttons to default appearance</li>
     *   <li>Highlights the selected filter button</li>
     *   <li>Updates the current filter state</li>
     *   <li>Triggers event filtering based on the selection</li>
     * </ol>
     * </p>
     *
     * @param selectedButton The MaterialButton that was clicked
     * @param filter The filter type to apply ("all", "open", "closed", or "drawn")
     */
    private void selectFilter(MaterialButton selectedButton, String filter) {
        // Reset all buttons to default state
        resetFilterButtons();

        // Highlight selected button
        selectedButton.setBackgroundColor(getResources().getColor(R.color.filter_selected, null));
        selectedButton.setTextColor(getResources().getColor(android.R.color.white, null));

        // Update current filter
        currentFilter = filter;

        // Filter events based on selection
        filterEvents(filter);
    }

    /**
     * Resets all filter buttons to their default appearance.
     * <p>
     * Sets all filter buttons (Total Events, Open, Closed, Drawn) to:
     * <ul>
     *   <li>Transparent background</li>
     *   <li>Default text color</li>
     *   <li>Outlined style (via XML)</li>
     * </ul>
     * This method is called before highlighting the newly selected filter.
     * </p>
     */
    private void resetFilterButtons() {
        // Reset all filter buttons to default outlined style
        int defaultTextColor = getResources().getColor(R.color.filter_text_default, null);

        // Note: For MaterialButton with outlined style, we set strokeColor instead of background
        btnTotalEvents.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnTotalEvents.setTextColor(defaultTextColor);

        btnOpen.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnOpen.setTextColor(defaultTextColor);

        btnClosed.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnClosed.setTextColor(defaultTextColor);

        btnDrawn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnDrawn.setTextColor(defaultTextColor);
    }

    /**
     * Filters the displayed events based on the specified filter type.
     * <p>
     * Filter types and their meanings:
     * <ul>
     *   <li><b>all</b> - Shows all events regardless of status</li>
     *   <li><b>open</b> - Shows only events that are currently accepting registrations</li>
     *   <li><b>closed</b> - Shows only events that are no longer accepting registrations</li>
     *   <li><b>drawn</b> - Shows only events where lottery/selection has been performed</li>
     * </ul>
     * </p>
     *
     * @param filter The filter type
     */
    private void filterEvents(String filter) {
        filteredEvents.clear();

        switch (filter) {
            case "all":
                // Load all events
                filteredEvents.addAll(allEvents);
                Log.d(TAG, "Showing all events: " + filteredEvents.size());
                break;

            case "open":
                // Load only open events (registration is currently open)
                for (Event event : allEvents) {
                    if (isEventOpen(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing open events: " + filteredEvents.size());
                break;

            case "closed":
                // Load only closed events (registration has closed)
                for (Event event : allEvents) {
                    if (isEventClosed(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing closed events: " + filteredEvents.size());
                break;

            case "drawn":
                // Load only drawn events (lottery has been performed)
                for (Event event : allEvents) {
                    if (isEventDrawn(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing drawn events: " + filteredEvents.size());
                break;
        }

        // Update adapter with filtered list
        adapter.submitList(new ArrayList<>(filteredEvents));
    }

    /**
     * Check if event is currently open for registration.
     *
     * @param event The event to check
     * @return true if event is open, false otherwise
     */
    private boolean isEventOpen(Event event) {
        // Event is open if it has registration dates set
        String regStart = event.getRegStart();
        String regStop = event.getRegStop();

        // Basic check - event has registration window defined
        boolean hasRegWindow = regStart != null && !regStart.isEmpty() &&
                regStop != null && !regStop.isEmpty();

        // TODO: Add date comparison logic to check if current date is within registration window
        return hasRegWindow;
    }

    /**
     * Check if event registration has closed.
     *
     * @param event The event to check
     * @return true if event is closed, false otherwise
     */
    private boolean isEventClosed(Event event) {
        // TODO: Implement proper logic based on registration end date
        // Check if current date is after reg_stop date
        return false;
    }

    /**
     * Check if event lottery has been drawn.
     *
     * @param event The event to check
     * @return true if lottery drawn, false otherwise
     */
    private boolean isEventDrawn(Event event) {
        // TODO: Implement proper logic based on draw status field
        // You may need to add a "draw_status" field to your Event model
        return false;
    }

    /**
     * Called when the activity is resumed after being paused.
     * <p>
     * Refreshes the event list with the currently active filter to ensure
     * the displayed data is up-to-date when the organizer returns to the dashboard.
     * This is especially important after editing an event.
     * </p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh event list when returning to this activity
        // This ensures we see any updates made in CreateEvent
        loadOrganizerEvents();
    }
}
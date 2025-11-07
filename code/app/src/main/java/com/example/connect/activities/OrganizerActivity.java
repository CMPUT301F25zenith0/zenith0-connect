package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;

/**
 * Activity serving as the main dashboard for event organizers
 * <p>
 * This activity provides organizers with a comprehensive interface to manage their events:
 * <ul>
 *   <li>View all events organized by the user</li>
 *   <li>Create new events</li>
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
 * @version 2.0

 */
public class OrganizerActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton btnNewEvent;
    private MaterialButton btnTotalEvents, btnOpen, btnClosed, btnDrawn;
    private RecyclerView recyclerViewEvents;
    private MaterialButton btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    private String currentFilter = "all"; // Track current filter

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the organizer dashboard by:
     * <ol>
     *   <li>Setting up all UI components</li>
     *   <li>Configuring click listeners for navigation and filtering</li>
     *   <li>Setting up the RecyclerView for event display</li>
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

        initializeViews();
        setupClickListeners();
        setupRecyclerView();

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
     * TODO
     * Configures the RecyclerView for displaying events.
     * <p>
     * Sets up a LinearLayoutManager for vertical scrolling of events.
     * The adapter will be connected once event data is retrieved from Firestore.
     * </p>
     */
    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEvents.setLayoutManager(layoutManager);

        // TODO: Set up adapter with event data from Firestore
        // EventAdapter adapter = new EventAdapter(eventsList);
        // recyclerViewEvents.setAdapter(adapter);
    }

    /**
     * TODO
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

        // TODO: Filter events based on selection
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
        int defaultColor = getResources().getColor(R.color.filter_default, null);
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
     * TODO
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
     * <p>
     * <b>Note:</b> Currently displays toast messages. Firestore query implementation pending.
     * </p>
     *
     * @param filter The filter type
     */
    private void filterEvents(String filter) {
        // TODO: Implement Firestore query based on filter
        switch (filter) {
            case "all":
                // Load all events
                Toast.makeText(this, "Showing all events", Toast.LENGTH_SHORT).show();
                break;
            case "open":
                // Load only open events
                Toast.makeText(this, "Showing open events", Toast.LENGTH_SHORT).show();
                break;
            case "closed":
                // Load only closed events
                Toast.makeText(this, "Showing closed events", Toast.LENGTH_SHORT).show();
                break;
            case "drawn":
                // Load only drawn events
                Toast.makeText(this, "Showing drawn events", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Called when the activity is resumed after being paused.
     * <p>
     * Refreshes the event list with the currently active filter to ensure
     * the displayed data is up-to-date when the organizer returns to the dashboard.
     * </p>
     */

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh event list when returning to this activity
        filterEvents(currentFilter);
    }
}
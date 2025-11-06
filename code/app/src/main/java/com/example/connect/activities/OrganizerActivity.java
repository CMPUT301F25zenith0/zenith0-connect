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
 * Organizer Dashboard Activity
 * Displays list of events and allows organizers to create new events
 */
public class OrganizerActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton btnNewEvent;
    private MaterialButton btnTotalEvents, btnOpen, btnClosed, btnDrawn;
    private RecyclerView recyclerViewEvents;
    private MaterialButton btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    private String currentFilter = "all"; // Track current filter

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

    private void setupClickListeners() {
        // New Event button - Navigate to CreateEvent
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
            // TODO: Navigate to Messages
            Toast.makeText(this, "Messages - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnNavMap.setOnClickListener(v -> {
            // TODO: Navigate to Map
            Toast.makeText(this, "Map - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(OrganizerActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });
    }

    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEvents.setLayoutManager(layoutManager);

        // TODO: Set up adapter with event data from Firestore
        // EventAdapter adapter = new EventAdapter(eventsList);
        // recyclerViewEvents.setAdapter(adapter);
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh event list when returning to this activity
        filterEvents(currentFilter);
    }
}
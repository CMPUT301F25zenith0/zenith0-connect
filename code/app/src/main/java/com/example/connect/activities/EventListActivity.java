package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.adapters.EventAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
    private Button interestFilterBtn, dateFilterBtn, locationFilterBtn;
    private EditText searchBar;
    private ListView eventsListView;

    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventRepository eventRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        initViews();
        setupAdapter();
        setupClickListeners();
        loadEvents();
    }

    private void initViews() {
        // Initialize navigation buttons
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);

        // Initialize filter buttons
        interestFilterBtn = findViewById(R.id.interest_filter_btn);
        dateFilterBtn = findViewById(R.id.date_filter_btn);
        locationFilterBtn = findViewById(R.id.location_filter_btn);

        // Initialize search bar and list view
        searchBar = findViewById(R.id.search_Bar);
        eventsListView = findViewById(R.id.events_ListView);

        // Initialize repository
        eventRepository = new EventRepository();
    }

    private void setupAdapter() {
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);

    }

    private void setupClickListeners() {
        // Navigation buttons
        scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, QRCodeScanner.class);
            startActivity(intent);
        });

        myEventsBtn.setOnClickListener(v -> {
            // TODO - Navigate to my events page
            Toast.makeText(this, "My Events - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        profileBtn.setOnClickListener(v -> {
            Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        notificationBtn.setOnClickListener(v -> {
            // TODO - Navigate to notifications page
            Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchEvents(s.toString());
                } else {
                    loadEvents();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter buttons
        interestFilterBtn.setOnClickListener(v -> {
            // TODO: Implement category/interest filter dialog
            Toast.makeText(this, "Interest Filter - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        dateFilterBtn.setOnClickListener(v -> {
            // TODO: Implement date filter dialog
            Toast.makeText(this, "Date Filter - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        locationFilterBtn.setOnClickListener(v -> {
            // TODO: Implement location filter dialog
            Toast.makeText(this, "Location Filter - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Load all events from Firestore
     */
    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                // ADD THIS LINE FOR DEBUGGING
                Log.d("EventListActivity", "Loaded " + events.size() + " events");

                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();

                if (events.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                } else {
                    // ADD THIS LINE TOO
                    Toast.makeText(EventListActivity.this, "Loaded " + events.size() + " events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventListActivity.this,
                        "Error loading events: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Search events by name
     */
    private void searchEvents(String query) {
        eventRepository.searchEventsByName(query, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventListActivity.this,
                        "Error searching events: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        loadEvents();
    }


}
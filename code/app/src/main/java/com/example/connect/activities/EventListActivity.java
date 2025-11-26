package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventListAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.example.connect.utils.UserActivityTracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    // UI
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
    private Button interestFilterBtn, dateFilterBtn, locationFilterBtn, clearFiltersBtn;
    private EditText searchBar;
    private RecyclerView eventsRecyclerView;

    private EventListAdapter eventAdapter;
    private List<Event> allEventsList;
    private EventRepository eventRepository;

    // Filter state variables
    private String selectedDate = "";
    private String selectedInterest = "";
    private String selectedLocation = "";
    private String currentSearchQuery = "";

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
        clearFiltersBtn = findViewById(R.id.clear_filters_btn);

        // Initialize search bar and RecyclerView
        searchBar = findViewById(R.id.search_Bar);
        eventsRecyclerView = findViewById(R.id.events_RecyclerView);

        // Initialize repository
        eventRepository = new EventRepository();
    }

    private void setupAdapter() {
        allEventsList = new ArrayList<>();

        // Create adapter with listeners
        eventAdapter = new EventListAdapter(new EventListAdapter.Listener() {
            @Override
            public void onDetails(Event event) {
                // Navigate to event details - FIXED: Changed to "EVENT_ID" to match EventDetails
                Intent intent = new Intent(EventListActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());  // CHANGED FROM "event_id" to "EVENT_ID"
                startActivity(intent);
            }

            @Override
            public void onJoin(Event event) {
                // Handle join waitlist - Navigate to EventDetails to join
                Intent intent = new Intent(EventListActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());  // CHANGED FROM "event_id" to "EVENT_ID"
                startActivity(intent);
            }
        });

        // Set up RecyclerView
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    private void setupClickListeners() {
        // Navigation buttons
        scanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, QRCodeScanner.class);
            startActivity(intent);
        });

        myEventsBtn.setOnClickListener(v -> {
            Toast.makeText(this, "My Events - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        profileBtn.setOnClickListener(v -> {
            Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        notificationBtn.setOnClickListener(v -> {
            Intent notifIntent = new Intent(EventListActivity.this, UserNotificationsActivity.class);
            startActivity(notifIntent);
        });

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Date filter button
        dateFilterBtn.setOnClickListener(v -> showDateFilterDialog());

        // Long press on date filter to clear
        dateFilterBtn.setOnLongClickListener(v -> {
            selectedDate = "";
            dateFilterBtn.setText("DATE");
            applyAllFilters();
            return true;
        });

        // Interest filter button
        interestFilterBtn.setOnClickListener(v -> showInterestFilterDialog());

        // Long press on interest filter to clear
        interestFilterBtn.setOnLongClickListener(v -> {
            selectedInterest = "";
            interestFilterBtn.setText("INTEREST");
            applyAllFilters();
            return true;
        });

        // Location filter button
        locationFilterBtn.setOnClickListener(v -> showLocationFilterDialog());

        // Long press on location filter to clear
        locationFilterBtn.setOnLongClickListener(v -> {
            selectedLocation = "";
            locationFilterBtn.setText("LOCATION");
            applyAllFilters();
            return true;
        });

        // Clear all filters button
        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnClickListener(v -> clearAllFilters());
        }

        // Long press home button to clear all filters
        homeBtn.setOnLongClickListener(v -> {
            clearAllFilters();
            return true;
        });
    }

    private void showDateFilterDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    selectedDate = formattedDate;
                    dateFilterBtn.setText("Date\n" + formattedDate);
                    applyAllFilters();
                },
                year, month, day
        );
        picker.show();
    }

    private void showInterestFilterDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter an interest (e.g., Music, Tech, Sports)");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Filter by Interest")
                .setView(input)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String userInterest = input.getText().toString().trim();
                    if (!userInterest.isEmpty()) {
                        selectedInterest = userInterest;
                        interestFilterBtn.setText("Interest\n" + userInterest);
                        applyAllFilters();
                    } else {
                        Toast.makeText(this, "Please enter an interest", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLocationFilterDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter a location");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Filter by Location")
                .setView(input)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String userLocation = input.getText().toString().trim();
                    if (!userLocation.isEmpty()) {
                        selectedLocation = userLocation;
                        locationFilterBtn.setText("Location\n" + userLocation);
                        applyAllFilters();
                    } else {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d("EventListActivity", "Loaded " + events.size() + " events");

                allEventsList.clear();
                allEventsList.addAll(events);

                // Submit to adapter and apply filters
                eventAdapter.submit(allEventsList);
                applyAllFilters();

                if (events.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventListActivity", "Error loading events", e);
                Toast.makeText(EventListActivity.this,
                        "Error loading events: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyAllFilters() {
        if (eventAdapter == null) return;

        List<Event> filteredEvents = new ArrayList<>(allEventsList);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filteredEvents = filterBySearch(filteredEvents, currentSearchQuery);
        }

        // Apply date filter
        if (!selectedDate.isEmpty()) {
            filteredEvents = filterByDate(filteredEvents, selectedDate);
        }

        // Apply interest filter
        if (!selectedInterest.isEmpty()) {
            filteredEvents = filterByInterest(filteredEvents, selectedInterest);
        }

        // Apply location filter
        if (!selectedLocation.isEmpty()) {
            filteredEvents = filterByLocation(filteredEvents, selectedLocation);
        }

        // Update adapter
        eventAdapter.submit(filteredEvents);

        Log.d("EventListActivity", "Filtered to " + filteredEvents.size() + " events");
    }

    private List<Event> filterBySearch(List<Event> events, String query) {
        List<Event> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Event event : events) {
            boolean matchesName = event.getName() != null &&
                    event.getName().toLowerCase().contains(lowerQuery);

            boolean matchesLocation = event.getLocation() != null &&
                    event.getLocation().toLowerCase().contains(lowerQuery);

            boolean matchesCategory = event.getCategory() != null &&
                    event.getCategory().toLowerCase().contains(lowerQuery);

            boolean matchesDescription = event.getDescription() != null &&
                    event.getDescription().toLowerCase().contains(lowerQuery);

            if (matchesName || matchesLocation || matchesCategory || matchesDescription) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    private List<Event> filterByDate(List<Event> events, String date) {
        List<Event> filtered = new ArrayList<>();
        for (Event event : events) {
            if (event.getDateTime() != null && event.getDateTime().contains(date)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private List<Event> filterByInterest(List<Event> events, String interest) {
        List<Event> filtered = new ArrayList<>();
        String lowerInterest = interest.toLowerCase();

        for (Event event : events) {
            boolean matchesCategory = event.getCategory() != null &&
                    event.getCategory().toLowerCase().contains(lowerInterest);

            boolean matchesTitle = event.getName() != null &&
                    event.getName().toLowerCase().contains(lowerInterest);

            boolean matchesDescription = event.getDescription() != null &&
                    event.getDescription().toLowerCase().contains(lowerInterest);

            if (matchesCategory || matchesTitle || matchesDescription) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    private List<Event> filterByLocation(List<Event> events, String location) {
        List<Event> filtered = new ArrayList<>();
        String lowerLocation = location.toLowerCase();

        for (Event event : events) {
            if (event.getLocation() != null &&
                    event.getLocation().toLowerCase().contains(lowerLocation)) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    private void clearAllFilters() {
        // Clear search
        if (searchBar != null) {
            searchBar.setText("");
        }
        currentSearchQuery = "";

        // Clear date filter
        selectedDate = "";
        if (dateFilterBtn != null) {
            dateFilterBtn.setText("DATE");
        }

        // Clear interest filter
        selectedInterest = "";
        if (interestFilterBtn != null) {
            interestFilterBtn.setText("INTEREST");
        }

        // Clear location filter
        selectedLocation = "";
        if (locationFilterBtn != null) {
            locationFilterBtn.setText("LOCATION");
        }

        // Apply filters (will show all events now)
        applyAllFilters();

        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserActivityTracker.markUserActive();
        loadEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
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
import java.util.Calendar;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
    private Button interestFilterBtn, dateFilterBtn, locationFilterBtn, clearFiltersBtn;
    private EditText searchBar;
    private ListView eventsListView;

    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private List<Event> allEventsList; // Store all events for client-side filtering
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

        // Initialize search bar and list view
        searchBar = findViewById(R.id.search_Bar);
        eventsListView = findViewById(R.id.events_ListView);

        // Initialize repository
        eventRepository = new EventRepository();
    }

    private void setupAdapter() {
        eventList = new ArrayList<>();
        allEventsList = new ArrayList<>(); // Initialize list to store all events
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
            // aalpesh added code here for entrant
            Intent notifIntent = new Intent(EventListActivity.this, NotificationsActivity.class);
            startActivity(notifIntent);
        });

        // Search functionality - client-side filtering
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

        // Date filter button - opens DatePickerDialog
        dateFilterBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                    EventListActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format date as yyyy-MM-dd
                        String formattedDate = String.format("%04d-%02d-%02d", 
                                selectedYear, selectedMonth + 1, selectedDay);
                        selectedDate = formattedDate;
                        
                        // Update button text to show selected date
                        dateFilterBtn.setText("Date: " + formattedDate);
                        
                        // Apply filters
                        applyAllFilters();
                    },
                    year, month, day
            );
            picker.show();
        });

        // Long press on date filter to clear
        dateFilterBtn.setOnLongClickListener(v -> {
            selectedDate = "";
            dateFilterBtn.setText("Date");
            applyAllFilters();
            return true;
        });

        // Interest filter button - opens dialog for user input
        interestFilterBtn.setOnClickListener(v -> {
            final EditText input = new EditText(EventListActivity.this);
            input.setHint("Enter an interest (e.g., Music, Tech, Sports)");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
            builder.setTitle("Filter by Interest");
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                String userInterest = input.getText().toString().trim();
                if (!userInterest.isEmpty()) {
                    selectedInterest = userInterest;
                    interestFilterBtn.setText("Interest: " + userInterest);
                    applyAllFilters();
                } else {
                    Toast.makeText(EventListActivity.this, "Please enter an interest", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        // Long press on interest filter to clear
        interestFilterBtn.setOnLongClickListener(v -> {
            selectedInterest = "";
            interestFilterBtn.setText("Interest");
            applyAllFilters();
            return true;
        });

        // Location filter button - opens dialog for user input
        locationFilterBtn.setOnClickListener(v -> {
            final EditText input = new EditText(EventListActivity.this);
            input.setHint("Enter a location");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
            builder.setTitle("Filter by Location");
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                String userLocation = input.getText().toString().trim();
                if (!userLocation.isEmpty()) {
                    selectedLocation = userLocation;
                    locationFilterBtn.setText("Location: " + userLocation);
                    applyAllFilters();
                } else {
                    Toast.makeText(EventListActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        // Long press on location filter to clear
        locationFilterBtn.setOnLongClickListener(v -> {
            selectedLocation = "";
            locationFilterBtn.setText("Location");
            applyAllFilters();
            return true;
        });

        // Clear all filters button
        if (clearFiltersBtn != null) {
            clearFiltersBtn.setOnClickListener(v -> {
                clearAllFilters();
            });
        }

        // Long press home button to clear all filters
        homeBtn.setOnLongClickListener(v -> {
            clearAllFilters();
            return true;
        });
    }

    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d("EventListActivity", "Loaded " + events.size() + " events");

                // Store all events for filtering
                allEventsList.clear();
                allEventsList.addAll(events);
                
                // Apply all active filters
                applyAllFilters();

                if (events.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("EventListActivity", "Loaded " + events.size() + " events");
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
        if (eventList == null || eventAdapter == null) return;
        
        eventList.clear();
        
        // Start with all events
        List<Event> filteredEvents = new ArrayList<>(allEventsList);
        
        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filteredEvents = filterBySearch(filteredEvents, currentSearchQuery);
        }
        
        // Apply date filter
        if (!selectedDate.isEmpty()) {
            filteredEvents = filterByDate(filteredEvents, selectedDate);
        }
        
        // Apply interest/category filter
        if (!selectedInterest.isEmpty()) {
            filteredEvents = filterByInterest(filteredEvents, selectedInterest);
        }
        
        // Apply location filter
        if (!selectedLocation.isEmpty()) {
            filteredEvents = filterByLocation(filteredEvents, selectedLocation);
        }
        
        // Update the displayed list
        eventList.addAll(filteredEvents);
        eventAdapter.notifyDataSetChanged();
        
        Log.d("EventListActivity", "Filtered to " + eventList.size() + " events. " +
                "Search: \"" + currentSearchQuery + "\", Date: \"" + selectedDate + 
                "\", Interest: \"" + selectedInterest + "\", Location: \"" + selectedLocation + "\"");
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
            if (event.getCategory() != null && 
                event.getCategory().toLowerCase().contains(lowerInterest)) {
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
            dateFilterBtn.setText("Date");
        }
        
        // Clear interest filter
        selectedInterest = "";
        if (interestFilterBtn != null) {
            interestFilterBtn.setText("Interest");
        }
        
        // Clear location filter
        selectedLocation = "";
        if (locationFilterBtn != null) {
            locationFilterBtn.setText("Location");
        }
        
        // Apply filters (will show all events now)
        applyAllFilters();
        
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        loadEvents();
    }


}
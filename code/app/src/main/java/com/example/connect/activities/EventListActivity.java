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

/**
 * Activity that displays a list of events with filtering and search capabilities.
 * <p>
 * This provides a comprehensive event browsing interface with the following features:
 * <ul>
 *   <li>Display all available events in a scrollable list</li>
 *   <li>Search events by name, location, category, or description</li>
 *   <li>Filter events by date, interest/category, and location</li>
 *   <li>Navigation to other app sections (profile, notifications, QR scanner)</li>
 *   <li>Client-side filtering for responsive user experience</li>
 * </ul>
 * </p>
 *
 * @author Zenith Team
 * @version 3.0
 */

public class EventListActivity extends AppCompatActivity {

    // UI
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

    /**
     * Called when the activity is first created.
     * Initializes all UI components, sets up event listeners, and loads events from the repository.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        initViews();
        setupAdapter();
        setupClickListeners();
        loadEvents();
    }

    /**
     * Initializes all view components and the event repository.
     * This method finds and assigns all buttons, text fields, and the ListView from the layout.
     */
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

    /**
     * Sets up event adapter and initializes the event lists.
     * Creates empty ArrayLists for storing events and connects the adapter to the ListView.
     */
    private void setupAdapter() {
        eventList = new ArrayList<>();
        allEventsList = new ArrayList<>(); // Initialize list to store all events
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);
    }

    /**
     * Configures click listeners for all interactive UI components.
     * <p>
     * Sets up the following interactions:
     * <ul>
     *   <li>Navigation button clicks (scan, profile, notifications, my events)</li>
     *   <li>Search bar text changes for real-time filtering</li>
     *   <li>Filter button clicks for date, interest, and location</li>
     *   <li>Long press gestures for clearing individual filters</li>
     *   <li>Clear all filters button</li>
     * </ul>
     * </p>
     */
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

        // Note - Vaisnavi move this to a method call --> Do not put entire functionality inside the click listener
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

    /**
     * Loads all events from the repository and stores them for filtering.
     * <p>
     * This method makes an asynchronous call to the EventRepository to fetch all events.
     * On success, events are stored in array adapter and filters are applied.
     * On failure, an error message is displayed to the user.
     * </p>
     */
    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                // Log action
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
                // Log Fail for debugging
                Log.e("EventListActivity", "Error loading events", e);
                Toast.makeText(EventListActivity.this,
                        "Error loading events: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Applies all active filters to the event list.
     * <p>
     * This chains together all active filters (search, date, interest, location) and updates the displayed list.
     * Filters are applied in sequence:
     * <ol>
     *   <li>Search filter (if query is not empty)</li>
     *   <li>Date filter (if date is selected)</li>
     *   <li>Interest/category filter (if interest is selected)</li>
     *   <li>Location filter (if location is selected)</li>
     * </ol>
     * After filtering, the adapter is notified to refresh the ListView.
     * </p>
     */
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

    /**
     * Filters events based on a search query.
     * <p>
     * Performs case insensitive matching against event name, location, category, and description.
     * An event is included if any of these fields contain the search query.
     * </p>
     *
     * @param events List of events to filter
     * @param query Search query string (case-insensitive)
     * @return Filtered list of events matching the search query
     */
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

    /**
     * Filters events based on specific date.
     * <p>
     * Checks if the events dateTime field contains the specified date string.
     * Expected date format is yyyy-MM-dd.
     * </p>
     * @param events List of events to filter
     * @param date Date string to match (format: yyyy-MM-dd)
     * @return Filtered list of events occurring on the specified date
     */
    private List<Event> filterByDate(List<Event> events, String date) {
        List<Event> filtered = new ArrayList<>();
        for (Event event : events) {
            if (event.getDateTime() != null && event.getDateTime().contains(date)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Filters events based on interest/category.
     * <p>
     * Performs case insensitive partial matching on the event's category field.
     * An event is included if its category contains the interest string.
     * </p>
     *
     * @param events List of events to filter
     * @param interest Interest/category string to match (case-insensitive)
     * @return Filtered list of events matching the interest category
     */
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

    /**
     * Filters based on location.
     * <p>
     * Performs case insensitive partial matching on the event's location field.
     * An event is included if its location contains the location string.
     * </p>
     *
     * @param events List of events to filter
     * @param location Location string to match (case insensitive)
     * @return Filtered list of events at or near the specified location
     */
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

    /**
     * Clears all active filters and resets the event list to show all events.
     * <p>
     * This method:
     * <ul>
     *   <li>Clears the search bar text</li>
     *   <li>Resets all filter state variables</li>
     *   <li>Updates filter button labels to default values</li>
     *   <li>Reapplies filters (showing all events)</li>
     *   <li>Displays a confirmation toast message</li>
     * </ul>
     * </p>
     */
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

    /**
     * Called when the activity is resumed after being paused.
     * <p>
     * Reloads events from the repository to ensure the list is up-to-date when the user returns to this activity.
     * </p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        loadEvents();
    }


}
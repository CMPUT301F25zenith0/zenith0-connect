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
 * Activity for displaying and managing a list of events.
 * <p>
 * This activity provides functionality to:
 * - Display all available events in a ListView
 * - Search events by name, location, category, or description
 * - Filter events by interest (matches title and description)
 * - Filter events by date
 * - Filter events by location
 * - Navigate to other screens (QR Scanner, Profile, My Events, Notifications)
 * <p>
 * The activity uses a multi-filter system where all active filters are combined
 * to provide refined search results. Users can apply multiple filters simultaneously,
 * and the results are updated in real-time as filters are modified.
 * <p>
 * Aakansh - V.1 - Creation of Event List Activity
 * Vaishnavi, Vansh - V.2 - Filter logic and methods | UI adjustments
 * @author Aakansh, Vaishnavi, Vansh
 * @version 2.0

 */

public class EventListActivity extends AppCompatActivity {

    // NAV / UI
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
    private EditText searchBar;
    private ListView eventsListView;


    // Filter buttons
    private Button interestFilterBtn, dateFilterBtn, locationFilterBtn;

    // Adapters / Data mangement
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private List<Event> allEventsList; // Store all events for filtering
    private EventRepository eventRepository;

    // Filter state variables
    private String selectedDate = "";
    private String selectedInterest = "";
    private String selectedLocation = "";
    private String currentSearchQuery = "";


    /**
     * Called when the activity is first created.
     * Initializes all views, sets up the adapter, configures click listeners, and loads events from the repository.
     * <p>
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the data it most recently supplied. Otherwise it is null.
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
     * Initializes all view components by finding them in the layout. This includes navigation buttons, filter buttons, search bar, list view, and the event repository.
     */
    private void initViews() {
        // Initializing
        // Nav buttons
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);

        // Filter Buttons
        interestFilterBtn = findViewById(R.id.interest_filter_btn);
        dateFilterBtn = findViewById(R.id.date_filter_btn);
        locationFilterBtn = findViewById(R.id.location_filter_btn);

        // Search bar and list view
        searchBar = findViewById(R.id.search_Bar);
        eventsListView = findViewById(R.id.events_ListView);

        // Event Repositoy
        eventRepository = new EventRepository();
    }

    /**
     * Sets up the EventAdapter and binds it to the ListView.
     * Initializes both the displayed event list and the full event list used for filtering operations.
     */
    private void setupAdapter() {
        eventList = new ArrayList<>();
        allEventsList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);
    }


    /**
     * Configures click listeners for all interactive UI components.
     * This includes:
     * - Navigation buttons (Scan, My Events, Profile, Notifications)
     * - Search bar with real-time filtering
     * - Filter buttons (Interest, Date, Location) with dialog inputs
     * - Clear functionality for each filter
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

        // Go to profile
        profileBtn.setOnClickListener(v -> {
            Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        // Got to Notifications
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
                currentSearchQuery = s.toString().trim();
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Hide keyboard on search action
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });

        // Interest filter button
        interestFilterBtn.setOnClickListener(v -> {
            final android.widget.EditText input = new android.widget.EditText(EventListActivity.this);
            input.setHint("Enter an interest (e.g., Music, Tech, Sports)");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
            builder.setTitle("Filter by Interest");
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                String userInterest = input.getText().toString().trim();
                if (!userInterest.isEmpty()) {
                    selectedInterest = userInterest;
                    interestFilterBtn.setText(userInterest);
                    applyAllFilters();
                    Toast.makeText(EventListActivity.this, "Interest filter applied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventListActivity.this, "Please enter an interest", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.setNeutralButton("Clear", (dialog, which) -> {
                selectedInterest = "";
                interestFilterBtn.setText("Interest");
                applyAllFilters();
                Toast.makeText(EventListActivity.this, "Interest filter cleared", Toast.LENGTH_SHORT).show();
            });

            builder.show();
        });

        // Date filter button
        dateFilterBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                    EventListActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDateStr = String.format("%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);
                        selectedDate = selectedDateStr;
                        dateFilterBtn.setText(selectedDateStr);
                        applyAllFilters();
                        Toast.makeText(EventListActivity.this, "Date filter applied", Toast.LENGTH_SHORT).show();
                    },
                    year, month, day
            );

            picker.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, "Clear",
                    (dialog, which) -> {
                        selectedDate = "";
                        dateFilterBtn.setText("Date");
                        applyAllFilters();
                        Toast.makeText(EventListActivity.this, "Date filter cleared", Toast.LENGTH_SHORT).show();
                    });

            picker.show();
        });

        // Location filter button
        locationFilterBtn.setOnClickListener(v -> {
            final android.widget.EditText input = new android.widget.EditText(EventListActivity.this);
            input.setHint("Enter a location");

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
            builder.setTitle("Filter by Location");
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                String userLocation = input.getText().toString().trim();
                if (!userLocation.isEmpty()) {
                    selectedLocation = userLocation;
                    locationFilterBtn.setText(userLocation);
                    applyAllFilters();
                    Toast.makeText(EventListActivity.this, "Location filter applied", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventListActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.setNeutralButton("Clear", (dialog, which) -> {
                selectedLocation = "";
                locationFilterBtn.setText("Location");
                applyAllFilters();
                Toast.makeText(EventListActivity.this, "Location filter cleared", Toast.LENGTH_SHORT).show();
            });

            builder.show();
        });
    }

    /**
     * Loads all events from Firestore using the EventRepository.
     * On success, stores events in allEventsList and applies any active filters.
     * On failure, displays an error message to the user.
     * <p>
     * This method is called during onCreate and onResume to ensure the event list
     * is always up-to-date.
     */
    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d("EventListActivity", "Loaded " + events.size() + " events");

                allEventsList.clear();
                allEventsList.addAll(events);

                // Apply any active filters
                applyAllFilters();

                if (events.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
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
     * Applies all active filters to the event list in sequence.
     * <p>
     * Filter application order:
     * 1. Search query (matches name, location, category, description)
     * 2. Date filter (exact date match in DateTime field)
     * 3. Interest filter (matches event name or description)
     * 4. Location filter (partial string matching)
     * <p>
     * All filters are applied cumulatively, meaning only events that pass
     * all active filters will be displayed. The filtered results are then
     * updated in the ListView adapter.
     * <p>
     * If no events match the filters, a toast message is displayed to inform
     * the user.
     */
    private void applyAllFilters() {
        if (allEventsList == null || allEventsList.isEmpty()) {
            return;
        }

        // Start with all events
        List<Event> filtered = new ArrayList<>(allEventsList);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            String lowerQuery = currentSearchQuery.toLowerCase();
            List<Event> searchFiltered = new ArrayList<>();
            for (Event event : filtered) {
                if (matchesSearch(event, lowerQuery)) {
                    searchFiltered.add(event);
                }
            }
            filtered = searchFiltered;
        }

        // Apply date filter
        if (!selectedDate.isEmpty()) {
            List<Event> dateFiltered = new ArrayList<>();
            for (Event event : filtered) {
                if (event.getDateTime() != null && event.getDateTime().contains(selectedDate)) {
                    dateFiltered.add(event);
                }
            }
            filtered = dateFiltered;
        }

        // Apply interest filter (matches with title and description)
        if (!selectedInterest.isEmpty()) {
            String lowerInterest = selectedInterest.toLowerCase();
            List<Event> interestFiltered = new ArrayList<>();
            for (Event event : filtered) {
                String title = event.getName() != null ? event.getName().toLowerCase() : "";
                String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";

                // Match interest text with title OR description
                if (title.contains(lowerInterest) || description.contains(lowerInterest)) {
                    interestFiltered.add(event);
                }
            }
            filtered = interestFiltered;
        }

        // Apply location filter
        if (!selectedLocation.isEmpty()) {
            String lowerLocation = selectedLocation.toLowerCase();
            List<Event> locationFiltered = new ArrayList<>();
            for (Event event : filtered) {
                if (event.getLocation() != null &&
                        event.getLocation().toLowerCase().contains(lowerLocation)) {
                    locationFiltered.add(event);
                }
            }
            filtered = locationFiltered;
        }

        // Update the displayed list
        eventList.clear();
        eventList.addAll(filtered);
        eventAdapter.notifyDataSetChanged();

        // Show message if no results
        if (filtered.isEmpty() && !allEventsList.isEmpty()) {
            Toast.makeText(this, "No events match your filters", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if an event matches the given search query.
     * <p>
     * The search is performed across multiple event fields:
     * - Event name
     * - Location
     * - Category
     * - Description
     * <p>
     * The search is case-insensitive and uses partial string matching.
     * <p>
     * @param event The event to check against the search query
     * @param query The search query string (should be lowercase)
     * @return true if the event matches the query in any field, false otherwise
     */
    private boolean matchesSearch(Event event, String query) {
        return (event.getName() != null && event.getName().toLowerCase().contains(query)) ||
                (event.getLocation() != null && event.getLocation().toLowerCase().contains(query)) ||
                (event.getCategory() != null && event.getCategory().toLowerCase().contains(query)) ||
                (event.getDescription() != null && event.getDescription().toLowerCase().contains(query));
    }

    /**
     * Called when the activity resumes from a paused state.
     * Reloads all events to ensure the list is up-to-date with any changes that may have occurred while the activity was paused.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        loadEvents();
    }
}
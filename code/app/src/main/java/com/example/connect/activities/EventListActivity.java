package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventAdapter;
import com.example.connect.adapters.PopularEventsAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity that displays a list of events with filtering and search
 * capabilities.
 * <p>
 * This provides a comprehensive event browsing interface with the following
 * features:
 * <ul>
 * <li>Display all available events in a scrollable list</li>
 * <li>Search events by name, location, category, or description</li>
 * <li>Filter events by date, interest/category, and location</li>
 * <li>Navigation to other app sections (profile, notifications, QR
 * scanner)</li>
 * <li>Client-side filtering for responsive user experience</li>
 * </ul>
 * </p>
 *
 * @author Zenith Team
 * @version  3.0
 */

public class EventListActivity extends AppCompatActivity {

    // UI
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
    private ListView eventsListView;

    // Header UI Elements
    private View headerView;
    private TextInputEditText searchBarHeader;
    private RecyclerView rvPopularEvents;
    private TextView tvCurrentLocation;
    private MaterialButton btnFilterDate, btnFilterInterest, btnFilterLocation;

    private EventAdapter eventAdapter;
    private PopularEventsAdapter popularEventsAdapter;
    private List<Event> eventList;
    private List<Event> allEventsList; // Store all events for client-side filtering
    private List<Event> popularEventsList;
    private EventRepository eventRepository;

    private String currentSearchQuery = "";
    private String currentDateFilter = ""; // "today", "this_week", "this_month", or ""
    private String currentInterestFilter = ""; // Category/interest filter
    private String currentLocationFilter = ""; // Location filter

    /**
     * Called when the activity is first created.
     * Initializes all UI components, sets up event listeners, and loads events from
     * the repository.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved
     *                           state, if any
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
     * This method finds and assigns all buttons, text fields, and the ListView from
     * the layout.
     */
    private void initViews() {
        // Initialize navigation buttons
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);

        // Initialize list view
        eventsListView = findViewById(R.id.events_ListView);

        // Initialize Header View
        LayoutInflater inflater = LayoutInflater.from(this);
        headerView = inflater.inflate(R.layout.layout_dashboard_header, eventsListView, false);

        // Find views inside header
        searchBarHeader = headerView.findViewById(R.id.etSearchHeader);
        rvPopularEvents = headerView.findViewById(R.id.rvPopularEvents);
        tvCurrentLocation = headerView.findViewById(R.id.tvCurrentLocation);
        btnFilterDate = headerView.findViewById(R.id.btnFilterDate);
        btnFilterInterest = headerView.findViewById(R.id.btnFilterInterest);
        btnFilterLocation = headerView.findViewById(R.id.btnFilterLocation);

        // Add header to ListView
        eventsListView.addHeaderView(headerView);

        // Initialize repository
        eventRepository = new EventRepository();
    }

    /**
     * Sets up event adapter and initializes the event lists.
     * Creates empty ArrayLists for storing events and connects the adapter to the
     * ListView.
     */
    private void setupAdapter() {
        eventList = new ArrayList<>();
        allEventsList = new ArrayList<>(); // Initialize list to store all events
        popularEventsList = new ArrayList<>();

        // Setup Main List Adapter
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);

        // Setup Popular Events Adapter (Horizontal RecyclerView)
        popularEventsAdapter = new PopularEventsAdapter(this, popularEventsList, event -> {
            // Handle popular event click
            Intent intent = new Intent(EventListActivity.this, EventDetails.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            startActivity(intent);
        });

        rvPopularEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularEvents.setAdapter(popularEventsAdapter);
    }

    /**
     * Configures click listeners for all interactive UI components.
     * <p>
     * Sets up the following interactions:
     * <ul>
     * <li>Navigation button clicks (scan, profile, notifications, my events)</li>
     * <li>Search bar text changes for real-time filtering</li>
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

        // Profile Icon in Header
        View profileHeader = headerView.findViewById(R.id.ivProfileHeader);
        if (profileHeader != null) {
            profileHeader.setOnClickListener(v -> {
                Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            });
        }

        // Search functionality - client-side filtering
        if (searchBarHeader != null) {
            searchBarHeader.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    applyAllFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Filter button click listeners
        if (btnFilterDate != null) {
            btnFilterDate.setOnClickListener(v -> showDateFilterDialog());
        }

        if (btnFilterInterest != null) {
            btnFilterInterest.setOnClickListener(v -> showInterestFilterDialog());
        }

        if (btnFilterLocation != null) {
            btnFilterLocation.setOnClickListener(v -> showLocationFilterDialog());
        }

        // ListView Item Click
        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            // Adjust position for header
            int adjPosition = position - eventsListView.getHeaderViewsCount();
            if (adjPosition >= 0 && adjPosition < eventList.size()) {
                Event event = eventList.get(adjPosition);
                Intent intent = new Intent(EventListActivity.this, EventDetails.class);
                intent.putExtra("event_id", event.getEventId());
                startActivity(intent);
            }
        });
    }

    /**
     * Loads all events from the repository and stores them for filtering.
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

                // Update Popular Events (events within 5 days)
                popularEventsList.clear();
                popularEventsList.addAll(getPopularEvents(events));
                popularEventsAdapter.notifyDataSetChanged();

                // Apply all active filters
                applyAllFilters();

                if (events.isEmpty()) {
                    Toast.makeText(EventListActivity.this, "No events found", Toast.LENGTH_SHORT).show();
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
     * Filters events to return only those within the next 5 days.
     * 
     * @param events List of all events
     * @return List of events within 5 days
     */
    private List<Event> getPopularEvents(List<Event> events) {
        List<Event> popularEvents = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000; // 5 days in milliseconds

        for (Event event : events) {
            if (event.getDateTime() != null && !event.getDateTime().isEmpty()) {
                try {
                    // Try to parse the date string
                    long eventTime = parseDateToMillis(event.getDateTime());
                    long timeDiff = eventTime - currentTime;

                    // Check if event is within next 5 days
                    if (timeDiff >= 0 && timeDiff <= fiveDaysInMillis) {
                        popularEvents.add(event);
                    }
                } catch (Exception e) {
                    // If parsing fails, skip this event
                    Log.d("EventListActivity", "Could not parse date for event: " + event.getName());
                }
            }
        }

        return popularEvents;
    }

    /**
     * Parses a date string to milliseconds.
     * Supports various date formats.
     * 
     * @param dateString Date string to parse
     * @return Milliseconds since epoch
     */
    private long parseDateToMillis(String dateString) throws Exception {
        // Try common date formats
        String[] formats = {
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd",
                "MMM dd, yyyy",
                "dd MMM yyyy"
        };

        for (String format : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, java.util.Locale.ENGLISH);
                java.util.Date date = sdf.parse(dateString);
                if (date != null) {
                    return date.getTime();
                }
            } catch (Exception e) {
                // Try next format
            }
        }

        throw new Exception("Unable to parse date: " + dateString);
    }

    /**
     * Applies all active filters to the event list.
     * <p>
     * This chains together all active filters (search, date, interest, location)
     * and updates the displayed list.
     * Filters are applied in sequence:
     * <ol>
     * <li>Search filter (if query is not empty)</li>
     * <li>Date filter (if date filter is active)</li>
     * <li>Interest filter (if interest filter is active)</li>
     * <li>Location filter (if location filter is active)</li>
     * </ol>
     * After filtering, the adapter is notified to refresh the ListView.
     * </p>
     */
    private void applyAllFilters() {
        if (eventList == null || eventAdapter == null)
            return;

        eventList.clear();

        // Start with all events
        List<Event> filteredEvents = new ArrayList<>(allEventsList);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filteredEvents = filterBySearch(filteredEvents, currentSearchQuery);
        }

        // Apply date filter
        if (!currentDateFilter.isEmpty()) {
            filteredEvents = filterByDate(filteredEvents, currentDateFilter);
        }

        // Apply interest filter
        if (!currentInterestFilter.isEmpty()) {
            filteredEvents = filterByInterest(filteredEvents, currentInterestFilter);
        }

        // Apply location filter
        if (!currentLocationFilter.isEmpty()) {
            filteredEvents = filterByLocation(filteredEvents, currentLocationFilter);
        }

        // Update the displayed list
        eventList.addAll(filteredEvents);
        eventAdapter.notifyDataSetChanged();

        Log.d("EventListActivity", "Filtered to " + eventList.size() + " events. " +
                "Search: \"" + currentSearchQuery + "\", Date: \"" + currentDateFilter + 
                "\", Interest: \"" + currentInterestFilter + "\", Location: \"" + currentLocationFilter + "\"");
    }

    /**
     * Filters events based on a search query.
     * <p>
     * Performs case insensitive matching against event name, location, category,
     * and description.
     * An event is included if any of these fields contain the search query.
     * </p>
     *
     * @param events List of events to filter
     * @param query  Search query string (case-insensitive)
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
     * Filters events based on date criteria.
     * 
     * @param events List of events to filter
     * @param dateFilter Date filter type ("today", "this_week", "this_month")
     * @return Filtered list of events matching the date criteria
     */
    private List<Event> filterByDate(List<Event> events, String dateFilter) {
        List<Event> filtered = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long oneDayInMillis = 24L * 60 * 60 * 1000;
        long oneWeekInMillis = 7L * 24 * 60 * 60 * 1000;
        long oneMonthInMillis = 30L * 24 * 60 * 60 * 1000;

        for (Event event : events) {
            if (event.getDateTime() != null && !event.getDateTime().isEmpty()) {
                try {
                    long eventTime = parseDateToMillis(event.getDateTime());
                    long timeDiff = eventTime - currentTime;

                    boolean matches = false;
                    switch (dateFilter) {
                        case "today":
                            matches = timeDiff >= 0 && timeDiff <= oneDayInMillis;
                            break;
                        case "this_week":
                            matches = timeDiff >= 0 && timeDiff <= oneWeekInMillis;
                            break;
                        case "this_month":
                            matches = timeDiff >= 0 && timeDiff <= oneMonthInMillis;
                            break;
                    }

                    if (matches) {
                        filtered.add(event);
                    }
                } catch (Exception e) {
                    // If parsing fails, skip this event
                    Log.d("EventListActivity", "Could not parse date for event: " + event.getName());
                }
            }
        }

        return filtered;
    }

    /**
     * Filters events based on interest (searches in event name and description).
     * 
     * @param events List of events to filter
     * @param interestFilter Search query to filter by
     * @return Filtered list of events matching the interest in name or description
     */
    private List<Event> filterByInterest(List<Event> events, String interestFilter) {
        List<Event> filtered = new ArrayList<>();
        String lowerFilter = interestFilter.toLowerCase();

        for (Event event : events) {
            boolean matchesName = event.getName() != null && 
                event.getName().toLowerCase().contains(lowerFilter);
            
            boolean matchesDescription = event.getDescription() != null && 
                event.getDescription().toLowerCase().contains(lowerFilter);

            if (matchesName || matchesDescription) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    /**
     * Filters events based on location.
     * 
     * @param events List of events to filter
     * @param locationFilter Location to filter by
     * @return Filtered list of events matching the location
     */
    private List<Event> filterByLocation(List<Event> events, String locationFilter) {
        List<Event> filtered = new ArrayList<>();
        String lowerFilter = locationFilter.toLowerCase();

        for (Event event : events) {
            if (event.getLocation() != null && 
                event.getLocation().toLowerCase().contains(lowerFilter)) {
                filtered.add(event);
            }
        }

        return filtered;
    }

    /**
     * Shows a dialog to select date filter options.
     */
    private void showDateFilterDialog() {
        String[] options = {"Today", "This Week", "This Month", "Clear"};
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Date")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        currentDateFilter = "today";
                        updateFilterButtonState(btnFilterDate, true);
                        break;
                    case 1:
                        currentDateFilter = "this_week";
                        updateFilterButtonState(btnFilterDate, true);
                        break;
                    case 2:
                        currentDateFilter = "this_month";
                        updateFilterButtonState(btnFilterDate, true);
                        break;
                    case 3:
                        currentDateFilter = "";
                        updateFilterButtonState(btnFilterDate, false);
                        break;
                }
                applyAllFilters();
            })
            .show();
    }

    /**
     * Shows a dialog with text input to filter by interest (searches in event name and description).
     */
    private void showInterestFilterDialog() {
        // Create an EditText for input
        final EditText input = new EditText(this);
        input.setHint("Enter keyword to search in event name or description");
        input.setText(currentInterestFilter);
        input.setPadding(50, 20, 50, 20);

        new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Interest")
            .setMessage("Search events by name or description")
            .setView(input)
            .setPositiveButton("Apply", (dialog, which) -> {
                String filterText = input.getText().toString().trim();
                if (!filterText.isEmpty()) {
                    currentInterestFilter = filterText;
                    updateFilterButtonState(btnFilterInterest, true);
                } else {
                    currentInterestFilter = "";
                    updateFilterButtonState(btnFilterInterest, false);
                }
                applyAllFilters();
            })
            .setNeutralButton("Clear", (dialog, which) -> {
                currentInterestFilter = "";
                updateFilterButtonState(btnFilterInterest, false);
                applyAllFilters();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Shows a dialog to select location filter.
     */
    private void showLocationFilterDialog() {
        // Get unique locations from all events
        Set<String> locations = new HashSet<>();
        for (Event event : allEventsList) {
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                locations.add(event.getLocation());
            }
        }

        if (locations.isEmpty()) {
            Toast.makeText(this, "No locations available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] locationArray = locations.toArray(new String[0]);
        String[] options = new String[locationArray.length + 1];
        System.arraycopy(locationArray, 0, options, 0, locationArray.length);
        options[options.length - 1] = "Clear";

        new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Location")
            .setItems(options, (dialog, which) -> {
                if (which == options.length - 1) {
                    // Clear filter
                    currentLocationFilter = "";
                    updateFilterButtonState(btnFilterLocation, false);
                } else {
                    currentLocationFilter = options[which];
                    updateFilterButtonState(btnFilterLocation, true);
                }
                applyAllFilters();
            })
            .show();
    }

    /**
     * Clears all active filters and resets the UI.
     */
    private void clearAllFilters() {
        currentSearchQuery = "";
        currentDateFilter = "";
        currentInterestFilter = "";
        currentLocationFilter = "";

        // Clear search bar
        if (searchBarHeader != null) {
            searchBarHeader.setText("");
        }

        // Reset filter button states
        updateFilterButtonState(btnFilterDate, false);
        updateFilterButtonState(btnFilterInterest, false);
        updateFilterButtonState(btnFilterLocation, false);

        // Apply filters (which will show all events now)
        applyAllFilters();

        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Updates the visual state of a filter button to indicate if it's active.
     * 
     * @param button The filter button to update
     * @param isActive Whether the filter is currently active
     */
    private void updateFilterButtonState(MaterialButton button, boolean isActive) {
        if (button == null) return;

        if (isActive) {
            // Active state - filled button with accent color
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_orange));
            button.setTextColor(ContextCompat.getColor(this, R.color.white));
            // Update icon tint for date, interest and location buttons
            if (button == btnFilterDate || button == btnFilterInterest || button == btnFilterLocation) {
                try {
                    button.setIconTint(ContextCompat.getColorStateList(this, R.color.white));
                } catch (Exception e) {
                    // Fallback if setIconTint is not available
                    Log.d("EventListActivity", "Could not set icon tint: " + e.getMessage());
                }
            }
        } else {
            // Inactive state - outlined button
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            button.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
            // Update icon tint for date, interest and location buttons
            if (button == btnFilterDate || button == btnFilterInterest || button == btnFilterLocation) {
                try {
                    button.setIconTint(ContextCompat.getColorStateList(this, R.color.accent_orange));
                } catch (Exception e) {
                    // Fallback if setIconTint is not available
                    Log.d("EventListActivity", "Could not set icon tint: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Called when the activity is resumed after being paused.
     * <p>
     * Reloads events from the repository to ensure the list is up-to-date when the
     * user returns to this activity.
     * </p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh events when returning to this activity
        loadEvents();
    }

}
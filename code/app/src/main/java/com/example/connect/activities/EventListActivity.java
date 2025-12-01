package com.example.connect.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventAdapter;
import com.example.connect.adapters.PopularEventsAdapter;
import com.example.connect.models.Event;
import com.example.connect.models.User;
import com.example.connect.network.EventRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main event dashboard activity displaying featured events and a searchable event list.
 *
 * <p>This activity serves as the primary event browsing interface with the following features:
 * <ul>
 *   <li>Horizontal carousel of popular/upcoming events (events within 5 days)</li>
 *   <li>Searchable list view of all available events</li>
 *   <li>Real-time filtering by search query, date, interest, and location</li>
 *   <li>Bottom navigation bar for app-wide navigation</li>
 *   <li>User profile image display in the header</li>
 * </ul>
 *
 * <p>Filter functionality:
 * <ul>
 *   <li>Search: Searches event names, locations, categories, and descriptions</li>
 *   <li>Date: Filter events by specific date using a date picker</li>
 *   <li>Interest: Filter by event labels/categories</li>
 *   <li>Location: Filter by event location</li>
 *   <li>Filters can be combined and applied simultaneously</li>
 *   <li>Long-press individual filter chips to clear that specific filter</li>
 * </ul>
 *
 * @author Zenith Team
 * @version 5.0
 */
public class EventListActivity extends AppCompatActivity {

    // Bottom navigation
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;

    // Primary list + header elements
    private ListView eventsListView;
    private View headerView;
    private TextInputEditText searchBarHeader;
    private RecyclerView rvPopularEvents;
    private ShapeableImageView profileHeaderImage;

    // Filter chips
    private Chip chipInterest, chipDate, chipLocation, chipClearFilters;

    private EventAdapter eventAdapter;
    private PopularEventsAdapter popularEventsAdapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> allEventsList = new ArrayList<>();
    private List<Event> popularEventsList = new ArrayList<>();

    private final EventRepository eventRepository = new EventRepository();
    private String currentSearchQuery = "";

    // Filter state variables
    private String selectedDate = "";
    private String selectedInterest = "";
    private String selectedLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        initViews();
        setupAdapter();
        setupClickListeners();
        loadProfileImage();
        loadEvents();
    }

    private void initViews() {
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);

        eventsListView = findViewById(R.id.events_ListView);

        LayoutInflater inflater = LayoutInflater.from(this);
        headerView = inflater.inflate(R.layout.layout_dashboard_header, eventsListView, false);

        searchBarHeader = headerView.findViewById(R.id.etSearchHeader);
        rvPopularEvents = headerView.findViewById(R.id.rvPopularEvents);

        // Initialize filter chips
        chipInterest = headerView.findViewById(R.id.chip_interest);
        chipDate = headerView.findViewById(R.id.chip_date);
        chipLocation = headerView.findViewById(R.id.chip_location);
        chipClearFilters = headerView.findViewById(R.id.chip_clear_filters);

        // Initialize profile header image
        profileHeaderImage = headerView.findViewById(R.id.ivProfileHeader);

        eventsListView.addHeaderView(headerView);
    }

    /**
     * Sets up adapters for both the main events list and the popular events carousel.
     */
    private void setupAdapter() {
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);

        popularEventsAdapter = new PopularEventsAdapter(this, popularEventsList, event -> {
            Intent intent = new Intent(EventListActivity.this, EventDetails.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            startActivity(intent);
        });

        rvPopularEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularEvents.setAdapter(popularEventsAdapter);
    }

    private void setupClickListeners() {
        scanBtn.setOnClickListener(v -> startActivity(new Intent(this, QRCodeScanner.class)));

        myEventsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MyEventsActivity.class)));

        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        notificationBtn.setOnClickListener(v ->
                startActivity(new Intent(this, UserNotificationsActivity.class)));

        View profileHeader = headerView.findViewById(R.id.ivProfileHeader);
        if (profileHeader != null) {
            profileHeader.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }

        eventsListView.setOnItemClickListener((parent, view, position, id) -> {
            int dataPosition = position - eventsListView.getHeaderViewsCount();
            if (dataPosition >= 0 && dataPosition < eventList.size()) {
                Event event = eventList.get(dataPosition);
                Intent intent = new Intent(EventListActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }
        });

        if (searchBarHeader != null) {
            searchBarHeader.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    applyAllFilters();
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Filter chip listeners
        if (chipInterest != null) {
            chipInterest.setOnClickListener(v -> showInterestFilterDialog());
            chipInterest.setOnLongClickListener(v -> {
                selectedInterest = "";
                chipInterest.setText(getString(R.string.filter_interest));
                applyAllFilters();
                return true;
            });
        }

        if (chipDate != null) {
            chipDate.setOnClickListener(v -> showDateFilterDialog());
            chipDate.setOnLongClickListener(v -> {
                selectedDate = "";
                chipDate.setText(getString(R.string.filter_date));
                applyAllFilters();
                return true;
            });
        }

        if (chipLocation != null) {
            chipLocation.setOnClickListener(v -> showLocationFilterDialog());
            chipLocation.setOnLongClickListener(v -> {
                selectedLocation = "";
                chipLocation.setText(getString(R.string.filter_location));
                applyAllFilters();
                return true;
            });
        }

        if (chipClearFilters != null) {
            chipClearFilters.setOnClickListener(v -> clearAllFilters());
        }
    }

    /**
     * Loads all events from the repository and updates both the main list
     * and the popular events carousel. Applies any active filters after loading.
     */
    private void loadEvents() {
        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d("EventListActivity", "Loaded " + events.size() + " events");

                allEventsList.clear();
                allEventsList.addAll(events);

                popularEventsList.clear();
                popularEventsList.addAll(getPopularEvents(events));
                popularEventsAdapter.notifyDataSetChanged();

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

    /**
     * Filters events to find "popular" ones (events occurring within the next 5 days).
     * Uses multiple date format parsers to handle different date string formats.
     *
     * @param events The list of all events to filter
     * @return List of events occurring within 5 days
     */
    private List<Event> getPopularEvents(List<Event> events) {
        List<Event> popularEvents = new ArrayList<>();
        long now = System.currentTimeMillis();
        long fiveDays = 5L * 24 * 60 * 60 * 1000;

        for (Event event : events) {
            if (event.getDateTime() == null || event.getDateTime().isEmpty()) continue;
            try {
                long eventTime = parseDateToMillis(event.getDateTime());
                long diff = eventTime - now;
                if (diff >= 0 && diff <= fiveDays) {
                    popularEvents.add(event);
                }
            } catch (Exception ex) {
                Log.d("EventListActivity", "Unable to parse date for event: " + event.getName());
            }
        }
        return popularEvents;
    }

    /**
     * Attempts to parse a date string using multiple common date formats.
     *
     * @param dateString The date string to parse
     * @return The date as milliseconds since epoch
     * @throws Exception if none of the formats can parse the date
     */
    private long parseDateToMillis(String dateString) throws Exception {
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
            } catch (Exception ignored) {
            }
        }
        throw new Exception("Unable to parse date: " + dateString);
    }

    private void applyAllFilters() {
        eventList.clear();
        List<Event> filtered = new ArrayList<>(allEventsList);

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            filtered = filterBySearch(filtered, currentSearchQuery);
        }

        // Apply date filter
        if (!selectedDate.isEmpty()) {
            filtered = filterByDate(filtered, selectedDate);
        }

        // Apply interest filter
        if (!selectedInterest.isEmpty()) {
            filtered = filterByInterest(filtered, selectedInterest);
        }

        // Apply location filter
        if (!selectedLocation.isEmpty()) {
            filtered = filterByLocation(filtered, selectedLocation);
        }

        eventList.addAll(filtered);
        eventAdapter.notifyDataSetChanged();

        // Also filter popular events with the same filters
        List<Event> filteredPopular = new ArrayList<>(getPopularEvents(allEventsList));

        // Apply search filter to popular events
        if (!currentSearchQuery.isEmpty()) {
            filteredPopular = filterBySearch(filteredPopular, currentSearchQuery);
        }

        // Apply date filter to popular events
        if (!selectedDate.isEmpty()) {
            filteredPopular = filterByDate(filteredPopular, selectedDate);
        }

        // Apply interest filter to popular events
        if (!selectedInterest.isEmpty()) {
            filteredPopular = filterByInterest(filteredPopular, selectedInterest);
        }

        // Apply location filter to popular events
        if (!selectedLocation.isEmpty()) {
            filteredPopular = filterByLocation(filteredPopular, selectedLocation);
        }

        popularEventsList.clear();
        popularEventsList.addAll(filteredPopular);
        popularEventsAdapter.notifyDataSetChanged();

        Log.d("EventListActivity", "Filtered to " + eventList.size() + " events. " +
                "Search: \"" + currentSearchQuery + "\", " +
                "Date: \"" + selectedDate + "\", " +
                "Interest: \"" + selectedInterest + "\", " +
                "Location: \"" + selectedLocation + "\"");
    }

    /**
     * Filters events by search query (case-insensitive).
     * Searches through event names, locations, categories, and descriptions.
     *
     * @param events The list of events to filter
     * @param query The search query
     * @return Filtered list of events matching the query
     */
    private List<Event> filterBySearch(List<Event> events, String query) {
        List<Event> filtered = new ArrayList<>();
        String lower = query.toLowerCase();

        for (Event event : events) {
            boolean matchesName = event.getName() != null && event.getName().toLowerCase().contains(lower);
            boolean matchesLocation = event.getLocation() != null && event.getLocation().toLowerCase().contains(lower);
            boolean matchesCategory = event.getCategory() != null && event.getCategory().toLowerCase().contains(lower);
            boolean matchesDescription = event.getDescription() != null && event.getDescription().toLowerCase().contains(lower);

            if (matchesName || matchesLocation || matchesCategory || matchesDescription) {
                filtered.add(event);
            }
        }
        return filtered;
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
                    if (chipDate != null) {
                        chipDate.setText(getString(R.string.filter_date) + "\n" + formattedDate);
                    }
                    applyAllFilters();
                },
                year, month, day
        );
        picker.show();
    }

    private void showInterestFilterDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter an interest (e.g., Music, Tech, Sports)");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Filter by Interest")
                .setView(input)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String userInterest = input.getText().toString().trim();
                    if (!userInterest.isEmpty()) {
                        selectedInterest = userInterest;
                        if (chipInterest != null) {
                            chipInterest.setText(getString(R.string.filter_interest) + "\n" + userInterest);
                        }
                        applyAllFilters();
                    } else {
                        Toast.makeText(this, "Please enter an interest", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLocationFilterDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter a location");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Filter by Location")
                .setView(input)
                .setPositiveButton("Apply", (dialog, which) -> {
                    String userLocation = input.getText().toString().trim();
                    if (!userLocation.isEmpty()) {
                        selectedLocation = userLocation;
                        if (chipLocation != null) {
                            chipLocation.setText(getString(R.string.filter_location) + "\n" + userLocation);
                        }
                        applyAllFilters();
                    } else {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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
        String lowerInterest = interest.toLowerCase(Locale.getDefault());

        for (Event event : events) {
            List<String> labels = event.getLabels();
            if (labels == null || labels.isEmpty()) {
                continue;
            }

            boolean matchesLabel = false;
            for (String label : labels) {
                if (label != null && label.toLowerCase(Locale.getDefault()).contains(lowerInterest)) {
                    matchesLabel = true;
                    break;
                }
            }

            if (matchesLabel) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Filters events by location (case-insensitive).
     *
     * @param events The list of events to filter
     * @param location The location string to filter by
     * @return Filtered list of events matching the location
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

    private void clearAllFilters() {
        // Clear search
        if (searchBarHeader != null) {
            searchBarHeader.setText("");
        }
        currentSearchQuery = "";

        // Clear date filter
        selectedDate = "";
        if (chipDate != null) {
            chipDate.setText(getString(R.string.filter_date));
        }

        // Clear interest filter
        selectedInterest = "";
        if (chipInterest != null) {
            chipInterest.setText(getString(R.string.filter_interest));
        }

        // Clear location filter
        selectedLocation = "";
        if (chipLocation != null) {
            chipLocation.setText(getString(R.string.filter_location));
        }

        // Apply filters (will show all events now)
        applyAllFilters();

        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads the current user's profile image from Firestore and displays it in the header.
     * Decodes the base64-encoded image string and sets it as the profile image bitmap.
     * If no profile image exists, the placeholder remains visible.
     */
    private void loadProfileImage() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || profileHeaderImage == null) {
            return;
        }

        String userId = firebaseUser.getUid();
        FirebaseFirestore.getInstance()
                .collection("accounts")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            String base64Image = user.getProfileImageUrl();
                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    if (decodedByte != null) {
                                        profileHeaderImage.setImageBitmap(decodedByte);
                                    }
                                } catch (Exception e) {
                                    Log.e("EventListActivity", "Error decoding profile image", e);
                                }
                            }
                        }
                    }
                });
    }

    /**
     * Reloads events and profile image when the activity resumes.
     * Ensures the displayed data is up-to-date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        loadProfileImage();
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.WaitingListAdapter;
import com.example.connect.models.Event;
import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for managing event lottery draw and viewing entrants
 * <p>
 * Features:
 * <ul>
 *   <li>View event details and statistics</li>
 *   <li>Filter entrants by status (Waiting, Selected, Enrolled, Canceled)</li>
 *   <li>View list of entrants with their details</li>
 *   <li>Navigate between different entrant categories</li>
 * </ul>
 * </p>
 *
 * @author Zenith Team
 * @version 2.0
 */
public class ManageDrawActivity extends AppCompatActivity {

    private static final String TAG = "ManageDrawActivity";

    // UI Components - Top Bar
    private ImageButton btnBack;
    private TextView tvTitle;

    // UI Components - Event Info Card
    private TextView tvEventName;
    private TextView tvStatus;
    private TextView tvCapacity;
    private TextView tvWaiting;
    private TextView tvLocation;
    private TextView tvDate;

    // UI Components - Filter Tabs
    private TextView btnTabWaiting;
    private TextView btnTabSelected;
    private TextView btnTabEnrolled;
    private TextView btnTabCanceled;

    // UI Components - Content Container
    private RecyclerView recyclerViewEntrants;

    // UI Components - Bottom Navigation
    private Button btnNavDashboard;
    private Button btnNavMessage;
    private Button btnNavMap;
    private Button btnNavProfile;

    // Data
    private String eventId;
    private Event currentEvent;
    private WaitingListAdapter adapter;
    private List<WaitingListEntry> allEntries = new ArrayList<>();
    private List<WaitingListEntry> filteredEntries = new ArrayList<>();
    private String currentFilter = "waiting";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_draw);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadEventData();
        loadWaitingListEntries();

        // Set default filter to Waiting
        selectTab(btnTabWaiting, "waiting");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        // Top Bar
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        // Event Info Card
        tvEventName = findViewById(R.id.tvEventName);
        tvStatus = findViewById(R.id.tvStatus);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvWaiting = findViewById(R.id.tvWaiting);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);

        // Filter Tabs
        btnTabWaiting = findViewById(R.id.btnTabWaiting);
        btnTabSelected = findViewById(R.id.btnTabSelected);
        btnTabEnrolled = findViewById(R.id.btnTabEnrolled);
        btnTabCanceled = findViewById(R.id.btnTabCanceled);

        // Bottom Navigation
        btnNavDashboard = findViewById(R.id.btnNavDashboard);
        btnNavMessage = findViewById(R.id.btnNavMessage);
        btnNavMap = findViewById(R.id.btnNavMap);
        btnNavProfile = findViewById(R.id.btnNavProfile);
    }

    /**
     * Setup click listeners for all interactive components
     */
    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Filter tabs
        btnTabWaiting.setOnClickListener(v -> selectTab(btnTabWaiting, "waiting"));
        btnTabSelected.setOnClickListener(v -> selectTab(btnTabSelected, "selected"));
        btnTabEnrolled.setOnClickListener(v -> selectTab(btnTabEnrolled, "enrolled"));
        btnTabCanceled.setOnClickListener(v -> selectTab(btnTabCanceled, "canceled"));

        btnNavDashboard.setOnClickListener(v -> {
            // Navigate back to OrganizerActivity (dashboard)
            Intent intent = new Intent(ManageDrawActivity.this, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNavMessage.setOnClickListener(v -> {
            Intent intent = new Intent(ManageDrawActivity.this, OrganizerMessagesActivity.class);
            startActivity(intent);
        });

        btnNavMap.setOnClickListener(v -> {
            Toast.makeText(this, "Map - Coming soon", Toast.LENGTH_SHORT).show();
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ManageDrawActivity.this, ProfileActivity.class);
            intent.putExtra("from_organizer", true);
            startActivity(intent);
        });
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        // Find the content container and add RecyclerView dynamically
        View contentContainer = findViewById(R.id.contentContainer);

        // Create RecyclerView
        recyclerViewEntrants = new RecyclerView(this);
        recyclerViewEntrants.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
        ));
        recyclerViewEntrants.setPadding(12, 12, 12, 12);

        // Add RecyclerView to content container
        if (contentContainer instanceof android.widget.FrameLayout) {
            ((android.widget.FrameLayout) contentContainer).addView(recyclerViewEntrants);
        }

        // Setup layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEntrants.setLayoutManager(layoutManager);

        // Setup adapter
        adapter = new WaitingListAdapter();
        recyclerViewEntrants.setAdapter(adapter);
    }

    /**
     * Load event data from Firestore
     */
    private void loadEventData() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentEvent = documentSnapshot.toObject(Event.class);
                        if (currentEvent != null) {
                            currentEvent.setEventId(documentSnapshot.getId());
                            displayEventInfo();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    Toast.makeText(this, "Error loading event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Display event information in the UI
     */
    private void displayEventInfo() {
        if (currentEvent == null) return;

        // Set event name
        tvEventName.setText(currentEvent.getName() != null ? currentEvent.getName() : "Unknown Event");

        // Set status
        String status = determineEventStatus(currentEvent);
        tvStatus.setText("Status: " + status);

        // Set capacity (draw_capacity from Firestore)
        int capacity = currentEvent.getDrawCapacity();
        tvCapacity.setText("👥 Capacity: " + capacity);

        // Set waiting count to 0 initially (will be updated after loading entries)
        tvWaiting.setText("👥 Waiting: 0");

        // Set location
        String location = currentEvent.getLocation();
        tvLocation.setText("📍 Location: " + (location != null ? location : "Not specified"));

        // Set date
        String dateTime = currentEvent.getDateTime();
        if (dateTime != null && !dateTime.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateTime);
                if (date != null) {
                    tvDate.setText("📅 " + outputFormat.format(date));
                }
            } catch (Exception e) {
                tvDate.setText("📅 " + dateTime);
            }
        } else {
            tvDate.setText("📅 Date not set");
        }
    }

    /**
     * Determine event status based on dates and other factors
     */
    private String determineEventStatus(Event event) {
        // Simple status determination - can be enhanced
        String regStart = event.getRegStart();
        String regStop = event.getRegStop();

        if (regStart == null || regStart.isEmpty()) {
            return "Draft";
        }

        // TODO: Add date comparison logic
        return "Open";
    }

    /**
     * Load waiting list entries from Firestore and fetch associated user data
     */
    private void loadWaitingListEntries() {
        // Query the waiting_lists collection for this event
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEntries.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No entrants found for event: " + eventId);
                        tvWaiting.setText("👥 Waiting: 0");
                        adapter.submitList(new ArrayList<>());
                        Toast.makeText(this, "No entrants found for this event",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int totalEntries = queryDocumentSnapshots.size();
                    int[] loadedCount = {0}; // Counter for completed user fetches

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WaitingListEntry entry = document.toObject(WaitingListEntry.class);

                        // Fetch user data for this entry
                        String userId = entry.getUserId();
                        if (userId != null && !userId.isEmpty()) {
                            db.collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            User user = userDoc.toObject(User.class);
                                            entry.setUser(user);
                                        } else {
                                            // Create placeholder user if not found
                                            User placeholderUser = new User();
                                            placeholderUser.setUserId(userId);
                                            placeholderUser.setName("Unknown User");
                                            entry.setUser(placeholderUser);
                                        }

                                        allEntries.add(entry);
                                        loadedCount[0]++;

                                        // When all users are loaded, update UI
                                        if (loadedCount[0] == totalEntries) {
                                            onAllEntriesLoaded();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error loading user: " + userId, e);

                                        // Create placeholder user on error
                                        User placeholderUser = new User();
                                        placeholderUser.setUserId(userId);
                                        placeholderUser.setName("Unknown User");
                                        entry.setUser(placeholderUser);

                                        allEntries.add(entry);
                                        loadedCount[0]++;

                                        // When all users are loaded, update UI
                                        if (loadedCount[0] == totalEntries) {
                                            onAllEntriesLoaded();
                                        }
                                    });
                        } else {
                            // Handle entry with no userId
                            User placeholderUser = new User();
                            placeholderUser.setName("Unknown User");
                            entry.setUser(placeholderUser);
                            allEntries.add(entry);
                            loadedCount[0]++;

                            if (loadedCount[0] == totalEntries) {
                                onAllEntriesLoaded();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waiting list entries", e);
                    Toast.makeText(this, "Error loading entrants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Called when all waiting list entries and user data have been loaded
     */
    private void onAllEntriesLoaded() {
        Log.d(TAG, "Loaded " + allEntries.size() + " entries with user data for event: " + eventId);

        // Count all statuses
        int waitingCount = 0;
        int selectedCount = 0;
        int enrolledCount = 0;
        int canceledCount = 0;

        for (WaitingListEntry entry : allEntries) {
            String status = entry.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "waiting":
                        waitingCount++;
                        break;
                    case "selected":
                        selectedCount++;
                        break;
                    case "enrolled":
                        enrolledCount++;
                        break;
                    case "canceled":
                        canceledCount++;
                        break;
                }
            }
        }

        // Update waiting count display
        tvWaiting.setText("👥 Waiting: " + waitingCount);

        // Log all counts for debugging
        Log.d(TAG, "Status counts - Waiting: " + waitingCount +
                ", Selected: " + selectedCount +
                ", Enrolled: " + enrolledCount +
                ", Canceled: " + canceledCount);

        // Apply current filter
        filterEntries(currentFilter);
    }

    /**
     * Select a tab and filter entries
     */
    private void selectTab(TextView selectedTab, String filter) {
        // Reset all tabs
        resetTabs();

        // Highlight selected tab
        selectedTab.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        selectedTab.setTextColor(getResources().getColor(android.R.color.black, null));

        // Update current filter
        currentFilter = filter;

        // Filter entries
        filterEntries(filter);
    }

    /**
     * Reset all tab appearances
     */
    private void resetTabs() {
        int defaultBackground = getResources().getColor(android.R.color.transparent, null);
        int defaultTextColor = getResources().getColor(android.R.color.black, null);

        btnTabWaiting.setBackgroundColor(defaultBackground);
        btnTabWaiting.setTextColor(defaultTextColor);

        btnTabSelected.setBackgroundColor(defaultBackground);
        btnTabSelected.setTextColor(defaultTextColor);

        btnTabEnrolled.setBackgroundColor(defaultBackground);
        btnTabEnrolled.setTextColor(defaultTextColor);

        btnTabCanceled.setBackgroundColor(defaultBackground);
        btnTabCanceled.setTextColor(defaultTextColor);
    }

    /**
     * Filter entries by status
     */
    private void filterEntries(String status) {
        filteredEntries.clear();

        for (WaitingListEntry entry : allEntries) {
            if (status.equalsIgnoreCase(entry.getStatus())) {
                filteredEntries.add(entry);
            }
        }

        Log.d(TAG, "Filtered " + filteredEntries.size() + " entries with status: " + status);

        // Update adapter
        adapter.submitList(new ArrayList<>(filteredEntries));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh entries when returning to this activity
        loadWaitingListEntries();
    }
}
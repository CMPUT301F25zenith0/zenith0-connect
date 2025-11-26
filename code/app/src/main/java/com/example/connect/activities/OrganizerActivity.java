package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.OrganizerEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import android.os.Environment;

import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.example.connect.utils.CsvUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Activity serving as the main dashboard for event organizers
 * <p>
 * This activity provides organizers with a comprehensive interface to manage their events:
 * <ul>
 *   <li>View all events organized by the user</li>
 *   <li>Create new events</li>
 *   <li>Edit existing events</li>
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
 * @version 3.0
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";

    // UI Components
    private MaterialButton btnNewEvent;
    private MaterialButton btnTotalEvents, btnOpen, btnClosed, btnDrawn;
    private RecyclerView recyclerViewEvents;
    private MaterialButton btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    // Data
    private OrganizerEventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private String currentFilter = "all"; // Track current filter

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        initializeViews();
        setupClickListeners();
        setupRecyclerView();

        // Load events
        loadOrganizerEvents();

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

        btnNavDashboard.setOnClickListener(v -> {
            // Already on dashboard - just refresh
            loadOrganizerEvents();
            selectFilter(btnTotalEvents, "all");

            if (recyclerViewEvents != null && allEvents.size() > 0) {
                recyclerViewEvents.smoothScrollToPosition(0);
            }

            Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
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
            profileIntent.putExtra("from_organizer", true);
            startActivity(profileIntent);
        });
    }

    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewEvents.setLayoutManager(layoutManager);

        // Setup adapter with listeners
        adapter = new OrganizerEventAdapter(new OrganizerEventAdapter.OrganizerEventListener() {
            @Override
            public void onEditEvent(Event event) {
                // Navigate to CreateEvent activity in edit mode
                Intent intent = new Intent(OrganizerActivity.this, CreateEvent.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                intent.putExtra("EDIT_MODE", true);
                startActivity(intent);
            }

            @Override
            public void onViewDetails(Event event) {
                // Navigate to EventDetails activity
                Intent intent = new Intent(OrganizerActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onManageDraw(Event event) {
                // Navigate to manage draw activity
                Intent intent = new Intent(OrganizerActivity.this, ManageDrawActivity.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onExportCSV(Event event) {
                // Export event data to CSV
                exportEnrolledEntrantsToCsv(event);
            }

            @Override
            public void onImageClick(Event event) {
                // Allow organizer to change/add event image
                Toast.makeText(OrganizerActivity.this,
                        "Change Image: " + event.getName(),
                        Toast.LENGTH_SHORT).show();

                // TODO: Implement image selection/update
            }
        });

        recyclerViewEvents.setAdapter(adapter);
    }

    private void loadOrganizerEvents() {
        db.collection("events")
                .whereEqualTo("organizer_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        allEvents.add(event);
                    }

                    Log.d(TAG, "Loaded " + allEvents.size() + " events for organizer: " + currentUserId);

                    // Apply current filter
                    filterEvents(currentFilter);

                    if (allEvents.isEmpty()) {
                        Toast.makeText(this, "No events found. Create your first event!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this,
                            "Error loading events: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void selectFilter(MaterialButton selectedButton, String filter) {
        // Reset all buttons to default state
        resetFilterButtons();

        // Highlight selected button
        selectedButton.setBackgroundColor(getResources().getColor(R.color.filter_selected, null));
        selectedButton.setTextColor(getResources().getColor(android.R.color.white, null));

        // Update current filter
        currentFilter = filter;

        // Filter events based on selection
        filterEvents(filter);
    }

    private void resetFilterButtons() {
        // Reset all filter buttons to default outlined style
        int defaultTextColor = getResources().getColor(R.color.filter_text_default, null);

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
        filteredEvents.clear();

        switch (filter) {
            case "all":
                filteredEvents.addAll(allEvents);
                Log.d(TAG, "Showing all events: " + filteredEvents.size());
                break;

            case "open":
                for (Event event : allEvents) {
                    if (isEventOpen(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing open events: " + filteredEvents.size());
                break;

            case "closed":
                for (Event event : allEvents) {
                    if (isEventClosed(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing closed events: " + filteredEvents.size());
                break;

            case "drawn":
                for (Event event : allEvents) {
                    if (isEventDrawn(event)) {
                        filteredEvents.add(event);
                    }
                }
                Log.d(TAG, "Showing drawn events: " + filteredEvents.size());
                break;
        }

        // Update adapter with filtered list
        adapter.submitList(new ArrayList<>(filteredEvents));
    }

    private boolean isEventOpen(Event event) {
        String regStart = event.getRegStart();
        String regStop = event.getRegStop();

        boolean hasRegWindow = regStart != null && !regStart.isEmpty() &&
                regStop != null && !regStop.isEmpty();

        // TODO: Add date comparison logic
        return hasRegWindow;
    }

    private boolean isEventClosed(Event event) {
        // TODO: Implement proper logic based on registration end date
        return false;
    }

    private boolean isEventDrawn(Event event) {
        // TODO: Implement proper logic based on draw status field
        return false;
    }
    /**
     * US 02.06.05
     * Export final list of ENROLLED entrants for this event as a CSV file.
     */
    private void exportEnrolledEntrantsToCsv(Event event) {
        if (event == null || event.getEventId() == null || event.getEventId().isEmpty()) {
            Toast.makeText(this, "Cannot export: event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = event.getEventId();
        String eventName = event.getName();

        Toast.makeText(this, "Preparing CSV for enrolled entrants…", Toast.LENGTH_SHORT).show();

        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .whereEqualTo("status", "enrolled")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this,
                                "No enrolled entrants for this event yet",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int total = querySnapshot.size();
                    int[] loadedCount = {0};
                    java.util.List<CsvUtils.CsvRow> rows = new java.util.ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry == null) {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                finishCsvExport(event, rows);
                            }
                            continue;
                        }

                        String userId = entry.getUserId();
                        if (userId == null || userId.isEmpty()) {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                finishCsvExport(event, rows);
                            }
                            continue;
                        }

                        db.collection("accounts")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    User user = userDoc.toObject(User.class);

                                    String name = (user != null && user.getName() != null)
                                            ? user.getName()
                                            : "Unknown User";
                                    String email = (user != null && user.getEmail() != null)
                                            ? user.getEmail()
                                            : "";
                                    String phone = (user != null && user.getPhone() != null)
                                            ? user.getPhone()
                                            : "";

                                    // Prefer enrolled_date; fall back to joined_date
                                    Timestamp ts = entry.getEnrolledDate() != null
                                            ? entry.getEnrolledDate()
                                            : entry.getJoinedDate();
                                    String joinedDate = formatTimestamp(ts);

                                    rows.add(new CsvUtils.CsvRow(name, email, phone, joinedDate));

                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) {
                                        finishCsvExport(event, rows);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading user for CSV", e);
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) {
                                        finishCsvExport(event, rows);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading enrolled entrants", e);
                    Toast.makeText(this,
                            "Error loading enrolled entrants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Called when we have collected all CSV rows.
     */
    private void finishCsvExport(Event event, java.util.List<CsvUtils.CsvRow> rows) {
        if (rows == null || rows.isEmpty()) {
            Toast.makeText(this,
                    "No enrolled entrants to export",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String csvContent = CsvUtils.buildEnrolledEntrantsCsv(
                event.getName(),
                event.getEventId(),
                rows
        );

        writeCsvToFile(event, csvContent);
    }

    /**
     * Writes CSV text to a file in the app's Downloads directory.
     * No extra storage permission needed (app-specific external storage).
     */
    private void writeCsvToFile(Event event, String csvContent) {
        // Public Download directory
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (dir == null) {
            Toast.makeText(this,
                    "Unable to access public Downloads folder",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure folder exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Safe filename
        String rawName = (event.getName() != null && !event.getName().isEmpty())
                ? event.getName()
                : "event";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        String fileName = "enrolled_" + safeName + ".csv";
        File outFile = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(csvContent.getBytes(StandardCharsets.UTF_8));
            fos.flush();

            String message = "CSV saved to Downloads: " + outFile.getAbsolutePath();
            Log.d(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV file", e);
            Toast.makeText(this,
                    "Error saving CSV: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Format a Firestore Timestamp into a readable string for CSV.
     */
    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        Date date = ts.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh event list when returning to this activity
        loadOrganizerEvents();
    }
}
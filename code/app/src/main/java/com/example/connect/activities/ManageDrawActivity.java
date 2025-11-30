package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.WaitingListAdapter;
import com.example.connect.models.Event;
import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.example.connect.utils.LotteryManager;
import com.example.connect.utils.NotificationHelper; // 🔹 NEW IMPORT
import com.google.android.material.button.MaterialButton; // 🔹 NEW IMPORT
import com.example.connect.utils.LotteryScheduler;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for managing event lottery draws and viewing entrants.
 *
 * Features:
 * - View event details and statistics
 * - Filter entrants by status (Waiting, Selected, Enrolled, Canceled)
 * - View list of entrants with their details
 * - Navigate between different entrant categories
 * - Send notifications to selected or canceled entrants
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

    // 🔹 UI Components - Notification Actions
    private MaterialButton btnNotifySelected;
    private MaterialButton btnNotifyCanceled;
    private MaterialButton btnCancelUnconfirmed;

    private MaterialButton btnNotifyWaiting;

    // manual lottery button
    private Button btnManualLottery;
    private Button btnDrawReplacement;

    private Button btnCancelUnresponsive;

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
    List<WaitingListEntry> allEntries = new ArrayList<>();
    List<WaitingListEntry> filteredEntries = new ArrayList<>();
    String currentFilter = "waiting";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isTest = false;

    // 🔹 Notifications
    private NotificationHelper notificationHelper;
    private LotteryManager lotteryManager;

    private boolean isFirstLoad = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_draw);

        // Initialize Firebase
        if (!isTest) {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        }

        // 🔹 Initialize helper
        notificationHelper = new NotificationHelper();

        lotteryManager = new LotteryManager();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadEventData();
        loadWaitingListEntries();

        // Set default filter to Waiting
        selectTab(btnTabWaiting, "waiting");
    }

    public void setIsTest(boolean isTest) { this.isTest = isTest; }
    public void setFirestore(com.google.firebase.firestore.FirebaseFirestore db) { this.db = db; }
    public void setAuth(com.google.firebase.auth.FirebaseAuth auth) { this.auth = auth; }

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

        // 🔹 Bulk notification buttons
        btnNotifySelected = findViewById(R.id.btnNotifySelected);
        btnNotifyCanceled = findViewById(R.id.btnNotifyCanceled);
        btnNotifyWaiting = findViewById(R.id.btnNotifyWaiting);
        btnDrawReplacement = findViewById(R.id.btnDrawReplacement);

        // manually run lottery
        btnManualLottery = findViewById(R.id.btnManualLottery);

        btnCancelUnresponsive = findViewById(R.id.btnCancelUnresponsive);
//        btnCancelUnconfirmed = findViewById(R.id.btnCancelUnconfirmed);

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
        btnTabWaiting.setOnClickListener(v -> {
            // US 02.02.01 – Organizer views waiting list
            selectTab(btnTabWaiting, "waiting");
        });

        btnTabSelected.setOnClickListener(v -> {
            // US 02.06.01 – View list of chosen entrants
            selectTab(btnTabSelected, "selected");
        });

        btnTabEnrolled.setOnClickListener(v -> selectTab(btnTabEnrolled, "enrolled"));
        btnTabCanceled.setOnClickListener(v -> selectTab(btnTabCanceled, "canceled"));

        // 🔹 US 02.07.02 - Notify all selected entrants
        btnNotifySelected.setOnClickListener(v -> handleNotifySelected());

        // 🔹 US 02.07.03 - Notify all canceled entrants
        btnNotifyCanceled.setOnClickListener(v -> handleNotifyCanceled());

        // 🔹 US 02.06.04 - Cancel entrants that did not sign up (still "selected")
        //  btnCancelUnconfirmed.setOnClickListener(v -> handleCancelUnconfirmed());


        btnNotifyWaiting.setOnClickListener(v -> handleNotifyWaitlist());

        btnDrawReplacement.setOnClickListener(v ->
                getDeclinedCount(eventId, declinedCount -> {
                    lotteryManager.performReplacementLottery(eventId, currentEvent.getName(), declinedCount, new LotteryManager.LotteryCallback() {
                        @Override
                        public void onSuccess(int selectedCount, int waitingListCount) {
                            Toast.makeText(ManageDrawActivity.this, "Replacement draw completed", Toast.LENGTH_SHORT).show();
                            loadWaitingListEntries(); // Activity updates UI
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ManageDrawActivity.this, "Failed replacement draw: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });

                })
        );

        btnManualLottery.setOnClickListener(v -> {
            LotteryScheduler scheduler = new LotteryScheduler();
            scheduler.runLotteryManually(eventId);
            Toast.makeText(this, "Manual lottery triggered for event: " + currentEvent.getName(), Toast.LENGTH_SHORT).show();
        });


        btnCancelUnresponsive.setOnClickListener(v -> {

            // Get unresponsive duration from the event (in hours, for example)
            long durationHours = currentEvent.getUnresponsiveDurationHours(); // default fallback if null
            if (durationHours <= 0) durationHours = 24; // fallback to 24h if not set

            // Convert hours to milliseconds
            long unresponsiveDurationMillis = durationHours * 60L * 60L * 1000L;

            // Cutoff timestamp
            long cutoffMillis = System.currentTimeMillis() - unresponsiveDurationMillis;
            Timestamp cutoffTimestamp = new Timestamp(new Date(cutoffMillis));

            long finalDurationHours = durationHours;
            db.collection("waiting_lists")
                    .document(eventId)
                    .collection("entrants")
                    .whereEqualTo("status", "selected")
                    .whereLessThan("selected_date", cutoffTimestamp)
                    .get()
                    .addOnSuccessListener(query -> {

                        if (query.isEmpty()) {
                            Toast.makeText(this, "No unresponsive entrants (selected >" + finalDurationHours + "h ago)", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No unresponsive selected entrants.");
                            return;
                        }

                        WriteBatch batch = db.batch();

                        for (DocumentSnapshot doc : query.getDocuments()) {
                            batch.update(doc.getReference(), "status", "canceled");
                            batch.update(doc.getReference(), "canceled_date", FieldValue.serverTimestamp());
                        }

                        batch.commit().addOnSuccessListener(aVoid -> {

                            Log.d(TAG, "Canceled " + query.size() + " unresponsive entrants.");

                            // Run replacement lottery
                            String eventName = currentEvent.getName() != null
                                    ? currentEvent.getName()
                                    : "your event";
                            int numCancelled = query.size();

                            lotteryManager.performReplacementLottery(
                                    eventId,
                                    eventName,
                                    numCancelled,
                                    new LotteryManager.LotteryCallback() {
                                        @Override
                                        public void onSuccess(int selectedCount, int waitingListCount) {
                                            Log.d(TAG, "Replacement lottery completed.");
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Log.e(TAG, "Replacement lottery failed: " + error);
                                        }
                                    }
                            );

                        });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch entrants", e));
        });

        adapter.setOnSendNotificationClickListener(entry -> {
            handleNotifyCustom(entry);
        });
        adapter.setOnCancelEntrantClickListener(entry -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Entrant")
                    .setMessage("Are you sure you want to cancel " + entry.getUser().getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelSingleEntrant(entry))
                    .setNegativeButton("No", null)
                    .show();
        });

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
            // US 02.02.02: Launch map view showing where entrants joined from
            Intent intent = new Intent(ManageDrawActivity.this, EntrantMapActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ManageDrawActivity.this, ProfileActivity.class);
            intent.putExtra("from_organizer", true);
            startActivity(intent);
        });
    }

    private void cancelSingleEntrant(WaitingListEntry entry) {
        if (entry.getDocumentId() == null || entry.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Error: Entry has no document ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "canceled");
        updates.put("canceled_date", Timestamp.now());

        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .document(entry.getDocumentId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, entry.getUser().getName() + " canceled", Toast.LENGTH_SHORT).show();
                    loadWaitingListEntries(); // refresh the list and counts
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to cancel " + entry.getUser().getName(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to cancel entrant", e);
                });
    }


    /**
     * Count how many entrants have declined for the event.
     */
    public interface OnDeclinedCountReady {
        void onReady(int declinedCount);
    }

    public void getDeclinedCount(String eventId, OnDeclinedCountReady callback) {
        db.collection("events")
                .document(eventId)
                .collection("declined")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int declinedCount = snapshot.size();
                    callback.onReady(declinedCount); // RETURNS int
                });
    }




    /**
     * Setup RecyclerView with adapter
     */
    void setupRecyclerView() {
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

        // Set status with draw info
        String status = determineEventStatus(currentEvent);
        if (currentEvent.isDrawCompleted()) {
            status = "Draw Completed ✓";
            // Format draw date if available
            if (currentEvent.getDrawDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                Date drawDate = currentEvent.getDrawDate().toDate();
                status += " (" + sdf.format(drawDate) + ")";
            }
        }
        tvStatus.setText("Status: " + status);

        // Set capacity (draw_capacity from Firestore)
        int capacity = currentEvent.getDrawCapacity();
        tvCapacity.setText("👥 Capacity: " + capacity);

        // Set waiting count to 0 initially (will be updated after loading)
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
                            db.collection("accounts")
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
    void onAllEntriesLoaded() {
        Log.d(TAG, "Loaded " + allEntries.size() + " entries with user data for event: " + eventId);

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

        tvWaiting.setText("👥 Waiting: " + waitingCount);

        btnTabWaiting.setText("Waiting (" + waitingCount + ")");
        btnTabSelected.setText("Selected (" + selectedCount + ")");
        btnTabEnrolled.setText("Enrolled (" + enrolledCount + ")");
        btnTabCanceled.setText("Canceled (" + canceledCount + ")");

        Log.d(TAG, "Status counts - Waiting: " + waitingCount +
                ", Selected: " + selectedCount +
                ", Enrolled: " + enrolledCount +
                ", Canceled: " + canceledCount);

        filterEntries(currentFilter);
    }

    /**
     * Select a tab and filter entries
     */
    private void selectTab(TextView selectedTab, String filter) {
        resetTabs();

        selectedTab.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        selectedTab.setTextColor(getResources().getColor(android.R.color.black, null));

        currentFilter = filter;

        // US-specific logs
        switch (filter) {
            case "waiting":
                Log.d(TAG, "US 02.02.01 - Organizer is viewing the waiting list");
                break;
            case "selected":
                Log.d(TAG, "US 02.06.01 - Organizer is viewing chosen entrants");
                break;
        }

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
    void filterEntries(String status) {
        filteredEntries.clear();

        for (WaitingListEntry entry : allEntries) {
            if (status.equalsIgnoreCase(entry.getStatus())) {
                filteredEntries.add(entry);
            }
        }

        Log.d(TAG, "Filtered " + filteredEntries.size() + " entries with status: " + status);

        adapter.submitList(new ArrayList<>(filteredEntries));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLoad) {
            loadWaitingListEntries();
        }
        isFirstLoad = false;
    }

    // ------------------------------------------------------------------
    // 🔹 Notification helpers (US 02.07.02 & 02.07.03)
    // ------------------------------------------------------------------

    private void handleNotifyCustom(WaitingListEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_custom_notification, null);

        EditText etTitle = view.findViewById(R.id.etCustomTitle);
        EditText etBody = view.findViewById(R.id.etCustomBody);
        Button btnSend = view.findViewById(R.id.btnSendCustom);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String body = etBody.getText().toString().trim();

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please enter title and message", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventName = currentEvent.getName() != null
                    ? currentEvent.getName()
                    : "your event";

            // SINGLE recipient list
            List<String> oneRecipient = Collections.singletonList(entry.getUserId());

            notificationHelper.notifyCustom(
                    eventId,
                    oneRecipient,
                    eventName,
                    new NotificationHelper.NotificationCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(getApplicationContext(), "Notification sent!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getApplicationContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    },
                    title,
                    body,
                    "custom"
            );
        });
    }


    /**
     * US 02.07.02 - Notify all selected entrants
     */
    /**
     * US 02.07.02 - Notify all selected entrants
     */
    private void handleNotifySelected() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedUserIds = new ArrayList<>();
        for (WaitingListEntry entry : allEntries) {
            String status = entry.getStatus();
            String uid = entry.getUserId();   // ✅ use userId from waiting list entry

            if (status != null
                    && "selected".equalsIgnoreCase(status.trim())
                    && uid != null
                    && !uid.isEmpty()) {
                selectedUserIds.add(uid);
            }
        }

        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "No selected entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNotifySelected.setEnabled(false);

        String eventName = currentEvent.getName() != null
                ? currentEvent.getName()
                : "your event";

        notificationHelper.notifyChosenEntrants(
                eventId,
                selectedUserIds,
                eventName,
                new NotificationHelper.NotificationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            btnNotifySelected.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnNotifySelected.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this,
                                    "Failed to send notifications: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    /**
     * US 02.07.03 - Notify all canceled entrants
     */
    private void handleNotifyCanceled() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> canceledUserIds = new ArrayList<>();
        for (WaitingListEntry entry : allEntries) {
            String status = entry.getStatus();
            String uid = entry.getUserId();   // ✅ use userId from waiting list entry

            if (status != null
                    && "canceled".equalsIgnoreCase(status.trim())
                    && uid != null
                    && !uid.isEmpty()) {
                canceledUserIds.add(uid);
            }
        }

        if (canceledUserIds.isEmpty()) {
            Toast.makeText(this, "No canceled entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNotifyCanceled.setEnabled(false);

        String eventName = currentEvent.getName() != null
                ? currentEvent.getName()
                : "your event";

        notificationHelper.notifyCanceledEntrants(
                eventId,
                canceledUserIds,
                eventName,
                new NotificationHelper.NotificationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            btnNotifyCanceled.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnNotifyCanceled.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this,
                                    "Failed to send notifications: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private void handleNotifyWaitlist(){
        if (currentEvent == null) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> waitingUserIds = new ArrayList<>();
        for (WaitingListEntry entry : allEntries) {
            String status = entry.getStatus();
            String uid = entry.getUserId();   // ✅ use userId from waiting list entry

            if (status != null
                    && "waiting".equalsIgnoreCase(status.trim())
                    && uid != null
                    && !uid.isEmpty()) {
                waitingUserIds.add(uid);
            }
        }

        if (waitingUserIds.isEmpty()) {
            Toast.makeText(this, "No waiting entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNotifyWaiting.setEnabled(false);

        String eventName = currentEvent.getName() != null
                ? currentEvent.getName()
                : "your event";

        notificationHelper.notifyAllWaitingListEntrants(
                eventId,
                waitingUserIds,
                eventName,
                new NotificationHelper.NotificationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            btnNotifyWaiting.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            btnNotifyWaiting.setEnabled(true);
                            Toast.makeText(ManageDrawActivity.this,
                                    "Failed to send notifications: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    /**
     * US 02.06.04 - Cancel entrants that did not sign up for the event
     *
     * Interpretation:
     *  - Entrants who were SELECTED but never ENROLLED.
     *  - We treat status == "selected" as "unconfirmed / did not sign up".
     *  - This method updates their status to "canceled" in Firestore.
     *
     * Organizer can then use "Notify Canceled" to message them.
     */
    private void handleCancelUnconfirmed() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect all entries that are still "selected"
        List<WaitingListEntry> toCancel = new ArrayList<>();
        for (WaitingListEntry entry : allEntries) {
            String status = entry.getStatus();
            if (status != null && "selected".equalsIgnoreCase(status.trim())) {
                toCancel.add(entry);
            }
        }

        if (toCancel.isEmpty()) {
            Toast.makeText(this, "No unconfirmed (selected) entrants to cancel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to avoid double-taps
//        btnCancelUnconfirmed.setEnabled(false);

        // Batch update in Firestore
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();

        for (WaitingListEntry entry : toCancel) {
            String docId = entry.getDocumentId();
            if (docId == null || docId.isEmpty()) {
                Log.w(TAG, "Skipping cancel for entry with no documentId, userId=" + entry.getUserId());
                continue;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "canceled");
            updates.put("canceled_date", now);

            batch.update(
                    db.collection("waiting_lists")
                            .document(eventId)
                            .collection("entrants")
                            .document(docId),
                    updates
            );
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Canceled " + toCancel.size() + " unconfirmed entrants");
                    runOnUiThread(() -> {
                        Toast.makeText(ManageDrawActivity.this,
                                "Canceled " + toCancel.size() + " unconfirmed entrants",
                                Toast.LENGTH_LONG).show();
//                        btnCancelUnconfirmed.setEnabled(true);
                        // Reload list so tabs + counts update
                        loadWaitingListEntries();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error canceling unconfirmed entrants", e);
                    runOnUiThread(() -> {
                        Toast.makeText(ManageDrawActivity.this,
                                "Failed to cancel entrants: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
//                        btnCancelUnconfirmed.setEnabled(true);
                    });
                });
    }



}

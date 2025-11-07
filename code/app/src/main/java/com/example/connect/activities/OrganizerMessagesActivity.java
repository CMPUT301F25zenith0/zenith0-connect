package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the Java for the organizer dashboard, it has the functionality for Notify All Entrants and for Notifying Waiting List.
 */
public class OrganizerMessagesActivity extends AppCompatActivity {
    private static final String TAG = "OrganizerMessagesActivityTag";

    private FirebaseFirestore db;
    private NotificationHelper notificationHelper;

    private Spinner spinnerEntrants;
    private EditText etEventId, etEventName, etSearch;
    private MaterialButton btnBack;
    private Button btnNotifyChosen, btnNotifyWaitingList;
    private MaterialButton btnFilterByEvent, btnNewMessage;
    private MaterialButton btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    private List<String> entrantUids = new ArrayList<>();
    private List<String> entrantNames = new ArrayList<>();
    String eventId = "TEST_EVENT"; // Default for testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        db = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper();

        // Initialize views
        initializeViews();

        // Load entrants
        loadEntrants();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        // Text inputs
        etEventId = findViewById(R.id.et_event_id);
        etEventName = findViewById(R.id.et_event_name);
        etSearch = findViewById(R.id.etSearch);

        // Top bar
        btnBack = findViewById(R.id.btnBack);

        // Notification buttons
        btnNotifyChosen = findViewById(R.id.btn_notify_chosen);
        btnNotifyWaitingList = findViewById(R.id.btn_notify_waiting_list);

        // Messages section buttons
        btnFilterByEvent = findViewById(R.id.btnFilterByEvent);
        btnNewMessage = findViewById(R.id.btnNewMessage);

        // Bottom navigation
        btnNavDashboard = findViewById(R.id.btnNavDashboard);
        btnNavMessage = findViewById(R.id.btnNavMessage);
        btnNavMap = findViewById(R.id.btnNavMap);
        btnNavProfile = findViewById(R.id.btnNavProfile);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // US 02.05.01 & US 01.04.01 - Notify chosen entrants
        btnNotifyChosen.setOnClickListener(v -> {
            String eventIdStr = etEventId.getText().toString().trim();
            String eventName = etEventName.getText().toString().trim();

            if (eventIdStr.isEmpty() || eventName.isEmpty()) {
                Toast.makeText(this, "Please enter event ID and name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Atomic counters for both groups
            AtomicInteger totalNotifications = new AtomicInteger(0);
            AtomicInteger completedGroups = new AtomicInteger(0);

            getChosenEntrants(eventIdStr, chosenIds -> {
                getNotChosenEntrants(eventIdStr, notChosenIds -> {

                    int tempTotalGroups = 0;
                    if (!chosenIds.isEmpty()) tempTotalGroups++;
                    if (!notChosenIds.isEmpty()) tempTotalGroups++;

                    if (tempTotalGroups == 0) {
                        Toast.makeText(this, "No entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Make it final so it can be used in inner class
                    final int totalGroups = tempTotalGroups;

                    NotificationHelper.NotificationCallback combinedCallback =
                            new NotificationHelper.NotificationCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    int sent = extractNumber(message);
                                    totalNotifications.addAndGet(sent);

                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        runOnUiThread(() -> Toast.makeText(
                                                OrganizerMessagesActivity.this,
                                                "Sent " + totalNotifications.get() + " notifications successfully!",
                                                Toast.LENGTH_LONG
                                        ).show());
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        runOnUiThread(() -> Toast.makeText(
                                                OrganizerMessagesActivity.this,
                                                "Some notifications failed: " + error,
                                                Toast.LENGTH_LONG
                                        ).show());
                                    }
                                }
                            };

                    // Send to chosen entrants
                    if (!chosenIds.isEmpty()) {
                        notificationHelper.notifyChosenEntrants(eventIdStr, chosenIds, eventName, combinedCallback);
                    }

                    // Send to not-chosen entrants
                    if (!notChosenIds.isEmpty()) {
                        notificationHelper.notifyNotChosenEntrants(eventIdStr, notChosenIds, eventName, combinedCallback);
                    }

                });
            });
        });

        // US 02.07.01 - Notify all waiting list entrants
        btnNotifyWaitingList.setOnClickListener(v -> {
            String eventIdStr = etEventId.getText().toString().trim();
            String eventName = etEventName.getText().toString().trim();

            if (eventIdStr.isEmpty() || eventName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            notificationHelper.notifyAllWaitingListEntrants(
                    eventIdStr,
                    eventName,
                    new NotificationHelper.NotificationCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            runOnUiThread(() ->
                                    Toast.makeText(OrganizerMessagesActivity.this,
                                            msg, Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() ->
                                    Toast.makeText(OrganizerMessagesActivity.this,
                                            "Error: " + error, Toast.LENGTH_LONG).show()
                            );
                        }
                    }
            );
        });

        // Search functionality
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        // TODO: Implement search messages functionality
                        Toast.makeText(OrganizerMessagesActivity.this,
                                "Search: " + s.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Filter by event button
        if (btnFilterByEvent != null) {
            btnFilterByEvent.setOnClickListener(v -> {
                // TODO: Implement filter by event dialog
                Toast.makeText(this, "Filter By Event - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        // New message button
        if (btnNewMessage != null) {
            btnNewMessage.setOnClickListener(v -> {
                // TODO: Navigate to new message compose screen
                Toast.makeText(this, "New Message - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Bottom Navigation
        if (btnNavDashboard != null) {
            btnNavDashboard.setOnClickListener(v -> {
                // TODO: Navigate to dashboard
                Toast.makeText(this, "Dashboard - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavMessage != null) {
            btnNavMessage.setOnClickListener(v -> {
                // Already on messages page
                Toast.makeText(this, "Already on Messages", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavMap != null) {
            btnNavMap.setOnClickListener(v -> {
                // TODO: Navigate to map
                Toast.makeText(this, "Map - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavProfile != null) {
            btnNavProfile.setOnClickListener(v -> {
                Intent profileIntent = new Intent(OrganizerMessagesActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            });
        }
    }

    int extractNumber(String message) {
        try {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception e) {
            Log.e("OrganizerMessagesActivity", "Failed to extract number from message: " + message, e);
        }
        return 0;
    }

    /**
     * Load all accounts/entrants from Firestore for the spinner
     */
    private void loadEntrants() {
        Log.d(TAG, "Loading entrants from Firestore...");
        db.collection("accounts").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to load entrants", task.getException());
                return;
            }

            entrantUids.clear();
            entrantNames.clear();

            int totalDocs = task.getResult().size();
            Log.d(TAG, "Total documents in accounts collection: " + totalDocs);

            for (QueryDocumentSnapshot doc : task.getResult()) {
                String name = doc.getString("full_name");
                String uid = doc.getId();
                if (name != null) {
                    entrantNames.add(name);
                    entrantUids.add(uid);
                    Log.d(TAG, "Added entrant: " + name + " UID: " + uid);
                } else {
                    Log.w(TAG, "Skipped document with UID " + uid + " because name is null");
                }
            }

            Log.d(TAG, "Total entrants added: " + entrantNames.size());
        });
    }

    /**
     * Get list of chosen entrant IDs from Firestore
     */
    private void getChosenEntrants(String eventId, EntrantListCallback callback) {
        db.collection("events").document(eventId)
                .collection("chosen")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> chosenIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        chosenIds.add(doc.getId());
                    }
                    Log.d(TAG, "Found " + chosenIds.size() + " chosen entrants for event " + eventId);
                    callback.onEntrantsLoaded(chosenIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get chosen entrants", e);
                    Toast.makeText(this, "Failed to load chosen entrants", Toast.LENGTH_SHORT).show();
                    callback.onEntrantsLoaded(new ArrayList<>());
                });
    }

    /**
     * Get list of not chosen entrant IDs from Firestore
     */
    private void getNotChosenEntrants(String eventId, EntrantListCallback callback) {
        db.collection("events").document(eventId)
                .collection("notChosen")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> notChosenIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        notChosenIds.add(doc.getId());
                    }
                    Log.d(TAG, "Found " + notChosenIds.size() + " not-chosen entrants");
                    callback.onEntrantsLoaded(notChosenIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get not-chosen entrants", e);
                    Toast.makeText(this, "Failed to load not-chosen entrants", Toast.LENGTH_SHORT).show();
                    callback.onEntrantsLoaded(new ArrayList<>());
                });
    }

    /**
     * Mark a user as chosen in Firestore
     */
    private void markUserAsChosen(String uid) {
        if (spinnerEntrants == null || spinnerEntrants.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "No entrant selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedName = entrantNames.get(spinnerEntrants.getSelectedItemPosition());
        Log.d(TAG, "Attempting to mark user as chosen: " + selectedName + " UID: " + uid);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", FieldValue.serverTimestamp());

        String currentEventId = etEventId.getText().toString().trim();
        if (currentEventId.isEmpty()) {
            currentEventId = eventId; // Use default if not set
        }

        db.collection("events")
                .document(currentEventId)
                .collection("chosen")
                .document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully marked as chosen: " + selectedName + " UID: " + uid);
                    Toast.makeText(this, selectedName + " marked as chosen!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark as chosen: " + selectedName + " UID: " + uid, e);
                    Toast.makeText(this, "Failed to mark as chosen: " + selectedName, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Mark a user as not chosen in Firestore
     */
    private void markUserAsNotChosen(String uid) {
        if (spinnerEntrants == null || spinnerEntrants.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "No entrant selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedName = entrantNames.get(spinnerEntrants.getSelectedItemPosition());
        Log.d(TAG, "Attempting to mark user as NOT chosen: " + selectedName + " UID: " + uid);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", FieldValue.serverTimestamp());

        String currentEventId = etEventId.getText().toString().trim();
        if (currentEventId.isEmpty()) {
            currentEventId = eventId; // Use default if not set
        }

        db.collection("events")
                .document(currentEventId)
                .collection("notChosen")
                .document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully marked as NOT chosen: " + selectedName + " UID: " + uid);
                    Toast.makeText(this, selectedName + " marked as NOT chosen!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark as NOT chosen: " + selectedName + " UID: " + uid, e);
                    Toast.makeText(this, "Failed to mark as NOT chosen: " + selectedName, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Add a user to waiting list in Firestore
     */
    private void addToWaitingList(String uid) {
        if (spinnerEntrants == null || spinnerEntrants.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "No entrant selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedName = entrantNames.get(spinnerEntrants.getSelectedItemPosition());
        Log.d(TAG, "Attempting to add user to waiting list: " + selectedName + " UID: " + uid);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", FieldValue.serverTimestamp());

        String currentEventId = etEventId.getText().toString().trim();
        if (currentEventId.isEmpty()) {
            currentEventId = eventId; // Use default if not set
        }

        db.collection("events")
                .document(currentEventId)
                .collection("waitingList")
                .document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully added to waiting list: " + selectedName + " UID: " + uid);
                    Toast.makeText(this, selectedName + " added to waiting list!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add to waiting list: " + selectedName + " UID: " + uid, e);
                    Toast.makeText(this, "Failed to add to waiting list: " + selectedName, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Callback interface for loading entrant lists
     */
    private interface EntrantListCallback {
        void onEntrantsLoaded(List<String> entrantIds);
    }
}
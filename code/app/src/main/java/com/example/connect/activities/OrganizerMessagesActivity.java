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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

            // Get chosen entrants from "chosen" collection
            getSelectedEntrants(eventIdStr, chosenIds -> {
                // Get not-chosen entrants (waiting list minus chosen)
                getNotChosenEntrants(eventIdStr, notChosenIds -> {

                    int tempTotalGroups = 0;
                    if (!chosenIds.isEmpty()) tempTotalGroups++;
                    if (!notChosenIds.isEmpty()) tempTotalGroups++;

                    if (tempTotalGroups == 0) {
                        Toast.makeText(this, "No entrants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

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
            Log.d(TAG, "Event id and event name: " + eventIdStr + eventName);

            if (eventIdStr.isEmpty() || eventName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get waiting list IDs from the waiting_lists collection
            getWaitingListEntrants(eventIdStr, waitingListIds -> {
                if (waitingListIds.isEmpty()) {
                    Toast.makeText(this, "No entrants in waiting list", Toast.LENGTH_SHORT).show();
                    return;
                }

                notificationHelper.notifyAllWaitingListEntrants(
                        eventIdStr,
                        waitingListIds,
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
     * Load all accounts/entrants from Firestore for use in a spinner or selection UI.
     * <p>
     * Retrieves all documents from the "accounts" collection and populates two lists:
     * - entrantNames: List of full names for display
     * - entrantUids: Corresponding list of user IDs
     * </p>
     */
    /**
     * Load all user accounts for use in a spinner or selection UI.
     *
     * Firestore structure:
     * - accounts/{userId} stores all user documents
     *
     * Populates:
     * - entrantNames: List<String> of display names or full names
     * - entrantUids: List<String> of corresponding user IDs
     */
    private void loadEntrants() {
        db.collection("accounts_N") // use your final "accounts" collection
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error loading entrants", task.getException());
                        return;
                    }

                    entrantUids.clear();
                    entrantNames.clear();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String displayName = doc.getString("display_name");
                        String fullName = doc.getString("full_name");
                        String name = displayName != null && !displayName.isEmpty() ? displayName
                                : fullName != null && !fullName.isEmpty() ? fullName
                                : "Unknown";

                        entrantNames.add(name);
                        entrantUids.add(doc.getId());
                    }

                    Log.d(TAG, "Loaded " + entrantNames.size() + " entrants");
                });
    }



    /**
     * Fetches the list of selected entrants for a specific event.
     *
     * Looks inside the "registrations" subcollection under the given event document in "events",
     * filtering for documents where status == "selected".
     *
     * @param eventId  The ID of the event
     * @param callback Callback that receives the list of selected entrant IDs
     */
    private void getSelectedEntrants(String eventId, EntrantIdsCallback callback) {
        db.collection("events_N")
                .document(eventId)
                .collection("registrations")
                .whereEqualTo("status", "selected")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        ids.add(doc.getId()); // doc ID = userId
                    }
                    callback.onEntrantIdsFetched(ids);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching selected entrants for event: " + eventId, e);
                    callback.onEntrantIdsFetched(new ArrayList<>());
                });
    }

    /**
     * Fetches all registered users for a given event.
     */
    private void getRegisteredUsers(String eventId, EntrantIdsCallback callback) {
        db.collection("events_N").document(eventId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snap) ids.add(doc.getId());
                    callback.onEntrantIdsFetched(ids);
                })
                .addOnFailureListener(e -> callback.onEntrantIdsFetched(new ArrayList<>()));
    }


    /**
     * Get not-chosen entrants for an event based on the new DB structure.
     *
     * Fetches registrations where status = "waiting" and removes users who are already "selected".
     *
     * @param eventId  The ID of the event
     * @param callback Callback that receives the list of not-chosen entrant IDs
     */
    private void getNotChosenEntrants(String eventId, EntrantIdsCallback callback) {
        db.collection("events_N")
                .document(eventId)
                .collection("registrations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> waitingIds = new ArrayList<>();
                    List<String> selectedIds = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        String userId = doc.getId();
                        if ("waiting".equals(status)) waitingIds.add(userId);
                        if ("selected".equals(status)) selectedIds.add(userId);
                    }

                    // Remove selected users from waiting list
                    waitingIds.removeAll(selectedIds);
                    callback.onEntrantIdsFetched(waitingIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching not-chosen entrants for event: " + eventId, e);
                    callback.onEntrantIdsFetched(new ArrayList<>());
                });
    }



    /**
     * Get all entrants on the waiting list for a given event
     * using the new Firestore structure.
     *
     * @param eventId  The ID of the event
     * @param callback Callback that receives the list of waiting list user IDs
     */
    private void getWaitingListEntrants(String eventId, EntrantIdsCallback callback) {
        db.collection("events_N")
                .document(eventId)
                .collection("registrations")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> waitingIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        waitingIds.add(doc.getId());
                    }
                    callback.onEntrantIdsFetched(waitingIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching waiting list entrants for event: " + eventId, e);
                    callback.onEntrantIdsFetched(new ArrayList<>());
                });
    }

    /**
     * Callback interface for fetching entrant IDs
     */
    private interface EntrantIdsCallback {
        void onEntrantIdsFetched(List<String> entrantIds);
    }

}
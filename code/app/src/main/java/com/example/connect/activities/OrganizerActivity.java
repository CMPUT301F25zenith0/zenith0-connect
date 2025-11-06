package com.example.connect.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.utils.NotificationHelper;
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
public class OrganizerActivity extends AppCompatActivity {
    private static final String TAG = "OrganizerActivityTag";

    private FirebaseFirestore db;
    private NotificationHelper notificationHelper;

    private Spinner spinnerEntrants;
    private EditText etEventId, etEventName, etMessage;
    private Button btnNotifyChosen, btnNotifyNotChosen, btnNotifyWaitingList;
    private Button btnMarkChosen, btnMarkNotChosen, btnAddToWaitingList;

    private List<String> entrantUids = new ArrayList<>();
    private List<String> entrantNames = new ArrayList<>();
    String eventId = "TEST_EVENT"; // Default for testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        db = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper();

        // Initialize views
//        spinnerEntrants = findViewById(R.id.spinner_entrants);
        etEventId = findViewById(R.id.et_event_id);
        etEventName = findViewById(R.id.et_event_name);
//        etMessage = findViewById(R.id.et_message);

        // Notification buttons
        btnNotifyChosen = findViewById(R.id.btn_notify_chosen);
//        btnNotifyNotChosen = findViewById(R.id.btn_notify_not_chosen);
        btnNotifyWaitingList = findViewById(R.id.btn_notify_waiting_list);

//        // Management buttons
//        btnMarkChosen = findViewById(R.id.btn_mark_chosen);
//        btnMarkNotChosen = findViewById(R.id.btn_mark_not_chosen);
//        btnAddToWaitingList = findViewById(R.id.btn_add_to_waiting_list);

        // Load entrants
        loadEntrants();

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Mark user as chosen (for testing/manual selection)
//        btnMarkChosen.setOnClickListener(v -> {
//            int selectedIndex = spinnerEntrants.getSelectedItemPosition();
//            if (selectedIndex >= 0) {
//                String selectedUid = entrantUids.get(selectedIndex);
//                markUserAsChosen(selectedUid);
//            }
//        });
//
//        // Mark user as not chosen (for testing/manual selection)
//        btnMarkNotChosen.setOnClickListener(v -> {
//            int selectedIndex = spinnerEntrants.getSelectedItemPosition();
//            if (selectedIndex >= 0) {
//                String selectedUid = entrantUids.get(selectedIndex);
//                markUserAsNotChosen(selectedUid);
//            }
//        });
//
//        // Add user to waiting list (for testing)
//        btnAddToWaitingList.setOnClickListener(v -> {
//            int selectedIndex = spinnerEntrants.getSelectedItemPosition();
//            if (selectedIndex >= 0) {
//                String selectedUid = entrantUids.get(selectedIndex);
//                addToWaitingList(selectedUid);
//            }
//        });

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
                                                OrganizerActivity.this,
                                                "Sent " + totalNotifications.get() + " notifications successfully!",
                                                Toast.LENGTH_LONG
                                        ).show());
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    if (completedGroups.incrementAndGet() == totalGroups) {
                                        runOnUiThread(() -> Toast.makeText(
                                                OrganizerActivity.this,
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
//            String message = etMessage.getText().toString().trim();

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
                                    Toast.makeText(OrganizerActivity.this,
                                            msg, Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() ->
                                    Toast.makeText(OrganizerActivity.this,
                                            "Error: " + error, Toast.LENGTH_LONG).show()
                            );
                        }
                    }
            );
        });
    }

    int extractNumber(String message) {
        try {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception e) {
            Log.e("OrganizerActivity", "Failed to extract number from message: " + message, e);
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
//
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                    android.R.layout.simple_spinner_item, entrantNames);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinnerEntrants.setAdapter(adapter);
//            Log.d(TAG, "Spinner adapter set.");
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
                    Log.d(TAG, "Found " + chosenIds.size() + " chosen entrants");
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

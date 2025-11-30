package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.NotificationMessageAdapter;
import com.example.connect.models.NotificationMessage;
import com.example.connect.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Organizer Messages Activity
 * Shows all notifications sent by the organizer
 * Features:
 * - View all sent notifications
 * - Search messages by title/body
 * - Filter by event
 * - Send new custom messages
 * - View notification details (recipients, status)
 */
public class OrganizerMessagesActivity extends AppCompatActivity {
    private static final String TAG = "OrganizerMessages";

    // Firebase

    private FirebaseFirestore db;
    private NotificationHelper notificationHelper;

    // UI Components
    private MaterialButton btnBack, btnFilterByEvent, btnNewMessage;
    private EditText etSearch;
    private RecyclerView recyclerViewMessages;
    private View tvNoMessages; // Changed to View since it's now a LinearLayout

    // Bottom Navigation
    private Button btnNavDashboard, btnNavMessage, btnNavMap, btnNavProfile;

    // Data
    private NotificationMessageAdapter adapter;
    private List<NotificationMessage> allMessages = new ArrayList<>();
    private List<NotificationMessage> filteredMessages = new ArrayList<>();
    private String currentEventFilter = null; // null = show all events
    private boolean isTest = false;

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        if (!isTest) {
            db = FirebaseFirestore.getInstance();
            notificationHelper = new NotificationHelper();
            loadAllNotifications();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        btnFilterByEvent = findViewById(R.id.btnFilterByEvent);
        btnNewMessage = findViewById(R.id.btnNewMessage);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        tvNoMessages = findViewById(R.id.tvNoMessages);

        // Bottom Navigation
        btnNavDashboard = findViewById(R.id.btnNavDashboard);
        btnNavMessage = findViewById(R.id.btnNavMessage);
        btnNavMap = findViewById(R.id.btnNavMap);
        btnNavProfile = findViewById(R.id.btnNavProfile);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);

        adapter = new NotificationMessageAdapter();
        adapter.setOnMessageClickListener(message -> showMessageDetails(message));
        recyclerViewMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMessages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterByEvent.setOnClickListener(v -> showEventFilterDialog());

        btnNewMessage.setOnClickListener(v -> showNewMessageDialog());

        // Bottom Navigation
        btnNavDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMessagesActivity.this, OrganizerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNavMessage.setOnClickListener(v -> {
            // Already here, do nothing or refresh
            Toast.makeText(this, "Already on Messages", Toast.LENGTH_SHORT).show();
        });

        btnNavMap.setOnClickListener(v -> {
            Toast.makeText(this, "Select an event from dashboard to view map", Toast.LENGTH_SHORT).show();
        });

        btnNavProfile.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMessagesActivity.this, ProfileActivity.class);
            intent.putExtra("from_organizer", true);
            startActivity(intent);
        });
    }

    /**
     * Load all notifications sent by this organizer
     * Filters by organizer_id and excludes "recommendations" type
     */
    void loadAllNotifications() {
        // First, get all events created by this organizer
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "dummy-organizer-id";

        db.collection("events")
                .whereEqualTo("organizer_id", currentUserId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.isEmpty()) {
                        Log.d(TAG, "No events found for this organizer");
                        showEmptyState();
                        return;
                    }

                    // Collect all event IDs for this organizer
                    List<String> organizerEventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot eventDoc : eventSnapshot) {
                        organizerEventIds.add(eventDoc.getId());
                    }

                    Log.d(TAG, "Found " + organizerEventIds.size() + " events for organizer");

                    // Now load notifications for these events
                    db.collection("notification_logs")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(notifSnapshot -> {
                                allMessages.clear();

                                if (notifSnapshot.isEmpty()) {
                                    Log.d(TAG, "No notifications found");
                                    showEmptyState();
                                    return;
                                }

                                for (QueryDocumentSnapshot doc : notifSnapshot) {
                                    String eventId = doc.getString("eventId");
                                    String type = doc.getString("type");

                                    // Filter: only include notifications for this organizer's events
                                    // AND exclude "recommendations" type
                                    if (eventId != null && organizerEventIds.contains(eventId)
                                            && !"recommendations".equals(type)) {

                                        NotificationMessage message = new NotificationMessage();
                                        message.setId(doc.getId());
                                        message.setTitle(doc.getString("title"));
                                        message.setBody(doc.getString("body"));
                                        message.setType(type);
                                        message.setEventId(eventId);
                                        message.setEventName(doc.getString("eventName"));
                                        message.setRecipientId(doc.getString("recipientId"));
                                        message.setTimestamp(doc.getTimestamp("timestamp"));
                                        message.setRead(doc.getBoolean("read") != null ? doc.getBoolean("read") : false);

                                        allMessages.add(message);
                                    }
                                }

                                Log.d(TAG, "Loaded " + allMessages.size() + " notifications for organizer");
                                filterMessages(etSearch.getText().toString());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading notifications", e);
                                Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                                showEmptyState();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading organizer events", e);
                    Toast.makeText(this, "Error loading your events", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    /**
     * Filter messages by search query and event filter
     */
    private void filterMessages(String query) {
        filteredMessages.clear();
        String lowerQuery = query.toLowerCase().trim();

        for (NotificationMessage message : allMessages) {
            // Apply event filter first
            if (currentEventFilter != null && !currentEventFilter.isEmpty()) {
                if (message.getEventId() == null || !message.getEventId().equals(currentEventFilter)) {
                    continue;
                }
            }

            // Apply search filter
            if (lowerQuery.isEmpty()) {
                filteredMessages.add(message);
            } else {
                boolean matchesTitle = message.getTitle() != null &&
                        message.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesBody = message.getBody() != null &&
                        message.getBody().toLowerCase().contains(lowerQuery);
                boolean matchesEvent = message.getEventName() != null &&
                        message.getEventName().toLowerCase().contains(lowerQuery);

                if (matchesTitle || matchesBody || matchesEvent) {
                    filteredMessages.add(message);
                }
            }
        }

        updateUI();
    }

    private void updateUI() {
        if (filteredMessages.isEmpty()) {
            showEmptyState();
        } else {
            tvNoMessages.setVisibility(View.GONE);
            recyclerViewMessages.setVisibility(View.VISIBLE);
            adapter.submitList(new ArrayList<>(filteredMessages));
        }
    }

    private void showEmptyState() {
        tvNoMessages.setVisibility(View.VISIBLE);
        recyclerViewMessages.setVisibility(View.GONE);
        adapter.submitList(new ArrayList<>());
    }

    /**
     * Show dialog to filter by event
     */
    private void showEventFilterDialog() {
        // Get unique events from messages
        Map<String, String> eventsMap = new HashMap<>();
        for (NotificationMessage message : allMessages) {
            if (message.getEventId() != null && message.getEventName() != null) {
                eventsMap.put(message.getEventId(), message.getEventName());
            }
        }

        if (eventsMap.isEmpty()) {
            Toast.makeText(this, "No events with messages", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> eventNames = new ArrayList<>(eventsMap.values());
        List<String> eventIds = new ArrayList<>(eventsMap.keySet());

        // Add "All Events" option at the beginning
        eventNames.add(0, "All Events");
        eventIds.add(0, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Event");
        builder.setItems(eventNames.toArray(new String[0]), (dialog, which) -> {
            currentEventFilter = eventIds.get(which);

            String filterText = eventNames.get(which);
            btnFilterByEvent.setText(filterText.equals("All Events") ?
                    "Filter By Event" : filterText);

            filterMessages(etSearch.getText().toString());

            Toast.makeText(this, "Filtered to: " + filterText, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show dialog to send a new custom message
     * Fixed: Only shows events created by this organizer
     */
    private void showNewMessageDialog() {
        // Get current user ID
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading events for organizer: " + currentUserId);

        // Get only events created by this organizer
        db.collection("events")
                .whereEqualTo("organizer_id", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Query returned " + querySnapshot.size() + " documents");

                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "You haven't created any events yet", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "No events found with organizer_id = " + currentUserId);
                        return;
                    }

                    List<String> eventNames = new ArrayList<>();
                    List<String> eventIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // Try both "name" and "event_title" fields
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) {
                            name = doc.getString("event_title");
                        }

                        String organizerId = doc.getString("organizer_id");
                        Log.d(TAG, "Found event: " + name + " (ID: " + doc.getId() + ", organizer_id: " + organizerId + ")");

                        if (name != null && !name.isEmpty()) {
                            eventNames.add(name);
                            eventIds.add(doc.getId());
                        } else {
                            Log.w(TAG, "Skipping event with null/empty name: " + doc.getId());
                        }
                    }

                    if (eventNames.isEmpty()) {
                        Toast.makeText(this, "No valid events found", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "All events had null/empty names");
                        return;
                    }

                    Log.d(TAG, "Successfully loaded " + eventNames.size() + " events for dropdown");
                    showNewMessageDialogStep2(eventNames, eventIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showNewMessageDialogStep2(List<String> eventNames, List<String> eventIds) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_message, null);

        Spinner spinnerEvent = dialogView.findViewById(R.id.spinnerEvent);
        Spinner spinnerRecipients = dialogView.findViewById(R.id.spinnerRecipients);
        EditText etTitle = dialogView.findViewById(R.id.etMessageTitle);
        EditText etBody = dialogView.findViewById(R.id.etMessageBody);
        Button btnSend = dialogView.findViewById(R.id.btnSendMessage);

        // Setup event spinner
        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, eventNames);
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvent.setAdapter(eventAdapter);

        // Setup recipients spinner
        String[] recipientOptions = {"All Waiting", "All Selected", "All Enrolled", "All Canceled"};
        ArrayAdapter<String> recipientAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, recipientOptions);
        recipientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecipients.setAdapter(recipientAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Send New Message")
                .setNegativeButton("Cancel", null)
                .create();

        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String body = etBody.getText().toString().trim();
            int eventPosition = spinnerEvent.getSelectedItemPosition();
            String recipientType = spinnerRecipients.getSelectedItem().toString();

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Please enter title and message", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedEventId = eventIds.get(eventPosition);
            String selectedEventName = eventNames.get(eventPosition);

            sendCustomMessageToGroup(selectedEventId, selectedEventName, recipientType, title, body);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Send custom message to a group of entrants
     */
    private void sendCustomMessageToGroup(String eventId, String eventName,
                                          String recipientType, String title, String body) {
        // Determine status to query
        String status;
        switch (recipientType) {
            case "All Waiting":
                status = "waiting";
                break;
            case "All Selected":
                status = "selected";
                break;
            case "All Enrolled":
                status = "enrolled";
                break;
            case "All Canceled":
                status = "canceled";
                break;
            default:
                Toast.makeText(this, "Invalid recipient type", Toast.LENGTH_SHORT).show();
                return;
        }

        // Get all entrants with this status
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No " + status + " entrants found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> recipientIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("user_id");
                        if (userId != null) {
                            recipientIds.add(userId);
                        }
                    }

                    if (recipientIds.isEmpty()) {
                        Toast.makeText(this, "No valid recipients found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Send custom notification
                    notificationHelper.notifyCustom(eventId, recipientIds, eventName,
                            new NotificationHelper.NotificationCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(OrganizerMessagesActivity.this,
                                                "Message sent to " + recipientIds.size() + " users",
                                                Toast.LENGTH_LONG).show();
                                        loadAllNotifications(); // Refresh
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(OrganizerMessagesActivity.this,
                                                "Failed to send: " + error,
                                                Toast.LENGTH_LONG).show();
                                    });
                                }
                            }, title, body, "custom");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading entrants", e);
                    Toast.makeText(this, "Error loading recipients", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Show detailed information about a notification message
     */
    private void showMessageDetails(NotificationMessage message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_message_details, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvBody = dialogView.findViewById(R.id.tvDetailBody);
        TextView tvEvent = dialogView.findViewById(R.id.tvDetailEvent);
        TextView tvType = dialogView.findViewById(R.id.tvDetailType);
        TextView tvTimestamp = dialogView.findViewById(R.id.tvDetailTimestamp);
        TextView tvRecipient = dialogView.findViewById(R.id.tvDetailRecipient);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailStatus);

        tvTitle.setText(message.getTitle());
        tvBody.setText(message.getBody());
        tvEvent.setText(message.getEventName() != null ? message.getEventName() : "Unknown Event");
        tvType.setText(formatNotificationType(message.getType()));

        if (message.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
        } else {
            tvTimestamp.setText("Unknown time");
        }

        // Load recipient name
        if (message.getRecipientId() != null) {
            db.collection("accounts")
                    .document(message.getRecipientId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            tvRecipient.setText(name != null ? name : "Unknown User");
                        } else {
                            tvRecipient.setText("Unknown User");
                        }
                    })
                    .addOnFailureListener(e -> tvRecipient.setText("Unknown User"));
        } else {
            tvRecipient.setText("Unknown User");
        }

        tvStatus.setText(message.isRead() ? "Read ✓" : "Unread");
        tvStatus.setTextColor(getResources().getColor(
                message.isRead() ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark,
                null
        ));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private String formatNotificationType(String type) {
        if (type == null) return "General";
        switch (type) {
            case "chosen": return "Selected";
            case "not_chosen": return "Not Selected";
            case "canceled": return "Canceled";
            case "waiting_list_announcement": return "Waiting List";
            case "custom": return "Custom";
            default: return type;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isTest) {
            loadAllNotifications(); // Only refresh in real app
        }
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";

    private RecyclerView recyclerViewNotifications;
    private View tvNoNotifications;
    private NotificationAdapter adapter;
    private MaterialButton btnBack;
    private ImageButton notiBackBtn;  // Changed to ImageButton
    private MaterialButton btnToggle;
    private MaterialButton homeBtn, myEventsBtn, scanBtn, profileBtn, notificationBtn;

    FirebaseFirestore db;
    String currentUserId;
    private boolean notificationsEnabled = true; // Default to enabled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup RecyclerView
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        recyclerViewNotifications.setAdapter(adapter);

        // Load notification preference
        loadNotificationPreference();

        // Load notifications
        loadNotifications();
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        recyclerViewNotifications = findViewById(R.id.recycler_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        // Back buttons (handle both types)
        btnBack = findViewById(R.id.btnBack);
        notiBackBtn = findViewById(R.id.noti_btn_back);  // ImageButton

        // Toggle button
        btnToggle = findViewById(R.id.btn_toggle);

        // Navigation buttons
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);
    }

    /**
     * Setup click listeners for all buttons
     */
    private void setupClickListeners() {
        // Back button listeners
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (notiBackBtn != null) {
            notiBackBtn.setOnClickListener(v -> finish());
        }

        // Toggle notification preference
        if (btnToggle != null) {
            btnToggle.setOnClickListener(v -> toggleNotificationPreference());
        }

        // Navigation buttons
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, EventListActivity.class);
                startActivity(intent);
            });
        }

        if (scanBtn != null) {
            scanBtn.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, QRCodeScanner.class);
                startActivity(intent);
            });
        }

        if (myEventsBtn != null) {
            myEventsBtn.setOnClickListener(v -> {
                // TODO - Navigate to my events page
                Toast.makeText(this, "My Events - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                Intent profileIntent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            });
        }

        if (notificationBtn != null) {
            notificationBtn.setOnClickListener(v -> {
                // Already on notifications page
                Toast.makeText(this, "Already on Notifications", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Loads the current user's notification preference from Firestore (NEW DB structure).
     *
     * - Reads the "notificationsEnabled" field from the user's document in "accounts"
     * - Defaults to true if the field is missing
     * - Updates the UI toggle accordingly
     */
    private void loadNotificationPreference() {
        db.collection("accounts_N")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get notification preference, default to true if null
                        Boolean enabled = documentSnapshot.getBoolean("notificationsEnabled");
                        notificationsEnabled = enabled != null ? enabled : true;

                        // Update UI toggle (switch/button)
                        updateToggleButton();
                    } else {
                        Log.w(TAG, "Account document not found for user: " + currentUserId);
                        notificationsEnabled = true; // default if missing
                        updateToggleButton();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading notification preference for user: " + currentUserId, e);
                    notificationsEnabled = true; // fallback default
                    updateToggleButton();
                });
    }

    /**
     * Toggles the current user's notification preference and updates Firestore.
     *
     * - Flips the current notificationsEnabled boolean
     * - Updates the "notificationsEnabled" field in "accounts/{userId}"
     * - Updates the UI toggle and shows a toast message
     * - Reverts the toggle if Firestore update fails
     */
    private void toggleNotificationPreference() {
        // Flip the local boolean
        notificationsEnabled = !notificationsEnabled;

        // Update Firestore
        db.collection("accounts_N")
                .document(currentUserId)
                .update("notificationsEnabled", notificationsEnabled)
                .addOnSuccessListener(aVoid -> {
                    // Update UI
                    updateToggleButton();

                    // Show toast message
                    String message = notificationsEnabled ?
                            "Notifications enabled" : "Notifications disabled";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Revert local toggle on failure
                    notificationsEnabled = !notificationsEnabled;
                    Log.e(TAG, "❌ Error updating notification preference for user: " + currentUserId, e);
                    Toast.makeText(this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Update toggle button text based on current preference
     */
    private void updateToggleButton() {
        if (btnToggle != null) {
            String buttonText = notificationsEnabled ? "YES" : "NO";
            btnToggle.setText(buttonText);

            // Optional: Change button appearance based on state
            if (notificationsEnabled) {
                btnToggle.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.holo_green_dark, null));
            } else {
                btnToggle.setBackgroundTintList(
                        getResources().getColorStateList(android.R.color.holo_red_dark, null));
            }
        }
    }


    /**
     * Loads the current user's notifications from Firestore.
     *
     * - Reads from "accounts/{userId}/notifications"
     * - Orders notifications by timestamp (most recent first)
     * - Updates the RecyclerView with the list of notifications
     * - Shows a "No notifications" message if none exist
     */
    private void loadNotifications() {
        db.collection("accounts_N")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "❌ Error loading notifications for user: " + currentUserId, error);
                        Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Handle empty snapshot
                    if (snapshots == null || snapshots.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                        recyclerViewNotifications.setVisibility(View.GONE);
                        return;
                    }

                    tvNoNotifications.setVisibility(View.GONE);
                    recyclerViewNotifications.setVisibility(View.VISIBLE);

                    // Convert Firestore documents into NotificationItem objects
                    List<NotificationItem> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        NotificationItem item = new NotificationItem(
                                doc.getId(),                                   // Document ID
                                doc.getString("title"),                        // Notification title
                                doc.getString("body"),                         // Notification body
                                doc.getString("type"),                         // Notification type
                                doc.getString("eventId"),                      // Related event ID
                                doc.getString("eventName"),                    // Related event name
                                doc.getDate("timestamp"),                      // Timestamp
                                doc.getBoolean("read") != null && doc.getBoolean("read"),       // Read status
                                doc.getBoolean("declined") != null && doc.getBoolean("declined") // Declined status
                        );
                        notifications.add(item);
                    }

                    // Update RecyclerView adapter
                    adapter.setNotifications(notifications);
                    Log.d(TAG, "✅ Loaded " + notifications.size() + " notifications for user: " + currentUserId);
                });
    }

    /**
     * US 01.05.03 - Decline an invitation
     */
    private void declineInvitation(NotificationItem notification) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation for " +
                        notification.eventName + "?")
                .setPositiveButton("Yes, Decline", (dialog, which) -> {
                    performDecline(notification);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * US 01.05.04 - Accept an invitation
     */
    private void acceptInvitation(NotificationItem notification) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Invitation")
                .setMessage("Do you want to accept this invitation for " + notification.eventName + "?")
                .setPositiveButton("Yes, Accept", (dialog, which) -> performAccept(notification))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Declines an invitation for a given notification.
     *
     * Updates:
     * 1. Sets the user's registration status to "cancelled" in events/{eventId}/registrations/{userId}.
     * 2. Updates the notification in accounts/{userId}/notifications/{notificationId}.
     * 3. Adds a log entry under events/{eventId}/logs/{logId} for analytics.
     *
     * @param notification The NotificationItem representing the invitation to decline.
     */
    void performDecline(NotificationItem notification) {
        String eventId = notification.eventId;
        if (eventId == null) return;

        String userId = currentUserId;

        // Remove user from "chosen" subcollection
        db.collection("events_N").document(eventId)
                .collection("chosen").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Add user to "declined" subcollection
                    Map<String, Object> declinedData = new HashMap<>();
                    declinedData.put("declinedAt", FieldValue.serverTimestamp());
                    declinedData.put("reason", "User declined invitation");

                    db.collection("events_N").document(eventId)
                            .collection("declined").document(userId)
                            .set(declinedData)
                            .addOnSuccessListener(aVoid2 -> {
                                // Update user's notification document
                                db.collection("accounts_N").document(userId)
                                        .collection("notifications").document(notification.id)
                                        .update(
                                                "declined", true,
                                                "declinedAt", FieldValue.serverTimestamp()
                                        )
                                        .addOnSuccessListener(aVoid3 -> {
                                            adapter.notifyDataSetChanged();
                                            Toast.makeText(this, "Invitation declined!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update notification as declined", e));
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to add user to declined list", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove user from chosen list", e));
    }


    /**
     * Accepts an invitation for a given notification.
     *
     * Workflow:
     * 1. Removes the current user from the event's "chosen" subcollection.
     * 2. Adds the user to the event's "accepted" subcollection with a timestamp and status.
     * 3. Updates the notification document in the user's notifications collection to mark it as accepted.
     *
     * @param notification The NotificationItem representing the invitation to accept.
     */
    /**
     * Accepts an invitation for a given notification.
     *
     * Updates:
     * 1. Sets the user's registration status to "confirmed" in events/{eventId}/registrations/{userId}.
     * 2. Updates the notification in accounts/{userId}/notifications/{notificationId}.
     * 3. Adds a log entry under events/{eventId}/logs/{logId} for analytics.
     *
     * @param notification The NotificationItem representing the invitation to accept.
     */
    void performAccept(NotificationItem notification) {
        String eventId = notification.eventId;
        if (eventId == null) return;

        String userId = currentUserId;

        DocumentReference chosenRef = db.collection("events_N").document(eventId)
                .collection("chosen").document(userId);

        // Remove user from "chosen" subcollection
        chosenRef.delete().addOnSuccessListener(aVoid -> {
            // Add user to "accepted" subcollection
            Map<String, Object> acceptedData = new HashMap<>();
            acceptedData.put("acceptedAt", FieldValue.serverTimestamp());
            acceptedData.put("status", "accepted");

            db.collection("events_N").document(eventId)
                    .collection("accepted").document(userId)
                    .set(acceptedData)
                    .addOnSuccessListener(aVoid2 -> {
                        // Update user's notification document
                        db.collection("accounts_N").document(userId)
                                .collection("notifications").document(notification.id)
                                .update(
                                        "accepted", true,
                                        "acceptedAt", FieldValue.serverTimestamp()
                                )
                                .addOnSuccessListener(aVoid3 -> {
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Invitation accepted!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update notification as accepted", e);
                                });
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add user to accepted list", e));
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to remove user from chosen list", e));
    }


    /**
     * Get current user ID
     */
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }



    /**
     * Data class for notification items
     */
    static class NotificationItem {
        String id;
        String title;
        String body;
        String type;
        String eventId;
        String eventName;
        Date timestamp;
        boolean read;
        boolean declined;



        NotificationItem(String id, String title, String body, String type,
                         String eventId, String eventName, Date timestamp, boolean read, boolean declined) {
            this.id = id;
            this.title = title;
            this.body = body;
            this.type = type;
            this.eventId = eventId;
            this.eventName = eventName;
            this.timestamp = timestamp;
            this.read = read;
            this.declined = declined;
        }
    }



    /**
     * RecyclerView Adapter for displaying notifications
     */
    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<NotificationItem> notifications = new ArrayList<>();

        void setNotifications(List<NotificationItem> notifications) {
            this.notifications = notifications;
            notifyDataSetChanged();
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
            NotificationItem item = notifications.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        /**
         * ViewHolder for notification items
         */
        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBody, tvTime, tvEventName;
            Button btnDecline, btnAccept; // added the accept button

            View indicator;
            ImageView icClose; // 🔹 Added this


            NotificationViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_notification_title);
                tvBody = itemView.findViewById(R.id.tv_notification_body);
                tvTime = itemView.findViewById(R.id.tv_notification_time);
                tvEventName = itemView.findViewById(R.id.tv_event_name);
                btnDecline = itemView.findViewById(R.id.btn_decline);
                btnAccept = itemView.findViewById(R.id.btn_accept); // added the accept button
                indicator = itemView.findViewById(R.id.notification_indicator);
                icClose = itemView.findViewById(R.id.ic_close); // 🔹 initialize close icon

                // 🔹 Handle close button click (local only)
                icClose.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        notifications.remove(position);
                        notifyItemRemoved(position);
                    }
                });
            }

            void bind(NotificationItem item) {
                tvTitle.setText(item.title);
                tvBody.setText(item.body);

                // US 01.05.03 - Show decline button only for "chosen" notifications not yet declined
                // MOdified the follwoing to add the accept invitation one.
                if ("chosen".equals(item.type) && !item.declined) {
                    btnDecline.setVisibility(View.VISIBLE);
                    btnAccept.setVisibility(View.VISIBLE);

                    btnDecline.setOnClickListener(v -> declineInvitation(item));
                    btnAccept.setOnClickListener(v -> acceptInvitation(item));

                } else if (item.declined) {
                    btnDecline.setVisibility(View.GONE);
                    btnAccept.setVisibility(View.GONE);
                    tvBody.setText("You declined this invitation.");
                } else {
                    btnDecline.setVisibility(View.GONE);
                    btnAccept.setVisibility(View.GONE);
                }


                // Format timestamp
                if (item.timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
                    tvTime.setText(sdf.format(item.timestamp));
                } else {
                    tvTime.setText("Just now");
                }

                // Show unread indicator
                indicator.setVisibility(item.read ? View.INVISIBLE : View.VISIBLE);

                // US 01.05.03 - Show decline button only for "chosen" notifications not yet declined
                if ("chosen".equals(item.type) && !item.declined) {
                    btnDecline.setVisibility(View.VISIBLE);
                    btnDecline.setOnClickListener(v -> declineInvitation(item));
                } else if (item.declined) {
                    btnDecline.setVisibility(View.GONE);
                    tvBody.setText("You declined this invitation."); // optional feedback
                } else {
                    btnDecline.setVisibility(View.GONE);
                }

                // Set background color based on type
                int backgroundColor;
                if ("chosen".equals(item.type)) {
                    backgroundColor = 0xFFE8F5E9; // Light green
                } else if ("not_chosen".equals(item.type)) {
                    backgroundColor = 0xFFFFF3E0; // Light orange
                } else {
                    backgroundColor = 0xFFE3F2FD; // Light blue
                }
                itemView.setBackgroundColor(backgroundColor);
            }
        }

    }

    public Map<String, Object> buildDeclinedDataForTest() {
        Map<String, Object> declinedData = new HashMap<>();
        declinedData.put("declinedAt", "FAKE_TIMESTAMP"); // placeholder for testing
        declinedData.put("reason", "User declined invitation");
        return declinedData;
    }
}
package com.example.connect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
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

/**
 * Activity for entrants to view their notifications and invitations.
 * US 01.05.03 - Allows entrants to decline invitations when they are chosen.
 */
public class EntrantActivity extends AppCompatActivity {

    private static final String TAG = "EntrantActivity";

    private RecyclerView recyclerViewNotifications;
    private View tvNoNotifications;
    private NotificationAdapter adapter;
    FirebaseFirestore db;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant);

        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        recyclerViewNotifications = findViewById(R.id.recycler_notifications);
        tvNoNotifications = findViewById(R.id.tv_no_notifications);

        // Setup RecyclerView
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        recyclerViewNotifications.setAdapter(adapter);

        // Load notifications
        loadNotifications();
    }

    /**
     * Load user's notifications from Firestore
     */
    private void loadNotifications() {
        db.collection("accounts")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading notifications", error);
                        Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        tvNoNotifications.setVisibility(View.VISIBLE);
                        recyclerViewNotifications.setVisibility(View.GONE);
                        return;
                    }

                    tvNoNotifications.setVisibility(View.GONE);
                    recyclerViewNotifications.setVisibility(View.VISIBLE);

                    List<NotificationItem> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        NotificationItem item = new NotificationItem(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("body"),
                                doc.getString("type"),
                                doc.getString("eventId"),
                                doc.getString("eventName"),
                                doc.getDate("timestamp"),
                                doc.getBoolean("read") != null && doc.getBoolean("read"),
                                doc.getBoolean("declined") != null && doc.getBoolean("declined")
                        );
                        notifications.add(item);
                    }

                    adapter.setNotifications(notifications);
                    Log.d(TAG, "Loaded " + notifications.size() + " notifications");
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
     * Perform the decline operation in Firestore
     */

    void performDecline(NotificationItem notification) {
        String eventId = notification.eventId;

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Declining invitation for eventId=" + eventId + " user=" + currentUserId);

        // Remove user from chosen list
        db.collection("events").document(eventId)
                .collection("chosen").document(currentUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Removed from chosen list");

                    // Add to declined list
                    Map<String, Object> declinedData = new HashMap<>();
                    declinedData.put("declinedAt", FieldValue.serverTimestamp());
                    declinedData.put("reason", "User declined invitation");

                    db.collection("events").document(eventId)
                            .collection("declined").document(currentUserId)
                            .set(declinedData)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "✅ Added to declined list");

                                // Update notification
                                db.collection("accounts").document(currentUserId)
                                        .collection("notifications").document(notification.id)
                                        .update("declined", true, "declinedAt", FieldValue.serverTimestamp())
                                        .addOnSuccessListener(aVoid3 -> {
                                            Log.d(TAG, "✅ Notification marked declined");
                                            notification.declined = true;
                                            adapter.notifyDataSetChanged(); // instantly refresh UI
                                            Toast.makeText(this, "Invitation declined successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to update notification", e));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Failed to add to declined list", e);
                                Toast.makeText(this,
                                        "Error declining invitation (declined list)",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to remove from chosen list", e);
                    Toast.makeText(this, "Error declining invitation (chosen list)", Toast.LENGTH_SHORT).show();
                });
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

    // ============== Data Class ==============

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

    // ============== RecyclerView Adapter ==============

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
            Button btnDecline;
            View indicator;

            NotificationViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_notification_title);
                tvBody = itemView.findViewById(R.id.tv_notification_body);
                tvTime = itemView.findViewById(R.id.tv_notification_time);
                tvEventName = itemView.findViewById(R.id.tv_event_name);
                btnDecline = itemView.findViewById(R.id.btn_decline);
                indicator = itemView.findViewById(R.id.notification_indicator);
            }

            void bind(NotificationItem item) {
                tvTitle.setText(item.title);
                tvBody.setText(item.body);

                if (item.eventName != null) {
                    tvEventName.setVisibility(View.VISIBLE);
                    tvEventName.setText("Event: " + item.eventName);
                } else {
                    tvEventName.setVisibility(View.GONE);
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
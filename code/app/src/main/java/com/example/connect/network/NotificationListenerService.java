package com.example.connect.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.connect.R;
import com.example.connect.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

/**
 * Background service that listens for new notifications in Firestore.
 * When a new notification arrives, it displays it to the user.
 *
 * This runs in the background and automatically shows notifications
 * when the organizer sends them via NotificationHelper.
 */
public class NotificationListenerService extends Service {

    private static final String TAG = "NotificationListener";
    private static final String CHANNEL_ID = "default";
    private static final String SERVICE_CHANNEL_ID = "notification_service";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private ListenerRegistration notificationListener;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();

        // Create notification channels
        createNotificationChannel();
        createServiceChannel();

        // Start as foreground service
        startForegroundService();

        // Start listening for notifications
        startListeningForNotifications();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Restart service if it gets killed
        return START_STICKY;
    }

    /**
     * Start the service in foreground mode with a persistent notification
     */
    private void startForegroundService() {
        // Create an intent to open the app when notification is tapped
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build the foreground notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
                .setContentTitle("Event Notifications Active")
                .setContentText("Listening for event updates...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true); // Cannot be dismissed

        // Start foreground
        startForeground(FOREGROUND_NOTIFICATION_ID, builder.build());

        Log.d(TAG, "Service started in foreground");
    }

    /**
     * Start listening for new notifications in Firestore
     */
    private void startListeningForNotifications() {
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.w(TAG, "No user logged in, cannot listen for notifications");
            return;
        }

        // Listen for new notifications added to user's notifications collection
        notificationListener = db.collection("accounts")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    // Process new notifications
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String title = dc.getDocument().getString("title");
                            String body = dc.getDocument().getString("body");
                            String type = dc.getDocument().getString("type");
                            Boolean read = dc.getDocument().getBoolean("read");

                            // Only show if it's a new notification (not already read)
                            if (title != null && body != null && (read == null || !read)) {
                                showNotification(title, body, type);

                                // Mark as read (optional)
                                dc.getDocument().getReference()
                                        .update("read", true)
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Failed to mark as read", e));
                            }
                        }
                    }
                });

        Log.d(TAG, "Started listening for notifications for user: " + userId);
    }

    /**
     * Display notification to user
     */
    private void showNotification(String title, String body, String type) {
        // Create an intent to open the app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Different colors/icons based on type
        if ("chosen".equals(type)) {
            builder.setColor(0xFF4CAF50); // Green
        } else if ("not_chosen".equals(type)) {
            builder.setColor(0xFFFF9800); // Orange
        } else {
            builder.setColor(0xFF2196F3); // Blue
        }

        // Show notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Notification displayed: " + title);
    }

    /**
     * Create notification channel for event notifications (required for Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Event Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications about event lottery results");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create notification channel for the foreground service
     */
    private void createServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    SERVICE_CHANNEL_ID,
                    "Notification Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps the app listening for event notifications");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Get current logged in user ID
     */
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Clean up listener
        if (notificationListener != null) {
            notificationListener.remove();
            Log.d(TAG, "Stopped listening for notifications");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
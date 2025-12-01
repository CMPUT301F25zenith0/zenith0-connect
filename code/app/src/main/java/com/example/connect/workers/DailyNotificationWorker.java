package com.example.connect.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.connect.utils.NotificationHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Background worker that sends daily notifications to users about recommended events
 * based on their interest preferences.
 *
 * Updated to prevent spam by checking if user already received recommendations
 * in the last 24 hours.
 *
 * @author Zenith Team
 * @version 1.1
 */
public class DailyNotificationWorker extends Worker {

    private static final String TAG = "DailyNotificationWorker";
    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public DailyNotificationWorker(@NonNull Context context,
                                   @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "▶ DailyNotificationWorker starting...");

        try {
            db.collection("accounts").get()
                    .addOnSuccessListener(query -> {
                        Log.d(TAG, "Found " + query.size() + " total users");
                        for (QueryDocumentSnapshot doc : query) {
                            processUser(doc.getId(), doc.getData());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load users", e);
                    });

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in doWork", e);
            return Result.retry();
        }
    }

    private void processUser(String userId, Map<String, Object> userData) {
        // Check if notifications are enabled (default to true if not set)
        boolean notificationsEnabled = userData.get("notificationsEnabled") == null ||
                (boolean) userData.get("notificationsEnabled");

        if (!notificationsEnabled) {
            Log.d(TAG, "Skipping " + userId + " — notifications disabled");
            return;
        }

        List<String> interests = (List<String>) userData.get("interests");
        if (interests == null || interests.isEmpty()) {
            Log.d(TAG, "User " + userId + " has no interests set");
            return;
        }

        Log.d(TAG, "Processing user " + userId + " with interests: " + interests);

        // Check if user already received recommendations in last 24 hours
        checkLastRecommendation(userId, interests);
    }

    /**
     * Check if user has received recommendation notifications in the last 24 hours
     * to prevent spam. Only sends recommendations if user is eligible.
     */
    private void checkLastRecommendation(String userId, List<String> interests) {
        // Calculate timestamp for 24 hours ago
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        com.google.firebase.Timestamp cutoffTimestamp =
                new com.google.firebase.Timestamp(oneDayAgo / 1000, 0);

        db.collection("notification_logs")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("type", "recommendations")
                .whereGreaterThan("timestamp", cutoffTimestamp)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Log.d(TAG, "User " + userId + " already received recommendations in last 24h - skipping");
                        return;
                    }

                    Log.d(TAG, "User " + userId + " eligible for recommendations (no notifications in last 24h)");
                    fetchRecommendedEvents(userId, interests);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking last recommendation for " + userId, e);
                    // On error, proceed anyway to avoid blocking legitimate notifications
                    fetchRecommendedEvents(userId, interests);
                });
    }

    private void fetchRecommendedEvents(String userId, List<String> interests) {
        db.collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> recommendedEventIds = new ArrayList<>();
                    List<String> recommendedTitles = new ArrayList<>();

                    long currentTime = System.currentTimeMillis();

                    for (var doc : query) {
                        // Parse the date_time string
                        String dateTimeStr = doc.getString("date_time");
                        if (dateTimeStr == null) continue;

                        try {
                            // Convert ISO string to timestamp
                            long eventTime = parseIsoToTimestamp(dateTimeStr);

                            // Skip past events
                            if (eventTime < currentTime) continue;

                        } catch (Exception e) {
                            Log.w(TAG, "Invalid date format: " + dateTimeStr);
                            continue;
                        }

                        List<String> labels = (List<String>) doc.get("labels");
                        if (labels == null) continue;

                        for (String tag : interests) {
                            if (labels.contains(tag)) {
                                recommendedEventIds.add(doc.getId());
                                String title = doc.getString("event_title");
                                recommendedTitles.add(title != null ? title : "Untitled Event");
                                break;
                            }
                        }
                    }

                    if (recommendedEventIds.isEmpty()) {
                        Log.d(TAG, "No recommended events for user: " + userId);
                        return;
                    }

                    Log.d(TAG, "Found " + recommendedEventIds.size() + " recommended events for user: " + userId);
                    sendDailyRecommendation(userId, recommendedEventIds, recommendedTitles);
                });
    }

    // Helper method to parse ISO 8601 string
    private long parseIsoToTimestamp(String isoDate) throws Exception {
        // Remove 'T' and parse
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date date = sdf.parse(isoDate);
        return date != null ? date.getTime() : 0;
    }

    private void sendDailyRecommendation(String userId, List<String> eventIds, List<String> eventTitles) {
        String title = "Events You Might Like!";

        // Limit to 5 events in notification
        int maxEvents = Math.min(5, eventTitles.size());
        StringBuilder body = new StringBuilder("We found ");
        body.append(eventTitles.size());
        body.append(eventTitles.size() == 1 ? " event " : " events ");
        body.append("matching your interests:\n");

        for (int i = 0; i < maxEvents; i++) {
            body.append("• ").append(eventTitles.get(i)).append("\n");
        }

        if (eventTitles.size() > maxEvents) {
            body.append("...and ").append(eventTitles.size() - maxEvents).append(" more!");
        }

        List<String> singleUser = new ArrayList<>();
        singleUser.add(userId);

        // Use first event ID for the notification reference
        String firstEventId = eventIds.get(0);

        notificationHelper.notifyCustom(
                firstEventId,
                singleUser,
                "Daily Recommendations",
                new NotificationHelper.NotificationCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "✅ Daily recommendation sent to user " + userId);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "❌ Failed to send recommendation to user " + userId + ": " + error);
                    }
                },
                title,
                body.toString(),
                "recommendations"
        );
    }
}
package com.example.connect.utils;

import android.util.Log;

import com.example.connect.models.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Scheduler that automatically checks for events needing lottery draws
 * Finds events where registration deadline has passed and performs lottery
 *
 * @author Zenith Team
 * @version 1.0
 */
public class LotteryScheduler {

    private static final String TAG = "LotteryScheduler";
    private final FirebaseFirestore db;
    private final LotteryManager lotteryManager;

    public LotteryScheduler() {
        this.db = FirebaseFirestore.getInstance();
        this.lotteryManager = new LotteryManager();
    }

    /**
     * Check all events and perform lottery for those whose registration has closed
     * This method is called periodically by the background worker
     */
    public void checkAndPerformLotteries() {
        Log.d(TAG, "Checking for events needing lottery draw...");

        // Query all events that haven't had lottery drawn yet
        db.collection("events")
                .whereEqualTo("draw_completed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Found " + querySnapshot.size() + " events without lottery");

                    int eligibleCount = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());

                        // Check if registration has closed
                        if (isRegistrationClosed(event)) {
                            eligibleCount++;
                            performLotteryForEvent(event);
                        }
                    }

                    Log.d(TAG, eligibleCount + " events are eligible for lottery draw");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for lottery events", e);
                });
    }

    /**
     * Check if event registration deadline has passed
     *
     * @param event The event to check
     * @return true if registration has closed
     */
    private boolean isRegistrationClosed(Event event) {
        String regStop = event.getRegStop();

        if (regStop == null || regStop.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date stopDate = sdf.parse(regStop);

            if (stopDate == null) {
                return false;
            }

            // Check if current time is after registration stop
            Date now = new Date();
            boolean closed = now.after(stopDate);

            if (closed) {
                Log.d(TAG, "Registration closed for event: " + event.getName());
            }

            return closed;

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing registration stop date for event: " + event.getName(), e);
            return false;
        }
    }

    /**
     * Perform lottery for a specific event
     */
    private void performLotteryForEvent(Event event) {
        Log.d(TAG, "Performing automatic lottery for event: " + event.getName() + " (ID: " + event.getEventId() + ")");

        lotteryManager.performAutomaticLottery(event.getEventId(), new LotteryManager.LotteryCallback() {
            @Override
            public void onSuccess(int selectedCount, int waitingListCount) {
                Log.d(TAG, "✓ Lottery completed for " + event.getName() +
                        ": Selected " + selectedCount + " out of " + waitingListCount + " entrants");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "✗ Lottery failed for " + event.getName() + ": " + error);
            }
        });
    }
}
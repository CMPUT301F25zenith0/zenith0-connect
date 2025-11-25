package com.example.connect;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.connect.utils.UserActivityTracker;
import com.example.connect.workers.LotteryWorker;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

/**
 * Custom Application class to track app lifecycle and manage user activity status.
 * Also initializes automatic lottery draw system.
 *
 * @author Zenith Team
 * @version 2.0
 */
public class ConnectApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "ConnectApplication";
    private static final String LOTTERY_WORK_NAME = "automatic_lottery_check";

    private int activityCount = 0; // Track number of activities in foreground

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "ConnectApplication starting...");

        // Register activity lifecycle callbacks for user tracking
        registerActivityLifecycleCallbacks(this);

        // Schedule automatic lottery checks
        scheduleAutomaticLotteryChecks();
    }

    /**
     * Schedule periodic lottery checks using WorkManager
     * Runs every 15 minutes to check for events needing lottery draws
     */
    private void scheduleAutomaticLotteryChecks() {
        try {
            // Create constraints - require network connection
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Create periodic work request - runs every 15 minutes
            PeriodicWorkRequest lotteryWorkRequest = new PeriodicWorkRequest.Builder(
                    LotteryWorker.class,
                    15, // Repeat interval
                    TimeUnit.MINUTES
            )
                    .setConstraints(constraints)
                    .addTag("lottery_automation")
                    .build();

            // Enqueue the work
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    LOTTERY_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule if already running
                    lotteryWorkRequest
            );

            Log.d(TAG, "✓ Automatic lottery check scheduled (every 15 minutes)");

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling lottery checks", e);
        }
    }

    // Activity Lifecycle Callbacks for User Activity Tracking

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Activity created
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activityCount == 0) {
            // App came to foreground - mark user as active
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                UserActivityTracker.markUserActive();
                Log.d(TAG, "App came to foreground - user marked as active");
            }
        }
        activityCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Activity resumed - user is active (handled by individual activities)
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Activity paused - but user might still be in app, don't mark inactive
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            // All activities stopped - app went to background
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                UserActivityTracker.markUserInactive();
                Log.d(TAG, "App went to background - user marked as inactive");
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Activity saving state
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Activity destroyed
    }
}
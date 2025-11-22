package com.example.connect;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.example.connect.utils.UserActivityTracker;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Custom Application class to track app lifecycle and manage user activity status.
 * Marks users as inactive only when the app goes to background or is closed.
 */
public class ConnectApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ConnectApplication";
    private int activityCount = 0; // Track number of activities in foreground

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

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


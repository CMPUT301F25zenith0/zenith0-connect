package com.example.connect.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main launcher activity for the app.
 * Checks if user should be auto-logged in based on Remember Me preference.
 * If not, displays login and account creation options.
 * <p>
 * Aakansh - Navigation to login and creation
 * Vansh - Auto-logging functionality
 * Alpesh - Notification methods (listener, channels, etc)
 * @author Aakansh Chatterjee, Aalpesh Dayal, Vansh Taneja
 * @version 3.0
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // UI bittons
    private Button btnLogin, btnAcctCreate;

    // Firebase
    private FirebaseAuth mAuth;

    // Auto logging requirement
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check for remember user to auto-login
        checkAutoLogin();


        // start the notification listener
        startNotificationListener();

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Create notification channels
        createNotificationChannels();
    }

    /**
     * Checks if user should be auto-logged in.
     * If Remember Me is enabled from past  and user is already authenticated with Firebase, skip MainActivity and go directly to EventListActivity.
     * Otherwise, show the MainActivity screen normally.
     */
    private void checkAutoLogin() {
        // Get Remember Me preference
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        // Get current Firebase user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d("MainActivity", "Remember Me: " + rememberMe + ", Current User: " +
                (currentUser != null ? currentUser.getUid() : "null"));

        // If Remember Me is enabled and user is logged in, auto-login
        if (rememberMe && currentUser != null) {
            Log.d("MainActivity", "Auto-login enabled, navigating to EventListActivity");

            // Go directly to EventListActivity
            Intent intent = new Intent(MainActivity.this, EventListActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so user can't go back to it
        } else {
            // Show MainActivity screen normally
            setContentView(R.layout.open_screen);

            // Set up your normal MainActivity UI and button listeners
            setupMainActivityUI();
        }
    }

    /**
     * Set up the MainActivity UI elements and listeners.
     * Initializes buttons for login, account creation, and QR testing.
     */
    private void setupMainActivityUI() {
        btnLogin = findViewById(R.id.btn_login);
        btnAcctCreate = findViewById(R.id.create_acct_btn);

        // Navigate to login activity when clicked
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigate to create profile activity when clicked
        btnAcctCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAcctActivity.class);
            startActivity(intent);
        });

    }


    /**
     * Start the notification listener service
     */
    private void startNotificationListener() {
        try {
            Intent serviceIntent = new Intent(this, NotificationListenerService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d(TAG, "Notification listener service started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start notification listener service", e);
        }
    }

    /**
     * Request notification permission for Android 13+
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    /**
     * Create notification channels for Android 8+
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // Channel for event notifications
                NotificationChannel eventChannel = new NotificationChannel(
                        "default",
                        "Event Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                eventChannel.setDescription("Notifications about event lottery results");
                manager.createNotificationChannel(eventChannel);

                // Channel for the foreground service
                NotificationChannel serviceChannel = new NotificationChannel(
                        "notification_service",
                        "Notification Service",
                        NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setDescription("Keeps the app listening for event notifications");
                serviceChannel.setShowBadge(false);
                manager.createNotificationChannel(serviceChannel);

                Log.d(TAG, "Notification channels created");
            }
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * Handles the response from notification permission request on Android 13+ devices.
     * Logs result of the permission request for debugging purposes.
     * <p>
     * @param requestCode The request code passed in {@link ActivityCompat#requestPermissions(android.app.Activity, String[], int)}.
     *                    Expected to be {@link #NOTIFICATION_PERMISSION_CODE} for notification permissions.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * <p>
     * Claude was used to help write java doc comments
     * Prompt: {code} write java doc for this code
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.w(TAG, "Notification permission denied - notifications may not work");
            }
        }
    }

    /**
     * Called when the activity is being destroyed.
     * Performs cleanup operations before the activity is finished.
     * <p>
     * <p>Note: The notification listener service is intentionally NOT stopped in this method
     * to ensure continuous reception of event notifications even after the activity is closed.
     * This allows the app to receive and process notifications in the background.</p>
     * <p>
     * Claude was used to help write java doc comments
     * Prompt: {code} Expandjava doc for this code
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Note: We don't stop the service here because we want it to keep running
        // to receive notifications even when the activity is closed
    }
}
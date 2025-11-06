package com.example.connect.activities;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.connect.R;
import com.example.connect.network.NotificationListenerService;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Main launcher activity for the app.
 * Currently contains a Login button that navigates to ProfileActivity.
 */
public class MainActivity extends AppCompatActivity {

    // tag for debugging
    private static final String TAG = "MainActivity";

    // notification_permission_code = 123
    static final int NOTIFICATION_PERMISSION_CODE = 123;

    // this is the initial login button
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btn_login);
        Button btnEntrant = findViewById(R.id.btn_entrant);

        // Navigate to OrganizerActivity when clicked
        btnLogin.setOnClickListener(v -> {
            // Temporary test login
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                // sign in with my account
                auth.signInWithEmailAndPassword("aalpesh@ualberta.ca", "zenithPassword123")
                        .addOnSuccessListener(result -> {
                            Log.d(TAG, "Signed in successfully!");
                            startNotificationListener();
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Sign-in failed", e));
            } else {
                // either way we start the notification listener
                Log.d(TAG, "Already signed in!");
                startNotificationListener();
            }

            // change first activity to organizer activity when you click login
            Intent intent = new Intent(MainActivity.this, OrganizerActivity.class);
            startActivity(intent);
        });

        // Navigate to EntrantActivity
        btnEntrant.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EntrantActivity.class);
            startActivity(intent);
        });

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Create notification channels
        createNotificationChannels();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Note: We don't stop the service here because we want it to keep running
        // to receive notifications even when the activity is closed
    }
}

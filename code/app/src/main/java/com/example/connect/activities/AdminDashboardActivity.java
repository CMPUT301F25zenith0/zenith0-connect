package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.utils.UserActivityTracker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for the Admin Dashboard.
 * Provides access to administrative features like managing events, profiles, images, and organizers.
 * @author Vansh Taneja
 * @version 1.0
 */
public class AdminDashboardActivity extends AppCompatActivity {

    // UI Elements
    private MaterialCardView cardManageEvents;
    private MaterialCardView cardManageProfiles;
    private MaterialCardView cardManageImages;
    private MaterialCardView cardManageOrganizers;
    private MaterialCardView cardNotificationLogs;
    private MaterialButton btnLogout;

    private TextView tvStatUsers;
    private TextView tvStatEvents;
    private TextView tvStatSystem;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    // Active users tracking
    private int activeUsersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();

        loadDashboardStats();
    }

    /**
     * Initializes all UI components by finding their references from the layout.
     * This method links the Java variables to their corresponding XML elements.
     */
    private void initViews() {
        cardManageEvents = findViewById(R.id.card_manage_events);
        cardManageProfiles = findViewById(R.id.card_manage_profiles);
        cardManageImages = findViewById(R.id.card_manage_images);
        cardManageOrganizers = findViewById(R.id.card_manage_organizers);
        cardNotificationLogs = findViewById(R.id.card_notification_logs);
        btnLogout = findViewById(R.id.btn_logout);

        tvStatUsers = findViewById(R.id.tv_stat_users);
        tvStatEvents = findViewById(R.id.tv_stat_events);
        tvStatSystem = findViewById(R.id.tv_stat_system);
    }

    // This Method updates data on the starting dashboard screen | E.g, Total User and Total Active Users
    private void loadDashboardStats() {
        // TODO - Implement System stat update

        // Query Firestore to get the total count of users in the accounts collection
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Get the total number of documents (users)
                    int totalUsers = queryDocumentSnapshots.size();

                    // Account for admin accounts --> should not be included
                    int nonAdminUsers = totalUsers - 6;

                    // Format the number with commas for better readability
                    String formattedTotalCount = formatNumber(nonAdminUsers);

                    // Update the total users TextView
                    tvStatUsers.setText(formattedTotalCount);

                    Log.d("AdminDashboard", "Total users: " + nonAdminUsers);

                    // Now get active users using the getActiveUsers method
                    getActiveUsers(new ActiveUsersCallback() {
                        @Override
                        public void onSuccess(List<String> activeUserIds) {
                            // Update active users count
                            activeUsersCount = activeUserIds.size();

                            // Format the number with commas for better readability
                            String formattedActiveCount = formatNumber(activeUsersCount);

                            // Update the active users TextView (tv_stat_events)
                            tvStatEvents.setText(formattedActiveCount);

                            Log.d("AdminDashboard", "Active users: " + activeUsersCount);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("AdminDashboard", "Failed to get active users: " + error);
                            // Set default value or show error
                            tvStatEvents.setText("0");
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // If query fails, log error and keep default value
                    Log.e("AdminDashboard", "Failed to get user count: " + e.getMessage(), e);
                    // Optionally, you could set an error message or keep the default value
                });
    }

    private void setupClickListeners() {
        cardManageEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Accessing Event Protocol...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminEventListActivity
        });

        cardManageProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminProfileListActivity.class);
            startActivity(intent);
        });

        cardManageImages.setOnClickListener(v -> {
            Toast.makeText(this, "Accessing Media Archives...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminImageListActivity
        });

        cardManageOrganizers.setOnClickListener(v -> {
            Toast.makeText(this, "Accessing Organizer Controls...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminOrganizerListActivity
        });

        cardNotificationLogs.setOnClickListener(v -> { // Modified
            Toast.makeText(this, "Accessing Comm Logs...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminNotificationLogActivity
        });

        btnLogout.setOnClickListener(v -> {
            // Mark user as inactive before logging out
            UserActivityTracker.markUserInactive();
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Called when the activity is resumed.
     * Marks the admin user as active and refreshes the dashboard data.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Mark admin user as active
        UserActivityTracker.markUserActive();
        // Refresh dashboard data
        loadDashboardStats();
    }
    
    /**
     * Called when the activity is paused (user navigates away).
     * Note: We don't mark users inactive here because they might be navigating to another activity.
     * The Application class handles marking users inactive when the app goes to background.
     */
    @Override
    protected void onPause() {
        super.onPause();

    }



    
    /**
     * Gets the list of currently active users from Firestore (excluding admins).
     * @param callback Callback to receive the list of active user IDs
     */
    public void getActiveUsers(ActiveUsersCallback callback) {
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> activeUserIds = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip admin accounts
                        Boolean isAdmin = document.getBoolean("admin");
                        if (isAdmin != null && isAdmin) {
                            continue;
                        }
                        
                        Boolean isActive = document.getBoolean("is_active");
                        Long lastActive = document.getLong("last_active_timestamp");
                        
                        // Check if user is currently active
                        if (UserActivityTracker.isUserCurrentlyActive(isActive, lastActive)) {
                            activeUserIds.add(document.getId());
                        }
                    }
                    
                    callback.onSuccess(activeUserIds);
                    Log.d("AdminDashboard", "Found " + activeUserIds.size() + " active users");
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminDashboard", "Failed to get active users: " + e.getMessage(), e);
                    callback.onFailure(e.getMessage());
                });
    }
    
    /**
     * Callback interface for active users query results.
     */
    public interface ActiveUsersCallback {
        void onSuccess(List<String> activeUserIds);
        void onFailure(String error);
    }

    /**
     * Formats a number with commas for better readability (e.g., 1204 -> "1,204")
     * @param number The number to format
     * @return Formatted string with commas
     */
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

}
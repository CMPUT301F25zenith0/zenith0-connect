package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

/**
 * Activity for the Admin Dashboard.
 * Provides access to administrative features like managing events, profiles,
 * images, and organizers.
 * @author Vansh Taneja
 * @version 1.0
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardManageEvents;
    private MaterialCardView cardManageProfiles;
    private MaterialCardView cardManageImages;
    private MaterialCardView cardManageOrganizers;
    private MaterialCardView cardNotificationLogs;
    private MaterialButton btnLogout;

    private TextView tvStatUsers; // Added
    private TextView tvStatEvents; // Added
    private TextView tvStatSystem; // Added

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadDashboardStats(); // Added

        updateData();
    }

    private void initViews() {
        cardManageEvents = findViewById(R.id.card_manage_events);
        cardManageProfiles = findViewById(R.id.card_manage_profiles);
        cardManageImages = findViewById(R.id.card_manage_images);
        cardManageOrganizers = findViewById(R.id.card_manage_organizers);
        cardNotificationLogs = findViewById(R.id.card_notification_logs);
        btnLogout = findViewById(R.id.btn_logout);

        tvStatUsers = findViewById(R.id.tv_stat_users); // Added
        tvStatEvents = findViewById(R.id.tv_stat_events); // Added
        tvStatSystem = findViewById(R.id.tv_stat_system); // Added
    }

    // Added method
    private void loadDashboardStats() {
        // TODO: Fetch real data from Firebase
        // For now, setting "Tech Advance" placeholder values
        tvStatUsers.setText("1,204");
        tvStatEvents.setText("42");
        tvStatSystem.setText("98%");
    }

    private void setupClickListeners() {
        cardManageEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Accessing Event Protocol...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminEventListActivity
        });

        cardManageProfiles.setOnClickListener(v -> {
            Toast.makeText(this, "Accessing User Database...", Toast.LENGTH_SHORT).show(); // Modified
            // TODO: Navigate to AdminProfileListActivity
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
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }


    // This Method updates data on the starting dashboard screen | E.g, Total User and Total Active Users
    private void updateData(){
        // Query Firestore to get the total count of users in the accounts collection
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Get the total number of documents (users)
                    int totalUsers = queryDocumentSnapshots.size();

                    // Account for admin accounts --> should not be included
                    totalUsers = totalUsers - 6;
                    
                    // Format the number with commas for better readability
                    String formattedCount = formatNumber(totalUsers);

                    
                    // Update the TextView with the user count
                    tvStatUsers.setText(formattedCount);
                    
                    Log.d("AdminDashboard", "Total users count: " + totalUsers);
                })
                .addOnFailureListener(e -> {
                    // If query fails, log error and keep default value
                    Log.e("AdminDashboard", "Failed to get user count: " + e.getMessage(), e);
                    // Optionally, you could set an error message or keep the default value
                });
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
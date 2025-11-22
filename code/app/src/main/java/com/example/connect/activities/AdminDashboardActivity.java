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

/**
 * Activity for the Admin Dashboard.
 * Provides access to administrative features like managing events, profiles,
 * images, and organizers.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
        loadDashboardStats(); // Added
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
}

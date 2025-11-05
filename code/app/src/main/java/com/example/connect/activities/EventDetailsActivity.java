package com.example.connect.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.example.connect.network.WaitingListRepository;
import com.google.android.material.button.MaterialButton;

/**
 * Activity to display all details of an event.
 * Shows all Firebase fields: name, date, time, location, price, 
 * regOpens, regCloses, maxParticipants, and poster image.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private Event event;
    private WaitingListRepository waitingListRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Get event from intent
        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Event details not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize waiting list repository
        waitingListRepo = new WaitingListRepository();

        // Setup back button
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Display event details
        displayEventDetails();
        
        // Load waiting list count
        loadWaitingListCount();

        // Setup Join Waitlist button
        MaterialButton btnJoin = findViewById(R.id.btn_join_waitlist);
        if (btnJoin != null) {
            btnJoin.setOnClickListener(v -> {
                // TODO: Implement join waitlist functionality
                Toast.makeText(this, "Join waitlist functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void displayEventDetails() {
        // Event Name
        TextView tvName = findViewById(R.id.tv_event_name);
        if (tvName != null) {
            tvName.setText(event.getName() != null ? event.getName() : "Event Name");
        }

        // Event Date (formatted nicely)
        TextView tvDate = findViewById(R.id.tv_event_date);
        if (tvDate != null && event.getDate() != null && !event.getDate().isEmpty()) {
            String formattedDate = event.getFormattedDate();
            tvDate.setText("Date: " + formattedDate);
            tvDate.setVisibility(android.view.View.VISIBLE);
        } else if (tvDate != null) {
            tvDate.setVisibility(android.view.View.GONE);
        }

        // Event Time
        TextView tvTime = findViewById(R.id.tv_event_time);
        if (tvTime != null && event.getTime() != null && !event.getTime().isEmpty()) {
            tvTime.setText("Time: " + event.getTime());
            tvTime.setVisibility(android.view.View.VISIBLE);
        } else if (tvTime != null) {
            tvTime.setVisibility(android.view.View.GONE);
        }

        // Location
        TextView tvLocation = findViewById(R.id.tv_event_location);
        if (tvLocation != null && event.getLocation() != null && !event.getLocation().isEmpty()) {
            tvLocation.setText("Location: " + event.getLocation());
            tvLocation.setVisibility(android.view.View.VISIBLE);
        } else if (tvLocation != null) {
            tvLocation.setVisibility(android.view.View.GONE);
        }

        // Price
        TextView tvPrice = findViewById(R.id.tv_event_price);
        if (tvPrice != null && event.getPrice() != null && !event.getPrice().isEmpty()) {
            tvPrice.setText("Price: " + event.getPrice());
            tvPrice.setVisibility(android.view.View.VISIBLE);
        } else if (tvPrice != null) {
            tvPrice.setVisibility(android.view.View.GONE);
        }

        // Registration Opens (formatted from Timestamp)
        TextView tvRegOpens = findViewById(R.id.tv_reg_opens);
        if (tvRegOpens != null) {
            String regOpensStr = event.getRegOpens(); // This now returns formatted string from Timestamp
            if (regOpensStr != null && !regOpensStr.isEmpty()) {
                tvRegOpens.setText("Registration Opens: " + regOpensStr);
                tvRegOpens.setVisibility(android.view.View.VISIBLE);
            } else {
                tvRegOpens.setVisibility(android.view.View.GONE);
            }
        }

        // Registration Closes (formatted from Timestamp)
        TextView tvRegCloses = findViewById(R.id.tv_reg_closes);
        if (tvRegCloses != null) {
            String regClosesStr = event.getRegCloses(); // This now returns formatted string from Timestamp
            if (regClosesStr != null && !regClosesStr.isEmpty()) {
                tvRegCloses.setText("Registration Closes: " + regClosesStr);
                tvRegCloses.setVisibility(android.view.View.VISIBLE);
            } else {
                tvRegCloses.setVisibility(android.view.View.GONE);
            }
        }

        // Max Participants
        TextView tvMaxParticipants = findViewById(R.id.tv_max_participants);
        if (tvMaxParticipants != null) {
            tvMaxParticipants.setText("Maximum Participants: " + event.getMaxParticipants());
            tvMaxParticipants.setVisibility(android.view.View.VISIBLE);
        }

        // Event ID (for debugging, can be hidden later)
        TextView tvEventId = findViewById(R.id.tv_event_id);
        if (tvEventId != null && event.getId() != null) {
            tvEventId.setText("Event ID: " + event.getId());
            tvEventId.setVisibility(android.view.View.GONE); // Hide by default
        }
    }

    /**
     * Load and display the waiting list count for this event.
     */
    private void loadWaitingListCount() {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            android.util.Log.w("EventDetailsActivity", "Event or Event ID is null/empty");
            return;
        }

        String eventId = event.getId();
        android.util.Log.d("EventDetailsActivity", "Loading waiting list count for Event ID: " + eventId);

        waitingListRepo.getWaitingListCount(eventId, new WaitingListRepository.WaitingListCountCallback() {
            @Override
            public void onSuccess(int count) {
                android.util.Log.d("EventDetailsActivity", "Waiting list count loaded successfully: " + count);
                runOnUiThread(() -> {
                    TextView tvWaitingListCount = findViewById(R.id.tv_waiting_list_count);
                    if (tvWaitingListCount != null) {
                        tvWaitingListCount.setText("Total Entrants on Waiting List: " + count);
                        tvWaitingListCount.setVisibility(android.view.View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("EventDetailsActivity", "Error loading waiting list count for Event ID: " + eventId, e);
                // Still show count as 0 if there's an error
                runOnUiThread(() -> {
                    TextView tvWaitingListCount = findViewById(R.id.tv_waiting_list_count);
                    if (tvWaitingListCount != null) {
                        tvWaitingListCount.setText("Total Entrants on Waiting List: 0");
                        tvWaitingListCount.setVisibility(android.view.View.VISIBLE);
                    }
                });
            }
        });
    }
}


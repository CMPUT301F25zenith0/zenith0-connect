package com.example.connect.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;

/**
 * Activity to display all details of an event.
 * Shows all Firebase fields: name, date, time, location, price, 
 * regOpens, regCloses, maxParticipants, and poster image.
 */
public class EventDetailsActivity extends AppCompatActivity {

    private Event event;

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

        // Setup back button
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Display event details
        displayEventDetails();

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

        // Event Date
        TextView tvDate = findViewById(R.id.tv_event_date);
        if (tvDate != null && event.getDate() != null && !event.getDate().isEmpty()) {
            tvDate.setText("Date: " + event.getDate());
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

        // Registration Opens
        TextView tvRegOpens = findViewById(R.id.tv_reg_opens);
        if (tvRegOpens != null && event.getRegOpens() != null && !event.getRegOpens().isEmpty()) {
            tvRegOpens.setText("Registration Opens: " + event.getRegOpens());
            tvRegOpens.setVisibility(android.view.View.VISIBLE);
        } else if (tvRegOpens != null) {
            tvRegOpens.setVisibility(android.view.View.GONE);
        }

        // Registration Closes
        TextView tvRegCloses = findViewById(R.id.tv_reg_closes);
        if (tvRegCloses != null && event.getRegCloses() != null && !event.getRegCloses().isEmpty()) {
            tvRegCloses.setText("Registration Closes: " + event.getRegCloses());
            tvRegCloses.setVisibility(android.view.View.VISIBLE);
        } else if (tvRegCloses != null) {
            tvRegCloses.setVisibility(android.view.View.GONE);
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
}

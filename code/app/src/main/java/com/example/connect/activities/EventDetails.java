package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;

public class EventDetails extends AppCompatActivity {

    private TextView eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        // Get the event ID from the Intent
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");



        // Check if the eventID exists --> otherwise this page does not work
        /*
        if (eventId != null) {
            // Use the event ID to fetch event details
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
        */

        // Working process testing
        // Title is pulled from instance --> then title is change to check if its correct
        eventTitle = findViewById(R.id.tvEventTitle);
        eventTitle.setText(eventId);
    }




}

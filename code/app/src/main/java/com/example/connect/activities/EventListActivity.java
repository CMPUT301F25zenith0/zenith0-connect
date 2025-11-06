package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;

public class EventListActivity extends AppCompatActivity {

    private Button scan;
    // New Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list); // Load login screen

        scan = findViewById(R.id.scan_btn);
        // Navigate to login activity when clicked
        scan.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, QRCodeScanner.class);
            startActivity(intent);
        });
    }
}

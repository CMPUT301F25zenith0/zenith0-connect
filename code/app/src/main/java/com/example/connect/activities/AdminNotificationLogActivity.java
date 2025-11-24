package com.example.connect.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AdminNotificationLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list); // Reuse generic list layout

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Notification Logs");
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView emptyState = findViewById(R.id.tv_empty_state);
        emptyState.setText("No logs available (Feature pending)");
        emptyState.setVisibility(android.view.View.VISIBLE);

        // TODO: Implement fetching logs from a 'notifications' collection if it exists
    }
}

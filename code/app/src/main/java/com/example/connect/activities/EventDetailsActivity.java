package com.example.connect.activities;

import android.os.Bundle;
import com.example.connect.R;
import com.google.android.material.button.MaterialButton;

public class EventDetailsActivity extends BaseBackActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        MaterialButton back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }
        setupBackUi("Event Details");
    }
}

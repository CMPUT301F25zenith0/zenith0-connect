package com.example.connect.activities;

import android.os.Bundle;
import com.example.connect.R;
import com.google.android.material.button.MaterialButton;

public class EditEventActivity extends BaseBackActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        setupBackUi("Edit Event");
        MaterialButton back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        }

        // TODO: load eventId = getIntent().getStringExtra("eventId")
    }
}

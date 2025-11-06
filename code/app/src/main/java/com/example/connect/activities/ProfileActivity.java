package com.example.connect.activities;

import android.os.Bundle;
import com.example.connect.R;

public class ProfileActivity extends BaseBackActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);   // use safe layout below
        setupBackUi("Profile");
    }
}

package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;

/**
 * Main launcher activity for the app.
 * Currently contains a Login button that navigates to ProfileActivity.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnAcctCreate, btnOrganizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_screen);

        btnLogin = findViewById(R.id.btn_login);
        btnAcctCreate = findViewById(R.id.create_acct_btn);
        btnOrganizer = findViewById(R.id.btn_organizer);

        // Navigate to login activity when clicked
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigate to create profile activity when clicked
        btnAcctCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateActivity.class);
            startActivity(intent);
        });

        // Navigate to organizer dashboard when clicked
        btnOrganizer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrganizerActivity.class);
            startActivity(intent);
        });
    }
}
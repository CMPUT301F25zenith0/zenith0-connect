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

    private Button btnLogin, btnAcctCreate, btnTestQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_screen);

        btnLogin = findViewById(R.id.btn_login);
        btnAcctCreate = findViewById(R.id.create_acct_btn);
        btnTestQr = findViewById(R.id.btn_test_qr); // Add this line

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

        // Navigate to QR test activity when clicked
        btnTestQr.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRTestActivity.class);
            startActivity(intent);
        });
    }
}
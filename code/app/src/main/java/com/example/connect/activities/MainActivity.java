package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main launcher activity for the app.
 * Checks if user should be auto-logged in based on Remember Me preference.
 * If not, displays login and account creation options.
 *
 * @author Aakansh Chatterjee
 * @version 2.0
 */
public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnAcctCreate;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check auto-login
        checkAutoLogin();
    }

    /**
     * Checks if user should be auto-logged in.
     * If Remember Me is enabled and user is already authenticated with Firebase,
     * skip MainActivity and go directly to EventListActivity.
     * Otherwise, show the MainActivity screen normally.
     */
    private void checkAutoLogin() {
        // Get Remember Me preference
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        // Get current Firebase user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d("MainActivity", "Remember Me: " + rememberMe + ", Current User: " +
                (currentUser != null ? currentUser.getUid() : "null"));

        // If Remember Me is enabled and user is logged in, auto-login
        if (rememberMe && currentUser != null) {
            Log.d("MainActivity", "Auto-login enabled, navigating to EventListActivity");

            // Go directly to EventListActivity
            Intent intent = new Intent(MainActivity.this, EventListActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so user can't go back to it
        } else {
            // Show MainActivity screen normally
            setContentView(R.layout.open_screen);

            // Set up your normal MainActivity UI and button listeners
            setupMainActivityUI();
        }
    }

    /**
     * Set up the MainActivity UI elements and listeners.
     * Initializes buttons for login, account creation, and QR testing.
     */
    private void setupMainActivityUI() {
        btnLogin = findViewById(R.id.btn_login);
        btnAcctCreate = findViewById(R.id.create_acct_btn);

        // Navigate to login activity when clicked
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigate to create profile activity when clicked
        btnAcctCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAcctActivity.class);
            startActivity(intent);
        });

    }
}
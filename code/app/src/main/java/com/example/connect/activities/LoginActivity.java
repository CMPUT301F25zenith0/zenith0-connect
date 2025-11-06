package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles user authentication and navigation to the app after login.
 */
public class LoginActivity extends AppCompatActivity {

    // UI
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnBack;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();
    }

    /** If already signed in, skip login. */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d("LoginActivity", "User already signed in: " + user.getUid());
            goToEventListAndFinish();
        }
    }

    private void initViews() {
        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_Password);
        btnLogin   = findViewById(R.id.login_btn);
        btnBack    = findViewById(R.id.back_btn);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    /** Main login flow. */
    private void loginUser() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        hideKeyboard();
        setLoading(true);
        Log.d("LoginActivity", "Attempting login for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("LoginActivity", "Login successful! UID: " + (user != null ? user.getUid() : "null"));
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        goToEventListAndFinish();
                    } else {
                        Exception e = task.getException();
                        Log.e("LoginActivity", "Login failed: " + (e != null ? e.getMessage() : "unknown"), e);
                        setLoading(false);
                        String msg = getErrorMessage(e != null ? e.getMessage() : null);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Navigate to the entrant’s Event List screen and clear back stack. */
    private void goToEventListAndFinish() {
        Intent i = new Intent(LoginActivity.this, EventListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) { // keep consistent with your create-account flow
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "Login");
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /** Map Firebase errors to user-friendly messages. */
    private String getErrorMessage(String firebaseError) {
        if (firebaseError == null) return "Login failed. Please try again.";

        String s = firebaseError.toLowerCase();
        if (s.contains("no user record") || s.contains("user not found")) {
            return "No account found with this email.";
        }
        if (s.contains("wrong password") || s.contains("invalid-credential")) {
            return "Incorrect password. Please try again.";
        }
        if (s.contains("too many requests")) {
            return "Too many failed attempts. Please try again later.";
        }
        if (s.contains("network")) {
            return "Network error. Please check your connection.";
        }
        return "Login failed: " + firebaseError;
    }
}

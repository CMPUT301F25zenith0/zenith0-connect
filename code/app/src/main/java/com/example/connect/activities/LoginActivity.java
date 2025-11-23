package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.utils.UserActivityTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity responsible for handling user authentication and login functionality.
 * <p>
 * This activity provides a login interface where users can enter their credentials
 * (email and password) to authenticate with Firebase Authentication. It includes
 * input validation, error handling, admin verification, and user-friendly error messages.
 * </p>
 * Aakansh - Login functionality
 * Vansh - Remember me functonality
 * @author Aakansh Chatterjee, Vansh Taneja
 * @version 2.1
 */

public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnBack;
    private CheckBox cbRememberMe;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // SharedPreferences for Remember Me
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the layout, Firebase authentication, and configures UI component listeners.
     * <p>
     * @param savedInstanceState Bundle containing the activity's previously saved state,
     *                          or null if there is no saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity); // Load login screen

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initViews();

        // Set click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components by finding their references from the layout.
     * This method links the Java variables to their corresponding XML elements.
     */
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_Password);
        btnLogin = findViewById(R.id.login_btn);
        btnBack = findViewById(R.id.back_btn);
        cbRememberMe = findViewById(R.id.remem_me_check);
    }

    /**
     * Sets up click listeners for interactive UI components.
     * Configures the login button to trigger authentication and the back button to return to the previous screen.
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        // Better way to handle back button
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Handles the user login process.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Retrieves and trims email and password input</li>
     *   <li>Validates the input fields</li>
     *   <li>Disables the login button to prevent duplicate requests</li>
     *   <li>Attempts authentication with Firebase</li>
     *   <li>Checks user's admin status from Firestore</li>
     *   <li>Handles success by navigating to EventListActivity</li>
     *   <li>Handles failure by displaying appropriate error messages</li>
     * </ol>
     * </p>
     */
    private void loginUser() {
        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Show loading state
        // Stops button spam
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Log Action
        Log.d("LoginActivity", "Attempting login for: " + email);

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Login successful, now check admin status
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.d("LoginActivity", "Authentication successful! UID: " );

                    // Check admin status in Firestore
                    checkAdminStatus(user);
                })
                .addOnFailureListener(e -> {
                    // Login failed
                    Log.e("LoginActivity", "Login failed: " + e.getMessage(), e);
                    resetButton();

                    // Show user-friendly error message
                    String errorMessage = getErrorMessage(e.getMessage());
                    Toast.makeText(LoginActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Checks if the authenticated user has admin privileges.
     * Retrieves the user document from Firestore and checks for the 'admin' attribute.
     * If admin is true, navigates to admin activity. Otherwise, proceeds with regular login.
     *
     * @param user The authenticated FirebaseUser
     */
    private void checkAdminStatus(FirebaseUser user) {
        // Query Firestore for user document
        db.collection("accounts").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if admin field exists and is true
                        Boolean isAdmin = documentSnapshot.getBoolean("admin");

                        if (isAdmin != null && isAdmin) {
                            // User is admin, navigate to admin activity
                            Log.d("LoginActivity", "Admin user detected! UID: " );
                            proceedWithAdminLogin();
                        } else {
                            // Regular user, proceed with normal login
                            Log.d("LoginActivity", "Regular user login! UID: ");
                            proceedWithRegularLogin();
                        }
                    } else {
                        // User document doesn't exist, block login
                        Log.e("LoginActivity", "User document not found in Firestore");
                        mAuth.signOut();
                        resetButton();
                        Toast.makeText(LoginActivity.this,
                                "Please contact support or create a new account.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve user document, proceed with regular login
                    Log.e("LoginActivity", "Failure to retrieve document");
                    mAuth.signOut();
                    resetButton();
                    Toast.makeText(LoginActivity.this,
                            "Wait, check details and try again OR Contact technical support.",
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Completes the login process for admin users.
     * Saves preferences and navigates to the admin activity.
     */
    private void proceedWithAdminLogin() {
        // Save Remember Me preference
        saveRememberMePreference(cbRememberMe.isChecked());

        // Mark user as active
        UserActivityTracker.markUserActive();

        Toast.makeText(LoginActivity.this,
                "Welcome back, Admin!",
                Toast.LENGTH_SHORT).show();

        // Navigate to admin activity (replace AdminActivity.class with your actual admin activity)
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish(); // Close login screen so user can't go back to it
    }

    /**
     * Completes the login process for regular users.
     * Saves preferences and navigates to the main application screen.
     */
    private void proceedWithRegularLogin() {
        // Save Remember Me preference
        saveRememberMePreference(cbRememberMe.isChecked());

        // Mark user as active
        UserActivityTracker.markUserActive();

        Toast.makeText(LoginActivity.this,
                "Welcome back!",
                Toast.LENGTH_SHORT).show();

        // Navigate to main app screen
        Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
        startActivity(intent);
        finish(); // Close login screen so user can't go back to it
    }

    /**
     * Saves the Remember Me preference to SharedPreferences.
     * @param rememberMe true if user wants to be remembered, false otherwise
     */
    private void saveRememberMePreference(boolean rememberMe) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
        Log.d("LoginActivity", "Remember Me preference saved: " + rememberMe);
    }

    /**
     * Validates user input for email and password fields.
     * <p>
     * Performs the following validations:
     * <ul>
     *   <li>Checks if email field is empty</li>
     *   <li>Validates email format using Android Patterns</li>
     *   <li>Checks if password field is empty</li>
     *   <li>Ensures password meets minimum length requirement (6 characters)</li>
     * </ul>
     * </p>
     *
     * @param email    The email address entered by the user
     * @param password The password entered by the user
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs(String email, String password) {
        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        // Check if email is valid format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        // Check if password is empty
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        // Check password length  --> need to match what we set in account creation
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Resets the login button to its default state.
     * Re-enables the button and restores the original button text after
     * a login attempt completes (successfully or unsuccessfully).
     */
    private void resetButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
    }

    /**
     * Converts technical Firebase error messages into user-friendly error messages.
     * <p>
     * Maps common Firebase authentication errors to clear, actionable messages
     * that users can understand without technical knowledge.
     * </p>
     *
     * @param firebaseError The raw error message from Firebase Authentication,
     *                     or null if no specific error message is available
     * @return A user-friendly error message string appropriate for display in the UI
     * <p>
     * AI: Claude was used to produce this
     * Prompt: Make a small list of basic Firebase errors. Write a small java method that takes the error and returns an easy to understand message
     */
    private String getErrorMessage(String firebaseError) {
        if (firebaseError == null) {
            return "Login failed. Please try again.";
        }

        if (firebaseError.contains("no user record") ||
                firebaseError.contains("user not found")) {
            return "No account found with this email.";
        }

        if (firebaseError.contains("wrong password") ||
                firebaseError.contains("invalid-credential")) {
            return "Incorrect password. Please try again.";
        }

        if (firebaseError.contains("too many requests")) {
            return "Too many failed attempts. Please try again later.";
        }

        if (firebaseError.contains("network")) {
            return "Network error. Please check your connection.";
        }

        // Default message for other errors
        return "Login failed: " + firebaseError;
    }
}
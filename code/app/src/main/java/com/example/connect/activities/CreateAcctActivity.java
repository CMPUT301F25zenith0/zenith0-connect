package com.example.connect.activities;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.network.CreateAccountRepo;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

/**
 * Activity responsible for user account creation and registration.
 * <p>
 * This activity provides a registration interface where new users can create an account,
 * by providing their personal information. It handles both Firebase Authentication for,
 * secure credential management and Firestore database operations for storing user profile data.
 * </p>
 * The account creation process follows a two-step approach:
 * <ol>
 *   <li>Create authentication credentials using Firebase Auth</li>
 *   <li>Save additional user profile data to Firestore database</li>
 * </ol>
 * Note: Passwords are managed securely by Firebase Auth and are never stored in Firestore.
 * </p>
 * @author Aakansh Chatterjee
 * @version 1.0
 */

public class CreateAcctActivity extends AppCompatActivity {
    // UI Elements
    private EditText etFullName, etDisplayName, etEmail, etPassword, etConfirmPassword, etMobileNumber;
    private MaterialButton btnCreateAccount;
    private ImageButton btnBack;

    // Repository for handling Firebase operations
    private CreateAccountRepo accountRepo;



    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the layout, Firebase services (Auth and Firestore), and configures UI component listeners.
     * <p>
     * @param savedInstanceState Bundle containing the activitys previously saved state, or null if there no saved state
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_activity); // Load activity screen


        // Initialize repository
        accountRepo = new CreateAccountRepo(
                FirebaseAuth.getInstance(),
                FirebaseFirestore.getInstance()
        );

        // Initialize inputs and buttons
        initViews();

        // Set click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components by finding their references from the layout.
     * This links the Java variables to their corresponding XML elements.
     */
    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email_profile);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_pass);
        etMobileNumber = findViewById(R.id.et_mobile_num);
        btnCreateAccount = findViewById(R.id.btn_create_acct);
        btnBack = findViewById(R.id.back_btn);
    }


    /**
     * Sets up click listeners for interactive UI components.
     * Configures the back button to return to the previous screen and the create account button to trigger the account creation process.
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnCreateAccount.setOnClickListener(v -> {
            createAccount();
        });
    }

    /**
     * Initiates the account creation process.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Retrieves and trims all input field values</li>
     *   <li>Validates all inputs for completeness and correctness</li>
     *   <li>Disables the create button to prevent duplicate submissions</li>
     *   <li>Creates Firebase Auth account with email and password</li>
     *   <li>On success, proceeds to save additional user data to Firestore</li>
     *   <li>On failure, displays appropriate error message and resets the button</li>
     * </ol>
     * Note: Firebase Auth automatically handles password encryption and security,
     * so passwords are never stored in plain text.
     * </p>
     */
    private void createAccount() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(fullName, displayName, email, password, confirmPassword, mobileNumber)) {
            return;
        }

        // Show loading state
        // Cannot spam the button
        btnCreateAccount.setEnabled(false);
        btnCreateAccount.setText("Creating Account...");

        // Logs the action
        Log.d("CreateActivity", "Starting Firebase Auth account creation");

        // Create Firebase Auth user
        // Use repository to create account
        accountRepo.registerUser(email, password, fullName, displayName, mobileNumber,
                new CreateAccountRepo.RegistrationCallback() {
                    @Override
                    public void onSuccess() {
                        resetButton();
                        Toast.makeText(CreateAcctActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        resetButton();
                        Toast.makeText(CreateAcctActivity.this,
                                error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Resets the create account button to its default state.
     * Re-enables the button and restores the original button text after an account creation attempt completes (successfully or unsuccessfully).
     */
    private void resetButton() {
        btnCreateAccount.setEnabled(true);
        btnCreateAccount.setText("Create Account");
    }

    /**
     * Validates all user input fields for account creation.
     * <p>
     * Performs comprehensive validation including:
     * <ul>
     *   <li>Checks that all required fields are not empty</li>
     *   <li>Validates email format using Android Patterns</li>
     *   <li>Ensures password meets minimum length requirement (6 characters)</li>
     *   <li>Verifies password and confirmation password match</li>
     *   <li>Validates mobile number has minimum 10 digits</li>
     * </ul>
     * If any validation fails, the appropriate error message is displayed on the corresponding field and focus is moved to that field.
     * </p>
     *
     * @param fullName        The user's full legal name
     * @param displayName     The user's display/public name
     * @param email           The user's email address
     * @param password        The user's chosen password
     * @param confirmPassword The password confirmation for verification
     * @param mobileNumber    The user's mobile phone number
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs(String fullName, String displayName, String email,
                                   String password, String confirmPassword, String mobileNumber) {

        // Check if full name is empty
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }

        // Check if display name is empty
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Display name is required");
            etDisplayName.requestFocus();
            return false;
        }

        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        // Check if email is valid --> looks like a valid email (inlcudes @)
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

        // Check password length --> adjustable --> is placeholder
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        // Search for ways to make this intake and check for an actual phone number format
        // Phone number is optional, but if provided, validate it
        if (!TextUtils.isEmpty(mobileNumber) && mobileNumber.length() < 10) {
            etMobileNumber.setError("Please enter a valid mobile number (at least 10 digits)");
            etMobileNumber.requestFocus();
            return false;
        }

        return true;
    }

}

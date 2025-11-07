package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for viewing and updating user profile information.
 * Users can update their name, email, and phone number here.
 * Also handles account deletion and switching between user/organizer views.
 *
 * Implements US 01.02.02: As an entrant, I want to update information such as
 * name, email and contact information on my profile.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etDeviceId;
    private MaterialButton btnSave, btnDelete, btnBack, btnLogout, btnOrgView;
    private ImageView profileImage;
    private ImageButton edit_image;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    /**
     * Initialize the activity when it starts.
     * Sets up Firebase, checks if user is logged in, loads profile data,
     * and sets up all the UI components and click listeners.
     *
     * @param savedInstanceState saved state from previous instance (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        initViews();
        setupViewMode(); // Check if opened from organizer view
        setupClickListeners();
        loadUserProfile();
        setupDeviceId();
    }

    /**
     * Adjust the UI based on where this activity was opened from.
     * If opened from OrganizerActivity, changes "Org View" button to "User View"
     * so users can navigate back to the user dashboard.
     */
    private void setupViewMode() {
        // Check if opened from OrganizerActivity
        boolean fromOrganizer = getIntent().getBooleanExtra("from_organizer", false);

        if (fromOrganizer && btnOrgView != null) {
            // Change button text to "User View" when in organizer mode
            btnOrgView.setText("User View");
        }
    }

    /**
     * Initialize all UI components by finding their views from the layout.
     * Gets references to all EditTexts, Buttons, and ImageViews.
     */
    private void initViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email_profile);
        etPhone = findViewById(R.id.et_phone);
        etDeviceId = findViewById(R.id.et_device);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.back_btn);
        btnLogout = findViewById(R.id.btn_logout);
        btnOrgView = findViewById(R.id.btn_org_view);
        profileImage = findViewById(R.id.profile_img);
        edit_image = findViewById(R.id.edit_profile);
    }

    /**
     * Set up click listeners for all buttons and interactive elements.
     * Handles back button, logout, save profile, delete account,
     * profile image editing, and switching between user/organizer views.
     */
    private void setupClickListeners() {
        // All the buttons required
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }


        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }


        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }


        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDeleteProfile());
        }


        if (edit_image != null) {
            edit_image.setOnClickListener(v -> {
                Toast.makeText(this, "Profile image upload coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Org View / User View button - behavior depends on context
        if (btnOrgView != null) {
            btnOrgView.setOnClickListener(v -> {
                // Check if opened from organizer view
                boolean fromOrganizer = getIntent().getBooleanExtra("from_organizer", false);

                if (fromOrganizer) {
                    // If in organizer view, go back to user event list (main dashboard)
                    Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Close profile and organizer activities
                } else {
                    // Normal user view, go to organizer view
                    Intent intent = new Intent(ProfileActivity.this, OrganizerActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    /**
     * Show a confirmation dialog before logging out.
     * Prevents accidental logouts by asking the user to confirm.
     */
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Perform the actual logout process.
     * Signs out from Firebase Authentication, clears "Remember Me" preferences,
     * and navigates back to the login screen.
     */
    private void performLogout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear "Remember Me" preference
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", false);
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen and clear activity stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Load the user's profile information from Firestore.
     * Retrieves name, email, and phone number from the "accounts" collection.
     * If no profile exists in Firestore, uses the email from Firebase Auth as fallback.
     */
    private void loadUserProfile() {
        db.collection("accounts").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Load data from Firestore
                                String name = document.getString("full_name");
                                String email = document.getString("email");
                                String phone = document.getString("mobile_num");

                                if (name != null) etName.setText(name);
                                if (email != null) etEmail.setText(email);
                                if (phone != null) etPhone.setText(phone);
                            } else {
                                // No profile exists, use Firebase Auth email
                                if (currentUser.getEmail() != null) {
                                    etEmail.setText(currentUser.getEmail());
                                }
                                Log.d("ProfileActivity", "No profile document found, creating new one");
                            }
                        } else {
                            Log.e("ProfileActivity", "Error loading profile", task.getException());
                            Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Set up the device ID field with a masked Android ID.
     * Retrieves the device's Android ID and displays only the last 4 characters
     * for privacy reasons. The field is read-only and cannot be edited.
     */
    private void setupDeviceId() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.isEmpty()) {
            // Mask the device ID (show only last 4 characters)
            String maskedId = "*******" + androidId.substring(Math.max(0, androidId.length() - 4));
            etDeviceId.setText(maskedId);
        } else {
            etDeviceId.setText("*******");
        }
        // Device ID is read-only
        etDeviceId.setEnabled(false);
    }

    /**
     * Save the updated profile information to Firestore.
     * First validates the input, then updates the Firestore document.
     * If the email address changed, also updates it in Firebase Auth.
     * Phone number is optional - if empty, sets it to null in Firestore.
     */
    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(name, email, phone)) {
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", name);
        updates.put("email", email);
        // Only add mobile_num if provided (phone number is optional)
        if (!TextUtils.isEmpty(phone)) {
            updates.put("mobile_num", phone);
        } else {
            // If phone is empty, set it to null or remove the field
            updates.put("mobile_num", null);
        }
        updates.put("updated_at", System.currentTimeMillis());

        // Update Firestore
        db.collection("accounts").document(userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");

                        if (task.isSuccessful()) {
                            // Also update Firebase Auth email if it changed
                            if (!email.equals(currentUser.getEmail())) {
                                updateFirebaseAuthEmail(email);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("ProfileActivity", "Error updating profile", task.getException());
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to update profile: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Update the email address in Firebase Authentication.
     * This is called when the user changes their email in the profile.
     * Note: updateEmail() is deprecated but still functional.
     *
     * @param newEmail the new email address to set
     */
    private void updateFirebaseAuthEmail(String newEmail) {
        // updateEmail() is deprecated but still works - updates the user's email in Firebase Auth
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile and email updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ProfileActivity", "Error updating email", task.getException());
                            Toast.makeText(ProfileActivity.this,
                                    "Profile updated but email update failed. Please re-authenticate to change email.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Show a confirmation dialog before deleting the account.
     * Warns the user that this action cannot be undone.
     */
    private void confirmDeleteProfile() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete the user's profile from both Firestore and Firebase Auth.
     * Deletes the Firestore document first, then the Firebase Auth account.
     * May require re-authentication if Firebase security policies require it.
     */
    private void deleteProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store userId before any deletion (important!)
        final String userIdToDelete = userId;
        if (userIdToDelete == null || userIdToDelete.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable delete button to prevent multiple clicks
        btnDelete.setEnabled(false);
        btnDelete.setText("Deleting...");

        // Delete Firestore FIRST, then Auth account
        // This ensures we have the userId available
        deleteFirestoreDocument(userIdToDelete, () -> {
            // After Firestore is deleted, delete Auth account
            deleteAuthAccount();
        });
    }

    /**
     * Delete the Firebase Auth account.
     * Attempts to delete the account directly. If Firebase requires
     * re-authentication (for security), prompts the user for their password.
     */
    private void deleteAuthAccount() {
        if (currentUser == null) {
            Log.e("ProfileActivity", "Current user is null");
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
            return;
        }

        // Try to delete the Auth account directly
        currentUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Auth account deleted successfully
                            Log.d("ProfileActivity", "Firebase Auth account deleted successfully");
                            // Firestore should already be deleted, just clean up
                            cleanupAndNavigate();
                        } else {
                            Exception exception = task.getException();
                            String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                            Log.e("ProfileActivity", "Error deleting Auth account: " + errorMsg, exception);

                            // Check if re-authentication is required
                            if (errorMsg != null && errorMsg.contains("requires recent authentication")) {
                                // Prompt user to re-authenticate
                                promptReAuthentication();
                            } else {
                                // Other error - Firestore should already be deleted, just clean up
                                Log.w("ProfileActivity", "Auth deletion failed: " + errorMsg);
                                Toast.makeText(ProfileActivity.this,
                                        "Auth deletion failed: " + errorMsg + ". Profile data may still be deleted.",
                                        Toast.LENGTH_LONG).show();
                                cleanupAndNavigate();
                            }
                        }
                    }
                });
    }

    /**
     * Show a dialog asking the user to enter their password for re-authentication.
     * Firebase requires recent authentication before allowing account deletion
     * as a security measure.
     */
    private void promptReAuthentication() {
        // Create a dialog to get password
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Re-authentication Required");
        builder.setMessage("Please enter your password to confirm account deletion:");

        // Create password input field
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");
        passwordInput.setPadding(50, 20, 50, 20);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                btnDelete.setEnabled(true);
                btnDelete.setText("Delete Profile");
                return;
            }
            // Re-authenticate with password
            reAuthenticateAndDelete(password);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
            dialog.dismiss();
        });

        builder.setOnCancelListener(dialog -> {
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
        });

        builder.show();
    }

    /**
     * Re-authenticate the user with their password, then attempt to delete
     * the Auth account again.
     *
     * @param password the user's password for re-authentication
     */
    private void reAuthenticateAndDelete(String password) {
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "Unable to re-authenticate: user email not found", Toast.LENGTH_SHORT).show();
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
            return;
        }

        // Create credential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

        // Re-authenticate
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("ProfileActivity", "Re-authentication successful, deleting Auth account");
                            // Now try deleting Auth account again
                            deleteAuthAccountAfterReAuth();
                        } else {
                            Exception exception = task.getException();
                            String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                            Log.e("ProfileActivity", "Re-authentication failed: " + errorMsg, exception);
                            btnDelete.setEnabled(true);
                            btnDelete.setText("Delete Profile");
                            Toast.makeText(ProfileActivity.this,
                                    "Re-authentication failed: " + (errorMsg.contains("password") ? "Incorrect password" : errorMsg),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Attempt to delete the Auth account after successful re-authentication.
     * This should succeed since the user has been re-authenticated.
     */
    private void deleteAuthAccountAfterReAuth() {
        if (currentUser == null) {
            Log.e("ProfileActivity", "Current user is null after re-auth");
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
            return;
        }

        currentUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("ProfileActivity", "Firebase Auth account deleted successfully after re-auth");
                            // Firestore should already be deleted, just clean up
                            cleanupAndNavigate();
                        } else {
                            Exception exception = task.getException();
                            String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                            Log.e("ProfileActivity", "Auth deletion failed after re-auth: " + errorMsg, exception);
                            btnDelete.setEnabled(true);
                            btnDelete.setText("Delete Profile");
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to delete account: " + errorMsg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Delete the user's document from the Firestore "accounts" collection.
     * First checks if the document exists for debugging purposes,
     * then proceeds with deletion and executes the callback when done.
     *
     * @param userIdToDelete the user ID whose document should be deleted
     * @param onComplete callback to execute after deletion completes (success or failure)
     */
    private void deleteFirestoreDocument(String userIdToDelete, Runnable onComplete) {
        Log.d("ProfileActivity", "Attempting to delete Firestore document for userId: " + userIdToDelete);
        Log.d("ProfileActivity", "Collection path: accounts/" + userIdToDelete);

        // First check if document exists (optional, but helps with debugging)
        db.collection("accounts").document(userIdToDelete)
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<com.google.firebase.firestore.DocumentSnapshot> getTask) {
                        if (getTask.isSuccessful()) {
                            com.google.firebase.firestore.DocumentSnapshot doc = getTask.getResult();
                            if (doc != null && doc.exists()) {
                                Log.d("ProfileActivity", "Document exists, proceeding with deletion");
                            } else {
                                Log.w("ProfileActivity", "Document does not exist in Firestore for userId: " + userIdToDelete);
                            }
                        }

                        // Proceed with deletion regardless
                        performFirestoreDeletion(userIdToDelete, onComplete);
                    }
                });
    }

    /**
     * Perform the actual deletion of the Firestore document.
     * Handles success and failure cases, and executes the callback in both scenarios.
     * Shows appropriate error messages if deletion fails.
     *
     * @param userIdToDelete the user ID whose document should be deleted
     * @param onComplete callback to execute when deletion attempt completes
     */
    private void performFirestoreDeletion(String userIdToDelete, Runnable onComplete) {
        db.collection("accounts").document(userIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileActivity", "✅ Firestore document deleted successfully for userId: " + userIdToDelete);
                    runOnUiThread(() -> {
                        // Show success message (optional)
                        Log.d("ProfileActivity", "Firestore deletion completed successfully");
                    });

                    // Execute callback (which will delete Auth account)
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e != null ? e.getMessage() : "Unknown error";
                    Log.e("ProfileActivity", "❌ Error deleting Firestore document for userId: " + userIdToDelete, e);
                    Log.e("ProfileActivity", "Error type: " + (e != null ? e.getClass().getName() : "null"));
                    Log.e("ProfileActivity", "Error message: " + errorMsg);

                    // Check for permission errors
                    if (errorMsg != null && errorMsg.contains("PERMISSION_DENIED")) {
                        Log.e("ProfileActivity", "⚠️ PERMISSION DENIED - Check Firestore security rules!");
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this,
                                    "Permission denied. Check Firestore security rules allow delete on accounts/{userId}",
                                    Toast.LENGTH_LONG).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this,
                                    "Error deleting profile data: " + errorMsg,
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    // Still execute callback even if Firestore deletion fails
                    // (Auth deletion might still work)
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    /**
     * Clean up user preferences and navigate back to the login screen.
     * Clears "Remember Me" preferences, signs out from Firebase,
     * and navigates to MainActivity (which shows login and create account options).
     * Called after account deletion is complete.
     */
    private void cleanupAndNavigate() {
        // Clear "Remember Me" preference
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", false);
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        // Sign out from Firebase (if not already signed out)
        mAuth.signOut();

        Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity (shows both Login and Create Account options)
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Validate the user's input fields.
     * Name and email are required fields. Email must be in valid format.
     * Phone number is optional, but if provided, must be at least 10 digits.
     *
     * @param name the user's full name
     * @param email the user's email address
     * @param phone the user's phone number (can be empty/null)
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs(String name, String email, String phone) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

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

        // Phone number is optional, but if provided, validate it
        if (!TextUtils.isEmpty(phone) && phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number (at least 10 digits)");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.models.User;
import com.example.connect.utils.UserActivityTracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

/**
 * Activity for viewing and updating user profile information.
 * Users can update their name, email, and phone number here.
 * Also handles account deletion and switching between user/organizer views.
 *
 * This class utilizes the user model for data handling.
 *
 * Implements US 01.02.02: As an entrant, I want to update information such as
 * name, email and contact information on my profile.
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // UI Components
    private EditText etName, etEmail, etPhone, etDeviceId, etDisplayName;
    private MaterialButton btnSave, btnDelete, btnLogout, btnOrgView; // btnBack removed from here
    private ImageButton btnBack, edit_image; // btnBack added here as ImageButton
    private ImageView profileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId;

    // Model
    private User currentUserModel;

    /**
     * Initialize the activity when it starts.
     * Sets up Firebase, checks if user is logged in, loads profile data, and sets up all the UI components and click listeners.
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
        firebaseUser = mAuth.getCurrentUser();

        // Check if opened from Admin Dashboard
        boolean isAdminView = getIntent().getBooleanExtra("IS_ADMIN_VIEW", false);

        if (isAdminView) {
            userId = getIntent().getStringExtra("user_id_admin_view");
            if (userId == null) {
                Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            if (firebaseUser == null) {
                Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            userId = firebaseUser.getUid();
        }

        initViews();

        if (isAdminView) {
            setupAdminView();
        } else {
            setupViewMode(); // Check if opened from organizer view
        }

        setupClickListeners();
        loadUserProfile();
        setupDeviceId();
    }

    /**
     * Initialize all UI components by finding their views from the layout.
     */
    private void initViews() {
        etDisplayName = findViewById(R.id.et_display_name); // Added Display Name
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email_profile);
        etPhone = findViewById(R.id.et_phone);
        etDeviceId = findViewById(R.id.et_device);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.back_btn); // Correctly cast to ImageButton
        btnLogout = findViewById(R.id.btn_logout);
        btnOrgView = findViewById(R.id.btn_org_view);
        profileImage = findViewById(R.id.profile_img);
        edit_image = findViewById(R.id.edit_profile);
    }

    /**
     * Set up UI for Admin read-only mode.
     */
    private void setupAdminView() {
        // Disable editing
        etDisplayName.setEnabled(false);
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);

        // Hide buttons
        btnSave.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
        btnOrgView.setVisibility(View.GONE);
        edit_image.setVisibility(View.GONE);
    }

    /**
     * Adjust the UI based on where this activity was opened from.
     */
    private void setupViewMode() {
        // Check if opened from OrganizerActivity
        boolean fromOrganizer = getIntent().getBooleanExtra("from_organizer", false);

        if (fromOrganizer && btnOrgView != null) {
            btnOrgView.setText("Switch to User View");
        }
    }

    /**
     * Set up click listeners for all buttons and interactive elements.
     */
    private void setupClickListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnLogout != null) btnLogout.setOnClickListener(v -> confirmLogout());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveProfile());
        if (btnDelete != null) btnDelete.setOnClickListener(v -> confirmDeleteProfile());

        if (edit_image != null) {
            edit_image.setOnClickListener(v -> {
                Toast.makeText(this, "Profile image upload coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnOrgView != null) {
            btnOrgView.setOnClickListener(v -> {
                boolean fromOrganizer = getIntent().getBooleanExtra("from_organizer", false);
                if (fromOrganizer) {
                    Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(ProfileActivity.this, OrganizerActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Load the user's profile information from Firestore using the User model.
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
                                // Convert Firestore document directly to User object
                                currentUserModel = document.toObject(User.class);

                                if (currentUserModel != null) {
                                    populateUI(currentUserModel);
                                }
                            } else {
                                // No profile exists, use Firebase Auth email
                                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                                    etEmail.setText(firebaseUser.getEmail());
                                    // Initialize a fresh model
                                    currentUserModel = new User();
                                    currentUserModel.setUserId(userId);
                                    currentUserModel.setEmail(firebaseUser.getEmail());
                                }
                                Log.d(TAG, "No profile document found, creating new one");
                            }
                        } else {
                            Log.e(TAG, "Error loading profile", task.getException());
                            Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Populates the UI fields using data from the User model.
     * @param user The user object containing profile data.
     */
    private void populateUI(User user) {
        if (user.getName() != null) etDisplayName.setText(user.getName()); // Bind Display Name
        if (user.getFullName() != null) etName.setText(user.getFullName());
        if (user.getEmail() != null) etEmail.setText(user.getEmail());
        if (user.getPhone() != null) etPhone.setText(user.getPhone());

        // Note: Profile image loading logic would go here using user.getProfileImageUrl()
    }

    /**
     * Set up the device ID field with a masked Android ID.
     */
    private void setupDeviceId() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.isEmpty()) {
            String maskedId = "*******" + androidId.substring(Math.max(0, androidId.length() - 4));
            etDeviceId.setText(maskedId);
        } else {
            etDeviceId.setText("*******");
        }
        etDeviceId.setEnabled(false);
    }

    /**
     * Save the updated profile information to Firestore.
     * Updates the User object and saves it to the database.
     */
    private void saveProfile() {
        String displayName = etDisplayName.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (!validateInputs(name, email, phone)) {
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Ensure we have a model instance
        if (currentUserModel == null) {
            currentUserModel = new User();
            currentUserModel.setUserId(userId);
        }

        // Update the model
        currentUserModel.setName(displayName); // Save Display Name
        currentUserModel.setFullName(name);
        currentUserModel.setEmail(email);
        currentUserModel.setPhone(TextUtils.isEmpty(phone) ? null : phone);

        // Save User object to Firestore
        db.collection("accounts").document(userId)
                .set(currentUserModel, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");

                        if (task.isSuccessful()) {
                            // Update Firebase Auth if email changed
                            if (firebaseUser != null && !email.equals(firebaseUser.getEmail())) {
                                updateFirebaseAuthEmail(email);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error updating profile", task.getException());
                            Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Update the email address in Firebase Authentication.
     */
    private void updateFirebaseAuthEmail(String newEmail) {
        if (firebaseUser == null) return;

        firebaseUser.updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile and email updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Error updating email", task.getException());
                            Toast.makeText(ProfileActivity.this, "Profile updated but email update failed. Please re-authenticate.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
     * Signs out from Firebase Authentication, clears "Remember Me" preferences, and navigates back to the login screen.
     */
    private void performLogout() {
        UserActivityTracker.markUserInactive();
        mAuth.signOut();

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", false);
        editor.remove("email");
        editor.remove("password");
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Show a confirmation dialog before deleting the account.
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
     */
    private void deleteProfile() {
        if (firebaseUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userIdToDelete = userId;
        btnDelete.setEnabled(false);
        btnDelete.setText("Deleting...");

        deleteFirestoreDocument(userIdToDelete, () -> deleteAuthAccount());
    }

    /**
     * Delete the Firebase Auth account.
     */
    private void deleteAuthAccount() {
        if (firebaseUser == null) return;

        firebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            cleanupAndNavigate();
                        } else {
                            Exception exception = task.getException();
                            String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                            Log.e(TAG, "Error deleting Auth account: " + errorMsg);

                            if (errorMsg != null && errorMsg.contains("requires recent authentication")) {
                                promptReAuthentication();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Auth deletion failed: " + errorMsg, Toast.LENGTH_LONG).show();
                                cleanupAndNavigate(); // Firestore is already deleted
                            }
                        }
                    }
                });
    }

    /**
     * Prompt user for re-authentication before sensitive operations.
     */
    private void promptReAuthentication() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Re-authentication Required");
        builder.setMessage("Please enter your password to confirm account deletion:");

        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                reAuthenticateAndDelete(password);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            btnDelete.setEnabled(true);
            btnDelete.setText("Delete Profile");
        });
        builder.show();
    }

    private void reAuthenticateAndDelete(String password) {
        if (firebaseUser == null || firebaseUser.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), password);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteAuthAccountAfterReAuth();
            } else {
                btnDelete.setEnabled(true);
                btnDelete.setText("Delete Profile");
                Toast.makeText(ProfileActivity.this, "Re-authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAuthAccountAfterReAuth() {
        if (firebaseUser != null) {
            firebaseUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) cleanupAndNavigate();
            });
        }
    }

    /**
     * Delete the user's document from the Firestore "accounts" collection.
     */
    private void deleteFirestoreDocument(String userIdToDelete, Runnable onComplete) {
        db.collection("accounts").document(userIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore document deleted");
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting Firestore document", e);
                    // Even if Firestore fails (permissions/etc), try to delete Auth or finish
                    if (onComplete != null) onComplete.run();
                });
    }

    private void cleanupAndNavigate() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        mAuth.signOut();

        Toast.makeText(ProfileActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
        if (!TextUtils.isEmpty(phone) && phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number (at least 10 digits)");
            etPhone.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseUser != null) {
            UserActivityTracker.markUserActive();
        }
    }
}
package com.example.connect.activities;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for viewing and updating user profile information.
 * Implements US 01.02.02: As an entrant I want to update information such as name, 
 * email and contact information on my profile.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etDeviceId;
    private MaterialButton btnSave, btnDelete, btnBack, btnOrgView, btnAdminView;
    private ImageView profileImage, ivEditProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

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
        setupClickListeners();
        loadUserProfile();
        setupDeviceId();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email_profile);
        etPhone = findViewById(R.id.et_phone);
        etDeviceId = findViewById(R.id.et_device);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);
        btnOrgView = findViewById(R.id.btn_org_view);
        btnAdminView = findViewById(R.id.btn_admin_view);
        profileImage = findViewById(R.id.profile_img);
        ivEditProfile = findViewById(R.id.ivEditProfile);
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Save button
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        // Delete button
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDeleteProfile());
        }

        // Edit profile image (placeholder)
        if (ivEditProfile != null) {
            ivEditProfile.setOnClickListener(v -> {
                Toast.makeText(this, "Profile image upload coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Org View button
        if (btnOrgView != null) {
            btnOrgView.setOnClickListener(v -> {
                Toast.makeText(this, "Organizer view coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Admin View button
        if (btnAdminView != null) {
            btnAdminView.setOnClickListener(v -> {
                Toast.makeText(this, "Admin view coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Load user profile data from Firestore.
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
     * Setup Device ID field with masked Android ID.
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
     * Save updated profile information to Firestore.
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
        updates.put("mobile_num", phone);
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
     * Update Firebase Auth email address.
     */
    private void updateFirebaseAuthEmail(String newEmail) {
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
     * Confirm and delete user profile.
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
     * Delete user profile from Firestore.
     * Note: This does not delete the Firebase Auth account.
     */
    private void deleteProfile() {
        db.collection("accounts").document(userId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                            // Clear fields
                            etName.setText("");
                            etEmail.setText("");
                            etPhone.setText("");
                        } else {
                            Log.e("ProfileActivity", "Error deleting profile", task.getException());
                            Toast.makeText(ProfileActivity.this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Validate user input fields.
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

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }
}


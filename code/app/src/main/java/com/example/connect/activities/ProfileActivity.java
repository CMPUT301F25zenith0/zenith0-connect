package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

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
    private MaterialButton btnSave, btnDelete, btnLogout, btnOrgView;
    private ImageButton btnBack, edit_image;
    private ImageView profileImage;

    // ChipGroup
    private ChipGroup chipGroupInterests;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String userId;

    // Model
    private User currentUserModel;

    // Data for Tags
    private List<String> selectedInterests = new ArrayList<>();

    private final String[] AVAILABLE_TAGS = {
            "Technology", "Music", "Art", "Sports", "Travel",
            "Food", "Gaming", "Photography", "Science", "Business",
            "Health", "Education", "Fashion", "Movies", "Literature"
    };

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        boolean isAdminView = getIntent().getBooleanExtra("IS_ADMIN_VIEW", false);
        if (isAdminView) {
            userId = getIntent().getStringExtra("user_id_admin_view");
        } else {
            if (firebaseUser == null) {
                finish();
                return;
            }
            userId = firebaseUser.getUid();
        }

        initViews();
        setupInterestChips(); // Initialize chips

        if (isAdminView) {
            setupAdminView();
        } else {
            setupViewMode();
        }

        setupClickListeners();
        loadUserProfile();
        setupDeviceId();
    }

    /**
     * Initialize all UI components by finding their views from the layout.
     */
    private void initViews() {
        etDisplayName = findViewById(R.id.et_display_name);
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
        chipGroupInterests = findViewById(R.id.chip_group_interests);
    }

    /**
     * Generates chips with visual state logic (Selected vs Unselected).
     */
    private void setupInterestChips() {
        chipGroupInterests.removeAllViews();

        // Define Color States for Background
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked }, // Selected state
                new int[] { -android.R.attr.state_checked } // Unselected state
        };

        int[] backgroundColors = new int[] {
                Color.parseColor("#0C3B5E"),
                Color.parseColor("#E0E0E0")
        };

        int[] textColors = new int[] {
                Color.WHITE,
                Color.BLACK
        };

        ColorStateList backgroundColorList = new ColorStateList(states, backgroundColors);
        ColorStateList textColorList = new ColorStateList(states, textColors);

        for (String tag : AVAILABLE_TAGS) {
            Chip chip = new Chip(this);
            chip.setText(tag);

            // Enable checkable
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true); // Show checkmark icon
            chip.setCheckedIconTint(ColorStateList.valueOf(Color.WHITE)); // White checkmark

            // Apply Colors
            chip.setChipBackgroundColor(backgroundColorList);
            chip.setTextColor(textColorList);

            // Handle Logic Select / Deselect
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Check limit (5)
                    if (selectedInterests.size() >= 5) {
                        chip.setChecked(false); // Visually uncheck immediately
                        Toast.makeText(this, "Maximum 5 interests allowed", Toast.LENGTH_SHORT).show();
                    } else {
                        // Avoid duplicates if logic misfires
                        if (!selectedInterests.contains(tag)) {
                            selectedInterests.add(tag);
                        }
                    }
                } else {
                    // User tapped a selected chip -> Deselect it
                    selectedInterests.remove(tag);
                }
            });

            chipGroupInterests.addView(chip);
        }
    }

    /**
     * Load the user's profile information from Firestore using the User model.
     */
    private void loadUserProfile() {
        db.collection("accounts").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentUserModel = document.toObject(User.class);
                            if (currentUserModel != null) {
                                populateUI(currentUserModel);
                            }
                        } else {
                            // Handle new user
                            if (firebaseUser != null) {
                                etEmail.setText(firebaseUser.getEmail());
                                currentUserModel = new User();
                                currentUserModel.setUserId(userId);
                            }
                        }
                    }
                });
    }

    /**
     * Populates the UI fields using data from the User model.
     * Updated to correctly handle ChipGroup population.
     * @param user The user object containing profile data.
     */
    private void populateUI(User user) {
        if (user.getName() != null) etDisplayName.setText(user.getName());
        if (user.getFullName() != null) etName.setText(user.getFullName());
        if (user.getEmail() != null) etEmail.setText(user.getEmail());
        if (user.getPhone() != null) etPhone.setText(user.getPhone());

        // Pre-select chips based on database data
        if (user.getInterests() != null) {
            // Clear the tracking list first.
            // trigger the OnCheckedChangeListener, which will add them to the list for us.
            selectedInterests.clear();

            List<String> dbInterests = user.getInterests();

            // Iterate through UI chips
            for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupInterests.getChildAt(i);
                String chipText = chip.getText().toString();

                // If the chip's text exists in the DB list, visually check it.
                if (dbInterests.contains(chipText)) {
                    // This call triggers the OnCheckedChangeListener defined in setupInterestChips
                    chip.setChecked(true);
                }
            }
        }
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

        if (!validateInputs(name, email, phone)) return;

        // CHECK MIN LIMIT (3)
        if (selectedInterests.size() < 3) {
            Toast.makeText(this, "Please select at least 3 interests", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        if (currentUserModel == null) {
            currentUserModel = new User();
            currentUserModel.setUserId(userId);
        }

        currentUserModel.setName(displayName);
        currentUserModel.setFullName(name);
        currentUserModel.setEmail(email);
        currentUserModel.setPhone(TextUtils.isEmpty(phone) ? null : phone);

        // Save interests list to model
        currentUserModel.setInterests(selectedInterests);

        db.collection("accounts").document(userId)
                .set(currentUserModel, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Set up UI for Admin read-only mode.
     */
    private void setupAdminView() {
        etDisplayName.setEnabled(false);
        etName.setEnabled(false);
        etEmail.setEnabled(false);
        etPhone.setEnabled(false);
        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            chipGroupInterests.getChildAt(i).setEnabled(false);
        }
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
        editor.clear().apply();
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
                .setMessage("Are you sure you want to delete your profile?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete the user's profile from both Firestore and Firebase Auth.
     */
    private void deleteProfile() {
        if (firebaseUser == null) return;
        db.collection("accounts").document(userId).delete().addOnCompleteListener(task -> {
            firebaseUser.delete().addOnCompleteListener(t -> {
                performLogout();
            });
        });
    }

    /**
     * Set up the device ID field with a masked Android ID.
     */
    private void setupDeviceId() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.isEmpty()) {
            // Mask the ID only showing the last 4 characters
            String maskedId = "*******" + androidId.substring(Math.max(0, androidId.length() - 4));
            etDeviceId.setText(maskedId);
        } else {
            etDeviceId.setText("*******");
        }
        // Ensure the field is not editable
        etDeviceId.setEnabled(false);
    }

    private boolean validateInputs(String name, String email, String phone) {
        if (TextUtils.isEmpty(name)) return false;
        if (TextUtils.isEmpty(email)) return false;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseUser != null) UserActivityTracker.markUserActive();
    }
}
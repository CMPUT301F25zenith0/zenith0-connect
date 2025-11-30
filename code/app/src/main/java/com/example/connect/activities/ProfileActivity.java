package com.example.connect.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    // Image Upload Variables
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

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
        setupInterestChips();
        setupImagePicker();

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
     * Sets up the launcher to handle the result from the gallery.
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Show the selected image immediately in the UI
                        profileImage.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    /**
     * Opens the gallery to pick an image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Generates chips with visual state logic (Selected vs Unselected).
     * Sets up the launcher to handle the result from the gallery.
     */
    private void setupInterestChips() {
        chipGroupInterests.removeAllViews();
        int[][] states = new int[][] { new int[] { android.R.attr.state_checked }, new int[] { -android.R.attr.state_checked } };
        int[] backgroundColors = new int[] { Color.parseColor("#0C3B5E"), Color.parseColor("#E0E0E0") };
        int[] textColors = new int[] { Color.WHITE, Color.BLACK };
        ColorStateList backgroundColorList = new ColorStateList(states, backgroundColors);
        ColorStateList textColorList = new ColorStateList(states, textColors);

        for (String tag : AVAILABLE_TAGS) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);    // Enable checkable
            chip.setClickable(true);

            chip.setCheckedIconVisible(true);     // Apply Colors
            chip.setCheckedIconTint(ColorStateList.valueOf(Color.WHITE));
            chip.setChipBackgroundColor(backgroundColorList);
            chip.setTextColor(textColorList);

            // Handle Logic Select / Deselect
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedInterests.size() >= 5) {
                        chip.setChecked(false);
                        Toast.makeText(this, "Maximum 5 interests allowed", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!selectedInterests.contains(tag)) selectedInterests.add(tag);
                    }
                } else {
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
     * Populates UI and decodes the Base64 image if it exists.
     */
    private void populateUI(User user) {
        if (user.getName() != null) etDisplayName.setText(user.getName());
        if (user.getFullName() != null) etName.setText(user.getFullName());
        if (user.getEmail() != null) etEmail.setText(user.getEmail());
        if (user.getPhone() != null) etPhone.setText(user.getPhone());

        // Decode and Display Image
        String base64Image = user.getProfileImageUrl();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profileImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding profile image", e);
            }
        }

        // Handle Chips
        if (user.getInterests() != null) {
            selectedInterests.clear();
            List<String> dbInterests = user.getInterests();
            for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupInterests.getChildAt(i);
                if (dbInterests.contains(chip.getText().toString())) {
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
        currentUserModel.setInterests(selectedInterests);

        // Convert and Save Image if a new one was selected
        if (selectedImageUri != null) {
            String base64Image = convertImageToBase64(selectedImageUri);
            if (base64Image != null) {
                currentUserModel.setProfileImageUrl(base64Image);
            }
        }

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
     * Logic extracted from CreateEvent to resize and convert image to Base64.
     * credit to Digaant Chhokra
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Resize image to reduce size (max 800x800) to respect Firestore document limits
            int maxSize = 800;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

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

        // Add listener for the edit image button
        if (edit_image != null) edit_image.setOnClickListener(v -> openImagePicker());
        // Optional: Allow clicking the profile image itself to edit
        if (profileImage != null) profileImage.setOnClickListener(v -> openImagePicker());

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
     * Delete the user's profile from both Firestore and Firebase Auth.
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
     * Delete the user's profile completely from Firebase Auth and Firestore.
     * This method:
     * 1. Deletes all events organized by this user (and their waiting lists)
     * 2. Removes user from all waiting lists they joined
     * 3. Removes user from all event arrays (chosen_entrants, enrolled_users)
     * 4. Deletes user's account document from Firestore
     * 5. Deletes user from Firebase Authentication
     * 
     * Note: Firebase Auth requires recent authentication (within last hour) to delete account.
     */
    private void deleteProfile() {
        if (firebaseUser == null) return;
        
        // Show loading indicator
        Toast.makeText(this, "Deleting account and all related data...", Toast.LENGTH_SHORT).show();
        
        // Step 1: Delete all events organized by this user and their waiting lists
        deleteOrganizedEvents(() -> {
            // Step 2: Remove user from all waiting lists
            removeFromAllWaitingLists(() -> {
                // Step 3: Remove user from event arrays (chosen_entrants, enrolled_users)
                removeFromEventArrays(() -> {
                    // Step 4: Delete from Firestore accounts collection
                    db.collection("accounts").document(userId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Account deleted from Firestore");
                                // Step 5: Delete from Firebase Authentication
                                deleteFromFirebaseAuth();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete from Firestore: " + e.getMessage());
                                // Still try to delete from Auth
                                deleteFromFirebaseAuth();
                                Toast.makeText(this, "Some data may not have been deleted. Please contact support.", Toast.LENGTH_LONG).show();
                            });
                });
            });
        });
    }
    
    /**
     * Delete all events organized by this user and their associated waiting lists.
     */
    private void deleteOrganizedEvents(Runnable onComplete) {
        db.collection("events")
                .whereEqualTo("organizer_id", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No events found for organizer: " + userId);
                        onComplete.run();
                        return;
                    }
                    
                    int totalEvents = querySnapshot.size();
                    final int[] deletedCount = {0};
                    
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        String eventId = eventDoc.getId();
                        
                        // Delete waiting list for this event
                        db.collection("waiting_lists").document(eventId)
                                .collection("entrants")
                                .get()
                                .addOnSuccessListener(entrantsSnapshot -> {
                                    // Delete all entrants from this waiting list
                                    for (QueryDocumentSnapshot entrantDoc : entrantsSnapshot) {
                                        entrantDoc.getReference().delete();
                                    }
                                    
                                    // Delete the waiting list document itself
                                    db.collection("waiting_lists").document(eventId).delete();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting waiting list for event " + eventId + ": " + e.getMessage());
                                });
                        
                        // Delete the event itself
                        eventDoc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    Log.d(TAG, "Deleted event: " + eventId + " (" + deletedCount[0] + "/" + totalEvents + ")");
                                    if (deletedCount[0] == totalEvents) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    deletedCount[0]++;
                                    Log.e(TAG, "Error deleting event " + eventId + ": " + e.getMessage());
                                    if (deletedCount[0] == totalEvents) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching organized events: " + e.getMessage());
                    // Continue with deletion process even if this fails
                    onComplete.run();
                });
    }
    
    /**
     * Remove user from all waiting lists they joined.
     */
    private void removeFromAllWaitingLists(Runnable onComplete) {
        // Get all waiting lists
        db.collection("waiting_lists")
                .get()
                .addOnSuccessListener(waitingListSnapshot -> {
                    if (waitingListSnapshot.isEmpty()) {
                        Log.d(TAG, "No waiting lists found");
                        onComplete.run();
                        return;
                    }
                    
                    int totalWaitingLists = waitingListSnapshot.size();
                    final int[] processedCount = {0};
                    
                    for (QueryDocumentSnapshot waitingListDoc : waitingListSnapshot) {
                        String eventId = waitingListDoc.getId();
                        
                        // Check if user is in the entrants subcollection for this waiting list
                        db.collection("waiting_lists")
                                .document(eventId)
                                .collection("entrants")
                                .whereEqualTo("user_id", userId)
                                .get()
                                .addOnSuccessListener(entrantsSnapshot -> {
                                    // Delete all entrant documents for this user
                                    for (QueryDocumentSnapshot entrantDoc : entrantsSnapshot) {
                                        entrantDoc.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Removed user from waiting list: " + eventId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error removing user from waiting list " + eventId + ": " + e.getMessage());
                                                });
                                    }
                                    
                                    processedCount[0]++;
                                    if (processedCount[0] == totalWaitingLists) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking waiting list " + eventId + ": " + e.getMessage());
                                    processedCount[0]++;
                                    if (processedCount[0] == totalWaitingLists) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching waiting lists: " + e.getMessage());
                    // Continue with deletion process even if this fails
                    onComplete.run();
                });
    }
    
    /**
     * Remove user from event arrays (chosen_entrants, enrolled_users) in all events.
     */
    private void removeFromEventArrays(Runnable onComplete) {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No events found to update");
                        onComplete.run();
                        return;
                    }
                    
                    int totalEvents = querySnapshot.size();
                    final int[] updatedCount = {0};
                    
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        // Check if event has chosen_entrants or enrolled_users arrays
                        boolean needsUpdate = false;
                        
                        if (eventDoc.contains("chosen_entrants")) {
                            List<String> chosenEntrants = (List<String>) eventDoc.get("chosen_entrants");
                            if (chosenEntrants != null && chosenEntrants.contains(userId)) {
                                needsUpdate = true;
                            }
                        }
                        
                        if (eventDoc.contains("enrolled_users")) {
                            List<String> enrolledUsers = (List<String>) eventDoc.get("enrolled_users");
                            if (enrolledUsers != null && enrolledUsers.contains(userId)) {
                                needsUpdate = true;
                            }
                        }
                        
                        if (needsUpdate) {
                            eventDoc.getReference().update(
                                    "chosen_entrants", FieldValue.arrayRemove(userId),
                                    "enrolled_users", FieldValue.arrayRemove(userId)
                            )
                            .addOnSuccessListener(aVoid -> {
                                updatedCount[0]++;
                                Log.d(TAG, "Removed user from event arrays: " + eventDoc.getId());
                                if (updatedCount[0] == totalEvents) {
                                    onComplete.run();
                                }
                            })
                            .addOnFailureListener(e -> {
                                updatedCount[0]++;
                                Log.e(TAG, "Error updating event " + eventDoc.getId() + ": " + e.getMessage());
                                if (updatedCount[0] == totalEvents) {
                                    onComplete.run();
                                }
                            });
                        } else {
                            updatedCount[0]++;
                            if (updatedCount[0] == totalEvents) {
                                onComplete.run();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events for array cleanup: " + e.getMessage());
                    // Continue with deletion process even if this fails
                    onComplete.run();
                });
    }
    
    /**
     * Delete user from Firebase Authentication.
     */
    private void deleteFromFirebaseAuth() {
        firebaseUser.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted from Firebase Authentication");
                    Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                    performLogout();
                })
                .addOnFailureListener(e -> {
                    Exception exception = e;
                    String errorMessage = "Failed to delete account from authentication";
                    if (exception != null && exception.getMessage() != null) {
                        if (exception.getMessage().contains("requires recent authentication")) {
                            errorMessage = "Account deletion requires recent login. Please log out and log back in, then try again.";
                        } else {
                            errorMessage = "Failed to delete account from authentication: " + exception.getMessage();
                        }
                    }
                    Log.e(TAG, "Failed to delete from Firebase Auth: " + exception);
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        });
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
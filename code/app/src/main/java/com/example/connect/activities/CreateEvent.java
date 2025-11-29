package com.example.connect.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for creating and publishing events created in the app.
 * <p>
 * This activity provides a comprehensive event creation interface that allows organizers to:
 * <ul>
 * <li>Set event details (name, description, location, price)</li>
 * <li>Select event categories/labels (Technology, Music, etc.)</li>
 * <li>Upload event images to Firebase Storage with visual previews</li>
 * <li>Configure date/time and registration periods with strict validation</li>
 * <li>Set capacity limits and waiting list parameters</li>
 * <li>Save drafts or publish events with QR code generation</li>
 * <li>Edit existing events</li>
 * </ul>
 * </p>
 * <p>
 * Events are stored in Firebase Firestore under the "events" collection.
 * Images are uploaded to Firebase Storage and their URLs are stored in Firestore.
 * Upon publication, a QR code is generated for easy event sharing and registration.
 * </p>
 *
 * @author Digaant
 * @version 3.1 (Merged)
 */
public class CreateEvent extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    // --- Label / Chips Data ---
    private ChipGroup chipGroupLabels;
    private List<String> selectedLabels = new ArrayList<>();
    private final String[] AVAILABLE_LABELS = {
            "Technology", "Music", "Art", "Sports", "Travel",
            "Food", "Gaming", "Photography", "Science", "Business",
            "Health", "Education", "Fashion", "Movies", "Literature"
    };

    // --- Edit mode ---
    private boolean isEditMode = false;
    private String editEventId = null;

    // --- UI Components ---
    private EditText etEventName, etDescription, etDrawCapacity, etWaitingList, etLocation, etPrice;
    private Button btnBack, btnStartDate, btnStartTime, btnEndDate, btnEndTime;
    private Button btnRegistrationOpens, btnRegistrationCloses, btnSaveDraft, btnPublishQR;
    private ImageView ivEventImage, ivAddImage;

    // --- Date and Time ---
    private Calendar startDateTime, endDateTime, registrationOpens, registrationCloses;
    private SimpleDateFormat dateFormat, timeFormat, dateTimeFormat;

    // --- Image Upload ---
    private Uri selectedImageUri;
    private String existingBase64Image = null; // Store existing image when editing
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // --- Firebase ---
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String organizerName;

    // --- Dialog ---
    private AlertDialog qrDialog;

    /**
     * Initialize the activity, sets up Firebase authentication, and prepares UI.
     * <p>
     * This method verifies user authentication status and redirects to login if necessary.
     * It also fetches the organizer's name from the Firestore accounts collection.
     * </p>
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to create events", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        // Check if in edit mode
        Intent intent = getIntent();
        if (intent.hasExtra("EVENT_ID") && intent.hasExtra("EDIT_MODE")) {
            isEditMode = intent.getBooleanExtra("EDIT_MODE", false);
            editEventId = intent.getStringExtra("EVENT_ID");
        }

        // Fetch organizer name from accounts collection
        fetchOrganizerName();

        // Methods to setup views
        initializeViews();
        initializeDateTimeFormats();
        setupImagePicker();
        setupLabelChips(); // Merged: Setup chips
        setupClickListeners();

        // Load event data if in edit mode
        if (isEditMode && editEventId != null) {
            loadEventForEditing(editEventId);
        }
    }


    /**
     * Fetches the organizer's display name from Firestore.
     * <p>
     * Attempts to retrieve the display_name field, falling back to full_name if not available.
     * If neither exists, defaults to "Organizer".
     * </p>
     */
    private void fetchOrganizerName() {
        db.collection("accounts").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        organizerName = documentSnapshot.getString("display_name");
                        if (organizerName == null || organizerName.isEmpty()) {
                            organizerName = documentSnapshot.getString("full_name");
                        }
                        if (organizerName == null || organizerName.isEmpty()) {
                            organizerName = "Organizer";
                        }
                        Log.d(TAG, "Organizer name: " + organizerName);
                    } else {
                        organizerName = "Organizer";
                        Log.w(TAG, "Account document not found for user: " + currentUserId);
                    }
                })
                .addOnFailureListener(e -> {
                    organizerName = "Organizer";
                    Log.e(TAG, "Error fetching organizer name", e);
                });
    }

    /**
     * Initializes all UI view references from the layout.
     * <p>
     * Connects EditTexts, Buttons, and ImageViews to their corresponding member variables.
     * Also initializes the default placeholder state for the image view.
     * </p>
     */
    private void initializeViews() {
        // EditTexts
        etEventName = findViewById(R.id.etEventName);
        etDescription = findViewById(R.id.etDescription);
        etDrawCapacity = findViewById(R.id.etDrawCapacity);
        etWaitingList = findViewById(R.id.etWaitingList);
        etLocation = findViewById(R.id.etLocation);
        etPrice = findViewById(R.id.etPrice);

        // Buttons
        btnBack = findViewById(R.id.btnBack);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnEndTime = findViewById(R.id.btnEndTime);
        btnRegistrationOpens = findViewById(R.id.btnRegistrationOpens);
        btnRegistrationCloses = findViewById(R.id.btnRegistrationCloses);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnPublishQR = findViewById(R.id.btnPublishQR);

        // ImageViews
        ivEventImage = findViewById(R.id.ivEventImage);
        ivAddImage = findViewById(R.id.ivAddImage);

        // ChipGroup for labels (Merged from Block 1)
        chipGroupLabels = findViewById(R.id.chipGroupLabels);

        // Initial UI State (Merged from Block 2)
        showPlaceholderImage();
    }

    /**
     * Generates chips for event labels with visual state logic (Selected vs Unselected).
     * <p>
     * Dynamically adds chips to the ChipGroup based on the AVAILABLE_LABELS array.
     * Handles selection toggling and visual updates for selected states.
     * </p>
     */
    private void setupLabelChips() {
        if (chipGroupLabels == null) return;

        chipGroupLabels.removeAllViews();

        // Define color states
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        int[] backgroundColors = new int[] {
                Color.parseColor("#0C3B5E"),  // Selected: Dark blue
                Color.parseColor("#E0E0E0")   // Unselected: Light gray
        };
        int[] textColors = new int[] {
                Color.WHITE,  // Selected text
                Color.BLACK   // Unselected text
        };

        ColorStateList backgroundColorList = new ColorStateList(states, backgroundColors);
        ColorStateList textColorList = new ColorStateList(states, textColors);

        for (String label : AVAILABLE_LABELS) {
            Chip chip = new Chip(this);
            chip.setText(label);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Apply visual states
            chip.setCheckedIconVisible(true);
            chip.setCheckedIconTint(ColorStateList.valueOf(Color.WHITE));
            chip.setChipBackgroundColor(backgroundColorList);
            chip.setTextColor(textColorList);

            // Handle selection/deselection
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedLabels.contains(label)) {
                        selectedLabels.add(label);
                    }
                } else {
                    selectedLabels.remove(label);
                }
            });

            chipGroupLabels.addView(chip);
        }
    }

    /**
     * Initializes date and time formatting objects and sets default calendar values.
     * <p>
     * Default event times are set to "Tomorrow" at 10:00 AM.
     * Date format: "MMM d, yyyy"
     * Time format: "hh:mma"
     * DateTime format: "yyyy-MM-dd'T'HH:mm:ss"
     * </p>
     */
    private void initializeDateTimeFormats() {
        dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mma", Locale.getDefault());
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        // Initialize calendars with current date/time + 1 day (Block 1 Logic)
        startDateTime = Calendar.getInstance();
        startDateTime.add(Calendar.DAY_OF_YEAR, 1);
        startDateTime.set(Calendar.HOUR_OF_DAY, 10);
        startDateTime.set(Calendar.MINUTE, 0);
        startDateTime.set(Calendar.SECOND, 0);

        endDateTime = Calendar.getInstance();
        endDateTime.add(Calendar.DAY_OF_YEAR, 1);
        endDateTime.set(Calendar.HOUR_OF_DAY, 12);
        endDateTime.set(Calendar.MINUTE, 0);
        endDateTime.set(Calendar.SECOND, 0);

        registrationOpens = Calendar.getInstance();
        registrationCloses = Calendar.getInstance();

        // Update button text to show default dates
        btnStartDate.setText(dateFormat.format(startDateTime.getTime()));
        btnStartTime.setText(timeFormat.format(startDateTime.getTime()));
        btnEndDate.setText(dateFormat.format(endDateTime.getTime()));
        btnEndTime.setText(timeFormat.format(endDateTime.getTime()));
    }

    /**
     * Sets up the activity result launcher for image selection.
     * <p>
     * Registers a callback that handles the result of the image picker intent,
     * applying a visual preview if successful.
     * </p>
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            applyUriPreview(selectedImageUri); // Block 2 helper
                            existingBase64Image = null; // Clear existing image
                        }
                    }
                }
        );
    }

    // --- Enhanced Image UI Helpers (From Block 2) ---

    /**
     * Applies a Bitmap image to the event image views with appropriate scaling.
     * @param bitmap The bitmap to display
     */
    private void applyBitmapPreview(Bitmap bitmap) {
        if (bitmap == null) {
            showPlaceholderImage();
            return;
        }
        ivEventImage.setImageBitmap(bitmap);
        ivAddImage.setImageBitmap(bitmap);
        ivAddImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAddImage.setPadding(0, 0, 0, 0);
        ivAddImage.clearColorFilter();
    }

    /**
     * Applies a URI image to the event image views with appropriate scaling.
     * @param uri The URI of the image to display
     */
    private void applyUriPreview(Uri uri) {
        if (uri == null) {
            showPlaceholderImage();
            return;
        }
        ivEventImage.setImageURI(uri);
        ivAddImage.setImageURI(uri);
        ivAddImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAddImage.setPadding(0, 0, 0, 0);
        ivAddImage.clearColorFilter();
    }

    /**
     * Resets the image views to show the default placeholder state.
     * Adds padding and color filters to the "Add Image" icon.
     */
    private void showPlaceholderImage() {
        if (ivEventImage == null || ivAddImage == null) return;
        ivEventImage.setImageResource(R.drawable.placeholder_img);
        ivAddImage.setImageResource(android.R.drawable.ic_input_add);
        ivAddImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivAddImage.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        ivAddImage.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue));
    }

    /**
     * Helper to convert density-independent pixels to pixels.
     */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // -----------------------------------------------

    /**
     * Load existing event data for editing.
     * <p>
     * Fetches event details from Firestore, populates text fields, sets date buttons,
     * decodes the Base64 image, and selects the appropriate label chips.
     * </p>
     *
     * @param eventId The ID of the event to edit
     */
    private void loadEventForEditing(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate basic fields
                        etEventName.setText(documentSnapshot.getString("event_title"));
                        etDescription.setText(documentSnapshot.getString("description"));
                        etLocation.setText(documentSnapshot.getString("location"));

                        String price = documentSnapshot.getString("price");
                        etPrice.setText(price != null && !price.equals("0") ? price : "");

                        // Draw capacity and waiting list
                        Long drawCapacity = documentSnapshot.getLong("draw_capacity");
                        if (drawCapacity != null && drawCapacity > 0) {
                            etDrawCapacity.setText(String.valueOf(drawCapacity));
                        }

                        Long waitingList = documentSnapshot.getLong("waiting_list");
                        if (waitingList != null && waitingList > 0) {
                            etWaitingList.setText(String.valueOf(waitingList));
                        }

                        // Parse Date/Time fields
                        parseAndSetDate(documentSnapshot.getString("date_time"), startDateTime, btnStartDate, btnStartTime);
                        parseAndSetDate(documentSnapshot.getString("end_time"), endDateTime, btnEndDate, btnEndTime);
                        parseAndSetRegDate(documentSnapshot.getString("reg_start"), registrationOpens, btnRegistrationOpens);
                        parseAndSetRegDate(documentSnapshot.getString("reg_stop"), registrationCloses, btnRegistrationCloses);

                        // Load Image (Block 2 Logic)
                        String base64Image = documentSnapshot.getString("image_base64");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            existingBase64Image = base64Image;
                            try {
                                byte[] decoded = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                applyBitmapPreview(bmp);
                            } catch (Exception e) {
                                Log.e(TAG, "Error decoding image", e);
                                showPlaceholderImage();
                            }
                        } else {
                            showPlaceholderImage();
                        }

                        // Load Labels (Block 1 Logic)
                        List<String> labels = (List<String>) documentSnapshot.get("labels");
                        if (labels != null && !labels.isEmpty()) {
                            selectedLabels.clear();
                            selectedLabels.addAll(labels);

                            // Check corresponding chips
                            if (chipGroupLabels != null) {
                                for (int i = 0; i < chipGroupLabels.getChildCount(); i++) {
                                    Chip chip = (Chip) chipGroupLabels.getChildAt(i);
                                    if (labels.contains(chip.getText().toString())) {
                                        chip.setChecked(true);
                                    }
                                }
                            }
                        }

                        btnPublishQR.setText("Update Event");
                        btnSaveDraft.setText("Save Changes");
                        Toast.makeText(this, "Loaded event for editing", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Helper to parse date strings and update UI buttons.
     */
    private void parseAndSetDate(String dateStr, Calendar calendar, Button dateBtn, Button timeBtn) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                calendar.setTime(dateTimeFormat.parse(dateStr));
                dateBtn.setText(dateFormat.format(calendar.getTime()));
                timeBtn.setText(timeFormat.format(calendar.getTime()));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date", e);
            }
        }
    }

    /**
     * Helper to parse registration date strings and update UI buttons.
     */
    private void parseAndSetRegDate(String dateStr, Calendar calendar, Button btn) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                calendar.setTime(dateTimeFormat.parse(dateStr));
                String dateTimeText = dateFormat.format(calendar.getTime()) + " " +
                        timeFormat.format(calendar.getTime());
                btn.setText(dateTimeText);
                btn.setTextColor(getResources().getColor(android.R.color.black));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing reg date", e);
            }
        }
    }


    /**
     * Configures click listeners for all interactive UI elements.
     * <p>
     * Sets up handlers for navigation, image selection, date/time pickers,
     * registration period selectors, and save/publish actions.
     * </p>
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivAddImage.setOnClickListener(v -> openImagePicker());
        ivEventImage.setOnClickListener(v -> openImagePicker());

        btnStartDate.setOnClickListener(v -> showDatePicker(startDateTime, btnStartDate));
        btnStartTime.setOnClickListener(v -> showTimePicker(startDateTime, btnStartTime));
        btnEndDate.setOnClickListener(v -> showDatePicker(endDateTime, btnEndDate));
        btnEndTime.setOnClickListener(v -> showTimePicker(endDateTime, btnEndTime));

        btnRegistrationOpens.setOnClickListener(v -> showDateTimePicker(registrationOpens, btnRegistrationOpens));
        btnRegistrationCloses.setOnClickListener(v -> showDateTimePicker(registrationCloses, btnRegistrationCloses));

        btnSaveDraft.setOnClickListener(v -> saveDraft());
        btnPublishQR.setOnClickListener(v -> {
            publishAndGenerateQR();
            updateUserStatus();
        });
    }

    /**
     * Updates the user's status to "organizer" in Firestore upon publishing an event.
     */
    private void updateUserStatus() {
        if (currentUserId == null) return;
        db.collection("accounts").document(currentUserId)
                .update("organizer", true)
                .addOnSuccessListener(aVoid -> Log.d("UpdateStatus", "User set as organizer"))
                .addOnFailureListener(e -> Log.e("UpdateStatus", "Failed to update organizer", e));
    }

    /**
     * Launches the system image picker to select an event image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Displays a date picker dialog and updates the specified calendar and button.
     * Includes strict validation to prevent selecting past dates.
     *
     * @param calendar The Calendar object to update
     * @param button The Button to display the selected date
     */
    private void showDatePicker(Calendar calendar, Button button) {
        // Block 1 Logic: Prevent past dates
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    if (selectedDate.before(minDate)) {
                        Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    button.setText(dateFormat.format(calendar.getTime()));
                    button.setTextColor(getResources().getColor(android.R.color.black));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog and updates the specified calendar and button.
     * Includes checks to prevent selecting past times for the current day.
     *
     * @param calendar The Calendar object to update
     * @param button The Button to display the selected time
     */
    private void showTimePicker(Calendar calendar, Button button) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, hourOfDay, minute) -> {
                    Calendar temp = (Calendar) calendar.clone();
                    temp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    temp.set(Calendar.MINUTE, minute);

                    // Block 1 Logic: Simple check
                    Calendar now = Calendar.getInstance();
                    if (temp.before(now)) {
                        Toast.makeText(this, "Cannot select past time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    button.setText(timeFormat.format(calendar.getTime()));
                    button.setTextColor(getResources().getColor(android.R.color.black));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false  // 12-hour format
        );

        timePickerDialog.show();
    }

    /**
     * Displays combined date and time picker dialogs for registration periods.
     * Shows date picker first, then automatically triggers time picker.
     *
     * @param calendar The Calendar object to update
     * @param button The Button to display the selected date-time
     */
    private void showDateTimePicker(Calendar calendar, Button button) {
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    if (selectedDate.before(minDate)) {
                        Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                // Validate complete datetime
                                Calendar now = Calendar.getInstance();
                                if (calendar.before(now)) {
                                    Toast.makeText(this, "Cannot select past date/time", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String dateTimeText = dateFormat.format(calendar.getTime()) + " " +
                                        timeFormat.format(calendar.getTime());
                                button.setText(dateTimeText);
                                button.setTextColor(getResources().getColor(android.R.color.black));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false  // 12-hour format
                    );

                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Validates all required input fields before saving or publishing.
     * <p>
     * Checks for non-empty event name, description, location.
     * Enforces that end time is after start time and start time is not in the past.
     * Validates that registration windows are logical (close after open).
     * </p>
     *
     * @return true if all validations pass, false otherwise
     */
    private boolean validateInputs() {
        if (etEventName.getText().toString().trim().isEmpty()) {
            etEventName.setError("Event name is required");
            etEventName.requestFocus();
            return false;
        }

        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        // Strict Date Validation (Block 1)
        Calendar now = Calendar.getInstance();
        if (startDateTime.before(now)) {
            Toast.makeText(this, "Event start time cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endDateTime.before(startDateTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate registration period if set
        if (!btnRegistrationOpens.getText().toString().contains("Select") &&
                !btnRegistrationCloses.getText().toString().contains("Select")) {

            if (registrationOpens.before(now)) {
                Toast.makeText(this, "Registration opening time cannot be in the past", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (registrationCloses.before(registrationOpens)) {
                Toast.makeText(this, "Registration closing must be after opening", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    /**
     * Saves the current event as a draft in Firestore.
     * Validates inputs, creates event data with "draft" status, and saves to the "events" collection.
     */
    private void saveDraft() {
        if (!validateInputs()) return;
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        Map<String, Object> eventData = createEventData(true);
        saveOrUpdateEvent(eventData, "Draft saved", "Failed to save draft");
    }

    /**
     * Publishes the event and generates a QR code.
     * Validates inputs, converts image, saves data, and then triggers QR generation.
     */
    private void publishAndGenerateQR() {
        if (!validateInputs()) return;
        Toast.makeText(this, isEditMode ? "Updating event..." : "Publishing event...", Toast.LENGTH_SHORT).show();

        Map<String, Object> eventData = createEventData(false);
        saveOrUpdateEvent(eventData, "Event published", "Failed to publish");
    }

    /**
     * Helper to handle image conversion and Firestore save operations.
     * Updates existing documents in edit mode or adds new ones for new events.
     *
     * @param eventData Map of event fields
     * @param successMsg Toast message for success
     * @param failMsg Toast message prefix for failure
     */
    private void saveOrUpdateEvent(Map<String, Object> eventData, String successMsg, String failMsg) {
        // Handle Image
        if (selectedImageUri != null) {
            String base64Image = convertImageToBase64(selectedImageUri);
            if (base64Image != null) eventData.put("image_base64", base64Image);
        } else if (existingBase64Image != null) {
            eventData.put("image_base64", existingBase64Image);
        }

        if (isEditMode && editEventId != null) {
            db.collection("events").document(editEventId)
                    .set(eventData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        if (!"draft".equals(eventData.get("status"))) {
                            // If publishing, generate QR
                            generateQRAndShow(editEventId);
                        } else {
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, failMsg + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("events")
                    .add(eventData)
                    .addOnSuccessListener(ref -> {
                        String id = ref.getId();
                        if (!"draft".equals(eventData.get("status"))) {
                            generateQRAndShow(id);
                            createWaitingList(id);
                        } else {
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, failMsg + ": " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Generates QR code data, saves it to the event document, and displays the QR dialog.
     *
     * @param eventId The ID of the event
     */
    private void generateQRAndShow(String eventId) {
        String qrData = QRGeneration.generateEventQRCodeData(eventId);
        Map<String, Object> qrUpdate = new HashMap<>();
        qrUpdate.put("qr_code_data", qrData);
        db.collection("events").document(eventId).set(qrUpdate, com.google.firebase.firestore.SetOptions.merge());
        showQRDialog(eventId, qrData);
    }

    /**
     * Converts a Uri to a compressed Base64 string for storage.
     * Resizes large images to max 800px dimension to save bandwidth/storage.
     *
     * @param imageUri The URI of the image to convert
     * @return Base64 string of the compressed image
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            int maxSize = 800;
            float ratio = Math.min((float) maxSize / bitmap.getWidth(), (float) maxSize / bitmap.getHeight());
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * ratio), Math.round(bitmap.getHeight() * ratio), true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            return android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image", e);
            return null;
        }
    }

    /**
     * Creates the waiting list structure in Firestore for a new event.
     * Sets initial capacity and timestamps.
     *
     * @param eventId The ID of the event
     */
    private void createWaitingList(String eventId) {
        Map<String, Object> waitingListData = new HashMap<>();
        waitingListData.put("event_id", eventId);
        waitingListData.put("created_at", FieldValue.serverTimestamp());

        // ✅ Get capacity (null = unlimited, number = limited)
        Integer capacity = getWaitingListCapacity();
        waitingListData.put("total_capacity", capacity);

        db.collection("waiting_lists").document(eventId)
                .set(waitingListData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Waiting list document created for event: " + eventId);
                    Log.d(TAG, "Capacity: " + (capacity == null ? "unlimited" : capacity));
                    Log.d(TAG, "Entrants subcollection will be created when first user joins");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating waiting list", e);
                });
    }

    /**
     * Parses and returns the waiting list capacity from user input.
     * Returns null for unlimited capacity (when field is empty).
     *
     * @return The waiting list capacity as Integer, or null for unlimited
     */
    private Integer getWaitingListCapacity() {
        String waitingList = etWaitingList.getText().toString().trim();
        if (!waitingList.isEmpty()) {
            try {
                String cleanNumber = waitingList.replaceAll("[^0-9]", "");
                if (!cleanNumber.isEmpty()) {
                    int capacity = Integer.parseInt(cleanNumber);
                    return capacity > 0 ? capacity : null;  // Return null if 0 or negative
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing waiting list capacity", e);
            }
        }
        return null;  // null = unlimited capacity
    }

    /**
     * Creates a map of all event data for Firestore storage.
     * Includes labels, calculated capacities, and timestamps.
     *
     * @param isDraft True if saving as draft, false if publishing
     * @return Map of event data
     */
    private Map<String, Object> createEventData(boolean isDraft) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_title", etEventName.getText().toString().trim());
        eventData.put("description", etDescription.getText().toString().trim());
        eventData.put("location", etLocation.getText().toString().trim());
        eventData.put("date_time", dateTimeFormat.format(startDateTime.getTime()));
        eventData.put("end_time", dateTimeFormat.format(endDateTime.getTime()));

        if (!btnRegistrationOpens.getText().toString().contains("Select"))
            eventData.put("reg_start", dateTimeFormat.format(registrationOpens.getTime()));
        else eventData.put("reg_start", "");

        if (!btnRegistrationCloses.getText().toString().contains("Select"))
            eventData.put("reg_stop", dateTimeFormat.format(registrationCloses.getTime()));
        else eventData.put("reg_stop", "");

        // Draw Capacity (Refined logic from Block 2)
        String drawCapStr = etDrawCapacity.getText().toString().trim().replaceAll("[^0-9]", "");
        int drawCap = drawCapStr.isEmpty() ? 0 : Integer.parseInt(drawCapStr);
        eventData.put("draw_capacity", drawCap);
        eventData.put("max_participants", drawCap);
        eventData.put("waiting_list", 0);

        String price = etPrice.getText().toString().trim();
        eventData.put("price", price.isEmpty() ? "0" : price);
        eventData.put("status", isDraft ? "draft" : "published");

        if (!isEditMode) eventData.put("created_at", System.currentTimeMillis());
        eventData.put("updated_at", System.currentTimeMillis());
        eventData.put("organizer_id", currentUserId);
        eventData.put("org_name", organizerName);
        eventData.put("draw_completed", false);
        eventData.put("selected_count", 0);

        // MERGED: Labels from Block 1
        eventData.put("labels", selectedLabels);

        return eventData;
    }

    /**
     * Displays a dialog showing the generated QR code with sharing options.
     *
     * @param eventId The ID of the event
     * @param qrData The data encoded in the QR code
     */
    private void showQRDialog(String eventId, String qrData) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_qr_generated, null);
        ImageView imgQr = dialogView.findViewById(R.id.imgQr);
        MaterialButton btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);
        MaterialButton btnShareQr = dialogView.findViewById(R.id.btnShareQr);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);

        Bitmap qrBitmap = QRGeneration.generateEventQRCode(eventId, 500, 500);
        if (qrBitmap != null) imgQr.setImageBitmap(qrBitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        qrDialog = builder.create();
        if (qrDialog.getWindow() != null) qrDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCopyLink.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Event Link", qrData));
            Toast.makeText(this, "Link copied", Toast.LENGTH_SHORT).show();
        });
        btnShareQr.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code");
            intent.putExtra(Intent.EXTRA_TEXT, "Join: " + qrData);
            startActivity(Intent.createChooser(intent, "Share Event"));
        });
        btnClose.setOnClickListener(v -> {
            qrDialog.dismiss();
            finish();
        });
        qrDialog.show();
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Dismisses any active dialogs to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrDialog != null && qrDialog.isShowing()) qrDialog.dismiss();
    }
}
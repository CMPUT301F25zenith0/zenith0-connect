package com.example.connect.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for creating and publishing events created in the app.
 * <p>
 * This activity provides a comprehensive event creation interface that allows organizers to:
 * <ul>
 *   <li>Set event details (name, description, location, price)</li>
 *   <li>Upload event images to Firebase Storage</li>
 *   <li>Configure date/time and registration periods</li>
 *   <li>Set capacity limits and waiting list parameters</li>
 *   <li>Save drafts or publish events with QR code generation</li>
 *   <li>Edit existing events</li>
 * </ul>
 * </p>
 * <p>
 * Events are stored in Firebase Firestore under the "events" collection.
 * Images are uploaded to Firebase Storage and their URLs are stored in Firestore.
 * Upon publication, a QR code is generated for easy event sharing and registration.
 * </p>
 *
 * @author Digaant
 * @version 3.0
 */
public class CreateEvent extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    // Edit mode
    private boolean isEditMode = false;
    private String editEventId = null;

    // UI Components
    private EditText etEventName, etDescription, etDrawCapacity, etWaitingList, etLocation, etPrice;
    private Button btnBack, btnStartDate, btnStartTime, btnEndDate, btnEndTime;
    private Button btnRegistrationOpens, btnRegistrationCloses, btnSaveDraft, btnPublishQR;
    private ImageView ivEventImage, ivAddImage;

    // Date and Time
    private Calendar startDateTime, endDateTime, registrationOpens, registrationCloses;
    private SimpleDateFormat dateFormat, timeFormat, dateTimeFormat;

    // Image Upload
    private Uri selectedImageUri;
    private String existingBase64Image = null; // Store existing image when editing
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String organizerName;

    // Dialog
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
        setupClickListeners();

        // Load event data if in edit mode
        if (isEditMode && editEventId != null) {
            loadEventForEditing(editEventId);
        }
    }

    /**
     * Load existing event data for editing
     *
     * @param eventId The ID of the event to edit
     */
    private void loadEventForEditing(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate fields with existing data
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

                        // Parse and set date/time fields
                        String dateTimeStr = documentSnapshot.getString("date_time");
                        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
                            try {
                                startDateTime.setTime(dateTimeFormat.parse(dateTimeStr));
                                btnStartDate.setText(dateFormat.format(startDateTime.getTime()));
                                btnStartTime.setText(timeFormat.format(startDateTime.getTime()));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing start date", e);
                            }
                        }

                        String endTimeStr = documentSnapshot.getString("end_time");
                        if (endTimeStr != null && !endTimeStr.isEmpty()) {
                            try {
                                endDateTime.setTime(dateTimeFormat.parse(endTimeStr));
                                btnEndDate.setText(dateFormat.format(endDateTime.getTime()));
                                btnEndTime.setText(timeFormat.format(endDateTime.getTime()));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing end date", e);
                            }
                        }

                        String regStartStr = documentSnapshot.getString("reg_start");
                        if (regStartStr != null && !regStartStr.isEmpty()) {
                            try {
                                registrationOpens.setTime(dateTimeFormat.parse(regStartStr));
                                String dateTimeText = dateFormat.format(registrationOpens.getTime()) + " " +
                                        timeFormat.format(registrationOpens.getTime());
                                btnRegistrationOpens.setText(dateTimeText);
                                btnRegistrationOpens.setTextColor(getResources().getColor(android.R.color.black));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing reg start", e);
                            }
                        }

                        String regStopStr = documentSnapshot.getString("reg_stop");
                        if (regStopStr != null && !regStopStr.isEmpty()) {
                            try {
                                registrationCloses.setTime(dateTimeFormat.parse(regStopStr));
                                String dateTimeText = dateFormat.format(registrationCloses.getTime()) + " " +
                                        timeFormat.format(registrationCloses.getTime());
                                btnRegistrationCloses.setText(dateTimeText);
                                btnRegistrationCloses.setTextColor(getResources().getColor(android.R.color.black));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing reg stop", e);
                            }
                        }

                        // Load image if available
                        String base64Image = documentSnapshot.getString("image_base64");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            existingBase64Image = base64Image; // Store for later use
                            try {
                                byte[] decoded = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                ivEventImage.setImageBitmap(bmp);
                            } catch (Exception e) {
                                Log.e(TAG, "Error decoding image", e);
                            }
                        }

                        // Change button text for edit mode
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
    }

    /**
     * Initializes date and time formatting objects and sets default calendar values.
     * <p>
     * Default event times are set to April 1, 2025, from 10:00 AM to 12:00 PM.
     * Date format: "MMM d, yyyy"
     * Time format: "hh:mma"
     * DateTime format: "yyyy-MM-dd'T'HH:mm:ss"
     * </p>
     */
    private void initializeDateTimeFormats() {
        dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mma", Locale.getDefault());
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        // Initialize calendars with default values
        startDateTime = Calendar.getInstance();
        startDateTime.set(2025, Calendar.APRIL, 1, 10, 0);

        endDateTime = Calendar.getInstance();
        endDateTime.set(2025, Calendar.APRIL, 1, 12, 0);

        registrationOpens = Calendar.getInstance();
        registrationCloses = Calendar.getInstance();
    }

    /**
     * Sets up the activity result launcher for image selection.
     * <p>
     * Registers a callback that handles the result of the image picker intent,
     * updating the event image view when an image is successfully selected.
     * </p>
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivEventImage.setImageURI(selectedImageUri);
                        existingBase64Image = null; // Clear existing image when new one is selected
                    }
                }
        );
    }

    /**
     * Configures click listeners for all interactive UI elements.
     * <p>
     * Sets up handlers for navigation, image selection, date/time pickers,
     * registration period selectors, and save/publish actions.
     * </p>
     */
    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Image upload
        ivAddImage.setOnClickListener(v -> openImagePicker());
        ivEventImage.setOnClickListener(v -> openImagePicker());

        // Start Date and Time
        btnStartDate.setOnClickListener(v -> showDatePicker(startDateTime, btnStartDate, true));
        btnStartTime.setOnClickListener(v -> showTimePicker(startDateTime, btnStartTime, true));

        // End Date and Time
        btnEndDate.setOnClickListener(v -> showDatePicker(endDateTime, btnEndDate, false));
        btnEndTime.setOnClickListener(v -> showTimePicker(endDateTime, btnEndTime, false));

        // Registration Period
        btnRegistrationOpens.setOnClickListener(v -> showDateTimePicker(registrationOpens, btnRegistrationOpens, "Opens"));
        btnRegistrationCloses.setOnClickListener(v -> showDateTimePicker(registrationCloses, btnRegistrationCloses, "Closes"));

        // Bottom Buttons
        btnSaveDraft.setOnClickListener(v -> saveDraft());
        btnPublishQR.setOnClickListener(v -> {
            publishAndGenerateQR();
            updateUserStatus();
        });
    }

    private void updateUserStatus() {

        // Ensure currentUserId is not null
        if (currentUserId == null) {
            Log.e("UpdateStatus", "No current user ID found!");
            return;
        }

        // Update the field "organizer" to true
        db.collection("accounts")
                .document(currentUserId)
                .update("organizer", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UpdateStatus", "User set as organizer");
                })
                .addOnFailureListener(e -> {
                    Log.e("UpdateStatus", "Failed to update organizer", e);
                });
    }


    /**
     * Launches the system image picker to select an event image.
     * <p>
     * Opens the device's media store to allow selection of an image from the gallery.
     * </p>
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Displays a date picker dialog and updates the specified calendar and button.
     *
     * @param calendar The Calendar object to update with the selected date
     * @param button The Button to display the selected date
     * @param isStart True if this is for the event start date, false for end date
     */
    private void showDatePicker(Calendar calendar, Button button, boolean isStart) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    button.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog and updates the specified calendar and button.
     *
     * @param calendar The Calendar object to update with the selected time
     * @param button The Button to display the selected time
     * @param isStart True if this is for the event start time, false for end time
     */
    private void showTimePicker(Calendar calendar, Button button, boolean isStart) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    button.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    /**
     * Displays combined date and time picker dialogs for registration periods.
     * <p>
     * Shows a date picker first, then automatically displays a time picker.
     * The button text is updated with the complete date time string.
     * </p>
     *
     * @param calendar The Calendar object to update
     * @param button The Button to display the selected date-time
     * @param label Label indicating the purpose (e.g., "Opens", "Closes")
     */
    private void showDateTimePicker(Calendar calendar, Button button, String label) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After date is selected, show time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                String dateTimeText = dateFormat.format(calendar.getTime()) + " " +
                                        timeFormat.format(calendar.getTime());
                                button.setText(dateTimeText);
                                button.setTextColor(getResources().getColor(android.R.color.black));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Validates all required input fields before saving or publishing.
     * <p>
     * Checks for non-empty event name, description, location, and valid date range.
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

        if (endDateTime.before(startDateTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Saves the current event as a draft in Firestore or updates existing draft.
     * <p>
     * Validates inputs, creates event data with "draft" status, and saves to the
     * "events" collection. Shows success/failure messages and closes activity on success.
     * </p>
     */
    private void saveDraft() {
        if (!validateInputs()) {
            return;
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        // Create event data map
        Map<String, Object> eventData = createEventData(true);

        // Add existing image if no new image selected
        if (selectedImageUri == null && existingBase64Image != null) {
            eventData.put("image_base64", existingBase64Image);
        } else if (selectedImageUri != null) {
            String base64Image = convertImageToBase64(selectedImageUri);
            if (base64Image != null) {
                eventData.put("image_base64", base64Image);
            }
        }

        if (isEditMode && editEventId != null) {
            // Update existing draft
            db.collection("events").document(editEventId)
                    .set(eventData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Draft updated with ID: " + editEventId);
                        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating draft", e);
                        Toast.makeText(this, "Failed to save changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Save new draft
            db.collection("events")
                    .add(eventData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Draft saved with ID: " + documentReference.getId());
                        Toast.makeText(this, "Draft saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving draft", e);
                        Toast.makeText(this, "Failed to save draft: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Publishes the event and generates a QR code for registration.
     * <p>
     * Converts image to Base64 and stores directly in Firestore if selected.
     * Updates existing event if in edit mode.
     * </p>
     */
    private void publishAndGenerateQR() {
        if (!validateInputs()) {
            return;
        }

        Toast.makeText(this, isEditMode ? "Updating event..." : "Publishing event...", Toast.LENGTH_SHORT).show();

        // Determine which image to use
        String base64Image = null;
        if (selectedImageUri != null) {
            base64Image = convertImageToBase64(selectedImageUri);
        } else if (existingBase64Image != null) {
            base64Image = existingBase64Image;
        }

        publishEventWithBase64Image(base64Image);
    }

    /**
     * Converts the selected image URI to Base64 string.
     *
     * @param imageUri The URI of the selected image
     * @return Base64 encoded string of the image, or null if conversion fails
     */
    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Resize image to reduce size (max 800x800)
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

            return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Publishes the event to Firestore with Base64 image.
     * Updates existing event if in edit mode, creates new event otherwise.
     *
     * @param base64Image The Base64 encoded image string, or null if no image
     */
    private void publishEventWithBase64Image(String base64Image) {
        // Create event data map
        Map<String, Object> eventData = createEventData(false);

        // Add Base64 image if available
        if (base64Image != null) {
            eventData.put("image_base64", base64Image);
        }

        if (isEditMode && editEventId != null) {
            // Update existing event
            db.collection("events").document(editEventId)
                    .set(eventData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Event updated with ID: " + editEventId);
                        Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating event", e);
                        Toast.makeText(this, "Failed to update event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Create new event
            db.collection("events")
                    .add(eventData)
                    .addOnSuccessListener(documentReference -> {
                        String eventId = documentReference.getId();
                        Log.d(TAG, "Event published with ID: " + eventId);

                        // Generate QR code data
                        String qrData = QRGeneration.generateEventQRCodeData(eventId);

                        // Update event document with QR code data
                        Map<String, Object> qrUpdate = new HashMap<>();
                        qrUpdate.put("qr_code_data", qrData);

                        db.collection("events").document(eventId)
                                .set(qrUpdate, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "QR code data saved to event");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving QR code data: " + e.getMessage(), e);
                                });

                        // Create waiting list structure
                        createWaitingList(eventId);

                        // Show QR dialog
                        showQRDialog(eventId, qrData);

                        Toast.makeText(this, "Event published successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error publishing event", e);
                        Toast.makeText(this, "Failed to publish event: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Creates the waiting list structure in Firestore for the published event.
     * Creates a document in "waiting_lists" collection with event metadata.
     * The entrants subcollection will be created automatically when first user joins.
     *
     * @param eventId The unique identifier of the event
     */
    private void createWaitingList(String eventId) {
        Map<String, Object> waitingListData = new HashMap<>();
        waitingListData.put("event_id", eventId);
        waitingListData.put("created_at", FieldValue.serverTimestamp());
        waitingListData.put("total_capacity", getWaitingListCapacity());
        // DON'T add "entries" array - we're using subcollection structure

        db.collection("waiting_lists").document(eventId)
                .set(waitingListData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Waiting list document created for event: " + eventId);
                    Log.d(TAG, "Entrants subcollection will be created when first user joins");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating waiting list", e);
                });
    }

    /**
     * Parses and returns the waiting list capacity from user input.
     * <p>
     * Extracts numeric characters from the input field and converts to an integer.
     * Returns 0 if input is empty or invalid.
     * </p>
     *
     * @return The waiting list capacity as an integer, or 0 if invalid
     */
    private int getWaitingListCapacity() {
        String waitingList = etWaitingList.getText().toString().trim();
        if (!waitingList.isEmpty()) {
            try {
                String cleanNumber = waitingList.replaceAll("[^0-9]", "");
                return cleanNumber.isEmpty() ? 0 : Integer.parseInt(cleanNumber);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Displays a dialog showing the generated QR code with sharing options.
     * <p>
     * The dialog includes QR code image, Copy Link button, Share button, and Close button.
     * </p>
     *
     * @param eventId The unique identifier of the event
     * @param qrData The QR code data string to be shared
     */
    private void showQRDialog(String eventId, String qrData) {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_qr_generated, null);

        // Get views from dialog
        ImageView imgQr = dialogView.findViewById(R.id.imgQr);
        MaterialButton btnCopyLink = dialogView.findViewById(R.id.btnCopyLink);
        MaterialButton btnShareQr = dialogView.findViewById(R.id.btnShareQr);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);

        // Generate and display QR code
        Bitmap qrBitmap = QRGeneration.generateEventQRCode(eventId, 500, 500);
        if (qrBitmap != null) {
            imgQr.setImageBitmap(qrBitmap);
        } else {
            Log.e(TAG, "Error generating QR code");
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        qrDialog = builder.create();

        // Make dialog background transparent
        if (qrDialog.getWindow() != null) {
            qrDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set up button listeners
        btnCopyLink.setOnClickListener(v -> copyLinkToClipboard(qrData));
        btnShareQr.setOnClickListener(v -> shareQRCode(qrData));
        btnClose.setOnClickListener(v -> {
            qrDialog.dismiss();
            finish();
        });

        qrDialog.show();
    }

    /**
     * Copies the event link to the system clipboard.
     * <p>
     * Creates a ClipData object with the QR data and places it on the clipboard.
     * Shows a toast message to confirm the action.
     * </p>
     *
     * @param qrData The event link or QR data to copy
     */
    private void copyLinkToClipboard(String qrData) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Event Link", qrData);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the system share sheet to share the event QR code data.
     * <p>
     * Creates an ACTION_SEND intent with the event information and opens
     * the Android share chooser.
     * </p>
     *
     * @param qrData The event link or QR data to share
     */
    private void shareQRCode(String qrData) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join the event: " + qrData);
        startActivity(Intent.createChooser(shareIntent, "Share Event"));
    }

    /**
     * Creates a comprehensive map of event data for Firestore storage.
     */
    private Map<String, Object> createEventData(boolean isDraft) {
        Map<String, Object> eventData = new HashMap<>();

        // Basic info
        eventData.put("event_title", etEventName.getText().toString().trim());
        eventData.put("description", etDescription.getText().toString().trim());
        eventData.put("location", etLocation.getText().toString().trim());

        // Date and time
        eventData.put("date_time", dateTimeFormat.format(startDateTime.getTime()));
        eventData.put("end_time", dateTimeFormat.format(endDateTime.getTime()));

        // Registration period
        if (btnRegistrationOpens.getText().toString().contains("Select")) {
            eventData.put("reg_start", "");
        } else {
            eventData.put("reg_start", dateTimeFormat.format(registrationOpens.getTime()));
        }

        if (btnRegistrationCloses.getText().toString().contains("Select")) {
            eventData.put("reg_stop", "");
        } else {
            eventData.put("reg_stop", dateTimeFormat.format(registrationCloses.getTime()));
        }

        // ⭐ FIX: Capacity - Parse draw_capacity properly
        String drawCapacityStr = etDrawCapacity.getText().toString().trim();
        int drawCapacity = 0;
        if (!drawCapacityStr.isEmpty()) {
            try {
                // Remove any non-numeric characters and parse
                String cleanNumber = drawCapacityStr.replaceAll("[^0-9]", "");
                if (!cleanNumber.isEmpty()) {
                    drawCapacity = Integer.parseInt(cleanNumber);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing draw capacity", e);
                drawCapacity = 0;
            }
        }

        eventData.put("draw_capacity", drawCapacity);
        Log.d(TAG, "Saving draw_capacity: " + drawCapacity);

        // ⭐ Also set max_participants to same value
        eventData.put("max_participants", drawCapacity);

        // Waiting list (starts at 0, updated as users join)
        eventData.put("waiting_list", 0);

        // Price
        String price = etPrice.getText().toString().trim();
        eventData.put("price", price.isEmpty() ? "0" : price);

        // Status and metadata
        eventData.put("status", isDraft ? "draft" : "published");

        // Only set created_at for new events
        if (!isEditMode) {
            eventData.put("created_at", System.currentTimeMillis());
        }

        // Always update the modified timestamp
        eventData.put("updated_at", System.currentTimeMillis());

        // Organizer info
        eventData.put("organizer_id", currentUserId);
        eventData.put("org_name", organizerName != null ? organizerName : "Organizer");

        // ⭐ ADD: Initialize lottery fields
        eventData.put("draw_completed", false);
        eventData.put("selected_count", 0);

        return eventData;
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * <p>
     * Dismisses the QR dialog if it's currently showing to prevent window leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrDialog != null && qrDialog.isShowing()) {
            qrDialog.dismiss();
        }
    }
}
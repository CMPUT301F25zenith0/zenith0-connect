package com.example.connect.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
 *   <li>Configure date/time and registration periods</li>
 *   <li>Set capacity limits and waiting list parameters</li>
 *   <li>Save drafts or publish events with QR code generation</li>
 * </ul>
 * </p>
 * <p>
 * Events are stored in Firebase Firestore under the "events" collection.
 * Upon publication, a QR code is generated for easy event sharing and registration.
 * </p>
 *
 * @author Digaant
 * @version 1.0
 */
public class CreateEvent extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    // UI Components
    private EditText etEventName, etDescription, etDrawCapacity, etWaitingList, etLocation, etPrice;
    private Button btnBack, btnStartDate, btnStartTime, btnEndDate, btnEndTime;
    private Button btnRegistrationOpens, btnRegistrationCloses, btnSaveDraft, btnPublishQR;
    private ImageView ivEventImage, ivAddImage;
    private SwitchMaterial switchGeolocation;

    // Date and Time
    private Calendar startDateTime, endDateTime, registrationOpens, registrationCloses;
    private SimpleDateFormat dateFormat, timeFormat, dateTimeFormat;

    // Image Upload
    private Uri selectedImageUri;
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
     * It also fetches the organizers name from the Firestore accounts collection.
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

        // Fetch organizer name from accounts collection
        fetchOrganizerName();


        // Methods to setup views
        initializeViews();
        initializeDateTimeFormats();
        setupImagePicker();
        setupClickListeners();
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
     * Connects EditTexts, Buttons, ImageViews, and Switches to their corresponding member variables for later use in the activity.
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

        // Switch
        switchGeolocation = findViewById(R.id.switchGeolocation);
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
     * Registers a callback that handles the result of the image picker intent, updating the event image view when an image is successfully selected.
     * </p>
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivEventImage.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    /**
     * Configures click listeners for all interactive UI elements.
     * <p>
     * Sets up handlers for:
     * <ul>
     *   <li>Navigation (back button)</li>
     *   <li>Image selection</li>
     *   <li>Date and time pickers</li>
     *   <li>Registration period selectors</li>
     *   <li>Save draft and publish actions</li>
     * </ul>
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
        btnPublishQR.setOnClickListener(v -> publishAndGenerateQR());
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
     * The button text is updated with the complete date time string and color is changed to black.
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
     * Checks for:
     * <ul>
     *   <li>Non-empty event name</li>
     *   <li>Non-empty description</li>
     *   <li>Non-empty location</li>
     *   <li>Valid date range (end time after start time)</li>
     * </ul>
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
     * Saves the current event as a draft in Firestore.
     * <p>
     * Validates inputs, creates event data with "draft" status, and saves to the
     * "events" collection. Shows success/failure messages and closes activity on success.
     * </p>
     */
    private void saveDraft() {
        if (!validateInputs()) {
            return;
        }

        // Create event data map
        Map<String, Object> eventData = createEventData(true);

        // Save to Firestore
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

    /**
     * Publishes the event and generates a QR code for registration.
     * <p>
     * Workflow:
     * <ol>
     *   <li>Validates all inputs</li>
     *   <li>Creates event data with "published" status</li>
     *   <li>Saves to Firestore "events" collection</li>
     *   <li>Generates QR code data and saves to event document</li>
     *   <li>Creates waiting list structure</li>
     *   <li>Displays QR code dialog for sharing</li>
     * </ol>
     * </p>
     */
    private void publishAndGenerateQR() {
        if (!validateInputs()) {
            return;
        }

        // Show loading message
        Toast.makeText(this, "Publishing event...", Toast.LENGTH_SHORT).show();

        // Create event data map
        Map<String, Object> eventData = createEventData(false);

        // Save to Firestore
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
                    Toast.makeText(this, "Failed to publish event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Creates the waiting list structure in Firestore for the published event.
     * <p>
     * Creates a document in the "waiting_lists" collection with:
     * <ul>
     *   <li>event_id: Reference to the event</li>
     *   <li>created_at: Timestamp of creation</li>
     *   <li>total_capacity: Maximum waiting list size</li>
     * </ul>
     * </p>
     *
     * @param eventId The unique identifier of the event
     */
    private void createWaitingList(String eventId) {
        // Create a placeholder document in waiting_lists collection
        Map<String, Object> waitingListData = new HashMap<>();
        waitingListData.put("event_id", eventId);
        waitingListData.put("created_at", System.currentTimeMillis());
        waitingListData.put("total_capacity", getWaitingListCapacity());

        db.collection("waiting_lists").document(eventId)
                .set(waitingListData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Waiting list created for event: " + eventId);
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
     * The dialog includes:
     * <ul>
     *   <li>QR code image (500x500 pixels)</li>
     *   <li>Copy Link button - Copies the event link to clipboard</li>
     *   <li>Share button - Opens system share sheet</li>
     *   <li>Close button - Dismisses dialog and closes activity</li>
     * </ul>
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
     * <p>
     * Includes all event information:
     * <ul>
     *   <li>Basic details (title, description, location)</li>
     *   <li>Date/time information (start, end, registration period)</li>
     *   <li>Capacity limits (draw capacity, waiting list)</li>
     *   <li>Price and geolocation requirements</li>
     *   <li>Status (draft or published)</li>
     *   <li>Organizer information (ID and name)</li>
     *   <li>Metadata (creation timestamp, image URI)</li>
     * </ul>
     * </p>
     *
     * @param isDraft True if saving as draft, false if publishing
     * @return Map containing all event data ready for Firestore storage
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

        // Capacity and waiting list
        String drawCapacity = etDrawCapacity.getText().toString().trim();
        if (!drawCapacity.isEmpty()) {
            try {
                eventData.put("draw_capacity", Integer.parseInt(drawCapacity));
            } catch (NumberFormatException e) {
                eventData.put("draw_capacity", 0);
            }
        } else {
            eventData.put("draw_capacity", 0);
        }

        eventData.put("waiting_list", getWaitingListCapacity());

        // Price
        String price = etPrice.getText().toString().trim();
        eventData.put("price", price.isEmpty() ? "0" : price);

        // Geolocation requirement
        eventData.put("require_geolocation", switchGeolocation.isChecked());

        // Status and metadata
        eventData.put("status", isDraft ? "draft" : "published");
        eventData.put("created_at", System.currentTimeMillis());

        // ✅ ADD ORGANIZER INFO
        eventData.put("organizer_id", currentUserId);
        eventData.put("org_name", organizerName != null ? organizerName : "Organizer");

        // Image URI
        if (selectedImageUri != null) {
            eventData.put("image_uri", selectedImageUri.toString());
        }

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
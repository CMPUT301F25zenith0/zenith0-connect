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

        initializeViews();
        initializeDateTimeFormats();
        setupImagePicker();
        setupClickListeners();
    }

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

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
     * Creates the waiting list structure for the event
     * Structure: waiting_lists/{eventId}/entries/
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

    private void copyLinkToClipboard(String qrData) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Event Link", qrData);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareQRCode(String qrData) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join the event: " + qrData);
        startActivity(Intent.createChooser(shareIntent, "Share Event"));
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrDialog != null && qrDialog.isShowing()) {
            qrDialog.dismiss();
        }
    }
}
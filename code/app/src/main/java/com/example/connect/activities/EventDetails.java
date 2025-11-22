package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for displaying detailed information about a specific event.
 * <p>
 * This activity retrieves event data from Firestore database and displays it to the user,
 * including event title, organization name, date & time, location, price, and registration details.
 * Users can join or leave the event's waiting list from this screen.
 * <p>
 * @author Aakansh Chatterjee
 * @version 2.0
 */

public class EventDetails extends AppCompatActivity {

    // UI Components
    private ProgressBar loadingSpinner;
    private ScrollView scrollContent;
    private ImageView btnBack, eventImage;
    private TextView eventTitle, tvOrgName, tvDateTime, tvLocation, tvPrice, tvRegWindow, tvWaitingList;
    private String description;
    private Button btnInfo, btnJoinList, btnLeaveList;

    // Initialize Firebase
    private FirebaseFirestore db;
    private String eventId;

    /**
     * Called when the activity is first created.
     * Initializes the UI, Firestore connection, and loads event details.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the event ID from the Intent
        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");

        // Initialize UI components
        initializeViews();

        // Check if the eventID exists --> otherwise this page does not work
        if (eventId != null) {
            // Use the event ID to fetch event details
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initializes all UI components by finding their references in the layout.
     * Also hides content initially until data is loaded from Firestore.
     */
    private void initializeViews() {
        scrollContent = findViewById(R.id.scroll_content);
        loadingSpinner = findViewById(R.id.spinner);
        btnBack = findViewById(R.id.back_btn);
        eventImage = findViewById(R.id.event_image);
        eventTitle = findViewById(R.id.event_title);
        tvOrgName = findViewById(R.id.tv_org_name);
        tvDateTime = findViewById(R.id.tv_date_time);
        tvLocation = findViewById(R.id.tv_location);
        tvPrice = findViewById(R.id.tv_price);
        tvRegWindow = findViewById(R.id.tv_reg_window);
        tvWaitingList = findViewById(R.id.tv_waiting_list);
        btnInfo = findViewById(R.id.btn_info);
        btnJoinList = findViewById(R.id.btn_join_list);
        btnLeaveList = findViewById(R.id.btn_leave_list);

        // Hide the content till data is replaced
        hideContent();
    }

    /**
     * Sets up click listeners for all interactive UI components.
     * Handles back button, info button, and waiting list join/leave buttons.
     */
    private void setupClickListeners() {
        // Better way to handle back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Info button
        btnInfo.setOnClickListener(v -> {
            showEventInfo();
        });

        // ------TO BE IMPLEMENTED-----
        // Join waiting list button
        btnJoinList.setOnClickListener(v -> {
            joinWaitingList();
        });

        // Leave waiting list button
        btnLeaveList.setOnClickListener(v -> {
            leaveWaitingList();
        });

    }

    /**
     * Loads event details from Firestore using the provided eventId.
     *
     * New DB structure:
     * - Event metadata:     /events/{eventId}
     * - Registrations:      /events/{eventId}/registrations
     *      → waiting users = query where status == "waiting"
     *
     * Logic:
     * 1. Load event metadata from /events/{eventId}.
     * 2. Then query registrations where status="waiting" to count waiting list size.
     * 3. Combine and display all event details.
     */
    private void loadEventDetails(String eventId) {

        // STEP 1 — Load the event metadata
        db.collection("events_N")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventSnap -> {

                    if (!eventSnap.exists()) {
                        Toast.makeText(EventDetails.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Extract event fields
                    String eventName = eventSnap.getString("event_title");
                    String organizationName = eventSnap.getString("org_name");
                    String location = eventSnap.getString("location");
                    description = eventSnap.getString("description");

                    // Format date/time
                    String dateTime = formatDateTime(eventSnap.get("date_time_start"), eventSnap.get("date_time_end"));

                    // Format price
                    Number priceValue = eventSnap.getDouble("price");
                    String price = (priceValue != null && priceValue.doubleValue() > 0)
                            ? "$" + priceValue
                            : "Free";

                    // Format registration window
                    String formattedRegStart = formatRegistrationDate(eventSnap.getString("reg_start"));
                    String formattedRegEnd   = formatRegistrationDate(eventSnap.getString("reg_end"));
                    String registrationWindow = "Registration Window: " + formattedRegStart + " - " + formattedRegEnd;

                    // STEP 2 — Query waiting list count from subcollection
                    db.collection("events_N")
                            .document(eventId)
                            .collection("registrations")
                            .whereEqualTo("status", "waiting")
                            .get()
                            .addOnSuccessListener(waitSnap -> {

                                long waitingCount = waitSnap.size();
                                String waitingListText = "Waiting List Count: " + waitingCount + " Entrants";

                                // STEP 3 — Display all details together
                                displayEventDetails(
                                        eventName,
                                        organizationName,
                                        dateTime,
                                        location,
                                        price,
                                        registrationWindow,
                                        waitingListText
                                );

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EventDetails.this, "Error loading waiting list", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetails.this, "Error loading event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }


    /**
     * Displays event details in the UI by populating all TextViews with loaded data.
     * Also handles transition from loading state to content display.
     * <p>
     * @param eventName The name/title of the event
     * @param organizationName The name of the organizing entity
     * @param dateTime The formatted date and time of the event
     * @param location The location where the event will take place
     * @param price The price to attend the event (or "Free")
     * @param registrationWindow The registration start and end dates
     * @param waitingListCount The current number of people on the waiting list
     */
    private void displayEventDetails(String eventName, String organizationName,
                                     String dateTime, String location, String price,
                                     String registrationWindow, String waitingListCount) {
        eventTitle.setText(eventName != null ? eventName : "Event Title");
        tvOrgName.setText(organizationName != null ? organizationName : "Organization Name");
        tvDateTime.setText(dateTime != null ? dateTime : "Date & Time");
        tvLocation.setText(location != null ? location : "Location");
        tvPrice.setText(price != null ? price : "Price");
        tvRegWindow.setText(registrationWindow);
        tvWaitingList.setText(waitingListCount);

        // Show content and hide spinner
        loadingSpinner.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        showContent();
    }

    /**
     * Formats a date/time object into a readable string format.
     * Handles Date objects, Firestore Timestamp objects, and pre-formatted strings.
     * <p>
     * Format: "hh:mm a, MMM dd, yyyy" (e.g., "05:00 PM, Oct 01, 2025")
     * <p>
     *
     * @param dateTimeObj The date/time object to format (can be Date, Timestamp, or String)
     * @param dateTimeEnd
     * @return A formatted date/time string, or "Date & Time" if the object is null or invalid
     */
    private String formatDateTime(Object dateTimeObj, Object dateTimeEnd) {
        if (dateTimeObj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault());
            return sdf.format((Date) dateTimeObj);

        } else if (dateTimeObj instanceof com.google.firebase.Timestamp) {
            Date date = ((com.google.firebase.Timestamp) dateTimeObj).toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);

        } else if (dateTimeObj instanceof String) {
            return (String) dateTimeObj;
        }
        return "Date & Time";
    }

    /**
     * Formats a registration date string from ISO format to readable format.
     * Converts "2025-11-07T00:00:00" to "Nov 07, 2025"
     *
     * @param dateString The date string in ISO format (yyyy-MM-dd'T'HH:mm:ss)
     * @return A formatted date string (MMM dd, yyyy), or the original string if parsing fails
     */
    private String formatRegistrationDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "TBD";
        }
        try {
            // Parse the ISO format date
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(dateString);

            // Format to readable date
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            // If parsing fails, return the original string
            return dateString;
        }
    }

    /**
     * Adds the current user to the event's registration list.
     *
     * Firestore Structure:
     * - /events/{eventId}/registrations/{userId} → document exists if registered
     * - /events/{eventId}/draw_capacity → max allowed participants
     * - /events/{eventId}/chosen, /accepted, /declined → track status
     */
    private void joinWaitingList() {
        if (eventId == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        DocumentReference eventRef = db.collection("events").document(eventId);
        CollectionReference regRef = eventRef.collection("registrations");

        eventRef.get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;

            Long capacity = eventDoc.getLong("draw_capacity");
            String organizerId = eventDoc.getString("organizer_id");
            if (organizerId != null && organizerId.equals(userId)) return; // organizer cannot join

            regRef.get().addOnSuccessListener(regSnap -> {
                List<String> registeredIds = new ArrayList<>();
                for (DocumentSnapshot doc : regSnap) registeredIds.add(doc.getId());

                if (registeredIds.contains(userId)) {
                    Toast.makeText(this, "Already registered", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (capacity != null && registeredIds.size() >= capacity) {
                    Toast.makeText(this, "Event full", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> registrationData = new HashMap<>();
                registrationData.put("joinedAt", FieldValue.serverTimestamp());

                regRef.document(userId)
                        .set(registrationData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            loadEventDetails(eventId);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show());
            });
        });
    }


    /**
     * Removes the current user from the event's registrations.
     */
    private void leaveWaitingList() {
        if (eventId == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        db.collection("events").document(eventId)
                .collection("registrations")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Left event", Toast.LENGTH_SHORT).show();
                    loadEventDetails(eventId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to leave event", Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays a dialog with the event description.
     * A popup with the event details and a close button.
     */
    private void showEventInfo() {
        // Create dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        // Layout for dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_event_info, null);
        builder.setView(dialogView);

        // Create and show dialog
        android.app.AlertDialog dialog = builder.create();

        // Make background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Get views from layout
        TextView tvDescription = dialogView.findViewById(R.id.tv_event_description);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        // Set description
        tvDescription.setText(description != null && !description.isEmpty() ? description : "No description available");

        // Close button click listener
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    /**
     * Hides all content views and displays the loading spinner.
     * Called when the activity first loads before event data is fetched.
     */
    private void hideContent() {
        loadingSpinner.setVisibility(View.VISIBLE);
        eventImage.setVisibility(View.GONE);
        eventTitle.setVisibility(View.GONE);
        tvOrgName.setVisibility(View.GONE);
        tvDateTime.setVisibility(View.GONE);
        tvLocation.setVisibility(View.GONE);
        tvPrice.setVisibility(View.GONE);
        tvRegWindow.setVisibility(View.GONE);
        tvWaitingList.setVisibility(View.GONE);
        btnInfo.setVisibility(View.GONE);
        btnJoinList.setVisibility(View.GONE);
        btnLeaveList.setVisibility(View.GONE);
    }

    /**
     * Shows all content views and hides the loading spinner.
     * Called after event data has been successfully loaded and displayed.
     */
    private void showContent() {
        loadingSpinner.setVisibility(View.GONE);
        eventImage.setVisibility(View.VISIBLE);
        eventTitle.setVisibility(View.VISIBLE);
        tvOrgName.setVisibility(View.VISIBLE);
        tvDateTime.setVisibility(View.VISIBLE);
        tvLocation.setVisibility(View.VISIBLE);
        tvPrice.setVisibility(View.VISIBLE);
        tvRegWindow.setVisibility(View.VISIBLE);
        tvWaitingList.setVisibility(View.VISIBLE);
        btnInfo.setVisibility(View.VISIBLE);
        btnJoinList.setVisibility(View.VISIBLE);
        btnLeaveList.setVisibility(View.VISIBLE);
    }


}

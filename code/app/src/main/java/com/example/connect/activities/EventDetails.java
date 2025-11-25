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
 * This activity retrieves event data from Firestore database and displays it to
 * the user,
 * including event title, organization name, date & time, location, price, and
 * registration details.
 * Users can join or leave the event's waiting list from this screen.
 * <p>
 * 
 * @author Aakansh Chatterjee
 * @version 2.0
 */

public class EventDetails extends AppCompatActivity {

    // UI Components
    private ProgressBar loadingSpinner;
    private ScrollView scrollContent;
    private ImageView btnBack, eventImage;
    private TextView eventTitle, tvOrgName, tvDateTime, tvLocation, tvPrice, tvRegWindow, tvWaitingList;
    private com.google.firebase.firestore.ListenerRegistration waitlistRegistration;
    private String description;
    private Button btnInfo, btnJoinList, btnLeaveList;
    private boolean isAdminView;

    // Initialize Firebase
    private FirebaseFirestore db;
    private String eventId;

    /**
     * Called when the activity is first created.
     * Initializes the UI, Firestore connection, and loads event details.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved
     *                           state
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdminView = getIntent().getBooleanExtra("IS_ADMIN_VIEW", false);
        setContentView(isAdminView ? R.layout.event_details_admin : R.layout.event_details);

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
        btnJoinList.setOnClickListener(v -> joinWaitingList());

        // Leave waiting list button
        btnLeaveList.setOnClickListener(v -> leaveWaitingList());

    }

    /**
     * Loads event details from Firestore using the provided eventID.
     * Retrieves event information including title, organization, date/time,
     * location,
     * price, registration window, and waiting list count.
     * <p>
     * 
     * @param eventId The unique identifier of the event to load
     */
    private void loadEventDetails(String eventId) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get and save event data
                        String eventName = documentSnapshot.getString("event_title");
                        String organizationName = documentSnapshot.getString("org_name");
                        String location = documentSnapshot.getString("location");
                        description = documentSnapshot.getString("description"); // gets used for the pop later

                        // Get and save date/time
                        Object dateTimeObj = documentSnapshot.get("date_time");
                        String dateTime = formatDateTime(dateTimeObj);

                        // Get and save price
                        Object priceObj = documentSnapshot.get("price");
                        String price = priceObj != null ? "$" + priceObj.toString() : "Free";

                        // Get and save registration window - FORMAT THE DATES HERE
                        String regStartDate = documentSnapshot.getString("reg_start");
                        String regEndDate = documentSnapshot.getString("reg_stop");

                        // Format the registration dates
                        String formattedRegStart = formatRegistrationDate(regStartDate);
                        String formattedRegEnd = formatRegistrationDate(regEndDate);

                        String registrationWindow = "Registration Window: " + formattedRegStart + " - "
                                + formattedRegEnd;

                        // Get and save waiting list count
                        // Display the details
                        displayEventDetails(eventName, organizationName, dateTime, location, price, registrationWindow);
                        listenForWaitlist(eventId);

                        // TODO: Load event image --> need to figure out where to store images Firestore
                        // cannot for us
                        // You can use Glide or Picasso to load images:
                        // String imageUrl = documentSnapshot.getString("imageUrl");
                        // Glide.with(this).load(imageUrl).into(eventImage);
                    } else {
                        Toast.makeText(EventDetails.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetails.this, "Error loading event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Displays event details in the UI by populating all TextViews with loaded
     * data.
     * Also handles transition from loading state to content display.
     * <p>
     * 
     * @param eventName          The name/title of the event
     * @param organizationName   The name of the organizing entity
     * @param dateTime           The formatted date and time of the event
     * @param location           The location where the event will take place
     * @param price              The price to attend the event (or "Free")
     * @param registrationWindow The registration start and end dates
     * @param waitingListCount   The current number of people on the waiting list
     */
    private void displayEventDetails(String eventName, String organizationName,
            String dateTime, String location, String price,
            String registrationWindow) {
        eventTitle.setText(eventName != null ? eventName : "Event Title");
        tvOrgName.setText(organizationName != null ? organizationName : "Organization Name");
        tvDateTime.setText(dateTime != null ? dateTime : "Date & Time");
        tvLocation.setText(location != null ? location : "Location");
        tvPrice.setText(price != null ? price : "Price");
        tvRegWindow.setText(registrationWindow);
        tvWaitingList.setText("Live Waitlist: --");

        // Show content and hide spinner
        loadingSpinner.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
        showContent();
    }

    private void listenForWaitlist(String eventId) {
        if (waitlistRegistration != null) {
            waitlistRegistration.remove();
        }

        waitlistRegistration = db.collection("waiting_lists")
                .document(eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    int count = 0;
                    if (snapshot != null && snapshot.exists()) {
                        java.util.List<String> entries = (java.util.List<String>) snapshot.get("entries");
                        count = entries != null ? entries.size() : 0;
                    }

                    tvWaitingList.setText("Live Waitlist: " + count + " entrant" + (count == 1 ? "" : "s"));
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistRegistration != null) {
            waitlistRegistration.remove();
            waitlistRegistration = null;
        }
    }

    /**
     * Formats a date/time object into a readable string format.
     * Handles Date objects, Firestore Timestamp objects, and pre-formatted strings.
     * <p>
     * Format: "hh:mm a, MMM dd, yyyy" (e.g., "05:00 PM, Oct 01, 2025")
     * <p>
     * 
     * @param dateTimeObj The date/time object to format (can be Date, Timestamp, or
     *                    String)
     * @return A formatted date/time string, or "Date & Time" if the object is null
     *         or invalid
     */
    private String formatDateTime(Object dateTimeObj) {
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
     * @return A formatted date string (MMM dd, yyyy), or the original string if
     *         parsing fails
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
     * Adds the current (logged in) user to the event's waiting list in Firestore.
     * Checks draw_capacity before adding to prevent exceeding the limit.
     * Updates the waiting list count and displays a success message.
     * <p>
     * - Verifying draw capacity hasn't been reached
     * - Adding user ID to waiting list collection
     * - Handling errors and edge cases
     */
    private void joinWaitingList() {
        if (eventId == null)
            return;

        // Get current user ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Edge case should never occur --> If it does, it tells us there is a mix up in
        // the database
        if (userId == null) {
            Toast.makeText(this, "Please sign in to join the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the event's draw_capacity
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    // Checks if the event actually exists --> edge case, if the user was able to
                    // open this detail page, the event exits
                    if (!eventDoc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if user is the organizer --> organizer cannot join their own event as a
                    // user
                    String organizerId = eventDoc.getString("organizer_id");
                    if (organizerId != null && organizerId.equals(userId)) {
                        Toast.makeText(this, "Organizers cannot join their own event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get draw_capacity from event document
                    Long drawCapacity = eventDoc.getLong("draw_capacity");
                    if (drawCapacity == null) {
                        Toast.makeText(this, "Draw capacity not set for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check the waiting list
                    db.collection("waiting_lists")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                // Checks if the waiting list entries exists
                                if (documentSnapshot.exists()) {
                                    // Get current entries
                                    List<String> entries = (List<String>) documentSnapshot.get("entries");
                                    // Get count of entrier directly from waiting list --> no use of outside counter
                                    int currentSize = entries != null ? entries.size() : 0;

                                    // Check if capacity is reached
                                    if (currentSize >= drawCapacity) {
                                        Toast.makeText(this, "Waiting list is full", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // Check if user is already in the waiting list
                                    if (entries != null && entries.contains(userId)) {
                                        Toast.makeText(this, "You're already on the waiting list", Toast.LENGTH_SHORT)
                                                .show();
                                        return;
                                    }

                                    // Event already has a waiting list, add user to entries
                                    db.collection("waiting_lists")
                                            .document(eventId)
                                            .update("entries", FieldValue.arrayUnion(userId))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                                                loadEventDetails(eventId);
                                            })
                                            // Display the error message --> easier when doing UI testing
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error joining waiting list: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Waitlist doesnt exist so create new waiting list document with this user as
                                    // first entry
                                    // First entry, no need to check capcaity
                                    Map<String, Object> waitlistData = new HashMap<>();
                                    List<String> entries = new ArrayList<>();
                                    entries.add(userId);
                                    waitlistData.put("entries", entries);

                                    db.collection("waiting_lists")
                                            .document(eventId)
                                            .set(waitlistData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                                                loadEventDetails(eventId);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error creating waiting list: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error accessing waiting list: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accessing event details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Removes the current user from the event's waiting list in Firestore.
     * Updates the waiting list count and displays a success message.
     * <p>
     * - Removing user ID from waiting list collection
     * - Handling errors and edge cases
     */
    private void leaveWaitingList() {
        if (eventId == null)
            return;

        // Get current user ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Edge Case --> shouldnt happen
        if (userId == null) {
            Toast.makeText(this, "Please sign in to leave the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to waiting list document for this event
        db.collection("waiting_lists")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get current entries
                        List<String> entries = (List<String>) documentSnapshot.get("entries");

                        // Check if user actually in the waiting list
                        if (entries == null || !entries.contains(userId)) {
                            Toast.makeText(this, "You're not on the waiting list", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Remove user from the waiting list
                        db.collection("waiting_lists")
                                .document(eventId)
                                .update("entries", FieldValue.arrayRemove(userId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                    loadEventDetails(eventId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error leaving waiting list: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Waiting list doesn't exist
                        Toast.makeText(this, "Waiting list does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accessing waiting list: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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

        // Make background transparetn
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
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

        // Check if opened from Admin Dashboard
        if (isAdminView) {
            btnJoinList.setVisibility(View.GONE);
            btnLeaveList.setVisibility(View.GONE);
        } else {
            btnJoinList.setVisibility(View.VISIBLE);
            btnLeaveList.setVisibility(View.VISIBLE);
        }
    }

}

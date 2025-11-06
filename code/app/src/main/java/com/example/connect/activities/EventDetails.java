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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying detailed information about a specific event.
 * <p>
 * This activity retrieves event data from Firestore database and displays it to the user,
 * including event title, organization name, date & time, location, price, and registration details.
 * Users can join or leave the event's waiting list from this screen.
 * <p>
 * @author Aakansh Chatterjee
 * @version 1.0
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
        btnBack = findViewById(R.id.btn_back);
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
            // TODO: Show event info dialog in a pop-up or navigate to info screen --> Cannot Leave as toast
            Toast.makeText(EventDetails.this, "Event Info: " + description, Toast.LENGTH_LONG).show();
        });

        // ------TO BE IMPLEMENTED-----
        /*
        // Join waiting list button
        btnJoinList.setOnClickListener(v -> {
            joinWaitingList();
        });

        // Leave waiting list button
        btnLeaveList.setOnClickListener(v -> {
            leaveWaitingList();
        });
         */
    }

    /**
     * Loads event details from Firestore using the provided eventID.
     * Retrieves event information including title, organization, date/time, location,
     * price, registration window, and waiting list count.
     * <p>
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
                        description = documentSnapshot.getString("description");

                        // Get and save date/time
                        Object dateTimeObj = documentSnapshot.get("date_time");
                        String dateTime = formatDateTime(dateTimeObj);

                        // Get and save price
                        Object priceObj = documentSnapshot.get("price");
                        String price = priceObj != null ? "$" + priceObj.toString() : "Free";

                        // Get and save registration window
                        String regStartDate = documentSnapshot.getString("reg_start");
                        String regEndDate = documentSnapshot.getString("reg_stop");
                        String registrationWindow = "Registration Window: " + regStartDate + " - " + regEndDate;

                        // Get and save waiting list count
                        Long waitingListCount = documentSnapshot.getLong("waiting_list");
                        String waitingListText = "Waiting List Count: " +
                                (waitingListCount != null ? waitingListCount : 0) + " Entrants";

                        // Display the details
                        displayEventDetails(eventName, organizationName, dateTime, location, price, registrationWindow, waitingListText);

                        // TODO: Load event image --> need to figure out where to store images Firestore cannot forr us
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

    // At the moment it gets a string --> This can be changed by saving something different in the database
    /**
     * Formats a date/time object into a readable string format.
     * Handles Date objects, Firestore Timestamp objects, and pre-formatted strings.
     * <p>
     * Format: "hh:mm a, MMM dd, yyyy" (e.g., "05:00 PM, Oct 01, 2025")
     *  <p>
     * @param dateTimeObj The date/time object to format (can be Date, Timestamp, or String)
     * @return A formatted date/time string, or "Date & Time" if the object is null or invalid
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
     * Adds the current user to the event's waiting list in Firestore.
     * Updates the waiting list count and displays a success message.
     * <p>
     * TODO: Implement full functionality including:
     * - Adding user ID to waiting list collection
     * - Incrementing waiting list count
     * - Handling errors and edge cases
     */
    private void joinWaitingList() {
        if (eventId == null) return;

        // TODO: Implement join waiting list logic
        // This should:
        // 1. Add current user to the waiting list in Firestore
        // 2. Update the waiting list count
        // 3. Show success message

        Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();

        // Reload event details to update count
        loadEventDetails(eventId);
    }

    /**
     * Removes the current user from the event's waiting list in Firestore.
     * Updates the waiting list count and displays a success message.
     * <p>
     * TODO: Implement full functionality including:
     * - Removing user ID from waiting list collection
     * - Decrementing waiting list count
     * - Handling errors and edge cases
     */
    private void leaveWaitingList() {
        if (eventId == null) return;

        // TODO: Implement leave waiting list logic
        // This should:
        // 1. Remove current user from the waiting list in Firestore
        // 2. Update the waiting list count
        // 3. Show success message

        Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();

        // Reload event details to update count
        loadEventDetails(eventId);
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

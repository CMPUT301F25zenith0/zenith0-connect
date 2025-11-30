package com.example.connect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.bumptech.glide.Glide;
import com.example.connect.models.Event;
import com.example.connect.utils.LocationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Base64;

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
    private ImageView btnBack, eventImage, btnReport;
    private TextView eventTitle, tvOrgName, tvDateTime, tvLocation, tvPrice, tvRegWindow, tvWaitingList;
    private com.google.firebase.firestore.ListenerRegistration waitlistRegistration;
    private String description;
    private Button btnInfo, btnJoinList, btnLeaveList;
    private boolean isAdminView;

    // Initialize Firebase
    private FirebaseFirestore db;
    private String eventId;
    
    // Location permission request (US 02.02.02)
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float GEO_RADIUS_METERS = 20_000f; // Default 20 km
    private String pendingUserIdForLocation = null; // Store userId when waiting for permission
    private Double pendingEventLatitude = null;
    private Double pendingEventLongitude = null;

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
        btnReport = findViewById(R.id.btn_report);
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

        // Report Dialog pop up
        btnReport.setOnClickListener(v -> showReportDialog());

    }

    // Pop up dialog for report
    private void showReportDialog() {
        // Ensure eventId is available before opening the dialog
        if (eventId != null) {
            com.example.connect.fragments.ReportDialogFragment dialog = com.example.connect.fragments.ReportDialogFragment.newInstance(eventId);
            dialog.show(getSupportFragmentManager(), "ReportEventDialogTag");
        } else {
            Toast.makeText(this, "Cannot report, event ID not found.", Toast.LENGTH_SHORT).show();
        }
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

                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String imageBase64 = documentSnapshot.getString("image_base64");
                        loadEventImage(imageUrl, imageBase64);
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
        tvOrgName.setText(organizationName != null ? "By " + organizationName : "Organization Name");
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

    /**
     * Loads the event poster into the header ImageView using either the hosted URL
     * or a base64 encoded fallback.
     */
    private void loadEventImage(String imageUrl, String imageBase64) {
        if (eventImage == null) {
            return;
        }

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(eventImage);
            eventImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return;
        }

        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    eventImage.setImageBitmap(bitmap);
                    eventImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (IllegalArgumentException ignored) {
                // malformed base64, fall through to placeholder
            }
        }

        eventImage.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    /**
     * Sets up a real-time listener for the waiting list count.
     * Counts entrants from the subcollection and updates the UI.
     *
     * @param eventId The unique identifier of the event
     */
    private void listenForWaitlist(String eventId) {
        if (waitlistRegistration != null) {
            waitlistRegistration.remove();
        }

        // ✅ Listen to the entrants subcollection instead of the document
        waitlistRegistration = db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")  // ✅ Count from subcollection
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("EventDetails", "Error listening to waitlist", error);
                        return;
                    }

                    int count = 0;
                    if (querySnapshot != null) {
                        count = querySnapshot.size();  // ✅ Count all entrants
                    }

                    Log.d("EventDetails", "Waitlist count: " + count);
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
            String dateString = ((String) dateTimeObj).trim();
            if (dateString.isEmpty()) {
                return "Date & Time";
            }

            String[] patterns = {
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
            };

            for (String pattern : patterns) {
                try {
                    SimpleDateFormat isoFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                    Date parsedDate = isoFormat.parse(dateString);
                    if (parsedDate != null) {
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                        return displayFormat.format(parsedDate);
                    }
                } catch (Exception ignored) {
                    // Try the next pattern
                }
            }

            return dateString;
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
    /**
     * Adds the current (logged in) user to the event's waiting list in Firestore.
     * Checks total_capacity (waiting list limit) before adding to prevent exceeding the limit.
     * If total_capacity is null or 0, allows unlimited entries.
     * Updates the waiting list count and displays a success message.
     */
    /**
     * Adds the current (logged in) user to the event's waiting list in Firestore.
     * Checks total_capacity (waiting list limit) before adding to prevent exceeding the limit.
     * If total_capacity is null or 0, allows unlimited entries.
     * Updates the waiting list count and displays a success message.
     */
    private void joinWaitingList() {
        if (eventId == null)
            return;

        // Get current user ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "Please sign in to join the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is the organizer first
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if user is the organizer
                    String organizerId = eventDoc.getString("organizer_id");
                    if (organizerId != null && organizerId.equals(userId)) {
                        Toast.makeText(this, "Organizers cannot join their own event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Check waiting list capacity (not draw_capacity)
                    db.collection("waiting_lists")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(waitingListDoc -> {
                                // ✅ Get total_capacity from waiting_lists collection (not events)
                                final Long totalCapacity = waitingListDoc.exists()
                                        ? waitingListDoc.getLong("total_capacity")
                                        : null;

                                // Check if user already in waiting list
                                db.collection("waiting_lists")
                                        .document(eventId)
                                        .collection("entrants")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener(entrantDoc -> {
                                            if (entrantDoc.exists()) {
                                                Toast.makeText(this, "You're already on the waiting list",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // ✅ Count ALL entrants (regardless of status) for capacity check
                                            db.collection("waiting_lists")
                                                    .document(eventId)
                                                    .collection("entrants")
                                                    .get()
                                                    .addOnSuccessListener(querySnapshot -> {
                                                        int currentSize = querySnapshot.size();

                                                        // ✅ ENFORCE: Only check limit if total_capacity is set and > 0
                                                        // null or 0 = unlimited waiting list
                                                        if (totalCapacity != null && totalCapacity > 0
                                                                && currentSize >= totalCapacity) {
                                                            Toast.makeText(this,
                                                                    "Waiting list is full (" + totalCapacity + " entrants)",
                                                                    Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        // All checks passed - add user to waiting list
                                                        addUserToWaitingList(userId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Error checking waiting list: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error checking your status: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error accessing waiting list: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accessing event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Helper method to add user to waiting list subcollection
     */
    /**
     * Helper method to add user to waiting list subcollection.
     * Ensures waiting list document exists before adding entrant.
     * US 02.02.02: Captures location if event requires geolocation.
     */
    private void addUserToWaitingList(String userId) {
        // First check if event requires geolocation and get event data
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    boolean requireGeo = false;
                    Double storedLat = null;
                    Double storedLng = null;
                    String locationText = null;
                    if (eventDoc.exists()) {
                        Boolean requireGeoObj = eventDoc.getBoolean("require_geolocation");
                        requireGeo = requireGeoObj != null && requireGeoObj;
                        storedLat = eventDoc.getDouble("location_latitude");
                        storedLng = eventDoc.getDouble("location_longitude");
                        locationText = eventDoc.getString("location");
                    }

                    final boolean finalRequireGeo = requireGeo;
                    resolveEventCoordinates(storedLat, storedLng, locationText, (resolvedLat, resolvedLng) -> {
                        pendingEventLatitude = resolvedLat;
                        pendingEventLongitude = resolvedLng;

                        if (finalRequireGeo) {
                            if (resolvedLat == null || resolvedLng == null) {
                                Toast.makeText(this, "Unable to verify event location. Please try again later.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            captureLocationAndAddToWaitingList(userId, resolvedLat, resolvedLng);
                        } else {
                            pendingEventLatitude = null;
                            pendingEventLongitude = null;
                            addToWaitingList(userId, null, null);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetails", "Error checking event geolocation requirement", e);
                    // don't; continue, should fail
                    Toast.makeText(this, "Error accessing event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Captures location and then adds user to waiting list
     */
    private void captureLocationAndAddToWaitingList(String userId, Double eventLat, Double eventLng) {
        LocationHelper locationHelper = new LocationHelper(this);
        if (eventLat == null || eventLng == null) {
            Toast.makeText(this, "Event coordinates unavailable. Cannot verify location.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Check if permission is already granted
        if (locationHelper.hasLocationPermission()) {
            // Permission already granted, get location
            locationHelper.getLastLocation((latitude, longitude) -> {
                if (latitude != null && longitude != null) {
                    Log.d("EventDetails", "Location captured: " + latitude + ", " + longitude);
                    if (isWithinGeoRadius(eventLat, eventLng, latitude, longitude)) {
                        addToWaitingList(userId, latitude, longitude);
                    } else {
                        Toast.makeText(this, "You must be within 5 km of the event to join.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Unable to get location. Please enable location services.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Permission not granted, request it
            pendingUserIdForLocation = userId;
            pendingEventLatitude = eventLat;
            pendingEventLongitude = eventLng;
            requestLocationPermission();
        }
    }

    /**
     * Validates whether the user's current coordinates are within the allowed radius of the event.
     */
    private boolean isWithinGeoRadius(Double eventLat, Double eventLng, Double userLat, Double userLng) {
        if (eventLat == null || eventLng == null || userLat == null || userLng == null) {
            return false;
        }
        float[] results = new float[1];
        Location.distanceBetween(eventLat, eventLng, userLat, userLng, results);
        return results[0] <= GEO_RADIUS_METERS;
    }

    /**
     * Attempts to resolve event coordinates using stored values or by geocoding the location text.
     */
    private void resolveEventCoordinates(Double storedLat, Double storedLng, String locationText, CoordinatesCallback callback) {
        if (storedLat != null && storedLng != null) {
            callback.onResult(storedLat, storedLng);
            return;
        }

        if (locationText == null || locationText.trim().isEmpty() || !Geocoder.isPresent()) {
            callback.onResult(null, null);
            return;
        }

        new Thread(() -> {
            Double lat = null;
            Double lng = null;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(locationText, 1);
                if (results != null && !results.isEmpty()) {
                    Address address = results.get(0);
                    lat = address.getLatitude();
                    lng = address.getLongitude();
                }
            } catch (Exception e) {
                Log.e("EventDetails", "Failed to geocode event location", e);
            }

            Double finalLat = lat;
            Double finalLng = lng;
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(finalLat, finalLng));
        }).start();
    }

    private interface CoordinatesCallback {
        void onResult(Double latitude, Double longitude);
    }
    
    /**
     * Request location permission from user
     */
    private void requestLocationPermission() {
        // Check if we should show explanation
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show explanation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Location Permission Required")
                    .setMessage("This event requires location verification. Please allow location access to join the waiting list.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        // Request permission
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Deny", (dialog, which) -> {
                        Toast.makeText(this, "Location permission is required to join this event's waiting list", Toast.LENGTH_LONG).show();
                        pendingUserIdForLocation = null;
                    })
                    .show();
        } else {
            // Request permission directly
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location capture
                if (pendingUserIdForLocation != null) {
                    captureLocationAndAddToWaitingList(pendingUserIdForLocation, pendingEventLatitude, pendingEventLongitude);
                    pendingUserIdForLocation = null;
                    pendingEventLatitude = null;
                    pendingEventLongitude = null;
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied. Cannot join waiting list without location.", Toast.LENGTH_LONG).show();
                pendingUserIdForLocation = null;
                pendingEventLatitude = null;
                pendingEventLongitude = null;
            }
        }
    }
    
    /**
     * Adds user to waiting list with optional location data
     */
    private void addToWaitingList(String userId, Double latitude, Double longitude) {
        // First, get the current waiting list to preserve total_capacity
        db.collection("waiting_lists")
                .document(eventId)
                .get()
                .addOnSuccessListener(waitingListDoc -> {
                    // Preserve existing total_capacity or set to null (unlimited)
                    Object totalCapacity = null;
                    if (waitingListDoc.exists()) {
                        totalCapacity = waitingListDoc.get("total_capacity");
                    }

                    // Ensure the waiting list document exists
                    Map<String, Object> waitingListData = new HashMap<>();
                    waitingListData.put("event_id", eventId);
                    waitingListData.put("created_at", FieldValue.serverTimestamp());
                    waitingListData.put("total_capacity", totalCapacity); // ✅ Preserve capacity

                    db.collection("waiting_lists")
                            .document(eventId)
                            .set(waitingListData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                // Now add user to entrants subcollection
                                Map<String, Object> entrantData = new HashMap<>();
                                entrantData.put("user_id", userId);
                                entrantData.put("status", "waiting");
                                entrantData.put("joined_date", FieldValue.serverTimestamp());
                                
                                // US 02.02.02: Add location data if available
                                if (latitude != null && longitude != null) {
                                    entrantData.put("latitude", latitude);
                                    entrantData.put("longitude", longitude);
                                    entrantData.put("location_captured_at", FieldValue.serverTimestamp());
                                }

                                db.collection("waiting_lists")
                                        .document(eventId)
                                        .collection("entrants")
                                        .document(userId)
                                        .set(entrantData)
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Joined waiting list", Toast.LENGTH_SHORT).show();
                                            loadEventDetails(eventId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error joining: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error creating waiting list: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accessing waiting list: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Removes the current user from the event's waiting list.
     * Deletes the user's document from the entrants subcollection.
     */
    private void leaveWaitingList() {
        if (eventId == null)
            return;

        // Get current user ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "Please sign in to leave the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is in the waiting list
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "You're not on the waiting list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Remove user from waiting list
                    db.collection("waiting_lists")
                            .document(eventId)
                            .collection("entrants")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                loadEventDetails(eventId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error leaving: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking status: " + e.getMessage(),
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

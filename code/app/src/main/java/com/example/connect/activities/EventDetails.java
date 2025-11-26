package com.example.connect.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

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
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2010;
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final double GEO_ALLOWED_RADIUS_KM = 60.0;
    private static final String FIELD_GEO_VERIFICATION_ENABLED = "geo_verification_enabled";
    private static final String FIELD_GEO_LOCATION = "geo_location";
    private static final String FIELD_WAITLIST_COUNT = "wait_list";
    private static final String FIELD_WAITLIST_COUNT_LEGACY = "waiting_list";
    private GeoPoint pendingDeviceLocation;
    private boolean geolocationVerificationEnabled;
    private GeoPoint eventGeoPoint;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        // Join waiting list button with geolocation verification
        btnJoinList.setOnClickListener(v -> initiateJoinFlow());

        // Leave waiting list button
        btnLeaveList.setOnClickListener(v -> leaveWaitingList());

    }

    private void initiateJoinFlow() {
        if (eventId == null) {
            return;
        }

        // Check if geolocation verification is required for this event
        if (geolocationVerificationEnabled && eventGeoPoint != null) {
            // Geolocation verification is required - must get user's location
            if (!hasLocationPermission()) {
                // Request location permission
                ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            // Permission granted, capture location and verify
            captureLocationAndVerify();
        } else {
            // Geolocation verification not required - optional location capture
            if (hasLocationPermission()) {
                captureLocationAndJoin();
            } else {
                // No permission, proceed without location
                pendingDeviceLocation = null;
                joinWaitingList();
            }
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Captures user location and verifies they are within the allowed radius.
     * This is called when geolocation verification is required.
     */
    @SuppressLint("MissingPermission")
    private void captureLocationAndVerify() {
        if (!hasLocationPermission()) {
            Toast.makeText(this, "Location permission is required to join this event", Toast.LENGTH_LONG).show();
            return;
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        // Show loading message
        Toast.makeText(this, "Verifying your location...", Toast.LENGTH_SHORT).show();

        // Try to get current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Successfully captured location, verify distance
                        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        verifyLocationAndJoin(userLocation);
                    } else {
                        // Try last known location as fallback
                        fetchLastKnownLocationForVerification();
                    }
                })
                .addOnFailureListener(e -> {
                    // Location capture failed, try fallback
                    fetchLastKnownLocationForVerification();
                });
    }

    /**
     * Captures user location for optional storage (when geolocation verification is not required).
     */
    @SuppressLint("MissingPermission")
    private void captureLocationAndJoin() {
        if (!hasLocationPermission()) {
            // No permission, proceed without location
            pendingDeviceLocation = null;
            joinWaitingList();
            return;
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        // Try to get current location (optional - don't block if it fails)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Successfully captured location, store it and proceed
                        pendingDeviceLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        joinWaitingList();
                    } else {
                        // Try last known location as fallback
                        fetchLastKnownLocationFallback();
                    }
                })
                .addOnFailureListener(e -> {
                    // Location capture failed, try fallback or proceed without location
                    fetchLastKnownLocationFallback();
                });
    }

    /**
     * Fetches last known location for verification (when geolocation verification is required).
     */
    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocationForVerification() {
        if (!hasLocationPermission() || fusedLocationClient == null) {
            Toast.makeText(this, "Unable to get your location. Please enable location services and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Successfully got last known location, verify distance
                        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        verifyLocationAndJoin(userLocation);
                    } else {
                        // No location available
                        Toast.makeText(this, "Unable to get your location. Please enable location services and try again.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to get your location. Please enable location services and try again.", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Fetches last known location for optional storage (when geolocation verification is not required).
     */
    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocationFallback() {
        if (!hasLocationPermission() || fusedLocationClient == null) {
            // No permission or client not initialized, proceed without location
            pendingDeviceLocation = null;
            joinWaitingList();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Successfully got last known location
                        pendingDeviceLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                    } else {
                        // No location available, proceed without it (optional feature)
                        pendingDeviceLocation = null;
                    }
                    joinWaitingList();
                })
                .addOnFailureListener(e -> {
                    // Location unavailable, proceed without it (optional feature)
                    pendingDeviceLocation = null;
                    joinWaitingList();
                });
    }

    /**
     * Verifies that the user's location is within the allowed radius of the event location.
     * If verified, proceeds to join the waiting list.
     *
     * @param userLocation The user's current location as a GeoPoint
     */
    private void verifyLocationAndJoin(GeoPoint userLocation) {
        if (eventGeoPoint == null) {
            Toast.makeText(this, "Event location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate distance between user and event location
        double distanceKm = calculateDistance(userLocation, eventGeoPoint);

        if (distanceKm <= GEO_ALLOWED_RADIUS_KM) {
            // User is within allowed radius - proceed to join
            pendingDeviceLocation = userLocation;
            joinWaitingList();
        } else {
            // User is outside allowed radius - deny access
            String message = String.format(Locale.getDefault(),
                    "You are %.1f km away from the event location. You must be within %.0f km to join this event.",
                    distanceKm, GEO_ALLOWED_RADIUS_KM);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Calculates the distance between two GeoPoints using the Haversine formula.
     *
     * @param point1 First GeoPoint (user location)
     * @param point2 Second GeoPoint (event location)
     * @return Distance in kilometers
     */
    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        final int EARTH_RADIUS_KM = 6371;

        double lat1 = Math.toRadians(point1.getLatitude());
        double lat2 = Math.toRadians(point2.getLatitude());
        double deltaLat = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double deltaLon = Math.toRadians(point2.getLongitude() - point1.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
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
                        Boolean geoEnabled = documentSnapshot.getBoolean(FIELD_GEO_VERIFICATION_ENABLED);
                        geolocationVerificationEnabled = geoEnabled != null && geoEnabled;
                        eventGeoPoint = documentSnapshot.getGeoPoint(FIELD_GEO_LOCATION);
                        pendingDeviceLocation = null;

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

                        // Display the details (waitlist count will be loaded separately)
                        displayEventDetails(eventName, organizationName, dateTime, location, price, registrationWindow, null);
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
            String registrationWindow, Long waitListCount) {
        eventTitle.setText(eventName != null ? eventName : "Event Title");
        tvOrgName.setText(organizationName != null ? organizationName : "Organization Name");
        tvDateTime.setText(dateTime != null ? dateTime : "Date & Time");
        tvLocation.setText(location != null ? location : "Location");
        tvPrice.setText(price != null ? price : "Price");
        tvRegWindow.setText(registrationWindow);
        if (waitListCount != null) {
            tvWaitingList.setText(formatWaitlistCount(waitListCount.intValue()));
        } else {
            tvWaitingList.setText("Live Waitlist: --");
        }

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
                        List<String> entries = (List<String>) snapshot.get("entries");
                        count = entries != null ? entries.size() : 0;
                    }
                    tvWaitingList.setText(formatWaitlistCount(count));
                });
    }

    private String formatWaitlistCount(int count) {
        return "Live Waitlist: " + count + " entrant" + (count == 1 ? "" : "s");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistRegistration != null) {
            waitlistRegistration.remove();
            waitlistRegistration = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (granted) {
                // Check if geolocation verification is required
                if (geolocationVerificationEnabled && eventGeoPoint != null) {
                    // Geolocation verification required - must verify location
                    captureLocationAndVerify();
                } else {
                    // Optional location capture
                    captureLocationAndJoin();
                }
            } else {
                // Permission denied
                if (geolocationVerificationEnabled && eventGeoPoint != null) {
                    // Geolocation verification is required but permission denied
                    Toast.makeText(this, "Location permission is required to join this event", Toast.LENGTH_LONG).show();
                } else {
                    // Optional location - proceed without it
                    pendingDeviceLocation = null;
                    joinWaitingList();
                }
            }
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
                    if (drawCapacity == null || drawCapacity == 0) {
                        Toast.makeText(this, "Draw capacity not set for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if user already in waiting list by checking the entries array
                    db.collection("waiting_lists")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(waitingListDoc -> {
                                // Check if user is already in entries array
                                List<String> entries = (List<String>) waitingListDoc.get("entries");
                                if (entries != null && entries.contains(userId)) {
                                    Toast.makeText(this, "You're already on the waiting list", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Check current waiting list size using entries array
                                int currentSize = entries != null ? entries.size() : 0;

                                // Check if capacity is reached
                                if (currentSize >= drawCapacity) {
                                    Toast.makeText(this, "Waiting list is full", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Add user to waiting list
                                addUserToWaitingList(userId);
                            })
                            .addOnFailureListener(e -> {
                                String errorMsg = e.getMessage();
                                if (errorMsg != null && errorMsg.contains("offline")) {
                                    Toast.makeText(this, "Please check your internet connection and try again", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Error checking waiting list: " + (errorMsg != null ? errorMsg : "Unknown error"),
                                            Toast.LENGTH_SHORT).show();
                                }
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
    private void addUserToWaitingList(String userId) {
        final GeoPoint locationToPersist = pendingDeviceLocation;
        pendingDeviceLocation = null;

        // First, ensure the waiting list document exists and add user to entries array
        Map<String, Object> waitingListDoc = new HashMap<>();
        waitingListDoc.put("event_id", eventId);
        waitingListDoc.put("created_at", FieldValue.serverTimestamp());
        waitingListDoc.put("total_capacity", 0);
        waitingListDoc.put("entries", FieldValue.arrayUnion(userId));

        db.collection("waiting_lists")
                .document(eventId)
                .set(waitingListDoc, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Now add user to entrants subcollection
                    Map<String, Object> entrantData = new HashMap<>();
                    entrantData.put("user_id", userId);
                    entrantData.put("status", "waiting");
                    entrantData.put("joined_date", FieldValue.serverTimestamp());
                    if (locationToPersist != null) {
                        entrantData.put("device_location", locationToPersist);
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

        // Check if user is in the waiting list by checking the entries array
        db.collection("waiting_lists")
                .document(eventId)
                .get()
                .addOnSuccessListener(waitingListDoc -> {
                    List<String> entries = (List<String>) waitingListDoc.get("entries");
                    if (entries == null || !entries.contains(userId)) {
                        Toast.makeText(this, "You're not on the waiting list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Remove user from entrants subcollection
                    db.collection("waiting_lists")
                            .document(eventId)
                            .collection("entrants")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Remove user from entries array
                                db.collection("waiting_lists")
                                        .document(eventId)
                                        .update("entries", FieldValue.arrayRemove(userId))
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                            loadEventDetails(eventId);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Still show success even if entries update fails
                                            Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                                            loadEventDetails(eventId);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                String errorMsg = e.getMessage();
                                if (errorMsg != null && errorMsg.contains("offline")) {
                                    Toast.makeText(this, "Please check your internet connection and try again", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Error leaving: " + (errorMsg != null ? errorMsg : "Unknown error"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("offline")) {
                        Toast.makeText(this, "Please check your internet connection and try again", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error checking status: " + (errorMsg != null ? errorMsg : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
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

package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.example.connect.models.WaitingListEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity for displaying a map view of where entrants joined the waiting list from.
 * US 02.02.02: As an organizer I want to see on a map where entrants joined my event waiting list from.
 */
public class EntrantMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "EntrantMapActivity";

    // UI Components
    private TextView tvTitle;
    private ImageButton btnBack;
    private TextView tvEntrantsInView;
    private TextView tvWithinZone;
    private TextView tvOutsideZone;

    // Map
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    // Data
    private String eventId;
    private List<WaitingListEntry> entrantsWithLocation = new ArrayList<>();
    private List<WaitingListEntry> allEntrants = new ArrayList<>();
    private Map<Marker, WaitingListEntry> markerToEntrantMap = new HashMap<>();
    private Map<String, com.example.connect.models.User> userCache = new HashMap<>();

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrant_map_view);

        // Get event ID from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        setupClickListeners();

        // Initialize map
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment not found!");
            Toast.makeText(this, "Error: Unable to load map. Please check Google Play Services.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load entrants data
        loadEntrants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload entrants when activity resumes (in case new entrants were added)
        if (eventId != null) {
            loadEntrants();
        }
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        tvEntrantsInView = findViewById(R.id.tvEntrantsInView);
        tvWithinZone = findViewById(R.id.tvWithinZone);
        tvOutsideZone = findViewById(R.id.tvOutsideZone);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Set up marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            WaitingListEntry entry = markerToEntrantMap.get(marker);
            if (entry != null && entry.getUser() != null) {
                String userName = entry.getUser().getName() != null ? entry.getUser().getName() : "Unknown";
                marker.setTitle(userName);
                marker.showInfoWindow();
            }
            return true;
        });

        // Update map with loaded data if already available
        if (!entrantsWithLocation.isEmpty()) {
            updateMapWithMarkers();
        }
    }

    /**
     * Load all entrants from the waiting list and filter those with location data
     * Then batch load all user data
     */
    private void loadEntrants() {
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allEntrants.clear();
                    entrantsWithLocation.clear();

                    Log.d(TAG, "Total documents retrieved: " + querySnapshot.size());

                    Set<String> userIdsToLoad = new HashSet<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        WaitingListEntry entry = document.toObject(WaitingListEntry.class);
                        if (entry != null) {
                            entry.setDocumentId(document.getId());
                            allEntrants.add(entry);

                            // Try to get location from document directly if model parsing fails
                            Double latitude = entry.getLatitude();
                            Double longitude = entry.getLongitude();

                            // Fallback: try reading directly from document
                            if (latitude == null || longitude == null) {
                                Object latObj = document.get("latitude");
                                Object lngObj = document.get("longitude");

                                if (latObj != null && lngObj != null) {
                                    try {
                                        if (latObj instanceof Number) {
                                            latitude = ((Number) latObj).doubleValue();
                                        } else if (latObj instanceof String) {
                                            latitude = Double.parseDouble((String) latObj);
                                        }
                                        if (lngObj instanceof Number) {
                                            longitude = ((Number) lngObj).doubleValue();
                                        } else if (lngObj instanceof String) {
                                            longitude = Double.parseDouble((String) lngObj);
                                        }
                                        entry.setLatitude(latitude);
                                        entry.setLongitude(longitude);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing location for entrant " + document.getId(), e);
                                    }
                                }
                            }

                            // Check if entry has valid location data
                            if (latitude != null && longitude != null &&
                                    !Double.isNaN(latitude) && !Double.isNaN(longitude) &&
                                    latitude != 0.0 && longitude != 0.0) {
                                entrantsWithLocation.add(entry);
                                Log.d(TAG, "Entrant " + document.getId() + " has location: " + latitude + ", " + longitude);

                                // Collect user IDs to batch load
                                if (entry.getUserId() != null && !entry.getUserId().isEmpty()) {
                                    userIdsToLoad.add(entry.getUserId());
                                }
                            } else {
                                Log.d(TAG, "Entrant " + document.getId() + " has no valid location data. Lat: " + latitude + ", Lng: " + longitude);
                            }
                        }
                    }

                    Log.d(TAG, "Loaded " + allEntrants.size() + " total entrants, " +
                            entrantsWithLocation.size() + " with location data");

                    updateStatistics();

                    // Batch load user data
                    if (!userIdsToLoad.isEmpty()) {
                        batchLoadUserData(new ArrayList<>(userIdsToLoad));
                    } else {
                        // No users to load, update map immediately
                        updateMapWithMarkers();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading entrants", e);
                    Toast.makeText(this, "Error loading entrants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Batch load user data for all entrants with location
     * Fixed: Issue #2 - loads all users in batches instead of individual queries
     */
    private void batchLoadUserData(List<String> userIds) {
        if (userIds.isEmpty()) {
            updateMapWithMarkers();
            return;
        }

        // Firestore 'in' queries support up to 10 items at a time
        final int BATCH_SIZE = 10;
        final int totalBatches = (int) Math.ceil(userIds.size() / (double) BATCH_SIZE);
        final int[] completedBatches = {0};

        for (int i = 0; i < userIds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, userIds.size());
            List<String> batch = userIds.subList(i, end);

            db.collection("accounts")
                    .whereIn("__name__", batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            if (document.exists()) {
                                com.example.connect.models.User user =
                                        document.toObject(com.example.connect.models.User.class);
                                if (user != null) {
                                    userCache.put(document.getId(), user);
                                    Log.d(TAG, "Loaded user data for " + user.getName());
                                }
                            }
                        }

                        completedBatches[0]++;

                        // Once all batches are complete, assign users to entries and update map
                        if (completedBatches[0] == totalBatches) {
                            assignUsersToEntries();
                            updateMapWithMarkers();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading batch of user data", e);
                        completedBatches[0]++;

                        // Continue even if a batch fails
                        if (completedBatches[0] == totalBatches) {
                            assignUsersToEntries();
                            updateMapWithMarkers();
                        }
                    });
        }
    }

    /**
     * Assign loaded user data to entrant entries
     */
    private void assignUsersToEntries() {
        for (WaitingListEntry entry : entrantsWithLocation) {
            if (entry.getUserId() != null && userCache.containsKey(entry.getUserId())) {
                entry.setUser(userCache.get(entry.getUserId()));
            }
        }
    }

    /**
     * Update map with markers for all entrants that have location data
     * Fixed: Issue #3 - only called once after all data is loaded
     */
    private void updateMapWithMarkers() {
        if (mMap == null) {
            Log.w(TAG, "Map is not ready yet");
            return;
        }

        // Clear existing markers
        mMap.clear();
        markerToEntrantMap.clear();

        if (entrantsWithLocation.isEmpty()) {
            Log.d(TAG, "No entrants with location data to display");
            Toast.makeText(this, "No entrants with location data to display", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Adding " + entrantsWithLocation.size() + " markers to map");

        // Add markers for each entrant
        com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder =
                new com.google.android.gms.maps.model.LatLngBounds.Builder();
        int markersAdded = 0;

        for (WaitingListEntry entry : entrantsWithLocation) {
            Double latitude = entry.getLatitude();
            Double longitude = entry.getLongitude();

            if (latitude != null && longitude != null &&
                    !Double.isNaN(latitude) && !Double.isNaN(longitude)) {

                LatLng location = new LatLng(latitude, longitude);

                // Create marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location);

                // Set title if user data is available
                if (entry.getUser() != null && entry.getUser().getName() != null) {
                    markerOptions.title(entry.getUser().getName());
                } else {
                    markerOptions.title("Entrant");
                }

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    markerToEntrantMap.put(marker, entry);
                    boundsBuilder.include(location);
                    markersAdded++;
                    Log.d(TAG, "Added marker at: " + latitude + ", " + longitude);
                } else {
                    Log.e(TAG, "Failed to add marker for location: " + latitude + ", " + longitude);
                }
            } else {
                Log.w(TAG, "Skipping entrant with invalid location: lat=" + latitude + ", lng=" + longitude);
            }
        }

        Log.d(TAG, "Total markers added: " + markersAdded);

        // Zoom to show all markers
        if (markersAdded > 0) {
            try {
                if (markersAdded == 1) {
                    // If only one marker, center on it with a reasonable zoom
                    WaitingListEntry first = entrantsWithLocation.get(0);
                    if (first.getLatitude() != null && first.getLongitude() != null) {
                        LatLng center = new LatLng(first.getLatitude(), first.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12));
                        Log.d(TAG, "Centered on single marker at: " + center.latitude + ", " + center.longitude);
                    }
                } else {
                    // Multiple markers - show all in bounds
                    com.google.android.gms.maps.model.LatLngBounds bounds = boundsBuilder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    Log.d(TAG, "Set camera to show all markers");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting camera bounds", e);
                // Fallback: center on first location
                if (!entrantsWithLocation.isEmpty()) {
                    WaitingListEntry first = entrantsWithLocation.get(0);
                    if (first.getLatitude() != null && first.getLongitude() != null) {
                        LatLng center = new LatLng(first.getLatitude(), first.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12));
                    }
                }
            }
        }
    }

    /**
     * Update statistics text views
     */
    private void updateStatistics() {
        int totalWithLocation = entrantsWithLocation.size();
        int totalEntrants = allEntrants.size();
        int withoutLocation = totalEntrants - totalWithLocation;

        tvEntrantsInView.setText("Entrants In View: " + totalWithLocation);
        tvWithinZone.setText("With Location: " + totalWithLocation);
        tvOutsideZone.setText("Without Location: " + withoutLocation);
    }

}
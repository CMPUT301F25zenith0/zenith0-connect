package com.example.connect.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * Helper class for capturing device location.
 * Used for US 02.02.02 - capturing location when joining waiting lists.
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    
    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get the current location (fresh location, not cached)
     * This method requests a fresh location update to ensure accuracy.
     * Falls back to last known location if current location is unavailable.
     */
    public void getLastLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted");
            callback.onLocationResult(null, null);
            return;
        }
        
        // Create a cancellation token for the location request
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        
        // Get current location (fresh location update) with high accuracy priority
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d(TAG, "Fresh location captured: " + latitude + ", " + longitude);
                        callback.onLocationResult(latitude, longitude);
                    } else {
                        // If current location is null, fall back to last known location
                        Log.w(TAG, "Current location is null, trying last known location");
                        getLastKnownLocation(callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting current location: " + e.getMessage() + ", trying last known location");
                    // Fall back to last known location if current location fails
                    getLastKnownLocation(callback);
                });
    }
    
    /**
     * Fallback method to get last known location
     */
    private void getLastKnownLocation(LocationCallback callback) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(lastLocation -> {
                    if (lastLocation != null) {
                        double latitude = lastLocation.getLatitude();
                        double longitude = lastLocation.getLongitude();
                        Log.d(TAG, "Last known location captured: " + latitude + ", " + longitude);
                        callback.onLocationResult(latitude, longitude);
                    } else {
                        Log.w(TAG, "No location available, location services may not be enabled");
                        callback.onLocationResult(null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting last location: " + e.getMessage());
                    callback.onLocationResult(null, null);
                });
    }
    
    public interface LocationCallback {
        void onLocationResult(Double latitude, Double longitude);
    }
}


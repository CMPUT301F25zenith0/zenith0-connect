package com.example.connect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;


public class QRCodeScanner extends AppCompatActivity {

    // For cam permissions
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    // For Specifying location where cam screen is shown and scanning occurs
    private DecoratedBarcodeView barcodeView;
    //
    private boolean isScanning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner); // testing XML --> going to integrate into our UI

        barcodeView = findViewById(R.id.barcode_scanner); // Finding location from XML for where the cam view is to be placed

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning(); // starts the scanning process --> continuous till QR code is found and scanned
        }
    }

    // Scanning process
    private void startScanning() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && isScanning) {
                    isScanning = false; // Prevent multiple scans
                    handleScannedData(result.getText());
                }
            }
        });
    }

    private void handleScannedData(String scannedData) {
        // Parse the QR code data
        // Expected format: "myapp://event/signup?eventId=event_12345"

        // parse the data for our event name
        try {
            Uri uri = Uri.parse(scannedData);

            // Verify it's your app's QR code
            if ("myapp".equals(uri.getScheme()) &&
                    "event".equals(uri.getHost()) &&
                    "/signup".equals(uri.getPath())) {

                String eventId = uri.getQueryParameter("eventId");

                if (eventId != null && !eventId.isEmpty()) {
                    // Navigate to event details/signup activity
                    navigateToEvent(eventId);
                } else {
                    showError("Invalid QR code: No event ID found");
                }
            } else {
                showError("Invalid QR code format");
            }
            // If there an error with parsing, we are told what kind, otherwise an expection is thrown by android studio as seen below
        } catch (Exception e) {
            showError("Failed to parse QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Navigates to the new activity with out event, adding the eventID to the intent --> To be accessed in our EventDetails Activity
    private void navigateToEvent(String eventId) {
        // Navigate to your event details/signup activity
        Intent intent = new Intent(this, EventDetails.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
        finish(); // Close scanner activity
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // Allow scanning again after error
        isScanning = true;
    }

    // Handles the request for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // Camera turns on
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    // Camera Pauses
    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    // Stop the camera completely
    // Called when this activity exited/destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}
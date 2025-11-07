package com.example.connect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Activity responsible for scanning QR codes using the device camera.
 * <p>
 * This activity uses the JourneyApps {@link DecoratedBarcodeView} for live camera scanning.
 * It handles camera permission requests, QR code parsing, and navigation to the
 * corresponding event activity when a valid QR code is detected.
 * </p>
 *  @author Aakansh Chatterjee
 *  @version 1.0
 */
public class QRCodeScanner extends AppCompatActivity {

    // For cam permissions
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    // For Specifying location where cam screen is shown and scanning occurs
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = true;

    private ImageButton backBtn;

    /**
     * Called when the activity is created. Initializes the camera view and handles permission checks.
     *
     * @param savedInstanceState Saved instance state bundle (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_screen);

        barcodeView = findViewById(R.id.barcode_scanner);
        backBtn = findViewById(R.id.back_btn);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning(); // starts the scanning process --> continuous till QR code is found and scanned
        }

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Starts continuous scanning using {@link BarcodeCallback}.
     * Stops scanning after the first successful QR code detection to prevent multiple triggers.
     */
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

    /**
     * Parses and validates scanned QR code data.
     * Expected format: {@code myapp://event/signup?eventId=event_12345}
     *
     * @param scannedData The raw text content of the scanned QR code.
     */
    private void handleScannedData(String scannedData) {
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
            // If there an error with parsing, we are told what kind, OR an exception is thrown by android studio as seen below
        } catch (Exception e) {
            showError("Failed to parse QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Navigates to the event details activity using the provided eventID.
     *
     * @param eventId The unique event identifier extracted from the QR code.
     */
    private void navigateToEvent(String eventId) {
        // Navigate to your event details/signup activity
        Intent intent = new Intent(this, EventDetails.class);
        intent.putExtra("EVENT_ID", eventId);
        startActivity(intent);
        finish(); // Close scanner activity
    }

    /**
     * Displays a Toast message for errors and re-enables scanning.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // Allow scanning again after error
        isScanning = true;
    }

    /**
     * Handles the result of camera permission requests.
     *
     * @param requestCode  The request code identifying the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
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

    /**
     * Resumes the camera preview when the activity becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    /**
     * Pauses the camera preview when the activity is no longer in the foreground.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    /**
     * Stops and releases the camera resources when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }
}
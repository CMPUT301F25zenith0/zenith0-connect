package com.example.connect.activities;

import android.app.Activity; // Required for shadowing onActivityStarted
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager; // Required for permission management

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider; // Required for accessing context
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements; // Required for creating the Shadow
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.Manifest.permission.CAMERA; // Import CAMERA permission constant
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for the QRCodeScanner Activity.
 * <p>
 * These tests use Robolectric to simulate the Android environment
 * and Reflection to access the private methods of QRCodeScanner.
 * </p>
 * <p>
 * Tests:
 * 1. Successful scan with valid event ID (testSuccessfulScan)
 * 2. Invalid URL scheme check (testInvalidScheme)
 * 3. Missing event ID query parameter (testMissingEventId)
 * 4. Malformed/empty QR data handling (testMalformedData_EmptyString)
 * 5. Camera permission denied flow (testPermissionDeniedFlow)
 * 6. Camera permission granted flow (testPermissionGrantedFlow)
 * </p>
 * Working as of 30/11/2025
 * @author Aakansh Chatterjee
 */
@RunWith(AndroidJUnit4.class)
@Config(
        sdk = 34,
        shadows = {QRCodeScannerTest.ShadowConnectApplication.class}
)
@LooperMode(LooperMode.Mode.PAUSED)
public class QRCodeScannerTest {

    private QRCodeScanner activity;
    private Method handleScannedDataMethod;
    private Field isScanningField;
    private static final int CAMERA_PERMISSION_REQUEST = 100; // Constant from QRCodeScanner

    /**
     * Shadow class to prevent the actual ConnectApplication's methods
     * (which crash due to uninitialized Firebase) from executing during testing.
     * The class name must match the fully qualified name of the class being shadowed.
     */
    @Implements(className = "com.example.connect.ConnectApplication")
    public static class ShadowConnectApplication extends ShadowApplication {
        // Shadow the method that is causing the crash (ConnectApplication.onActivityStarted)
        @Implementation
        public void onActivityStarted(Activity activity) {
            // Do nothing, preventing the call to FirebaseAuth.getInstance()
        }
    }


    @Before
    public void setUp() throws Exception {
        // SETUP FOR PERMISSION INTEGRATION TESTS
        // Ensure permission is REVOKED by default so onCreate() triggers the request.

        ShadowApplication shadowApplication = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
        shadowApplication.denyPermissions(CAMERA);

        // Launch ActivityScenario (this will trigger the permission request intent)
        ActivityScenario<QRCodeScanner> scenario = ActivityScenario.launch(QRCodeScanner.class);
        scenario.onActivity(act -> {
            activity = Mockito.spy(act); // Spy the activity to verify internal method calls
        });

        // Set up reflection for private methods/fields
        handleScannedDataMethod = QRCodeScanner.class.getDeclaredMethod("handleScannedData", String.class);
        handleScannedDataMethod.setAccessible(true);

        isScanningField = QRCodeScanner.class.getDeclaredField("isScanning");
        isScanningField.setAccessible(true);
    }

    /**
     * Helper method used by the unit tests (1-4) to simulate the flow of a successful scan.
     * It grants permission and consumes the initial request intent that fires in onCreate.
     */
    private void setupCoreTestEnvironment() {
        ShadowApplication shadowApplication = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
        shadowApplication.grantPermissions(CAMERA);

        // In onCreate(), since the permission is initially denied in setUp(), the activity attempts
        // to request permission first. We must clear this intent before proceeding with our test
        // which focuses on 'handleScannedData'.
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent permissionRequestIntent = shadowActivity.getNextStartedActivity();

        // Assert that the request was made, then consume it
        if (permissionRequestIntent != null) {
            assertEquals("android.content.pm.action.REQUEST_PERMISSIONS", permissionRequestIntent.getAction());
        }
    }

    /**
     * Test Case 1: Successful Scan with Valid Event ID
     * Input: "myapp://event/signup?eventId=test_event_123"
     * Expected: Intent launched for EventDetails with correct extra, and activity finishes.
     */
    @Test
    public void testSuccessfulScan() throws Exception {
        setupCoreTestEnvironment();

        // INput
        String validQrData = "myapp://event/signup?eventId=test_event_123";
        // Simulate isScanning being turned OFF by BarcodeCallback just before calling handleScannedData
        isScanningField.setBoolean(activity, false);

        // Act
        handleScannedDataMethod.invoke(activity, validQrData);

        // Assert
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.peekNextStartedActivity();

        // Verify the correct Intent was launched
        assertEquals(EventDetails.class.getName(), startedIntent.getComponent().getClassName());

        // Verify the correct data was passed
        assertEquals("test_event_123", startedIntent.getStringExtra("EVENT_ID"));

        // Verify the scanner activity was closed
        assertTrue(activity.isFinishing());

        // Verify no error message was shown
        assertEquals(null, ShadowToast.getTextOfLatestToast());
    }

    /**
     * Test Case 2: Invalid Scheme Check
     * Input: "http://event/signup?eventId=test_event_123" --> replace "myapp" with "http"
     * Expected: showError called ("Invalid QR code format") and scanning re-enabled.
     */
    @Test
    public void testInvalidScheme() throws Exception {
        setupCoreTestEnvironment();

        // Arrange
        String invalidSchemeData = "http://event/signup?eventId=test_event_123";
        // Simulate isScanning being turned OFF by BarcodeCallback just before calling handleScannedData
        isScanningField.setBoolean(activity, false);

        // Act
        handleScannedDataMethod.invoke(activity, invalidSchemeData);

        // Assert
        // Verify the correct error message was shown
        String toastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals("Invalid QR code format", toastMessage);

        // Verify scanning was re-enabled (happens inside showError) --> Live Active Scanning till valid QR is found
        assertTrue(isScanningField.getBoolean(activity));

        // Verify no Intent was launched
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertEquals(null, shadowActivity.peekNextStartedActivity());
    }

    /**
     * Test Case 3: Missing eventId Query Parameter
     * Input: "myapp://event/signup?someOtherParam=value"
     * Expected: showError called ("Invalid QR code: No event ID found") and scanning re-enabled.
     */
    @Test
    public void testMissingEventId() throws Exception {
        setupCoreTestEnvironment();

        // Arrange
        String missingEventIdData = "myapp://event/signup?someOtherParam=value";
        // Simulate isScanning being turned OFF by BarcodeCallback just before calling handleScannedData
        isScanningField.setBoolean(activity, false);

        // Act
        handleScannedDataMethod.invoke(activity, missingEventIdData);

        // Assert
        // Verify the correct error message was shown
        String toastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals("Invalid QR code: No event ID found", toastMessage);

        // 2. Verify scanning was re-enabled
        assertTrue(isScanningField.getBoolean(activity));

        // 3. Verify no Intent was launched
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertEquals(null, shadowActivity.peekNextStartedActivity());
    }

    /**
     * Test Case 4: Handling Malformed or Non-URI Data (Empty String)
     * Input: "" (Empty string)
     * Expected: The Exception is caught, showError is called, and scanning re-enabled.
     */
    @Test
    public void testMalformedData_EmptyString() throws Exception {
        setupCoreTestEnvironment();

        // Arrange
        String malformedData = "";
        // Simulate isScanning being turned OFF by BarcodeCallback just before calling handleScannedData
        isScanningField.setBoolean(activity, false);

        // Act
        handleScannedDataMethod.invoke(activity, malformedData);

        // Assert
        // Verify an error message was shown (due to the catch block or failed parsing)
        String toastMessage = ShadowToast.getTextOfLatestToast();
        // Check for either the exception message OR the invalid format message.
        boolean errorToastFound = toastMessage.startsWith("Failed to parse QR code:") || "Invalid QR code format".equals(toastMessage);
        assertTrue("Expected error toast not found. Actual: " + toastMessage, errorToastFound);

        // Verify scanning was re-enabled
        assertTrue(isScanningField.getBoolean(activity));

        // Verify no Intent was launched
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertEquals(null, shadowActivity.peekNextStartedActivity());
    }

    // =========================================================================
    // INTEGRATION TESTS FOR CAMERA PERMISSION FLOW
    // =========================================================================

    /**
     * Integration Test 5: Permission Denied Scenario
     * Scenario: Activity launched, permission requested, user denies permission.
     * Expected: Toast shown, activity finishes.
     * Note: setUp() ensures permission is revoked before launch.
     */
    @Test
    public void testPermissionDeniedFlow() throws Exception {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        // Verify that onCreate() triggered the permission request immediately
        Intent permissionRequestIntent = shadowActivity.getNextStartedActivity();
        // Use string literal for the Intent action
        assertEquals("android.content.pm.action.REQUEST_PERMISSIONS", permissionRequestIntent.getAction());

        // Simulate the user denying the permission
        activity.onRequestPermissionsResult(
                CAMERA_PERMISSION_REQUEST,
                new String[]{CAMERA},
                new int[]{PackageManager.PERMISSION_DENIED}
        );

        // Verify the error message Toast
        String toastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals("Camera permission is required to scan QR codes", toastMessage);

        // Verify the activity closed itself
        assertTrue(activity.isFinishing());

        // Verify scanning state is not enabled (
        assertTrue(isScanningField.getBoolean(activity));
    }

    /**
     * Integration Test 6: Permission Granted Scenario
     * Scenario: Activity launched, permission requested, user grants permission.
     * Expected: startScanning() (which sets isScanning to true) is called, no finish() call.
     * Note: setUp() ensures permission is revoked before launch.
     */
    @Test
    public void testPermissionGrantedFlow() throws Exception {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        // Verify that onCreate() triggered the permission request immediately
        Intent permissionRequestIntent = shadowActivity.getNextStartedActivity();
        assertEquals("android.content.pm.action.REQUEST_PERMISSIONS", permissionRequestIntent.getAction());

        // Simulate the user granting the permission
        activity.onRequestPermissionsResult(
                CAMERA_PERMISSION_REQUEST,
                new String[]{CAMERA},
                new int[]{PackageManager.PERMISSION_GRANTED}
        );

        // Verify scanning was started (by checking the internal flag set by startScanning())
        assertTrue(isScanningField.getBoolean(activity));

        // Verify no error toast was shown
        assertEquals(null, ShadowToast.getTextOfLatestToast());

        // Verify the activity did NOT close itself
        assertFalse(activity.isFinishing());
    }
}
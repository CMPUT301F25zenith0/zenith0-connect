package com.example.connect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.activities.QRGeneration;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for QRGeneration class
 * Tests QR code generation functionality
 * <p>
 * Tests:
 * 1. Default QR code generation (400x400)
 * 2. QR code data string generation
 * 3. URL format validation
 * 4. Long event ID handling
 * 5. Different event IDs produce different data
 * <p>
 * Working as of 11/07/2025
 * @author Digaant Chhokra
 */
@RunWith(AndroidJUnit4.class)
public class QRGenerationTest {

    // Test 1: Default QR code generation (400x400)
    @Test
    public void testGenerateDefaultSizeQRCode() {
        String eventId = "default_test_event";

        Bitmap qrBitmap = QRGeneration.generateEventQRCode(eventId);

        assertNotNull("Default QR code bitmap should not be null", qrBitmap);
        assertEquals("Default width should be 400", 400, qrBitmap.getWidth());
        assertEquals("Default height should be 400", 400, qrBitmap.getHeight());
    }

    // Test 2: QR code data string generation
    @Test
    public void testGenerateEventQRCodeData() {
        String eventId = "data_test_789";
        String expectedUrl = "myapp://event/signup?eventId=" + eventId;

        String qrData = QRGeneration.generateEventQRCodeData(eventId);

        assertNotNull("QR data string should not be null", qrData);
        assertEquals("QR data should match expected URL format", expectedUrl, qrData);
    }

    // Test 3: Verify URL format contains required components
    @Test
    public void testQRDataUrlFormat() {
        String eventId = "format_test_123";

        String qrData = QRGeneration.generateEventQRCodeData(eventId);

        assertTrue("URL should start with myapp://", qrData.startsWith("myapp://"));
        assertTrue("URL should contain event/signup", qrData.contains("event/signup"));
        assertTrue("URL should contain eventId parameter", qrData.contains("eventId="));
        assertTrue("URL should end with the event ID", qrData.endsWith(eventId));
    }

    // Test 4: Long event ID handling
    @Test
    public void testGenerateQRCodeWithLongEventId() {
        String eventId = "very_long_event_id_" + "x".repeat(100); // Very long ID

        Bitmap qrBitmap = QRGeneration.generateEventQRCode(eventId);
        String qrData = QRGeneration.generateEventQRCodeData(eventId);

        assertNotNull("QR code should handle long event IDs", qrBitmap);
        assertNotNull("QR data should not be null for long event ID", qrData);
        assertTrue("QR data should contain the full long event ID", qrData.contains(eventId));
    }

    // Test 5: Different event IDs should produce different QR data
    @Test
    public void testDifferentEventIdsProduceDifferentQRData() {
        String eventId1 = "event_001";
        String eventId2 = "event_002";

        String qrData1 = QRGeneration.generateEventQRCodeData(eventId1);
        String qrData2 = QRGeneration.generateEventQRCodeData(eventId2);

        assertNotNull("QR data 1 should not be null", qrData1);
        assertNotNull("QR data 2 should not be null", qrData2);
        assertTrue("Different event IDs should produce different QR data",
                !qrData1.equals(qrData2));
    }
}
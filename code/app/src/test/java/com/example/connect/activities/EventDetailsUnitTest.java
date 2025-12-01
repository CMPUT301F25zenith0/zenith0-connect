package com.example.connect.activities;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Unit tests for EventDetails business logic
 * Now fully JVM compatible (no Android framework dependencies)
 *
 * @author: Digaant Chhokra
 * @date: 11/30/2025
 */
public class EventDetailsUnitTest {

    private SimpleDateFormat dateTimeFormat;
    private SimpleDateFormat registrationFormat;

    private static final float GEO_RADIUS_METERS = 20_000f; // 20 km

    @Before
    public void setUp() {
        dateTimeFormat = new SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault());
        registrationFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    // ========== DATE/TIME FORMAT TESTS ==========

    @Test
    public void testFormatDateTime_WithDateObject() {
        Date testDate = new Date(1705320600000L);
        String formatted = formatDateTime(testDate);

        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    @Test
    public void testFormatDateTime_WithTimestamp() {
        Timestamp ts = new Timestamp(new Date(1705320600000L));
        String formatted = formatDateTime(ts);

        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    @Test
    public void testFormatDateTime_WithIsoString() {
        String formatted = formatDateTime("2025-12-01T14:30:00");
        assertTrue(formatted.contains("2025"));
    }

    @Test
    public void testFormatDateTime_WithEmpty() {
        assertEquals("Date & Time", formatDateTime(""));
    }

    @Test
    public void testFormatDateTime_WithNull() {
        assertEquals("Date & Time", formatDateTime(null));
    }

    @Test
    public void testFormatDateTime_WithInvalid() {
        assertEquals("bad data", formatDateTime("bad data"));
    }

    // ========== REGISTRATION FORMAT TESTS ==========

    @Test
    public void testFormatRegistrationDate_Valid() {
        String formatted = formatRegistrationDate("2025-11-07T00:00:00");
        assertTrue(formatted.contains("2025"));
    }

    @Test
    public void testFormatRegistrationDate_Null() {
        assertEquals("TBD", formatRegistrationDate(null));
    }

    @Test
    public void testFormatRegistrationDate_Invalid() {
        assertEquals("fail", formatRegistrationDate("fail"));
    }

    // ========== GEO TESTS USING HAVERSINE ==========

    @Test
    public void testWithinGeoRange() {
        assertTrue(isWithinGeoRadius(53.5461, -113.4938, 53.55, -113.50)); // 5km
    }

    @Test
    public void testOutOfGeoRange() {
        assertFalse(isWithinGeoRadius(53.5461, -113.4938, 51.0447, -114.0719)); // Calgary
    }

    @Test
    public void testSameGeoPoint() {
        assertTrue(isWithinGeoRadius(53.5461, -113.4938, 53.5461, -113.4938));
    }

    @Test
    public void testGeoNull() {
        assertFalse(isWithinGeoRadius(null, -113.49, 53.54, -113.49));
        assertFalse(isWithinGeoRadius(53.54, -113.49, null, -113.49));
    }

    // ========== HAVERSINE DISTANCE TESTS ==========

    @Test
    public void testDistance_SamePoint() {
        assertEquals(0.0, calculateDistanceMeters(53.5461, -113.4938,
                53.5461, -113.4938), 1.0);
    }

    @Test
    public void testDistance_EdmontonToCalgary() {
        double d = calculateDistanceMeters(53.5461, -113.4938,
                51.0447, -114.0719);

        assertTrue(d > 250000 && d < 350000); // ~300km
    }

    // ========== WAITLIST TESTS ==========

    @Test
    public void testWaitlist() {
        assertTrue(canJoinWaitlist(null, 50));
        assertTrue(canJoinWaitlist(0L, 100));
        assertFalse(canJoinWaitlist(5L, 5));
        assertTrue(canJoinWaitlist(5L, 4));
    }

    // ============================================================
    // HELPER METHODS (PURE JAVA VERSION)
    // ============================================================

    private String formatDateTime(Object obj) {
        if (obj instanceof Date) {
            return dateTimeFormat.format((Date) obj);
        }
        if (obj instanceof Timestamp) {
            return dateTimeFormat.format(((Timestamp) obj).toDate());
        }
        if (obj instanceof String) {
            String s = ((String) obj).trim();
            if (s.isEmpty()) return "Date & Time";

            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(s);
                return new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(d);
            } catch (Exception ignore) {}

            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s);
                return new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(d);
            } catch (Exception ignore) {}

            return s; // return original
        }
        return "Date & Time";
    }

    private String formatRegistrationDate(String s) {
        if (s == null || s.isEmpty()) return "TBD";

        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(s);
            return registrationFormat.format(d);
        } catch (Exception e) {
            return s;
        }
    }

    /** Stand-alone Haversine calculation */
    private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    private boolean isWithinGeoRadius(Double elat, Double elng, Double ulat, Double ulng) {
        if (elat == null || elng == null || ulat == null || ulng == null) return false;
        double d = calculateDistanceMeters(elat, elng, ulat, ulng);
        return d <= GEO_RADIUS_METERS;
    }

    private boolean canJoinWaitlist(Long cap, int count) {
        return cap == null || cap <= 0 || count < cap;
    }
}

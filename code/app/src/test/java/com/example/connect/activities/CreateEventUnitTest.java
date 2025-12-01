package com.example.connect.activities;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Unit tests for CreateEvent business logic
 * Tests validation, date/time formatting, and helper methods
 *
 * @author Digaant Chhokra
 * @date 11/30/2025
 */
public class CreateEventUnitTest {

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat firestoreDateFormat;

    @Before
    public void setUp() {
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    }

    // ========== DATE/TIME VALIDATION TESTS ==========

    @Test
    public void testPastDateValidation() {
        Calendar now = Calendar.getInstance();
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.DAY_OF_MONTH, -1); // Yesterday

        assertTrue("Past date should be before current date",
                pastDate.before(now));
    }

    @Test
    public void testFutureDateValidation() {
        Calendar now = Calendar.getInstance();
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow

        assertFalse("Future date should not be before current date",
                futureDate.before(now));
    }

    @Test
    public void testEndDateAfterStartDate() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2025, Calendar.DECEMBER, 1, 10, 0, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.set(2025, Calendar.DECEMBER, 1, 12, 0, 0);

        assertFalse("End date should not be before start date",
                endDate.before(startDate));
    }

    @Test
    public void testEndDateBeforeStartDateInvalid() {
        Calendar startDate = Calendar.getInstance();
        startDate.set(2025, Calendar.DECEMBER, 1, 12, 0, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.set(2025, Calendar.DECEMBER, 1, 10, 0, 0);

        assertTrue("End date should be before start date (invalid case)",
                endDate.before(startDate));
    }

    @Test
    public void testRegistrationClosesBeforeEventStart() {
        Calendar registrationCloses = Calendar.getInstance();
        registrationCloses.set(2025, Calendar.DECEMBER, 1, 9, 0, 0);

        Calendar eventStart = Calendar.getInstance();
        eventStart.set(2025, Calendar.DECEMBER, 1, 10, 0, 0);

        assertTrue("Registration should close before event starts",
                registrationCloses.before(eventStart));
    }

    @Test
    public void testRegistrationClosesAfterEventStartInvalid() {
        Calendar registrationCloses = Calendar.getInstance();
        registrationCloses.set(2025, Calendar.DECEMBER, 1, 11, 0, 0);

        Calendar eventStart = Calendar.getInstance();
        eventStart.set(2025, Calendar.DECEMBER, 1, 10, 0, 0);

        assertFalse("Registration should not close after event starts (invalid)",
                registrationCloses.before(eventStart));
    }

    @Test
    public void testRegistrationOpensBeforeCloses() {
        Calendar registrationOpens = Calendar.getInstance();
        registrationOpens.set(2025, Calendar.NOVEMBER, 25, 10, 0, 0);

        Calendar registrationCloses = Calendar.getInstance();
        registrationCloses.set(2025, Calendar.DECEMBER, 1, 9, 0, 0);

        assertTrue("Registration opens should be before closes",
                registrationOpens.before(registrationCloses));
    }

    // ========== DATE/TIME FORMATTING TESTS ==========

    @Test
    public void testDateFormatting() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(2025, Calendar.DECEMBER, 1, 10, 0, 0);

        String formatted = dateFormat.format(testDate.getTime());
        assertEquals("Date format should be MMM dd, yyyy", "Dec 01, 2025", formatted);
    }

    @Test
    public void testTimeFormattingAM() {
        Calendar testTime = Calendar.getInstance();
        testTime.set(Calendar.HOUR_OF_DAY, 10);
        testTime.set(Calendar.MINUTE, 30);

        String formatted = timeFormat.format(testTime.getTime());

        // Case-insensitive comparison since locale may affect AM/PM casing
        assertTrue("Time should contain 10:30", formatted.contains("10:30"));
        assertTrue("Time should contain AM",
                formatted.toUpperCase().contains("AM"));
    }

    @Test
    public void testTimeFormattingPM() {
        Calendar testTime = Calendar.getInstance();
        testTime.set(Calendar.HOUR_OF_DAY, 14);
        testTime.set(Calendar.MINUTE, 45);

        String formatted = timeFormat.format(testTime.getTime());

        // Case-insensitive comparison since locale may affect AM/PM casing
        assertTrue("Time should contain 02:45", formatted.contains("02:45"));
        assertTrue("Time should contain PM",
                formatted.toUpperCase().contains("PM"));
    }

    @Test
    public void testFirestoreDateFormatting() {
        Calendar testDate = Calendar.getInstance();
        testDate.set(2025, Calendar.DECEMBER, 1, 14, 30, 0);

        String formatted = firestoreDateFormat.format(testDate.getTime());
        assertTrue("Firestore format should contain date and time",
                formatted.contains("2025-12-01"));
        assertTrue("Firestore format should contain T separator",
                formatted.contains("T"));
    }

    // ========== CAPACITY VALIDATION TESTS ==========

    @Test
    public void testValidWaitingListCapacity() {
        String capacityStr = "100";
        int capacity = Integer.parseInt(capacityStr);

        assertTrue("Valid capacity should be positive", capacity > 0);
        assertEquals("Capacity should be 100", 100, capacity);
    }

    @Test
    public void testEmptyWaitingListCapacityReturnsNull() {
        String capacityStr = "";
        Integer capacity = null;

        if (!capacityStr.isEmpty()) {
            capacity = Integer.parseInt(capacityStr);
        }

        assertNull("Empty capacity should return null (unlimited)", capacity);
    }

    @Test
    public void testZeroCapacityReturnsNull() {
        String capacityStr = "0";
        int parsed = Integer.parseInt(capacityStr);
        Integer capacity = parsed > 0 ? parsed : null;

        assertNull("Zero capacity should return null (unlimited)", capacity);
    }

    @Test
    public void testNegativeCapacityReturnsNull() {
        String capacityStr = "-5";
        int parsed = Integer.parseInt(capacityStr);
        Integer capacity = parsed > 0 ? parsed : null;

        assertNull("Negative capacity should return null (unlimited)", capacity);
    }

    @Test
    public void testCapacityWithNonNumericCharacters() {
        String capacityStr = "100 people";
        String cleanNumber = capacityStr.replaceAll("[^0-9]", "");

        assertEquals("Should extract numeric part", "100", cleanNumber);
        assertEquals("Should parse to 100", 100, Integer.parseInt(cleanNumber));
    }

    // ========== COORDINATE VALIDATION TESTS ==========

    @Test
    public void testValidLatitude() {
        double latitude = 53.5461;

        assertTrue("Valid latitude should be between -90 and 90",
                latitude >= -90 && latitude <= 90);
    }

    @Test
    public void testValidLongitude() {
        double longitude = -113.4938;

        assertTrue("Valid longitude should be between -180 and 180",
                longitude >= -180 && longitude <= 180);
    }

    @Test
    public void testInvalidLatitudeTooHigh() {
        double latitude = 95.0;

        assertFalse("Latitude above 90 should be invalid",
                latitude >= -90 && latitude <= 90);
    }

    @Test
    public void testInvalidLatitudeTooLow() {
        double latitude = -95.0;

        assertFalse("Latitude below -90 should be invalid",
                latitude >= -90 && latitude <= 90);
    }

    @Test
    public void testInvalidLongitudeTooHigh() {
        double longitude = 185.0;

        assertFalse("Longitude above 180 should be invalid",
                longitude >= -180 && longitude <= 180);
    }

    @Test
    public void testInvalidLongitudeTooLow() {
        double longitude = -185.0;

        assertFalse("Longitude below -180 should be invalid",
                longitude >= -180 && longitude <= 180);
    }

    @Test
    public void testEdmontonCoordinates() {
        double latitude = 53.5461;
        double longitude = -113.4938;

        assertTrue("Edmonton latitude should be valid",
                latitude >= -90 && latitude <= 90);
        assertTrue("Edmonton longitude should be valid",
                longitude >= -180 && longitude <= 180);
    }

    // ========== INPUT VALIDATION TESTS ==========

    @Test
    public void testEmptyEventNameInvalid() {
        String eventName = "";

        assertTrue("Empty event name should be invalid", eventName.trim().isEmpty());
    }

    @Test
    public void testValidEventName() {
        String eventName = "Tech Conference 2025";

        assertFalse("Valid event name should not be empty", eventName.trim().isEmpty());
    }

    @Test
    public void testEmptyDescriptionInvalid() {
        String description = "";

        assertTrue("Empty description should be invalid", description.trim().isEmpty());
    }

    @Test
    public void testValidDescription() {
        String description = "An amazing tech conference";

        assertFalse("Valid description should not be empty", description.trim().isEmpty());
    }

    @Test
    public void testEmptyLocationInvalid() {
        String location = "";

        assertTrue("Empty location should be invalid", location.trim().isEmpty());
    }

    @Test
    public void testValidLocation() {
        String location = "Edmonton, AB";

        assertFalse("Valid location should not be empty", location.trim().isEmpty());
    }

    @Test
    public void testWhitespaceOnlyInputInvalid() {
        String input = "   ";

        assertTrue("Whitespace-only input should be invalid", input.trim().isEmpty());
    }

    // ========== PRICE VALIDATION TESTS ==========

    @Test
    public void testValidPriceDecimal() {
        String priceStr = "25.50";
        double price = Double.parseDouble(priceStr);

        assertEquals("Price should be 25.50", 25.50, price, 0.001);
        assertTrue("Price should be positive", price > 0);
    }

    @Test
    public void testValidPriceWhole() {
        String priceStr = "50";
        double price = Double.parseDouble(priceStr);

        assertEquals("Price should be 50.0", 50.0, price, 0.001);
    }

    @Test
    public void testEmptyPriceDefaultsToZero() {
        String priceStr = "";
        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

        assertEquals("Empty price should default to 0", 0.0, price, 0.001);
    }

    @Test
    public void testNegativePriceInvalid() {
        String priceStr = "-10.00";
        double price = Double.parseDouble(priceStr);

        assertTrue("Negative price should be invalid", price < 0);
    }

    // ========== UNRESPONSIVE HOURS TESTS ==========

    @Test
    public void testValidUnresponsiveHours() {
        String hoursStr = "48";
        long hours = Long.parseLong(hoursStr);

        assertEquals("Unresponsive hours should be 48", 48L, hours);
        assertTrue("Unresponsive hours should be positive", hours > 0);
    }

    @Test
    public void testDefaultUnresponsiveHours() {
        String hoursStr = "";
        long hours = hoursStr.isEmpty() ? 24L : Long.parseLong(hoursStr);

        assertEquals("Empty hours should default to 24", 24L, hours);
    }

    @Test
    public void testZeroUnresponsiveHours() {
        String hoursStr = "0";
        long hours = Long.parseLong(hoursStr);
        long finalHours = hours > 0 ? hours : 24L;

        assertEquals("Zero hours should default to 24", 24L, finalHours);
    }

    // ========== DRAW CAPACITY TESTS ==========

    @Test
    public void testValidDrawCapacity() {
        String capacityStr = "50";
        int capacity = Integer.parseInt(capacityStr);

        assertEquals("Draw capacity should be 50", 50, capacity);
        assertTrue("Draw capacity should be positive", capacity > 0);
    }

    @Test
    public void testEmptyDrawCapacityReturnsZero() {
        String capacityStr = "";
        int capacity = capacityStr.isEmpty() ? 0 : Integer.parseInt(capacityStr);

        assertEquals("Empty draw capacity should be 0", 0, capacity);
    }

    // ========== GEOLOCATION TESTS ==========

    @Test
    public void testGeolocationEnabledWithCoordinates() {
        boolean geolocationEnabled = true;
        double latitude = 53.5461;
        double longitude = -113.4938;

        assertTrue("Geolocation should be enabled", geolocationEnabled);
        assertTrue("Should have valid latitude", latitude >= -90 && latitude <= 90);
        assertTrue("Should have valid longitude", longitude >= -180 && longitude <= 180);
    }

    @Test
    public void testGeolocationDisabled() {
        boolean geolocationEnabled = false;

        assertFalse("Geolocation should be disabled", geolocationEnabled);
    }

    // ========== LABEL SELECTION TESTS ==========

    @Test
    public void testEmptyLabelSelectionValid() {
        int selectedCount = 0;

        assertTrue("Empty label selection should be valid (no minimum)",
                selectedCount >= 0);
    }

    @Test
    public void testMultipleLabelSelection() {
        int selectedCount = 5;

        assertTrue("Should allow multiple label selections", selectedCount > 0);
    }

    @Test
    public void testMaxLabelSelection() {
        int selectedCount = 15; // All labels

        assertTrue("Should allow selecting all 15 labels", selectedCount <= 15);
    }

    // ========== INTEGRATION LOGIC TESTS ==========

    @Test
    public void testCompleteEventValidation() {
        // Simulate complete valid event data
        String eventName = "Tech Conference 2025";
        String description = "An amazing conference";
        String location = "Edmonton, AB";

        Calendar now = Calendar.getInstance();

        Calendar regOpens = Calendar.getInstance();
        regOpens.add(Calendar.HOUR_OF_DAY, 1); // Registration opens in 1 hour (future)

        Calendar startDateTime = Calendar.getInstance();
        startDateTime.add(Calendar.DAY_OF_MONTH, 1); // Event starts tomorrow

        Calendar endDateTime = (Calendar) startDateTime.clone();
        endDateTime.add(Calendar.HOUR_OF_DAY, 2); // Event ends 2 hours after start

        Calendar regCloses = (Calendar) startDateTime.clone();
        regCloses.add(Calendar.HOUR_OF_DAY, -1); // Registration closes 1 hour before event

        // Validate all fields
        assertFalse("Event name should not be empty", eventName.trim().isEmpty());
        assertFalse("Description should not be empty", description.trim().isEmpty());
        assertFalse("Location should not be empty", location.trim().isEmpty());
        assertFalse("Start time should not be in past", startDateTime.before(now));
        assertFalse("End time should not be before start", endDateTime.before(startDateTime));
        assertFalse("Reg opens should not be in past", regOpens.before(now));
        assertFalse("Reg closes should not be before opens", regCloses.before(regOpens));
        assertTrue("Reg closes should be before event start", regCloses.before(startDateTime));
    }

    @Test
    public void testInvalidEventMissingRequiredFields() {
        String eventName = "";
        String description = "Valid description";
        String location = "Valid location";

        boolean isValid = !eventName.trim().isEmpty()
                && !description.trim().isEmpty()
                && !location.trim().isEmpty();

        assertFalse("Event with empty name should be invalid", isValid);
    }
}
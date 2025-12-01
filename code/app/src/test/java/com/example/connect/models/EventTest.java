package com.example.connect.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for Event model
 * Tests constructors, getters, setters, and business logic
 *
 * @author Digaant Chhokra
 * @date 12/01/2025
 */
public class EventTest {

    private static final String TEST_EVENT_ID = "event_123";
    private static final String TEST_NAME = "Tech Conference 2025";
    private static final String TEST_DESCRIPTION = "An amazing tech conference";
    private static final String TEST_LOCATION = "Edmonton, AB";
    private static final String TEST_DATE_TIME = "2025-12-01T14:30:00";
    private static final String TEST_ORGANIZER_ID = "organizer_456";

    @Before
    public void setUp() {
        // Setup if needed
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    public void testDefaultConstructor() {
        Event event = new Event();

        assertNotNull("Event should be created", event);
        assertNull("Default event ID should be null", event.getEventId());
        assertNull("Default name should be null", event.getName());
        assertFalse("Draw should not be completed by default", event.isDrawCompleted());
        assertEquals("Selected count should be 0", 0, event.getSelectedCount());
    }

    @Test
    public void testBasicConstructor() {
        Event event = new Event(TEST_NAME, TEST_DATE_TIME, 100);

        assertEquals("Name should match", TEST_NAME, event.getName());
        assertEquals("DateTime should match", TEST_DATE_TIME, event.getDateTime());
        assertEquals("Max participants should be 100", 100, event.getMaxParticipants());
        assertEquals("Current participants should be 0", 0, event.getCurrentParticipants());
        assertEquals("Price should be Free", "Free", event.getPrice());
        assertFalse("Draw should not be completed", event.isDrawCompleted());
        assertEquals("Unresponsive hours should be 24", Long.valueOf(24L), event.getUnresponsiveDurationHours());
    }

    // ========== GETTER AND SETTER TESTS ==========

    @Test
    public void testEventIdGetterSetter() {
        Event event = new Event();
        event.setEventId(TEST_EVENT_ID);

        assertEquals("Event ID should match", TEST_EVENT_ID, event.getEventId());
    }

    @Test
    public void testNameGetterSetter() {
        Event event = new Event();
        event.setName(TEST_NAME);

        assertEquals("Name should match", TEST_NAME, event.getName());
    }

    @Test
    public void testLocationCoordinatesGetterSetter() {
        Event event = new Event();
        event.setLocationLatitude(53.5461);
        event.setLocationLongitude(-113.4938);

        assertEquals("Latitude should match", Double.valueOf(53.5461), event.getLocationLatitude());
        assertEquals("Longitude should match", Double.valueOf(-113.4938), event.getLocationLongitude());
    }

    @Test
    public void testDrawCapacityGetterSetter() {
        Event event = new Event();
        event.setDrawCapacity(50);

        assertEquals("Draw capacity should be 50", 50, event.getDrawCapacity());
    }

    @Test
    public void testLabelsGetterSetter() {
        Event event = new Event();
        List<String> labels = Arrays.asList("Technology", "Conference", "Networking");
        event.setLabels(labels);

        assertEquals("Labels size should be 3", 3, event.getLabels().size());
        assertTrue("Should contain Technology", event.getLabels().contains("Technology"));
    }

    @Test
    public void testGeolocationRequirementGetterSetter() {
        Event event = new Event();
        event.setRequireGeolocation(true);

        assertTrue("Geolocation should be required", event.isRequireGeolocation());

        event.setRequireGeolocation(false);
        assertFalse("Geolocation should not be required", event.isRequireGeolocation());
    }

    // ========== LOTTERY/DRAW TESTS ==========

    @Test
    public void testDrawCompletedGetterSetter() {
        Event event = new Event();
        event.setDrawCompleted(true);

        assertTrue("Draw should be completed", event.isDrawCompleted());
    }

    @Test
    public void testDrawDateGetterSetter() {
        Event event = new Event();
        Timestamp timestamp = new Timestamp(new Date());
        event.setDrawDate(timestamp);

        assertEquals("Draw date should match", timestamp, event.getDrawDate());
    }

    @Test
    public void testSelectedCountGetterSetter() {
        Event event = new Event();
        event.setSelectedCount(25);

        assertEquals("Selected count should be 25", 25, event.getSelectedCount());
    }

    // ========== CAPACITY AND REGISTRATION TESTS ==========

    @Test
    public void testIsFullWhenAtCapacity() {
        Event event = new Event();
        event.setMaxParticipants(100);
        event.setCurrentParticipants(100);

        assertTrue("Event should be full", event.isFull());
    }

    @Test
    public void testIsFullWhenBelowCapacity() {
        Event event = new Event();
        event.setMaxParticipants(100);
        event.setCurrentParticipants(50);

        assertFalse("Event should not be full", event.isFull());
    }

    @Test
    public void testIsFullWhenOverCapacity() {
        Event event = new Event();
        event.setMaxParticipants(100);
        event.setCurrentParticipants(150);

        assertTrue("Event should be full when over capacity", event.isFull());
    }

    @Test
    public void testRegistrationDatesGetterSetter() {
        Event event = new Event();
        event.setRegStart("2025-11-01T00:00:00");
        event.setRegStop("2025-11-30T23:59:59");

        assertEquals("Reg start should match", "2025-11-01T00:00:00", event.getRegStart());
        assertEquals("Reg stop should match", "2025-11-30T23:59:59", event.getRegStop());
    }

    // ========== IMAGE TESTS ==========

    @Test
    public void testImageUrlGetterSetter() {
        Event event = new Event();
        String imageUrl = "https://example.com/event.jpg";
        event.setImageUrl(imageUrl);

        assertEquals("Image URL should match", imageUrl, event.getImageUrl());
    }

    @Test
    public void testImageBase64GetterSetter() {
        Event event = new Event();
        String base64 = "base64encodedstring";
        event.setImageBase64(base64);

        assertEquals("Image base64 should match", base64, event.getImageBase64());
    }

    // ========== UNRESPONSIVE HOURS TESTS ==========

    @Test
    public void testUnresponsiveHoursDefaultValue() {
        Event event = new Event();

        assertEquals("Default unresponsive hours should be 24",
                Long.valueOf(24L), event.getUnresponsiveDurationHours());
    }

    @Test
    public void testUnresponsiveHoursCustomValue() {
        Event event = new Event();
        event.setUnresponsiveDurationHours(48L);

        assertEquals("Unresponsive hours should be 48",
                Long.valueOf(48L), event.getUnresponsiveDurationHours());
    }

    @Test
    public void testUnresponsiveHoursNullValue() {
        Event event = new Event();
        event.setUnresponsiveDurationHours(null);

        assertEquals("Null should default to 24",
                Long.valueOf(24L), event.getUnresponsiveDurationHours());
    }

    // ========== EQUALS AND HASHCODE TESTS ==========

    @Test
    public void testEqualsSameObject() {
        Event event = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event.setEventId(TEST_EVENT_ID);

        assertTrue("Same object should be equal", event.equals(event));
    }

    @Test
    public void testEqualsWithIdenticalEvents() {
        Event event1 = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event1.setEventId(TEST_EVENT_ID);
        event1.setLocation(TEST_LOCATION);

        Event event2 = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event2.setEventId(TEST_EVENT_ID);
        event2.setLocation(TEST_LOCATION);

        assertTrue("Identical events should be equal", event1.equals(event2));
    }

    @Test
    public void testEqualsWithDifferentEvents() {
        Event event1 = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event1.setEventId(TEST_EVENT_ID);

        Event event2 = new Event("Different Event", TEST_DATE_TIME, 100);
        event2.setEventId("different_id");

        assertFalse("Different events should not be equal", event1.equals(event2));
    }

    @Test
    public void testHashCodeConsistency() {
        Event event1 = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event1.setEventId(TEST_EVENT_ID);

        Event event2 = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event2.setEventId(TEST_EVENT_ID);

        assertEquals("Equal objects should have same hash code",
                event1.hashCode(), event2.hashCode());
    }

    // ========== VALIDATION TESTS ==========

    @Test
    public void testCompleteEventObject() {
        Event event = new Event(TEST_NAME, TEST_DATE_TIME, 100);
        event.setEventId(TEST_EVENT_ID);
        event.setDescription(TEST_DESCRIPTION);
        event.setLocation(TEST_LOCATION);
        event.setOrganizerId(TEST_ORGANIZER_ID);
        event.setDrawCapacity(50);
        event.setRequireGeolocation(true);

        assertEquals("Name should match", TEST_NAME, event.getName());
        assertEquals("Event ID should match", TEST_EVENT_ID, event.getEventId());
        assertEquals("Description should match", TEST_DESCRIPTION, event.getDescription());
        assertEquals("Location should match", TEST_LOCATION, event.getLocation());
        assertEquals("Organizer ID should match", TEST_ORGANIZER_ID, event.getOrganizerId());
        assertEquals("Draw capacity should be 50", 50, event.getDrawCapacity());
        assertTrue("Geolocation should be required", event.isRequireGeolocation());
    }

    @Test
    public void testEventWithNullOptionalFields() {
        Event event = new Event();

        assertNull("Event ID should be null", event.getEventId());
        assertNull("Description should be null", event.getDescription());
        assertNull("Image URL should be null", event.getImageUrl());
        assertNull("Labels should be null", event.getLabels());
        assertNull("Draw date should be null", event.getDrawDate());
    }
}
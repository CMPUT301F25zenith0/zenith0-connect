package com.example.connect.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * Unit tests for WaitingListEntry model
 * Tests constructors, getters, setters, and status transitions
 *
 * @author Digaant Chhokra
 * @date 12/01/2025
 */
public class WaitingListEntryTest {

    private static final String TEST_USER_ID = "user_123";
    private static final String TEST_DOCUMENT_ID = "doc_456";
    private static final String STATUS_WAITING = "waiting";
    private static final String STATUS_SELECTED = "selected";
    private static final String STATUS_ENROLLED = "enrolled";
    private static final String STATUS_CANCELED = "canceled";

    private Timestamp testTimestamp;

    @Before
    public void setUp() {
        testTimestamp = new Timestamp(new Date());
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    public void testDefaultConstructor() {
        WaitingListEntry entry = new WaitingListEntry();

        assertNotNull("Entry should be created", entry);
        assertNull("Default user ID should be null", entry.getUserId());
        assertNull("Default status should be null", entry.getStatus());
        assertNull("Default joined date should be null", entry.getJoinedDate());
    }

    @Test
    public void testParameterizedConstructor() {
        WaitingListEntry entry = new WaitingListEntry(TEST_USER_ID, STATUS_WAITING, testTimestamp);

        assertEquals("User ID should match", TEST_USER_ID, entry.getUserId());
        assertEquals("Status should be waiting", STATUS_WAITING, entry.getStatus());
        assertEquals("Joined date should match", testTimestamp, entry.getJoinedDate());
    }

    // ========== BASIC GETTER/SETTER TESTS ==========

    @Test
    public void testDocumentIdGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setDocumentId(TEST_DOCUMENT_ID);

        assertEquals("Document ID should match", TEST_DOCUMENT_ID, entry.getDocumentId());
    }

    @Test
    public void testUserIdGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setUserId(TEST_USER_ID);

        assertEquals("User ID should match", TEST_USER_ID, entry.getUserId());
    }

    @Test
    public void testStatusGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(STATUS_SELECTED);

        assertEquals("Status should be selected", STATUS_SELECTED, entry.getStatus());
    }

    // ========== STATUS TRANSITION TESTS ==========

    @Test
    public void testStatusWaiting() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(STATUS_WAITING);

        assertEquals("Status should be waiting", STATUS_WAITING, entry.getStatus());
    }

    @Test
    public void testStatusSelected() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(STATUS_SELECTED);

        assertEquals("Status should be selected", STATUS_SELECTED, entry.getStatus());
    }

    @Test
    public void testStatusEnrolled() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(STATUS_ENROLLED);

        assertEquals("Status should be enrolled", STATUS_ENROLLED, entry.getStatus());
    }

    @Test
    public void testStatusCanceled() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus(STATUS_CANCELED);

        assertEquals("Status should be canceled", STATUS_CANCELED, entry.getStatus());
    }

    // ========== TIMESTAMP TESTS ==========

    @Test
    public void testJoinedDateGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setJoinedDate(testTimestamp);

        assertEquals("Joined date should match", testTimestamp, entry.getJoinedDate());
    }

    @Test
    public void testSelectedDateGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setSelectedDate(testTimestamp);

        assertEquals("Selected date should match", testTimestamp, entry.getSelectedDate());
    }

    @Test
    public void testEnrolledDateGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setEnrolledDate(testTimestamp);

        assertEquals("Enrolled date should match", testTimestamp, entry.getEnrolledDate());
    }

    @Test
    public void testCanceledDateGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setCanceledDate(testTimestamp);

        assertEquals("Canceled date should match", testTimestamp, entry.getCanceledDate());
    }

    // ========== LOCATION TESTS (US 02.02.02) ==========

    @Test
    public void testLocationCoordinatesGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setLatitude(53.5461);
        entry.setLongitude(-113.4938);

        assertEquals("Latitude should match", Double.valueOf(53.5461), entry.getLatitude());
        assertEquals("Longitude should match", Double.valueOf(-113.4938), entry.getLongitude());
    }

    @Test
    public void testLocationCapturedAtGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setLocationCapturedAt(testTimestamp);

        assertEquals("Location captured timestamp should match", testTimestamp, entry.getLocationCapturedAt());
    }

    @Test
    public void testNullLocationCoordinates() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setLatitude(null);
        entry.setLongitude(null);

        assertNull("Latitude should be null", entry.getLatitude());
        assertNull("Longitude should be null", entry.getLongitude());
    }

    // ========== USER OBJECT TESTS ==========

    @Test
    public void testUserObjectGetterSetter() {
        WaitingListEntry entry = new WaitingListEntry();
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setName("John Doe");

        entry.setUser(user);

        assertNotNull("User should not be null", entry.getUser());
        assertEquals("User ID should match", TEST_USER_ID, entry.getUser().getUserId());
        assertEquals("User name should match", "John Doe", entry.getUser().getName());
    }

    @Test
    public void testNullUserObject() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setUser(null);

        assertNull("User should be null", entry.getUser());
    }

    // ========== COMPLETE OBJECT TESTS ==========

    @Test
    public void testCompleteWaitingListEntry() {
        WaitingListEntry entry = new WaitingListEntry(TEST_USER_ID, STATUS_WAITING, testTimestamp);
        entry.setDocumentId(TEST_DOCUMENT_ID);
        entry.setLatitude(53.5461);
        entry.setLongitude(-113.4938);
        entry.setLocationCapturedAt(testTimestamp);

        User user = new User();
        user.setUserId(TEST_USER_ID);
        entry.setUser(user);

        assertEquals("Document ID should match", TEST_DOCUMENT_ID, entry.getDocumentId());
        assertEquals("User ID should match", TEST_USER_ID, entry.getUserId());
        assertEquals("Status should be waiting", STATUS_WAITING, entry.getStatus());
        assertEquals("Joined date should match", testTimestamp, entry.getJoinedDate());
        assertEquals("Latitude should match", Double.valueOf(53.5461), entry.getLatitude());
        assertEquals("Longitude should match", Double.valueOf(-113.4938), entry.getLongitude());
        assertNotNull("User should not be null", entry.getUser());
    }

    @Test
    public void testStatusTransitionWithTimestamps() {
        WaitingListEntry entry = new WaitingListEntry(TEST_USER_ID, STATUS_WAITING, testTimestamp);

        Timestamp selectedTime = new Timestamp(new Date(testTimestamp.toDate().getTime() + 3600000));
        entry.setStatus(STATUS_SELECTED);
        entry.setSelectedDate(selectedTime);

        Timestamp enrolledTime = new Timestamp(new Date(selectedTime.toDate().getTime() + 3600000));
        entry.setStatus(STATUS_ENROLLED);
        entry.setEnrolledDate(enrolledTime);

        assertEquals("Status should be enrolled", STATUS_ENROLLED, entry.getStatus());
        assertNotNull("Joined date should be set", entry.getJoinedDate());
        assertNotNull("Selected date should be set", entry.getSelectedDate());
        assertNotNull("Enrolled date should be set", entry.getEnrolledDate());
        assertTrue("Selected date should be after joined date",
                entry.getSelectedDate().toDate().after(entry.getJoinedDate().toDate()));
        assertTrue("Enrolled date should be after selected date",
                entry.getEnrolledDate().toDate().after(entry.getSelectedDate().toDate()));
    }
}
package com.example.connect.activities;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.example.connect.models.Event;
import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.example.connect.utils.CsvUtils;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Unit tests for OrganizerActivity business logic
 * Tests validation, filtering, CSV export, and helper methods
 *
 * @author Digaant Chhokra
 * @date 12/01/2025
 */
public class OrganizerActivityTest {

    private SimpleDateFormat dateTimeFormat;
    private static final String TEST_EVENT_ID = "test_event_123";
    private static final String TEST_USER_ID = "test_user_456";

    @Before
    public void setUp() {
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    // ========== EVENT FILTERING TESTS ==========

    @Test
    public void testIsEventOpen_WithValidRegWindow() {
        Event event = new Event();
        event.setRegStart("2024-01-01");
        event.setRegStop("2024-12-31");

        assertTrue("Event with valid registration window should be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithNullRegStart() {
        Event event = new Event();
        event.setRegStart(null);
        event.setRegStop("2024-12-31");

        assertFalse("Event with null regStart should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithEmptyRegStart() {
        Event event = new Event();
        event.setRegStart("");
        event.setRegStop("2024-12-31");

        assertFalse("Event with empty regStart should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithNullRegStop() {
        Event event = new Event();
        event.setRegStart("2024-01-01");
        event.setRegStop(null);

        assertFalse("Event with null regStop should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithEmptyRegStop() {
        Event event = new Event();
        event.setRegStart("2024-01-01");
        event.setRegStop("");

        assertFalse("Event with empty regStop should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithBothNull() {
        Event event = new Event();
        event.setRegStart(null);
        event.setRegStop(null);

        assertFalse("Event with both null dates should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithBothEmpty() {
        Event event = new Event();
        event.setRegStart("");
        event.setRegStop("");

        assertFalse("Event with both empty dates should not be open",
                hasRegistrationWindow(event));
    }

    @Test
    public void testIsEventOpen_WithNullEvent() {
        assertFalse("Null event should not be open",
                hasRegistrationWindow(null));
    }

    // ========== TIMESTAMP FORMATTING TESTS ==========

    @Test
    public void testFormatTimestamp_WithValidTimestamp() {
        // Jan 15, 2024 10:30:00
        Timestamp timestamp = new Timestamp(new Date(1705320600000L));
        String formatted = formatTimestamp(timestamp);

        assertNotNull("Formatted timestamp should not be null", formatted);
        assertFalse("Formatted timestamp should not be empty", formatted.isEmpty());
        assertTrue("Format should match yyyy-MM-dd HH:mm pattern",
                formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    public void testFormatTimestamp_WithNullTimestamp() {
        String formatted = formatTimestamp(null);

        assertEquals("Null timestamp should return empty string", "", formatted);
    }

    @Test
    public void testFormatTimestamp_WithCurrentTime() {
        Timestamp timestamp = new Timestamp(new Date());
        String formatted = formatTimestamp(timestamp);

        assertNotNull("Formatted current time should not be null", formatted);
        assertFalse("Formatted current time should not be empty", formatted.isEmpty());
        assertTrue("Should contain date separator", formatted.contains("-"));
        assertTrue("Should contain time separator", formatted.contains(":"));
    }

    @Test
    public void testFormatTimestamp_ConsistentFormat() {
        Date testDate = new Date(1705320600000L);
        Timestamp timestamp = new Timestamp(testDate);
        String formatted = formatTimestamp(timestamp);

        // Format using same SimpleDateFormat to verify consistency
        String expected = dateTimeFormat.format(testDate);
        assertEquals("Formatted timestamp should match expected format",
                expected, formatted);
    }

    // ========== CSV EXPORT VALIDATION TESTS ==========

    @Test
    public void testEventValidation_ValidEvent() {
        Event event = new Event();
        event.setEventId("valid_id_123");
        event.setName("Test Event");

        assertTrue("Valid event should pass validation",
                isValidEventForExport(event));
    }

    @Test
    public void testEventValidation_NullEvent() {
        assertFalse("Null event should fail validation",
                isValidEventForExport(null));
    }

    @Test
    public void testEventValidation_NullEventId() {
        Event event = new Event();
        event.setEventId(null);
        event.setName("Test Event");

        assertFalse("Event with null ID should fail validation",
                isValidEventForExport(event));
    }

    @Test
    public void testEventValidation_EmptyEventId() {
        Event event = new Event();
        event.setEventId("");
        event.setName("Test Event");

        assertFalse("Event with empty ID should fail validation",
                isValidEventForExport(event));
    }

    @Test
    public void testEventValidation_WhitespaceEventId() {
        Event event = new Event();
        event.setEventId("   ");
        event.setName("Test Event");

        assertFalse("Event with whitespace-only ID should fail validation",
                isValidEventForExport(event));
    }

    // ========== CSV ROW CREATION TESTS ==========

    @Test
    public void testCsvRowCreation_WithCompleteData() {
        CsvUtils.CsvRow row = new CsvUtils.CsvRow(
                "John Doe",
                "john@example.com",
                "555-1234",
                "2024-01-15 10:30"
        );

        assertNotNull("CSV row should be created successfully", row);
    }

    @Test
    public void testCsvRowCreation_WithNullValues() {
        CsvUtils.CsvRow row = new CsvUtils.CsvRow(null, null, null, null);

        assertNotNull("CSV row with null values should be created", row);
    }

    @Test
    public void testCsvRowCreation_WithEmptyValues() {
        CsvUtils.CsvRow row = new CsvUtils.CsvRow("", "", "", "");

        assertNotNull("CSV row with empty values should be created", row);
    }

    @Test
    public void testCsvRowCreation_WithPartialData() {
        CsvUtils.CsvRow row = new CsvUtils.CsvRow(
                "Jane Smith",
                "",
                null,
                "2024-01-20 14:00"
        );

        assertNotNull("CSV row with partial data should be created", row);
    }

    @Test
    public void testCsvRowCreation_WithSpecialCharacters() {
        CsvUtils.CsvRow row = new CsvUtils.CsvRow(
                "O'Brien, James",
                "james@o-brien.com",
                "+1 (555) 123-4567",
                "2024-01-15 10:30"
        );

        assertNotNull("CSV row with special characters should be created", row);
    }

    // ========== FILENAME SANITIZATION TESTS ==========

    @Test
    public void testSafeFilenameGeneration_WithSpecialCharacters() {
        String rawName = "Test Event #1 @ 2024!";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Special characters should be replaced with underscores",
                "Test_Event__1___2024_", safeName);
        assertTrue("Safe filename should only contain alphanumeric and _-",
                safeName.matches("[a-zA-Z0-9_-]+"));
    }

    @Test
    public void testSafeFilenameGeneration_WithSlashes() {
        String rawName = "Event/2024/Summer";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Slashes should be replaced", "Event_2024_Summer", safeName);
        assertFalse("Should not contain slashes", safeName.contains("/"));
    }

    @Test
    public void testSafeFilenameGeneration_WithSpaces() {
        String rawName = "My Event Name";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Spaces should be replaced with underscores",
                "My_Event_Name", safeName);
        assertFalse("Should not contain spaces", safeName.contains(" "));
    }

    @Test
    public void testSafeFilenameGeneration_AlreadySafe() {
        String rawName = "MyEvent2024";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Safe filename should remain unchanged",
                "MyEvent2024", safeName);
    }

    @Test
    public void testSafeFilenameGeneration_WithHyphensAndUnderscores() {
        String rawName = "my-event_2024";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Hyphens and underscores should be preserved",
                "my-event_2024", safeName);
    }

    @Test
    public void testSafeFilenameGeneration_EmptyString() {
        String rawName = "";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertEquals("Empty string should remain empty", "", safeName);
    }

    @Test
    public void testSafeFilenameGeneration_WithUnicode() {
        String rawName = "Événement 2024";
        String safeName = rawName.replaceAll("[^a-zA-Z0-9_-]", "_");

        assertFalse("Should not contain unicode characters",
                safeName.contains("é"));
        assertTrue("Should only contain safe characters",
                safeName.matches("[a-zA-Z0-9_-]+"));
    }

    // ========== EVENT MODEL TESTS ==========

    @Test
    public void testEventIdExtraction() {
        Event event = new Event();
        event.setEventId(TEST_EVENT_ID);

        assertEquals("Event ID should match", TEST_EVENT_ID, event.getEventId());
    }

    @Test
    public void testEventNameExtraction() {
        Event event = new Event();
        String testName = "Tech Conference 2025";
        event.setName(testName);

        assertEquals("Event name should match", testName, event.getName());
    }

    @Test
    public void testEventRegistrationDates() {
        Event event = new Event();
        event.setRegStart("2024-01-01");
        event.setRegStop("2024-12-31");

        assertEquals("RegStart should match", "2024-01-01", event.getRegStart());
        assertEquals("RegStop should match", "2024-12-31", event.getRegStop());
    }

    @Test
    public void testEventCreation_DefaultValues() {
        Event event = new Event();

        assertNull("Default event ID should be null", event.getEventId());
        assertNull("Default event name should be null", event.getName());
        assertNull("Default regStart should be null", event.getRegStart());
        assertNull("Default regStop should be null", event.getRegStop());
    }

    @Test
    public void testEventOrganizerIdExtraction() {
        Event event = new Event();
        event.setOrganizerId(TEST_USER_ID);

        assertEquals("Organizer ID should match", TEST_USER_ID, event.getOrganizerId());
    }

    // ========== WAITING LIST ENTRY TESTS ==========

    @Test
    public void testWaitingListEntryStatus_Enrolled() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus("enrolled");

        assertEquals("Status should be enrolled", "enrolled", entry.getStatus());
    }

    @Test
    public void testWaitingListEntryStatus_Waiting() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus("waiting");

        assertEquals("Status should be waiting", "waiting", entry.getStatus());
    }

    @Test
    public void testWaitingListEntryStatus_Selected() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus("selected");

        assertEquals("Status should be selected", "selected", entry.getStatus());
    }

    @Test
    public void testWaitingListEntryStatus_Cancelled() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setStatus("cancelled");

        assertEquals("Status should be cancelled", "cancelled", entry.getStatus());
    }

    @Test
    public void testWaitingListEntryUserId() {
        WaitingListEntry entry = new WaitingListEntry();
        entry.setUserId(TEST_USER_ID);

        assertEquals("User ID should match", TEST_USER_ID, entry.getUserId());
    }

    @Test
    public void testWaitingListEntryDates() {
        WaitingListEntry entry = new WaitingListEntry();
        Timestamp now = new Timestamp(new Date());

        entry.setJoinedDate(now);
        entry.setEnrolledDate(now);

        assertEquals("Joined date should match", now, entry.getJoinedDate());
        assertEquals("Enrolled date should match", now, entry.getEnrolledDate());
    }

    @Test
    public void testWaitingListEntryDatePriority() {
        WaitingListEntry entry = new WaitingListEntry();
        Timestamp joinedTime = new Timestamp(new Date(1705320600000L));
        Timestamp enrolledTime = new Timestamp(new Date(1705407000000L));

        entry.setJoinedDate(joinedTime);
        entry.setEnrolledDate(enrolledTime);

        // Prefer enrolled_date over joined_date for CSV export
        Timestamp preferredDate = entry.getEnrolledDate() != null
                ? entry.getEnrolledDate()
                : entry.getJoinedDate();

        assertEquals("Should prefer enrolled date", enrolledTime, preferredDate);
    }

    // ========== USER MODEL TESTS ==========

    @Test
    public void testUserDataExtraction_Complete() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("555-1234");

        assertEquals("User name should match", "John Doe", user.getName());
        assertEquals("User email should match", "john@example.com", user.getEmail());
        assertEquals("User phone should match", "555-1234", user.getPhone());
    }

    @Test
    public void testUserCreation_DefaultValues() {
        User user = new User();

        assertNull("Default name should be null", user.getName());
        assertNull("Default email should be null", user.getEmail());
        assertNull("Default phone should be null", user.getPhone());
    }

    @Test
    public void testUserDataExtraction_PartialData() {
        User user = new User();
        user.setName("Jane Smith");

        assertEquals("User name should match", "Jane Smith", user.getName());
        assertNull("Email should be null", user.getEmail());
        assertNull("Phone should be null", user.getPhone());
    }

    @Test
    public void testUserDataFallback_NullName() {
        User user = null;
        String name = (user != null && user.getName() != null)
                ? user.getName()
                : "Unknown User";

        assertEquals("Should fallback to Unknown User", "Unknown User", name);
    }

    @Test
    public void testUserDataFallback_EmptyEmail() {
        User user = new User();
        user.setEmail(null);

        String email = (user != null && user.getEmail() != null)
                ? user.getEmail()
                : "";

        assertEquals("Should fallback to empty string", "", email);
    }

    // ========== EVENT LIST MANAGEMENT TESTS ==========

    @Test
    public void testEventListInitialization() {
        List<Event> events = new ArrayList<>();

        assertTrue("Event list should be empty initially", events.isEmpty());
        assertEquals("Event list size should be 0", 0, events.size());
    }

    @Test
    public void testEventListAddition() {
        List<Event> events = new ArrayList<>();
        Event event1 = createTestEvent("Event 1", "2024-01-01", "2024-12-31");
        Event event2 = createTestEvent("Event 2", "2024-02-01", "2024-11-30");

        events.add(event1);
        events.add(event2);

        assertEquals("Event list should contain 2 events", 2, events.size());
        assertTrue("Should contain event1", events.contains(event1));
        assertTrue("Should contain event2", events.contains(event2));
    }

    @Test
    public void testEventListClearing() {
        List<Event> events = new ArrayList<>();
        events.add(createTestEvent("Event 1", "2024-01-01", "2024-12-31"));
        events.add(createTestEvent("Event 2", "2024-02-01", "2024-11-30"));

        assertFalse("List should not be empty before clearing", events.isEmpty());

        events.clear();

        assertTrue("List should be empty after clearing", events.isEmpty());
        assertEquals("List size should be 0 after clearing", 0, events.size());
    }

    @Test
    public void testEventListFiltering_OpenEvents() {
        List<Event> allEvents = new ArrayList<>();
        allEvents.add(createTestEvent("Event 1", "2024-01-01", "2024-12-31"));
        allEvents.add(createTestEvent("Event 2", null, null));
        allEvents.add(createTestEvent("Event 3", "2024-03-01", "2024-09-30"));

        List<Event> openEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (hasRegistrationWindow(event)) {
                openEvents.add(event);
            }
        }

        assertEquals("Should have 3 total events", 3, allEvents.size());
        assertEquals("Should have 2 open events", 2, openEvents.size());
    }

    @Test
    public void testEventListFiltering_AllEvents() {
        List<Event> allEvents = createTestEvents(5);
        List<Event> filteredEvents = new ArrayList<>(allEvents);

        assertEquals("Filtered list should match all events",
                allEvents.size(), filteredEvents.size());
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    public void testCompleteEventValidation() {
        Event event = new Event();
        event.setEventId("event_123");
        event.setName("Tech Conference");
        event.setRegStart("2024-01-01");
        event.setRegStop("2024-12-31");

        boolean isValid = isValidEventForExport(event)
                && hasRegistrationWindow(event);

        assertTrue("Complete event should be valid", isValid);
    }

    @Test
    public void testInvalidEventMissingRequiredFields() {
        Event event = new Event();
        event.setEventId("");
        event.setName("Tech Conference");
        event.setRegStart("2024-01-01");
        event.setRegStop("2024-12-31");

        boolean isValid = isValidEventForExport(event);

        assertFalse("Event with empty ID should be invalid", isValid);
    }

    @Test
    public void testCsvExportDataFlow() {
        // Simulate the data flow for CSV export
        WaitingListEntry entry = new WaitingListEntry();
        entry.setUserId(TEST_USER_ID);
        entry.setStatus("enrolled");
        entry.setEnrolledDate(new Timestamp(new Date()));

        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("555-1234");

        // Extract data as done in exportEnrolledEntrantsToCsv
        String name = (user != null && user.getName() != null)
                ? user.getName() : "Unknown User";
        String email = (user != null && user.getEmail() != null)
                ? user.getEmail() : "";
        String phone = (user != null && user.getPhone() != null)
                ? user.getPhone() : "";

        Timestamp ts = entry.getEnrolledDate() != null
                ? entry.getEnrolledDate()
                : entry.getJoinedDate();
        String joinedDate = formatTimestamp(ts);

        assertEquals("Name should be extracted", "John Doe", name);
        assertEquals("Email should be extracted", "john@example.com", email);
        assertEquals("Phone should be extracted", "555-1234", phone);
        assertNotNull("Date should be formatted", joinedDate);
        assertFalse("Date should not be empty", joinedDate.isEmpty());
    }

    // ========== HELPER METHODS ==========

    /**
     * Replicates the logic from OrganizerActivity.isEventOpen()
     */
    private boolean hasRegistrationWindow(Event event) {
        if (event == null) return false;

        String regStart = event.getRegStart();
        String regStop = event.getRegStop();

        return regStart != null && !regStart.isEmpty() &&
                regStop != null && !regStop.isEmpty();
    }

    /**
     * Replicates the timestamp formatting logic
     */
    private String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        Date date = ts.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Validates if an event is ready for CSV export
     */
    private boolean isValidEventForExport(Event event) {
        if (event == null) return false;
        if (event.getEventId() == null) return false;
        if (event.getEventId().trim().isEmpty()) return false;
        return true;
    }

    /**
     * Creates a test event with the given parameters
     */
    private Event createTestEvent(String name, String regStart, String regStop) {
        Event event = new Event();
        event.setName(name);
        event.setEventId(TEST_EVENT_ID);
        event.setRegStart(regStart);
        event.setRegStop(regStop);
        return event;
    }

    /**
     * Creates a list of test events
     */
    private List<Event> createTestEvents(int count) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(createTestEvent("Event " + i, "2024-01-01", "2024-12-31"));
        }
        return events;
    }
}
package com.example.connect.models;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Unit tests for Report model
 * Tests constructors, getters, setters, and helper methods
 *
 * @author Digaant Chhokra
 * @date 12/01/2025
 */
public class ReportTest {

    private static final String TEST_REPORT_ID = "report_123";
    private static final String TEST_EVENT_ID = "event_456";
    private static final String TEST_REPORTER_ID = "user_789";
    private static final String TEST_DESCRIPTION = "Inappropriate event content";
    private static final float TEST_SEVERITY = 7.5f;
    private static final String TEST_STATUS = "pending";

    @Before
    public void setUp() {
        // Setup if needed
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    public void testDefaultConstructor() {
        Report report = new Report();

        assertNotNull("Report should be created", report);
        assertNull("Default report ID should be null", report.getReportId());
        assertNull("Default event ID should be null", report.getEvent_id());
        assertNull("Default reporter ID should be null", report.getReporter_id());
    }

    // ========== GETTER AND SETTER TESTS ==========

    @Test
    public void testReportIdGetterSetter() {
        Report report = new Report();
        report.setReportId(TEST_REPORT_ID);

        assertEquals("Report ID should match", TEST_REPORT_ID, report.getReportId());
    }

    @Test
    public void testEventIdGetterSetter() {
        Report report = new Report();
        report.setEvent_id(TEST_EVENT_ID);

        assertEquals("Event ID should match", TEST_EVENT_ID, report.getEvent_id());
    }

    @Test
    public void testReporterIdGetterSetter() {
        Report report = new Report();
        report.setReporter_id(TEST_REPORTER_ID);

        assertEquals("Reporter ID should match", TEST_REPORTER_ID, report.getReporter_id());
    }

    @Test
    public void testDescriptionGetterSetter() {
        Report report = new Report();
        report.setDescription(TEST_DESCRIPTION);

        assertEquals("Description should match", TEST_DESCRIPTION, report.getDescription());
    }

    @Test
    public void testSeverityRatingGetterSetter() {
        Report report = new Report();
        report.setSeverity_rating(TEST_SEVERITY);

        assertEquals("Severity rating should match", TEST_SEVERITY, report.getSeverity_rating(), 0.01f);
    }

    @Test
    public void testTimestampGetterSetter() {
        Report report = new Report();
        Date timestamp = new Date();
        report.setTimestamp(timestamp);

        assertEquals("Timestamp should match", timestamp, report.getTimestamp());
    }

    @Test
    public void testStatusGetterSetter() {
        Report report = new Report();
        report.setStatus(TEST_STATUS);

        assertEquals("Status should match", TEST_STATUS, report.getStatus());
    }

    // ========== HELPER METHOD TESTS ==========

    @Test
    public void testGetItemType() {
        Report report = new Report();

        assertEquals("Item type should be Event", "Event", report.getItemType());
    }

    @Test
    public void testGetReportedItemId() {
        Report report = new Report();
        report.setEvent_id(TEST_EVENT_ID);

        assertEquals("Reported item ID should match event ID",
                TEST_EVENT_ID, report.getReportedItemId());
    }

    @Test
    public void testGetReason() {
        Report report = new Report();
        report.setDescription(TEST_DESCRIPTION);

        assertEquals("Reason should match description",
                TEST_DESCRIPTION, report.getReason());
    }

    // ========== SEVERITY RATING TESTS ==========

    @Test
    public void testSeverityRatingZero() {
        Report report = new Report();
        report.setSeverity_rating(0.0f);

        assertEquals("Severity should be 0", 0.0f, report.getSeverity_rating(), 0.01f);
    }

    @Test
    public void testSeverityRatingMaximum() {
        Report report = new Report();
        report.setSeverity_rating(10.0f);

        assertEquals("Severity should be 10", 10.0f, report.getSeverity_rating(), 0.01f);
    }

    @Test
    public void testSeverityRatingDecimal() {
        Report report = new Report();
        report.setSeverity_rating(5.75f);

        assertEquals("Severity should be 5.75", 5.75f, report.getSeverity_rating(), 0.01f);
    }

    // ========== STATUS TESTS ==========

    @Test
    public void testStatusPending() {
        Report report = new Report();
        report.setStatus("pending");

        assertEquals("Status should be pending", "pending", report.getStatus());
    }

    @Test
    public void testStatusResolved() {
        Report report = new Report();
        report.setStatus("resolved");

        assertEquals("Status should be resolved", "resolved", report.getStatus());
    }

    @Test
    public void testStatusRejected() {
        Report report = new Report();
        report.setStatus("rejected");

        assertEquals("Status should be rejected", "rejected", report.getStatus());
    }

    // ========== NULL VALUE TESTS ==========

    @Test
    public void testSetNullDescription() {
        Report report = new Report();
        report.setDescription(TEST_DESCRIPTION);
        report.setDescription(null);

        assertNull("Description should be null", report.getDescription());
    }

    @Test
    public void testSetNullTimestamp() {
        Report report = new Report();
        report.setTimestamp(new Date());
        report.setTimestamp(null);

        assertNull("Timestamp should be null", report.getTimestamp());
    }

    @Test
    public void testSetNullStatus() {
        Report report = new Report();
        report.setStatus(TEST_STATUS);
        report.setStatus(null);

        assertNull("Status should be null", report.getStatus());
    }

    // ========== COMPLETE OBJECT TESTS ==========

    @Test
    public void testCompleteReportObject() {
        Report report = new Report();
        Date timestamp = new Date();

        report.setReportId(TEST_REPORT_ID);
        report.setEvent_id(TEST_EVENT_ID);
        report.setReporter_id(TEST_REPORTER_ID);
        report.setDescription(TEST_DESCRIPTION);
        report.setSeverity_rating(TEST_SEVERITY);
        report.setTimestamp(timestamp);
        report.setStatus(TEST_STATUS);

        assertEquals("Report ID should match", TEST_REPORT_ID, report.getReportId());
        assertEquals("Event ID should match", TEST_EVENT_ID, report.getEvent_id());
        assertEquals("Reporter ID should match", TEST_REPORTER_ID, report.getReporter_id());
        assertEquals("Description should match", TEST_DESCRIPTION, report.getDescription());
        assertEquals("Severity should match", TEST_SEVERITY, report.getSeverity_rating(), 0.01f);
        assertEquals("Timestamp should match", timestamp, report.getTimestamp());
        assertEquals("Status should match", TEST_STATUS, report.getStatus());
    }

    @Test
    public void testReportObjectConsistency() {
        Report report = new Report();
        report.setEvent_id(TEST_EVENT_ID);
        report.setDescription(TEST_DESCRIPTION);

        // Verify helper methods return consistent data
        assertEquals("Reported item ID should match event ID",
                report.getEvent_id(), report.getReportedItemId());
        assertEquals("Reason should match description",
                report.getDescription(), report.getReason());
        assertEquals("Item type should always be Event", "Event", report.getItemType());
    }

    // ========== TIMESTAMP VALIDATION TESTS ==========

    @Test
    public void testTimestampInPast() {
        Report report = new Report();
        Date pastDate = new Date(1609459200000L); // Jan 1, 2021
        report.setTimestamp(pastDate);

        assertTrue("Past timestamp should be accepted",
                report.getTimestamp().before(new Date()));
    }

    @Test
    public void testTimestampCurrent() {
        Report report = new Report();
        Date currentDate = new Date();
        report.setTimestamp(currentDate);

        assertNotNull("Current timestamp should not be null", report.getTimestamp());
        assertTrue("Current timestamp should be recent",
                Math.abs(currentDate.getTime() - report.getTimestamp().getTime()) < 1000);
    }
}
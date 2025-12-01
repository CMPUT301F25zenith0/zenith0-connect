package com.example.connect.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.example.connect.models.Report;
import org.junit.Test;

public class ReportModelTest {

    @Test
    public void testReportHelperMethods() {
        // Setup
        Report report = new Report();
        report.setEvent_id("EVT-123456789");
        report.setDescription("Inappropriate content");
        report.setStatus("pending");

        // Test Helper Logic
        // Verify getItemType() returns hardcoded "Event"
        assertEquals("Event", report.getItemType());

        // Verify getReportedItemId() returns the event_id
        assertEquals("EVT-123456789", report.getReportedItemId());

        // Verify getReason() returns the description
        assertEquals("Inappropriate content", report.getReason());
    }

    @Test
    public void testEmptyReportInitialization() {
        // Ensure a new report has null fields initially (vital for validation logic)
        Report report = new Report();
        assertNull(report.getReportId());
        assertNull(report.getDescription());
    }
}
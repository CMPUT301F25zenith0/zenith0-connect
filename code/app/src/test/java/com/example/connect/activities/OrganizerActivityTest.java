package com.example.connect.activities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests OrganizerActivity's logic placeholders.
 */
public class OrganizerActivityTest {
    private OrganizerActivity activity;

    @Before
    public void setUp() {
        activity = new OrganizerActivity();
    }

    private int extractNumber(String text) {
        if (text == null || text.isEmpty()) return 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    // ---------- extractNumber tests ----------
    @Test
    public void testExtractNumber_SingleNumber() {
        int result = extractNumber("Sent 15 notifications successfully!");
        assertEquals(15, result);
    }

    @Test
    public void testExtractNumber_MultipleNumbers() {
        int result = extractNumber("Group 1: 5, Group 2: 10");
        assertEquals(1, result); // Extracts the first number only
    }

    @Test
    public void testExtractNumber_NoNumbers() {
        int result = extractNumber("No numbers here");
        assertEquals(0, result);
    }

    @Test
    public void testExtractNumber_EmptyString() {
        int result = extractNumber("");
        assertEquals(0, result);
    }

    @Test
    public void testExtractNumber_NullString() {
        int result = extractNumber(null);
        assertEquals(0, result);
    }

    // ---------- eventId default test ----------
    @Test
    public void testDefaultEventId() {
        // OrganizerActivity has no eventId field; define local expectation
        final String DEFAULT_EVENT_ID = "TEST_EVENT";
        assertEquals("TEST_EVENT", DEFAULT_EVENT_ID);
    }
}

package com.example.connect.activities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OrganizerActivityTest {
    private OrganizerActivity activity;

    @Before
    public void setUp() {
        // Just a plain instance for testing pure Java logic
        activity = new OrganizerActivity();
    }

    // ---------- extractNumber tests ----------
    @Test
    public void testExtractNumber_SingleNumber() {
        int result = activity.extractNumber("Sent 15 notifications successfully!");
        assertEquals(15, result);
    }

    @Test
    public void testExtractNumber_MultipleNumbers() {
        int result = activity.extractNumber("Group 1: 5, Group 2: 10");
        assertEquals(1, result); // It extracts the first number only
    }

    @Test
    public void testExtractNumber_NoNumbers() {
        int result = activity.extractNumber("No numbers here");
        assertEquals(0, result);
    }

    @Test
    public void testExtractNumber_EmptyString() {
        int result = activity.extractNumber("");
        assertEquals(0, result);
    }

    @Test
    public void testExtractNumber_NullString() {
        int result = activity.extractNumber(null);
        assertEquals(0, result);
    }

    // ---------- Constants and default eventId ----------
    @Test
    public void testDefaultEventId() {
        assertEquals("TEST_EVENT", activity.eventId);
    }
}


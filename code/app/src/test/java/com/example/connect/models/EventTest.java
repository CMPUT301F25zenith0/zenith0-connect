package com.example.connect.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Event class.
 */
public class EventTest {

    @Test
    public void testEventCreation() {
        Event e = new Event("Swimming", "2025-12-01", 20);
        assertEquals("Swimming", e.getName());
        assertEquals("2025-12-01", e.getDate());
        assertEquals(20, e.getMaxParticipants());
    }
}

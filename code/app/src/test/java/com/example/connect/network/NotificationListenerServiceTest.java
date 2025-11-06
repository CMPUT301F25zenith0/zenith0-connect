package com.example.connect.network;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for NotificationListenerService logic (pure Java, no Android dependencies)
 */
public class NotificationListenerServiceTest {

    // -----------------------------
    // Helper class to simulate notification logic
    // -----------------------------
    public static class NotificationHelper {
        public static class NotificationData {
            public final String title;
            public final String body;
            public final String type;
            public final int color;

            public NotificationData(String title, String body, String type, int color) {
                this.title = title;
                this.body = body;
                this.type = type;
                this.color = color;
            }
        }

        /**
         * Build notification data based on type
         */
        public NotificationData buildNotificationData(String title, String body, String type) {
            int color;
            switch (type) {
                case "chosen":
                    color = 0xFF4CAF50; // Green
                    break;
                case "not_chosen":
                    color = 0xFFFF9800; // Orange
                    break;
                default:
                    color = 0xFF2196F3; // Blue
            }
            return new NotificationData(title, body, type, color);
        }
    }

    // -----------------------------
    // Test class
    // -----------------------------
    private NotificationHelper helper;

    @Before
    public void setUp() {
        helper = new NotificationHelper();
    }

    @Test
    public void testBuildNotificationData_ChosenType() {
        NotificationHelper.NotificationData data =
                helper.buildNotificationData("Event Success", "You were chosen!", "chosen");

        assertEquals("Event Success", data.title);
        assertEquals("You were chosen!", data.body);
        assertEquals("chosen", data.type);
        assertEquals(0xFF4CAF50, data.color);
    }

    @Test
    public void testBuildNotificationData_NotChosenType() {
        NotificationHelper.NotificationData data =
                helper.buildNotificationData("Event Update", "Better luck next time!", "not_chosen");

        assertEquals("Event Update", data.title);
        assertEquals("Better luck next time!", data.body);
        assertEquals("not_chosen", data.type);
        assertEquals(0xFFFF9800, data.color);
    }

    @Test
    public void testBuildNotificationData_DefaultType() {
        NotificationHelper.NotificationData data =
                helper.buildNotificationData("Event Info", "Some info", "other");

        assertEquals("Event Info", data.title);
        assertEquals("Some info", data.body);
        assertEquals("other", data.type);
        assertEquals(0xFF2196F3, data.color);
    }
}

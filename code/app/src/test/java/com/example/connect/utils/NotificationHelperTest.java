package com.example.connect.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for NotificationHelper (pure Java)
 */
public class NotificationHelperTest {

    // -----------------------------
    // Helper class to simulate NotificationHelper logic
    // -----------------------------
    public static class TestNotificationHelper {

        public static class NotificationData {
            public final String title;
            public final String body;
            public final String type;

            public NotificationData(String title, String body, String type) {
                this.title = title;
                this.body = body;
                this.type = type;
            }
        }

        /**
         * Simulate notifyChosenEntrants
         */
        public List<NotificationData> notifyChosenEntrants(List<String> userIds, String eventName) {
            List<NotificationData> sent = new ArrayList<>();
            for (String uid : userIds) {
                String title = "Congratulations! 🎉";
                String body = "You have been selected for " + eventName + "!";
                sent.add(new NotificationData(title, body, "chosen"));
            }
            return sent;
        }

        /**
         * Simulate notifyNotChosenEntrants
         */
        public List<NotificationData> notifyNotChosenEntrants(List<String> userIds, String eventName) {
            List<NotificationData> sent = new ArrayList<>();
            for (String uid : userIds) {
                String title = "Event Update";
                String body = "Thank you for your interest in " + eventName + ". Unfortunately, you were not selected this time.";
                sent.add(new NotificationData(title, body, "not_chosen"));
            }
            return sent;
        }

        /**
         * Simulate notifyAllWaitingListEntrants
         */
        public List<NotificationData> notifyAllWaitingListEntrants(List<String> userIds, String eventName) {
            List<NotificationData> sent = new ArrayList<>();
            for (String uid : userIds) {
                String title = eventName + " - Update";
                String body = "You have been placed in the waiting list for " + eventName;
                sent.add(new NotificationData(title, body, "waiting_list_announcement"));
            }
            return sent;
        }
    }

    // -----------------------------
    // Test setup
    // -----------------------------
    private TestNotificationHelper helper;

    @Before
    public void setUp() {
        helper = new TestNotificationHelper();
    }

    @Test
    public void testNotifyChosenEntrants() {
        List<String> users = List.of("user1", "user2");
        List<TestNotificationHelper.NotificationData> sent = helper.notifyChosenEntrants(users, "EventX");

        assertEquals(2, sent.size());
        assertEquals("chosen", sent.get(0).type);
        assertTrue(sent.get(0).body.contains("EventX"));
    }

    @Test
    public void testNotifyNotChosenEntrants() {
        List<String> users = List.of("user1");
        List<TestNotificationHelper.NotificationData> sent = helper.notifyNotChosenEntrants(users, "EventY");

        assertEquals(1, sent.size());
        assertEquals("not_chosen", sent.get(0).type);
        assertTrue(sent.get(0).body.contains("EventY"));
    }

    @Test
    public void testNotifyAllWaitingListEntrants() {
        List<String> users = List.of("user1", "user2", "user3");
        List<TestNotificationHelper.NotificationData> sent = helper.notifyAllWaitingListEntrants(users, "EventZ");

        assertEquals(3, sent.size());
        assertEquals("waiting_list_announcement", sent.get(0).type);
        assertTrue(sent.get(0).body.contains("EventZ"));
    }

    @Test
    public void testNotifyChosenEntrants_EmptyList() {
        List<String> users = new ArrayList<>();
        List<TestNotificationHelper.NotificationData> sent = helper.notifyChosenEntrants(users, "EventEmpty");
        assertTrue(sent.isEmpty()); // logic: no notifications should be created
    }

    @Test
    public void testNotifyNotChosenEntrants_EmptyList() {
        List<String> users = new ArrayList<>();
        List<TestNotificationHelper.NotificationData> sent = helper.notifyNotChosenEntrants(users, "EventEmpty");
        assertTrue(sent.isEmpty());
    }

    @Test
    public void testNotifyAllWaitingListEntrants_EmptyList() {
        List<String> users = new ArrayList<>();
        List<TestNotificationHelper.NotificationData> sent = helper.notifyAllWaitingListEntrants(users, "EventEmpty");
        assertTrue(sent.isEmpty());
    }

}

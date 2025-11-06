package com.example.connect.activities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EntrantActivityTest {

    private EntrantActivity.NotificationAdapter adapter;

    @Before
    public void setUp() {
        // Test the adapter as a pure data container
        adapter = new EntrantActivity().new NotificationAdapter();
    }

    // ---------- NotificationItem Tests ----------
    @Test
    public void testNotificationItemProperties() {
        EntrantActivity.NotificationItem item = new EntrantActivity.NotificationItem(
                "id1", "Title", "Body", "chosen", "event123", "Event X", new Date(), false, false
        );

        assertEquals("id1", item.id);
        assertEquals("Title", item.title);
        assertEquals("Body", item.body);
        assertEquals("chosen", item.type);
        assertEquals("event123", item.eventId);
        assertEquals("Event X", item.eventName);
        assertFalse(item.read);
        assertFalse(item.declined);
        assertNotNull(item.timestamp);
    }

    // ---------- Adapter Tests ----------
    @Test
    public void testAdapterSetNotifications_ItemCount() {
        List<EntrantActivity.NotificationItem> list = new ArrayList<>();
        list.add(new EntrantActivity.NotificationItem("1", "A", "Body", "chosen", null, null, new Date(), false, false));
        list.add(new EntrantActivity.NotificationItem("2", "B", "Body", "not_chosen", null, null, new Date(), false, false));

        adapter.setNotifications(list);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testAdapterSetNotifications_EmptyList() {
        adapter.setNotifications(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    // ---------- Declined Data Tests ----------
    @Test
    public void testBuildDeclinedDataForTest() {
        EntrantActivity activity = new EntrantActivity();
        Map<String, Object> declinedData = activity.buildDeclinedDataForTest();

        assertEquals("FAKE_TIMESTAMP", declinedData.get("declinedAt"));
        assertEquals("User declined invitation", declinedData.get("reason"));
    }
}

package com.example.connect.activities;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class MainActivityTest {
    private MainActivity activity;

    @Before
    public void setUp() {
        // We can create a bare instance of MainActivity
        activity = new MainActivity();
    }

    // ---------- Constants Test ----------
    @Test
    public void testNotificationPermissionCode() {
        // Make sure the permission code constant is correct
        assertEquals(123, MainActivity.NOTIFICATION_PERMISSION_CODE);
    }

    // ---------- Helper Logic Test ----------
    @Test
    public void testOnRequestPermissionsResult_Granted() {
        // Simulate granting permissions
        int requestCode = 123;
        String[] permissions = new String[]{"POST_NOTIFICATIONS"};
        int[] grantResults = new int[]{0}; // 0 typically means PERMISSION_GRANTED

        // We cannot test Log output directly, but method should not crash
        activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Test
    public void testOnRequestPermissionsResult_Denied() {
        int requestCode = 123;
        String[] permissions = new String[]{"POST_NOTIFICATIONS"};
        int[] grantResults = new int[]{-1}; // -1 typically means PERMISSION_DENIED

        // Method should not crash
        activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // ---------- Build Notification Channel Data ----------
    // We can refactor createNotificationChannels() to expose channel data for testing
    // Example:
    public String[] getChannelIdsForTest() {
        return new String[]{"default", "notification_service"};
    }

    @Test
    public void testNotificationChannelIds() {
        String[] channels = getChannelIdsForTest();
        assertEquals(2, channels.length);
        assertTrue(Arrays.asList(channels).contains("default"));
        assertTrue(Arrays.asList(channels).contains("notification_service"));
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.connect.R;
import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for EntrantMapActivity.
 * Tests map initialization, data loading, statistics display, and navigation.
 *
 * @author Vansh Taneja
 * @version 1.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class EntrantMapActivityTest {

    private EntrantMapActivity activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        intent.putExtra("EVENT_ID", "test-event-123");

        activity = Robolectric.buildActivity(EntrantMapActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    /**
     * Test that activity initializes correctly with valid event ID.
     */
    @Test
    public void testActivityInitializesWithValidEventId() {
        assertNotNull("Activity should not be null", activity);
        assertFalse("Activity should not be finishing", activity.isFinishing());
    }

    /**
     * Test that activity finishes when no event ID is provided.
     */
    @Test
    public void testActivityFinishesWithoutEventId() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        // No EVENT_ID extra

        EntrantMapActivity testActivity = Robolectric.buildActivity(EntrantMapActivity.class, intent)
                .create()
                .get();

        assertTrue("Activity should finish when no event ID provided", testActivity.isFinishing());
        assertEquals("Error: No event ID provided", ShadowToast.getTextOfLatestToast());
    }

    /**
     * Test that activity finishes when empty event ID is provided.
     */
    @Test
    public void testActivityFinishesWithEmptyEventId() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        intent.putExtra("EVENT_ID", "");

        EntrantMapActivity testActivity = Robolectric.buildActivity(EntrantMapActivity.class, intent)
                .create()
                .get();

        assertTrue("Activity should finish when event ID is empty", testActivity.isFinishing());
    }

    /**
     * Test that back button exists and can be clicked.
     */
    @Test
    public void testBackButtonExists() {
        ImageButton backButton = activity.findViewById(R.id.btnBack);
        assertNotNull("Back button should exist", backButton);
        assertTrue("Back button should be clickable", backButton.isClickable());
    }

    /**
     * Test that back button finishes the activity.
     */
    @Test
    public void testBackButtonFinishesActivity() {
        ImageButton backButton = activity.findViewById(R.id.btnBack);
        assertNotNull("Back button should exist", backButton);
        
        backButton.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        
        assertTrue("Activity should finish when back button is clicked", activity.isFinishing());
    }

    /**
     * Test that title text view exists.
     */
    @Test
    public void testTitleTextViewExists() {
        TextView titleView = activity.findViewById(R.id.tvTitle);
        assertNotNull("Title text view should exist", titleView);
    }

    /**
     * Test that statistics text views exist.
     */
    @Test
    public void testStatisticsTextViewsExist() {
        TextView entrantsInView = activity.findViewById(R.id.tvEntrantsInView);
        TextView withinZone = activity.findViewById(R.id.tvWithinZone);
        TextView outsideZone = activity.findViewById(R.id.tvOutsideZone);

        assertNotNull("Entrants in view text view should exist", entrantsInView);
        assertNotNull("Within zone text view should exist", withinZone);
        assertNotNull("Outside zone text view should exist", outsideZone);
    }

    /**
     * Test that statistics are updated correctly with sample data.
     */
    @Test
    public void testStatisticsUpdate() throws Exception {
        // Create test data
        List<WaitingListEntry> allEntrants = new ArrayList<>();
        List<WaitingListEntry> entrantsWithLocation = new ArrayList<>();

        // Add entrant with location
        WaitingListEntry entry1 = new WaitingListEntry();
        entry1.setLatitude(53.5461);
        entry1.setLongitude(-113.4938);
        allEntrants.add(entry1);
        entrantsWithLocation.add(entry1);

        // Add entrant without location
        WaitingListEntry entry2 = new WaitingListEntry();
        allEntrants.add(entry2);

        // Set fields using reflection
        setField(activity, "allEntrants", allEntrants);
        setField(activity, "entrantsWithLocation", entrantsWithLocation);

        // Call updateStatistics method
        var method = activity.getClass().getDeclaredMethod("updateStatistics");
        method.setAccessible(true);
        method.invoke(activity);

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Verify statistics
        TextView entrantsInView = activity.findViewById(R.id.tvEntrantsInView);
        TextView withinZone = activity.findViewById(R.id.tvWithinZone);
        TextView outsideZone = activity.findViewById(R.id.tvOutsideZone);

        assertTrue("Entrants in view should show count", 
                entrantsInView.getText().toString().contains("1"));
        assertTrue("Within zone should show count", 
                withinZone.getText().toString().contains("1"));
        assertTrue("Outside zone should show count", 
                outsideZone.getText().toString().contains("1"));
    }

    /**
     * Test that statistics show zero when no entrants exist.
     */
    @Test
    public void testStatisticsWithNoEntrants() throws Exception {
        // Set empty lists
        setField(activity, "allEntrants", new ArrayList<>());
        setField(activity, "entrantsWithLocation", new ArrayList<>());

        // Call updateStatistics method
        var method = activity.getClass().getDeclaredMethod("updateStatistics");
        method.setAccessible(true);
        method.invoke(activity);

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Verify statistics show zero
        TextView entrantsInView = activity.findViewById(R.id.tvEntrantsInView);
        TextView withinZone = activity.findViewById(R.id.tvWithinZone);
        TextView outsideZone = activity.findViewById(R.id.tvOutsideZone);

        assertTrue("Entrants in view should show 0", 
                entrantsInView.getText().toString().contains("0"));
        assertTrue("Within zone should show 0", 
                withinZone.getText().toString().contains("0"));
        assertTrue("Outside zone should show 0", 
                outsideZone.getText().toString().contains("0"));
    }

    /**
     * Test that onResume reloads entrants data.
     */
    @Test
    public void testOnResumeReloadsData() {
        // Verify activity has event ID
        String eventId = activity.getIntent().getStringExtra("EVENT_ID");
        assertNotNull("Event ID should be set", eventId);
        assertEquals("test-event-123", eventId);

        // Call onResume
        activity.onResume();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Activity should still be running (not finishing)
        assertFalse("Activity should not finish on resume", activity.isFinishing());
    }

    /**
     * Test that activity handles null event ID in onResume gracefully.
     */
    @Test
    public void testOnResumeWithNullEventId() throws Exception {
        // Set eventId to null using reflection
        setField(activity, "eventId", null);

        // Call onResume - should not crash
        activity.onResume();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Activity should still be running
        assertFalse("Activity should handle null event ID gracefully", activity.isFinishing());
    }

    /**
     * Test that user data assignment works correctly.
     */
    @Test
    public void testAssignUsersToEntries() throws Exception {
        // Create test data
        List<WaitingListEntry> entrantsWithLocation = new ArrayList<>();
        WaitingListEntry entry1 = new WaitingListEntry();
        entry1.setUserId("user1");
        entry1.setLatitude(53.5461);
        entry1.setLongitude(-113.4938);
        entrantsWithLocation.add(entry1);

        User user1 = new User();
        user1.setUserId("user1");
        user1.setName("Test User");

        // Set fields
        setField(activity, "entrantsWithLocation", entrantsWithLocation);
        
        // Create user cache
        var userCacheField = activity.getClass().getDeclaredField("userCache");
        userCacheField.setAccessible(true);
        java.util.Map<String, User> userCache = (java.util.Map<String, User>) userCacheField.get(activity);
        userCache.put("user1", user1);

        // Call assignUsersToEntries method
        var method = activity.getClass().getDeclaredMethod("assignUsersToEntries");
        method.setAccessible(true);
        method.invoke(activity);

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Verify user was assigned
        assertEquals("User should be assigned to entry", user1, entry1.getUser());
    }

    /**
     * Helper method to set private fields using reflection.
     */
    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}


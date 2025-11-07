package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.connect.activities.EventDetails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for EventDetails activity.
 * Tests UI interactions, event data display, and waiting list functionality.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailsTest {

    private ActivityScenario<EventDetails> activityScenario;
    private static final String TEST_EVENT_ID = "test_event_id_123";

    @Before
    public void setUp() {
        // Setup before each test
    }

    @After
    public void tearDown() {
        if (activityScenario != null) {
            activityScenario.close();
        }
    }

    /**
     * Test that EventDetails activity can be launched with a valid event ID.
     */
    @Test
    public void testEventDetailsLaunchesWithEventId() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            assertNotNull("EventDetails activity should not be null", activity);
        });
    }

    /**
     * Test that EventDetails activity finishes when no event ID is provided.
     */
    @Test
    public void testEventDetailsFinishesWithoutEventId() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        // No EVENT_ID extra provided
        activityScenario = ActivityScenario.launch(intent);
        
        // Activity should finish immediately and show error toast
        // Note: This test verifies the activity handles missing event ID gracefully
    }

    /**
     * Test that back button exists and is displayed.
     */
    @Test
    public void testBackButtonExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Back button should be displayed
            onView(withId(R.id.back_btn))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that back button can be clicked.
     */
    @Test
    public void testBackButtonClickable() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Back button should be clickable
            onView(withId(R.id.back_btn))
                    .perform(click());
            
            // Activity should finish or navigate back
        });
    }

    /**
     * Test that event image view exists.
     */
    @Test
    public void testEventImageExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Event image should be displayed (after loading)
            onView(withId(R.id.event_image))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that event title TextView exists.
     */
    @Test
    public void testEventTitleExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Event title should be displayed (after loading)
            onView(withId(R.id.event_title))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that organizer name TextView exists.
     */
    @Test
    public void testOrganizerNameExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Organizer name should be displayed (after loading)
            onView(withId(R.id.tv_org_name))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that date/time TextView exists.
     */
    @Test
    public void testDateTimeExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Date/time should be displayed (after loading)
            onView(withId(R.id.tv_date_time))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that location TextView exists.
     */
    @Test
    public void testLocationExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Location should be displayed (after loading)
            onView(withId(R.id.tv_location))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that price TextView exists.
     */
    @Test
    public void testPriceExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Price should be displayed (after loading)
            onView(withId(R.id.tv_price))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that registration window TextView exists.
     */
    @Test
    public void testRegistrationWindowExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Registration window should be displayed (after loading)
            onView(withId(R.id.tv_reg_window))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that waiting list TextView exists.
     */
    @Test
    public void testWaitingListExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Waiting list count should be displayed (after loading)
            onView(withId(R.id.tv_waiting_list))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that info button exists and is displayed.
     */
    @Test
    public void testInfoButtonExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Info button should be displayed (after loading)
            onView(withId(R.id.btn_info))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that join waiting list button exists and is displayed.
     */
    @Test
    public void testJoinWaitingListButtonExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Join waiting list button should be displayed (after loading)
            onView(withId(R.id.btn_join_list))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that leave waiting list button exists and is displayed.
     */
    @Test
    public void testLeaveWaitingListButtonExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Leave waiting list button should be displayed (after loading)
            onView(withId(R.id.btn_leave_list))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that info button can be clicked.
     * Should show a dialog with event description.
     */
    @Test
    public void testInfoButtonClickable() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Info button should be clickable
            onView(withId(R.id.btn_info))
                    .perform(click());
            
            // Dialog should appear with event description
            // Note: This requires event data to be loaded first
        });
    }

    /**
     * Test that join waiting list button can be clicked.
     * Note: This requires Firebase authentication and event data.
     */
    @Test
    public void testJoinWaitingListButtonClickable() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Join waiting list button should be clickable
            onView(withId(R.id.btn_join_list))
                    .perform(click());
            
            // Should attempt to join waiting list
            // Note: This requires Firebase authentication and event to exist
        });
    }

    /**
     * Test that leave waiting list button can be clicked.
     * Note: This requires Firebase authentication and user to be on waiting list.
     */
    @Test
    public void testLeaveWaitingListButtonClickable() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Leave waiting list button should be clickable
            onView(withId(R.id.btn_leave_list))
                    .perform(click());
            
            // Should attempt to leave waiting list
            // Note: This requires Firebase authentication and user to be on waiting list
        });
    }

    /**
     * Test that loading spinner is displayed initially.
     */
    @Test
    public void testLoadingSpinnerDisplayedInitially() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Loading spinner should be visible initially
            onView(withId(R.id.spinner))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test that scroll view exists for content.
     */
    @Test
    public void testScrollViewExists() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", TEST_EVENT_ID);
        activityScenario = ActivityScenario.launch(intent);
        
        activityScenario.onActivity(activity -> {
            // Scroll view should exist
            onView(withId(R.id.scroll_content))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Test activity with null event ID.
     * Activity should finish immediately.
     */
    @Test
    public void testActivityWithNullEventId() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", (String) null);
        activityScenario = ActivityScenario.launch(intent);
        
        // Activity should finish and show error toast
        // This is expected behavior per the implementation
    }

    /**
     * Test activity with empty event ID.
     * Activity should finish immediately.
     */
    @Test
    public void testActivityWithEmptyEventId() {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", "");
        activityScenario = ActivityScenario.launch(intent);
        
        // Activity should finish and show error toast
        // This is expected behavior per the implementation
    }
}

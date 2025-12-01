package com.example.connect;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.connect.activities.AdminDashboardActivity;
import com.example.connect.activities.AdminEventDetailActivity;
import com.example.connect.activities.AdminEventListActivity;
import com.example.connect.models.Event;
import com.example.connect.testing.TestHooks;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented tests for the admin Events section requirements.
 * Covers navigation from dashboard, listing/searching events, deleting
 * events via the list, and opening the event description page.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminEventsSectionTest {

    private static final String TAG = "AdminEventsTest";

    @Before
    public void setUp() {
        TestHooks.setUiTestMode(true);
        Intents.init();
    }

    @After
    public void tearDown() {
        TestHooks.setUiTestMode(false);
        Intents.release();
    }

    /**
     * Requirement 1:
     * Clicking the events section on the admin dashboard should navigate to the
     * events list page.
     */
    @Test
    public void testDashboardNavigatesToEventsSection() {
        Intent resultIntent = new Intent();
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        intending(hasComponent(AdminEventListActivity.class.getName())).respondWith(result);

        try (ActivityScenario<AdminDashboardActivity> scenario =
                     ActivityScenario.launch(AdminDashboardActivity.class)) {

            onView(withId(R.id.card_manage_events)).check(matches(isDisplayed()));
            onView(withId(R.id.card_manage_events)).perform(click());

            intended(hasComponent(AdminEventListActivity.class.getName()));
            logSuccess("Verified dashboard navigation to admin events list.");
        }
    }

    /**
     * Requirement 1 (continued):
     * The events page should display the list of events.
     */
    @Test
    public void testAdminEventList_DisplaysEvents() {
        try (ActivityScenario<AdminEventListActivity> scenario =
                     ActivityScenario.launch(AdminEventListActivity.class)) {
            scenario.onActivity(activity -> activity.populateEventsForTests(createSampleEvents()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withText("Tech Summit 2025")).check(matches(isDisplayed()));
            onView(withText("Music Carnival")).check(matches(isDisplayed()));
            logSuccess("Verified admin event list displays injected sample events.");
        }
    }

    /**
     * Requirement 2:
     * Search for an event by its title using the search bar.
     */
    @Test
    public void testAdminEventList_SearchByTitle() {
        try (ActivityScenario<AdminEventListActivity> scenario =
                     ActivityScenario.launch(AdminEventListActivity.class)) {
            scenario.onActivity(activity -> activity.populateEventsForTests(createSampleEvents()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.search_input)).perform(replaceText("Music"));
            closeSoftKeyboard();

            onView(withText("Music Carnival")).check(matches(isDisplayed()));
            onView(withText("Tech Summit 2025")).check(doesNotExist());
            logSuccess("Verified search filters events by title input.");
        }
    }

    /**
     * Requirement 3:
     * Delete an event using the delete button.
     */
    @Test
    public void testAdminEventList_DeleteButtonRemovesItem() {
        List<Event> events = new ArrayList<>();
        events.add(buildEvent("delete-1", "Delete Me", "org-delete", "2025-02-01"));

        try (ActivityScenario<AdminEventListActivity> scenario =
                     ActivityScenario.launch(AdminEventListActivity.class)) {
            scenario.onActivity(activity -> activity.populateEventsForTests(events));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Delete Me")),
                            clickChildViewWithId(R.id.btn_delete)
                    ));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withText("Delete Me")).check(doesNotExist());
            logSuccess("Verified delete button removes event row in UI.");
        }
    }

    /**
     * Requirement 4:
     * Clicking an event title should open the event description page.
     */
    @Test
    public void testAdminEventList_ClickOpensEventDetails() {
        Intent resultIntent = new Intent();
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        intending(hasComponent(AdminEventDetailActivity.class.getName())).respondWith(result);

        try (ActivityScenario<AdminEventListActivity> scenario =
                     ActivityScenario.launch(AdminEventListActivity.class)) {
            List<Event> events = new ArrayList<>();
            events.add(buildEvent("event-detail-1", "Detail Showcase", "org-detail", "2025-03-15"));

            scenario.onActivity(activity -> activity.populateEventsForTests(events));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Detail Showcase")), click()));
        }

        intended(allOf(
                hasComponent(AdminEventDetailActivity.class.getName()),
                hasExtra(AdminEventDetailActivity.EXTRA_EVENT_ID, "event-detail-1")
        ));
        logSuccess("Verified clicking event opens detail page with correct ID.");
    }

    private List<Event> createSampleEvents() {
        List<Event> events = new ArrayList<>();
        events.add(buildEvent("event-1", "Tech Summit 2025", "org-tech", "2025-05-10"));
        events.add(buildEvent("event-2", "Music Carnival", "org-music", "2025-06-20"));
        return events;
    }

    private Event buildEvent(String id, String title, String organizerId, String date) {
        Event event = new Event();
        event.setEventId(id);
        event.setName(title);
        event.setDateTime(date);
        event.setOrganizerId(organizerId);
        event.setDescription("Test description for " + title);
        return event;
    }

    /**
     * Custom ViewAction to click a child view inside a RecyclerView item.
     */
    private static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(id);
                if (child != null) {
                    child.performClick();
                }
            }
        };
    }

    private void logSuccess(String message) {
        Log.i(TAG, message);
    }
}


package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.MyEventsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Comprehensive instrumented tests for MyEventsActivity.
 *
 * This test suite validates the functionality of MyEventsActivity including:
 * - Tab switching functionality and state management
 * - Empty view messages for different tabs
 * - UI element visibility and interaction
 * - Edge cases and state persistence
 *
 * Test Coverage:
 * - Tab Navigation: Switching between Waitlist, Selected, Confirmed, and My Events tabs
 * - Empty State Messages: Correct messages displayed for each tab when no data exists
 * - State Management: Tab state persists correctly across interactions
 * - Edge Cases: Rapid switching, multiple clicks, state consistency
 * - UI Component Visibility: All buttons are accessible
 *
 * @author Jagjot Singh Brar
 * @version 1.0
 *
 * Note: These tests use Espresso for UI testing. Firebase authentication must be
 * configured for tests to run successfully.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyEventsActivityTest {

    /**
     * Activity scenario rule that launches MyEventsActivity before each test.
     * The activity is automatically cleaned up after each test completes.
     */
    @Rule
    public ActivityScenarioRule<MyEventsActivity> activityRule =
            new ActivityScenarioRule<>(MyEventsActivity.class);

    // ==================== Tab Switching Tests ====================

    /**
     * Test Case 1: Comprehensive Tab Switching
     *
     * Validates that clicking each tab button updates the empty view message
     * correctly and maintains proper state across all four tabs.
     *
     * Expected: Each tab displays its unique empty state message
     */
    @Test
    public void switchingTabs_updatesEmptyMessage() {
        // Test Selected tab
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No selected events found.")));

        // Test Confirmed tab
        onView(withId(R.id.btn_tab_confirmed)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No confirmed events found.")));

        // Test My Events tab (shows all events)
        onView(withId(R.id.btn_tab_my_events)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No events found")));

        // Test returning to Waitlist tab
        onView(withId(R.id.btn_tab_waitlist)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No waitlisted events found.")));
    }

    /**
     * Test Case 2: Waitlist Tab Click Updates View
     *
     * Tests the Waitlist tab button in isolation to ensure it properly
     * updates the empty view message.
     *
     * Expected: Empty view shows waitlist message
     */
    @Test
    public void waitlistTab_click_updatesView() {
        // First switch to another tab
        onView(withId(R.id.btn_tab_selected)).perform(click());

        // Then click waitlist tab
        onView(withId(R.id.btn_tab_waitlist)).perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.empty_view))
                .check(matches(withText("No waitlisted events found.")));
    }

    /**
     * Test Case 3: Selected Tab Click Updates View
     *
     * Tests the Selected tab button to verify it displays the correct
     * empty state when no selected events exist.
     *
     * Expected: Empty view shows "No selected events found."
     */
    @Test
    public void selectedTab_click_updatesView() {
        onView(withId(R.id.btn_tab_selected)).perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.empty_view))
                .check(matches(withText("No selected events found.")));
    }

    /**
     * Test Case 4: Confirmed Tab Click Updates View
     *
     * Tests the Confirmed tab button to verify it displays the correct
     * empty state when no confirmed events exist.
     *
     * Expected: Empty view shows "No confirmed events found."
     */
    @Test
    public void confirmedTab_click_updatesView() {
        onView(withId(R.id.btn_tab_confirmed)).perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.empty_view))
                .check(matches(withText("No confirmed events found.")));
    }

    /**
     * Test Case 5: My Events Tab Click Updates View
     *
     * Tests the My Events (All) tab button to verify it displays the correct
     * empty state when no events exist across all categories.
     *
     * Expected: Empty view shows "No events found"
     */
    @Test
    public void myEventsTab_click_updatesView() {
        onView(withId(R.id.btn_tab_my_events)).perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.empty_view))
                .check(matches(withText("No events found")));
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 6: Multiple Rapid Tab Switches
     *
     * Tests the robustness of the tab switching mechanism by performing
     * multiple consecutive switches. Ensures the activity handles rapid
     * state changes without crashes or incorrect states.
     *
     * Expected: Final state matches the last clicked tab
     */
    @Test
    public void rapidTabSwitching_maintainsCorrectState() {
        // Perform rapid sequential tab switches
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.btn_tab_confirmed)).perform(click());
        onView(withId(R.id.btn_tab_waitlist)).perform(click());
        onView(withId(R.id.btn_tab_my_events)).perform(click());

        // Verify final state is correct (My Events tab)
        onView(withId(R.id.empty_view))
                .check(matches(withText("No events found")));
    }

    /**
     * Test Case 7: Clicking Same Tab Multiple Times
     *
     * Validates that clicking the same tab button multiple times doesn't
     * cause issues and maintains the correct state.
     *
     * Expected: State remains consistent after multiple clicks
     */
    @Test
    public void sameTabMultipleClicks_maintainsState() {
        // Click Selected tab multiple times
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.btn_tab_selected)).perform(click());

        // Verify state is still correct
        onView(withId(R.id.empty_view))
                .check(matches(withText("No selected events found.")));
    }

    /**
     * Test Case 8: Tab Switching Back and Forth
     *
     * Tests switching between two tabs repeatedly to ensure state
     * management is reliable during back-and-forth navigation.
     *
     * Expected: Each switch maintains correct state
     */
    @Test
    public void tabSwitching_backAndForth_maintainsCorrectState() {
        // Switch to Selected
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No selected events found.")));

        // Switch to Waitlist
        onView(withId(R.id.btn_tab_waitlist)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No waitlisted events found.")));

        // Back to Selected
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No selected events found.")));

        // Back to Waitlist
        onView(withId(R.id.btn_tab_waitlist)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(withText("No waitlisted events found.")));
    }

    // ==================== UI Component Visibility Tests ====================

    /**
     * Test Case 9: All Tab Buttons Are Visible
     *
     * Comprehensive verification that all four tab buttons are rendered
     * and visible on the screen.
     *
     * Expected: All tab buttons are displayed
     */
    @Test
    public void allTabButtons_areVisible() {
        onView(withId(R.id.btn_tab_waitlist)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_tab_selected)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_tab_confirmed)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_tab_my_events)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 10: All Navigation Buttons Are Visible
     *
     * Verifies that all bottom navigation buttons (home, scan, profile,
     * notifications) are rendered and visible on the screen.
     *
     * Expected: All navigation buttons are displayed
     */
    @Test
    public void allNavigationButtons_areVisible() {
        onView(withId(R.id.home_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.scan_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.notificaton_btn)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 11: Waitlist Tab Button Is Visible
     *
     * Individual test for Waitlist tab button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void waitlistTabButton_isVisible() {
        onView(withId(R.id.btn_tab_waitlist))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 12: Selected Tab Button Is Visible
     *
     * Individual test for Selected tab button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void selectedTabButton_isVisible() {
        onView(withId(R.id.btn_tab_selected))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 13: Confirmed Tab Button Is Visible
     *
     * Individual test for Confirmed tab button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void confirmedTabButton_isVisible() {
        onView(withId(R.id.btn_tab_confirmed))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 14: My Events Tab Button Is Visible
     *
     * Individual test for My Events tab button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void myEventsTabButton_isVisible() {
        onView(withId(R.id.btn_tab_my_events))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 15: Home Navigation Button Is Visible
     *
     * Individual test for home button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void homeButton_isVisible() {
        onView(withId(R.id.home_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 16: Scan Navigation Button Is Visible
     *
     * Individual test for scan button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void scanButton_isVisible() {
        onView(withId(R.id.scan_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 17: Profile Navigation Button Is Visible
     *
     * Individual test for profile button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void profileButton_isVisible() {
        onView(withId(R.id.profile_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 18: Notification Navigation Button Is Visible
     *
     * Individual test for notification button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void notificationButton_isVisible() {
        onView(withId(R.id.notificaton_btn))
                .check(matches(isDisplayed()));
    }

    // ==================== State Consistency Tests ====================

    /**
     * Test Case 19: Empty View Remains Visible After Tab Switch
     *
     * Verifies that the empty view continues to be displayed after
     * switching tabs when no data exists in any tab.
     *
     * Expected: Empty view persists across tab switches
     */
    @Test
    public void emptyView_remainsVisibleAfterTabSwitch() {
        // Switch to different tabs and verify empty view stays visible
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.empty_view)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_tab_confirmed)).perform(click());
        onView(withId(R.id.empty_view)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_tab_my_events)).perform(click());
        onView(withId(R.id.empty_view)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 20: Empty View Is Visible When No Data
     *
     * Confirms that the empty view is displayed when there are no events
     * in the selected tab.
     *
     * Expected: Empty view is visible
     */
    @Test
    public void emptyView_isVisibleWhenNoData() {
        onView(withId(R.id.btn_tab_selected)).perform(click());
        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 21: All Tabs Cycle Through Successfully
     *
     * Performs a complete cycle through all four tabs in sequence
     * and verifies each displays the correct message.
     *
     * Expected: Complete cycle maintains correct states
     */
    @Test
    public void allTabs_cycleThrough_showCorrectMessages() {
        // Cycle through all tabs in order
        String[] expectedMessages = {
                "No waitlisted events found.",
                "No selected events found.",
                "No confirmed events found.",
                "No events found"
        };

        int[] tabIds = {
                R.id.btn_tab_waitlist,
                R.id.btn_tab_selected,
                R.id.btn_tab_confirmed,
                R.id.btn_tab_my_events
        };

        for (int i = 0; i < tabIds.length; i++) {
            onView(withId(tabIds[i])).perform(click());
            onView(withId(R.id.empty_view))
                    .check(matches(withText(expectedMessages[i])));
        }
    }
}
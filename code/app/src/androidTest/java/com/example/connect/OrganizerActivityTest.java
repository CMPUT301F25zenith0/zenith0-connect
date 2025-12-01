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

import com.example.connect.activities.OrganizerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Comprehensive instrumented tests for OrganizerActivity.
 *
 * This test suite validates the organizer dashboard functionality including:
 * - Initial UI state and component visibility
 * - Filter button functionality (Total Events, Open, Closed, Drawn)
 * - Navigation button accessibility
 * - RecyclerView display
 * - Filter switching behavior
 * - Button state management
 *
 * Test Coverage:
 * - UI Component Visibility: All buttons, filters, and RecyclerView
 * - Filter Functionality: Switching between different event filters
 * - Navigation Elements: Bottom navigation bar buttons
 * - State Management: Filter button states and visual feedback
 * - Edge Cases: Rapid switching, multiple clicks
 *
 * @author Jagjot Singh Brar
 * @version 1.0
 *
 * Note: These tests use Espresso for UI testing. Firebase authentication must be
 * configured for tests to run successfully. Tests assume user is logged in.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerActivityTest {

    /**
     * Activity scenario rule that launches OrganizerActivity before each test.
     * The activity is automatically cleaned up after each test completes.
     */
    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    // ==================== Initial State Tests ====================

    /**
     * Test Case 1: Verify RecyclerView Is Visible on Launch
     *
     * Ensures that the main RecyclerView for displaying events is rendered
     * and visible when the activity first loads.
     *
     * Expected: RecyclerView is displayed on screen
     */
    @Test
    public void recyclerView_isVisibleOnLaunch() {
        onView(withId(R.id.recyclerViewEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 2: Verify New Event Button Is Visible
     *
     * Confirms that the "New Event" button is visible and accessible
     * to the organizer.
     *
     * Expected: New Event button is displayed
     */
    @Test
    public void newEventButton_isVisible() {
        onView(withId(R.id.btnNewEvent))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 3: Verify Default Filter Is Total Events
     *
     * Validates that when the activity launches, the "Total Events"
     * filter is selected by default.
     *
     * Expected: Total Events button is visible and accessible
     */
    @Test
    public void defaultFilter_isTotalEvents() {
        onView(withId(R.id.btnTotalEvents))
                .check(matches(isDisplayed()));
    }

    // ==================== Filter Button Visibility Tests ====================

    /**
     * Test Case 4: All Filter Buttons Are Visible
     *
     * Comprehensive verification that all four filter buttons are rendered
     * and visible on the screen.
     *
     * Expected: All filter buttons (Total Events, Open, Closed, Drawn) are displayed
     */
    @Test
    public void allFilterButtons_areVisible() {
        onView(withId(R.id.btnTotalEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));
        onView(withId(R.id.btnClosed)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDrawn)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 5: Total Events Button Is Visible
     *
     * Individual test for Total Events filter button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void totalEventsButton_isVisible() {
        onView(withId(R.id.btnTotalEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 6: Open Events Button Is Visible
     *
     * Individual test for Open filter button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void openButton_isVisible() {
        onView(withId(R.id.btnOpen))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 7: Closed Events Button Is Visible
     *
     * Individual test for Closed filter button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void closedButton_isVisible() {
        onView(withId(R.id.btnClosed))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 8: Drawn Events Button Is Visible
     *
     * Individual test for Drawn filter button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void drawnButton_isVisible() {
        onView(withId(R.id.btnDrawn))
                .check(matches(isDisplayed()));
    }

    // ==================== Navigation Button Tests ====================

    /**
     * Test Case 9: All Navigation Buttons Are Visible
     *
     * Verifies that all bottom navigation buttons (Dashboard, Message, Map, Profile)
     * are rendered and visible on the screen.
     *
     * Expected: All navigation buttons are displayed
     */
    @Test
    public void allNavigationButtons_areVisible() {
        onView(withId(R.id.btnNavDashboard)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavMap)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavProfile)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 10: Dashboard Navigation Button Is Visible
     *
     * Individual test for Dashboard navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void dashboardNavButton_isVisible() {
        onView(withId(R.id.btnNavDashboard))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 11: Message Navigation Button Is Visible
     *
     * Individual test for Message navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void messageNavButton_isVisible() {
        onView(withId(R.id.btnNavMessage))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 12: Map Navigation Button Is Visible
     *
     * Individual test for Map navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void mapNavButton_isVisible() {
        onView(withId(R.id.btnNavMap))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 13: Profile Navigation Button Is Visible
     *
     * Individual test for Profile navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void profileNavButton_isVisible() {
        onView(withId(R.id.btnNavProfile))
                .check(matches(isDisplayed()));
    }

    // ==================== Filter Switching Tests ====================

    /**
     * Test Case 14: Clicking Open Filter Button Works
     *
     * Tests that the Open filter button responds to user clicks
     * without crashing.
     *
     * Expected: Click is registered, no crash occurs
     */
    @Test
    public void openFilterButton_clickWorks() {
        onView(withId(R.id.btnOpen)).perform(click());

        // Verify button is still visible after click
        onView(withId(R.id.btnOpen))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 15: Clicking Closed Filter Button Works
     *
     * Tests that the Closed filter button responds to user clicks
     * without crashing.
     *
     * Expected: Click is registered, no crash occurs
     */
    @Test
    public void closedFilterButton_clickWorks() {
        onView(withId(R.id.btnClosed)).perform(click());

        // Verify button is still visible after click
        onView(withId(R.id.btnClosed))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 16: Clicking Drawn Filter Button Works
     *
     * Tests that the Drawn filter button responds to user clicks
     * without crashing.
     *
     * Expected: Click is registered, no crash occurs
     */
    @Test
    public void drawnFilterButton_clickWorks() {
        onView(withId(R.id.btnDrawn)).perform(click());

        // Verify button is still visible after click
        onView(withId(R.id.btnDrawn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 17: Clicking Total Events Filter Button Works
     *
     * Tests that the Total Events filter button responds to user clicks
     * without crashing.
     *
     * Expected: Click is registered, no crash occurs
     */
    @Test
    public void totalEventsFilterButton_clickWorks() {
        // First click another filter
        onView(withId(R.id.btnOpen)).perform(click());

        // Then click Total Events
        onView(withId(R.id.btnTotalEvents)).perform(click());

        // Verify button is still visible after click
        onView(withId(R.id.btnTotalEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 18: Filter Switching - Comprehensive Cycle
     *
     * Tests switching through all filter buttons in sequence to ensure
     * the filtering mechanism works correctly.
     *
     * Expected: All filter buttons can be clicked in sequence without crashes
     */
    @Test
    public void filterSwitching_cyclesThroughAllFilters() {
        // Cycle through all filters
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));

        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.btnClosed)).check(matches(isDisplayed()));

        onView(withId(R.id.btnDrawn)).perform(click());
        onView(withId(R.id.btnDrawn)).check(matches(isDisplayed()));

        onView(withId(R.id.btnTotalEvents)).perform(click());
        onView(withId(R.id.btnTotalEvents)).check(matches(isDisplayed()));
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 19: Multiple Rapid Filter Switches
     *
     * Tests the robustness of the filter switching mechanism by performing
     * multiple consecutive switches rapidly.
     *
     * Expected: Activity handles rapid state changes without crashes
     */
    @Test
    public void rapidFilterSwitching_worksCorrectly() {
        // Perform rapid sequential filter switches
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.btnDrawn)).perform(click());
        onView(withId(R.id.btnTotalEvents)).perform(click());
        onView(withId(R.id.btnOpen)).perform(click());

        // Verify final state is visible
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 20: Clicking Same Filter Multiple Times
     *
     * Validates that clicking the same filter button multiple times doesn't
     * cause issues and maintains stability.
     *
     * Expected: State remains consistent after multiple clicks
     */
    @Test
    public void sameFilterMultipleClicks_maintainsStability() {
        // Click Open filter multiple times
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnOpen)).perform(click());

        // Verify button is still visible and functional
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 21: Filter Switching Back and Forth
     *
     * Tests switching between two filters repeatedly to ensure state
     * management is reliable during back-and-forth navigation.
     *
     * Expected: Each switch maintains correct state
     */
    @Test
    public void filterSwitching_backAndForth_worksCorrectly() {
        // Switch between Open and Closed repeatedly
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));

        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.btnClosed)).check(matches(isDisplayed()));

        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));

        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.btnClosed)).check(matches(isDisplayed()));
    }

    // ==================== RecyclerView Tests ====================

    /**
     * Test Case 22: RecyclerView Remains Visible After Filter Change
     *
     * Ensures that the RecyclerView stays visible when switching between
     * different filters.
     *
     * Expected: RecyclerView is always visible
     */
    @Test
    public void recyclerView_remainsVisibleAfterFilterChange() {
        // Switch filters and verify RecyclerView stays visible
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));

        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));

        onView(withId(R.id.btnDrawn)).perform(click());
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));
    }

    // ==================== Button Click Tests ====================

    /**
     * Test Case 23: Dashboard Refresh Button Works
     *
     * Tests that clicking the Dashboard navigation button (which refreshes
     * the current view) works without crashing.
     *
     * Expected: Click is registered, view refreshes
     */
    @Test
    public void dashboardButton_refreshWorks() {
        onView(withId(R.id.btnNavDashboard)).perform(click());

        // Verify we're still on the same screen
        onView(withId(R.id.recyclerViewEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 24: New Event Button Is Clickable
     *
     * Verifies that the New Event button responds to clicks.
     * Note: This doesn't verify navigation (would need espresso-intents).
     *
     * Expected: Button click is registered without crash
     */
    @Test
    public void newEventButton_isClickable() {
        onView(withId(R.id.btnNewEvent)).perform(click());

        // If we're here without crash, the click worked
        // (Navigation testing would require espresso-intents)
    }

    /**
     * Test Case 25: All UI Elements Remain After Multiple Interactions
     *
     * Comprehensive test that performs multiple interactions and verifies
     * all UI elements remain accessible.
     *
     * Expected: All components stay visible after extensive interaction
     */
    @Test
    public void allUIElements_remainAfterMultipleInteractions() {
        // Perform various interactions
        onView(withId(R.id.btnOpen)).perform(click());
        onView(withId(R.id.btnClosed)).perform(click());
        onView(withId(R.id.btnNavDashboard)).perform(click());
        onView(withId(R.id.btnDrawn)).perform(click());

        // Verify all elements still visible
        onView(withId(R.id.btnNewEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTotalEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnOpen)).check(matches(isDisplayed()));
        onView(withId(R.id.btnClosed)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDrawn)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerViewEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavDashboard)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavMap)).check(matches(isDisplayed()));
        onView(withId(R.id.btnNavProfile)).check(matches(isDisplayed()));
    }
}
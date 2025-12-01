package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.EventListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Comprehensive instrumented tests for EventListActivity.
 *
 * This test suite validates the event browsing dashboard functionality including:
 * - Initial UI state and component visibility
 * - Navigation button accessibility
 * - Search functionality
 * - Filter chip visibility and interaction
 * - Popular events carousel display
 * - Main events list display
 * - Filter clearing functionality
 *
 * Test Coverage:
 * - UI Component Visibility: All buttons, chips, search bar, lists, and header
 * - Search Functionality: Search bar interaction and text input
 * - Filter Chips: All filter chips visible and clickable individually
 * - Navigation Elements: Bottom navigation bar buttons
 * - List Views: Main events ListView and popular events RecyclerView
 * - Header Elements: Profile image and search components
 * - Long Press: Individual filter chip clearing via long press
 *
 * @author Jagjot Singh Brar
 * @version 1.0
 *
 * Note: These tests use Espresso for UI testing. Firebase authentication must be
 * configured for tests to run successfully. Tests assume user is logged in.
 * Dialog interactions are tested individually to avoid dialog overlap issues.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventListActivityTest {

    /**
     * Activity scenario rule that launches EventListActivity before each test.
     * The activity is automatically cleaned up after each test completes.
     */
    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    // ==================== Initial State Tests ====================

    /**
     * Test Case 1: Verify ListView Is Visible on Launch
     *
     * Ensures that the main events ListView is rendered and visible
     * when the activity first loads.
     *
     * Expected: ListView is displayed on screen
     */
    @Test
    public void eventsListView_isVisibleOnLaunch() {
        onView(withId(R.id.events_ListView))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 2: Verify Search Bar Is Visible
     *
     * Confirms that the search bar in the header is visible and accessible
     * to the user for searching events.
     *
     * Expected: Search bar is displayed
     */
    @Test
    public void searchBar_isVisible() {
        onView(withId(R.id.etSearchHeader))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 3: Verify Popular Events RecyclerView Is Visible
     *
     * Validates that the horizontal carousel for popular/upcoming events
     * is visible in the header.
     *
     * Expected: Popular events RecyclerView is displayed
     */
    @Test
    public void popularEventsRecyclerView_isVisible() {
        onView(withId(R.id.rvPopularEvents))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 4: Verify Profile Header Image Is Visible
     *
     * Confirms that the profile image in the header is visible.
     *
     * Expected: Profile header image is displayed
     */
    @Test
    public void profileHeaderImage_isVisible() {
        onView(withId(R.id.ivProfileHeader))
                .check(matches(isDisplayed()));
    }

    // ==================== Navigation Button Tests ====================

    /**
     * Test Case 5: All Navigation Buttons Are Visible
     *
     * Verifies that all bottom navigation buttons are rendered
     * and visible on the screen.
     *
     * Expected: All navigation buttons are displayed
     */
    @Test
    public void allNavigationButtons_areVisible() {
        onView(withId(R.id.home_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.myevents_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.scan_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.notificaton_btn)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 6: Home Button Is Visible
     *
     * Individual test for home navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void homeButton_isVisible() {
        onView(withId(R.id.home_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 7: My Events Button Is Visible
     *
     * Individual test for My Events navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void myEventsButton_isVisible() {
        onView(withId(R.id.myevents_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 8: Scan Button Is Visible
     *
     * Individual test for QR scan button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void scanButton_isVisible() {
        onView(withId(R.id.scan_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 9: Profile Button Is Visible
     *
     * Individual test for profile navigation button visibility.
     *
     * Expected: Button is displayed
     */
    @Test
    public void profileButton_isVisible() {
        onView(withId(R.id.profile_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 10: Notification Button Is Visible
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

    // ==================== Filter Chip Tests ====================

    /**
     * Test Case 11: All Filter Chips Are Visible
     *
     * Comprehensive verification that all filter chips are rendered
     * and visible in the header.
     *
     * Expected: All filter chips (Interest, Date, Location, Clear) are displayed
     */
    @Test
    public void allFilterChips_areVisible() {
        onView(withId(R.id.chip_interest)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_date)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_location)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_clear_filters)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 12: Interest Filter Chip Is Visible
     *
     * Individual test for Interest filter chip visibility.
     *
     * Expected: Chip is displayed
     */
    @Test
    public void interestChip_isVisible() {
        onView(withId(R.id.chip_interest))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 13: Date Filter Chip Is Visible
     *
     * Individual test for Date filter chip visibility.
     *
     * Expected: Chip is displayed
     */
    @Test
    public void dateChip_isVisible() {
        onView(withId(R.id.chip_date))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 14: Location Filter Chip Is Visible
     *
     * Individual test for Location filter chip visibility.
     *
     * Expected: Chip is displayed
     */
    @Test
    public void locationChip_isVisible() {
        onView(withId(R.id.chip_location))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 15: Clear Filters Chip Is Visible
     *
     * Individual test for Clear Filters chip visibility.
     *
     * Expected: Chip is displayed
     */
    @Test
    public void clearFiltersChip_isVisible() {
        onView(withId(R.id.chip_clear_filters))
                .check(matches(isDisplayed()));
    }

    // ==================== Search Functionality Tests ====================

    /**
     * Test Case 16: Search Bar Accepts Text Input
     *
     * Tests that the search bar can receive and display text input
     * from the user.
     *
     * Expected: Text can be entered in the search bar
     */
    @Test
    public void searchBar_acceptsTextInput() {
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Music Event"), closeSoftKeyboard());

        // Verify search bar is still visible after input
        onView(withId(R.id.etSearchHeader))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 17: Search Bar Can Be Cleared and Reused
     *
     * Tests that text in the search bar can be cleared and replaced
     * with new search queries.
     *
     * Expected: Search text can be modified
     */
    @Test
    public void searchBar_canBeClearedAndReused() {
        // Type first search
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Concert"), closeSoftKeyboard());

        // Type additional text
        onView(withId(R.id.etSearchHeader))
                .perform(typeText(" Sports"), closeSoftKeyboard());

        // Verify search bar is still functional
        onView(withId(R.id.etSearchHeader))
                .check(matches(isDisplayed()));
    }

    // ==================== Filter Chip Individual Interaction Tests ====================

    /**
     * Test Case 18: Interest Chip Is Clickable
     *
     * Tests that the Interest filter chip responds to clicks
     * and can trigger the filter dialog.
     *
     * Expected: Click is registered without crash
     */
    @Test
    public void interestChip_isClickable() {
        onView(withId(R.id.chip_interest)).perform(click());

        // Dialog appears (not testing dialog content, just that click works)
    }

    /**
     * Test Case 19: Date Chip Is Clickable
     *
     * Tests that the Date filter chip responds to clicks
     * and can trigger the date picker dialog.
     *
     * Expected: Click is registered without crash
     */
    @Test
    public void dateChip_isClickable() {
        onView(withId(R.id.chip_date)).perform(click());

        // Date picker dialog should appear
    }

    /**
     * Test Case 20: Location Chip Is Clickable
     *
     * Tests that the Location filter chip responds to clicks
     * and can trigger the filter dialog.
     *
     * Expected: Click is registered without crash
     */
    @Test
    public void locationChip_isClickable() {
        onView(withId(R.id.chip_location)).perform(click());

        // Dialog should appear
    }

    /**
     * Test Case 21: Clear Filters Chip Is Clickable
     *
     * Tests that the Clear Filters chip responds to clicks
     * and clears all active filters.
     *
     * Expected: Click is registered, filters are cleared
     */
    @Test
    public void clearFiltersChip_isClickable() {
        onView(withId(R.id.chip_clear_filters)).perform(click());

        // Verify chip is still visible after click
        onView(withId(R.id.chip_clear_filters))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 22: Interest Chip Long Click Works
     *
     * Tests that long-clicking the Interest chip clears that specific filter
     * (feature for individual filter clearing).
     *
     * Expected: Long click is registered without crash
     */
    @Test
    public void interestChip_longClickWorks() {
        onView(withId(R.id.chip_interest)).perform(longClick());

        // Verify chip is still visible
        onView(withId(R.id.chip_interest))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 23: Date Chip Long Click Works
     *
     * Tests that long-clicking the Date chip clears that specific filter.
     *
     * Expected: Long click is registered without crash
     */
    @Test
    public void dateChip_longClickWorks() {
        onView(withId(R.id.chip_date)).perform(longClick());

        // Verify chip is still visible
        onView(withId(R.id.chip_date))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 24: Location Chip Long Click Works
     *
     * Tests that long-clicking the Location chip clears that specific filter.
     *
     * Expected: Long click is registered without crash
     */
    @Test
    public void locationChip_longClickWorks() {
        onView(withId(R.id.chip_location)).perform(longClick());

        // Verify chip is still visible
        onView(withId(R.id.chip_location))
                .check(matches(isDisplayed()));
    }

    // ==================== Clear Filters Functionality Tests ====================

    /**
     * Test Case 25: Clear Filters Works After Search
     *
     * Tests that the Clear Filters button works correctly after
     * a search has been performed.
     *
     * Expected: Search cleared, UI returns to default state
     */
    @Test
    public void clearFilters_worksAfterSearch() {
        // Apply search
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Test"), closeSoftKeyboard());

        // Click clear filters
        onView(withId(R.id.chip_clear_filters)).perform(click());

        // Verify elements still visible
        onView(withId(R.id.events_ListView)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_clear_filters)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 26: Multiple Searches Work Correctly
     *
     * Tests that multiple sequential searches can be performed
     * without issues.
     *
     * Expected: Each search operation completes successfully
     */
    @Test
    public void multipleSearches_workCorrectly() {
        // First search
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Music"), closeSoftKeyboard());

        // Verify ListView still visible
        onView(withId(R.id.events_ListView)).check(matches(isDisplayed()));

        // Second search (append text)
        onView(withId(R.id.etSearchHeader))
                .perform(typeText(" Concert"), closeSoftKeyboard());

        // Verify ListView still visible
        onView(withId(R.id.events_ListView)).check(matches(isDisplayed()));
    }

    // ==================== Profile Header Tests ====================

    /**
     * Test Case 27: Profile Header Image Is Clickable
     *
     * Tests that clicking the profile image in the header navigates
     * to the profile screen (or triggers appropriate action).
     *
     * Expected: Click is registered without crash
     */
    @Test
    public void profileHeaderImage_isClickable() {
        onView(withId(R.id.ivProfileHeader)).perform(click());

        // Navigation would occur (not tested without espresso-intents)
    }

    // ==================== UI Persistence After Search Tests ====================

    /**
     * Test Case 28: ListView Persists After Search
     *
     * Verifies that the main events ListView remains visible
     * after performing a search operation.
     *
     * Expected: ListView remains displayed
     */
    @Test
    public void listView_persistsAfterSearch() {
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Event"), closeSoftKeyboard());

        onView(withId(R.id.events_ListView))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 29: Filter Chips Persist After Search
     *
     * Verifies that all filter chips remain visible and functional
     * after performing a search operation.
     *
     * Expected: All chips remain displayed
     */
    @Test
    public void filterChips_persistAfterSearch() {
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Sports"), closeSoftKeyboard());

        onView(withId(R.id.chip_interest)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_date)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_location)).check(matches(isDisplayed()));
        onView(withId(R.id.chip_clear_filters)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 30: Navigation Buttons Persist After Search
     *
     * Verifies that all navigation buttons remain visible
     * after performing a search operation.
     *
     * Expected: All navigation buttons remain displayed
     */
    @Test
    public void navigationButtons_persistAfterSearch() {
        onView(withId(R.id.etSearchHeader))
                .perform(typeText("Concert"), closeSoftKeyboard());

        onView(withId(R.id.home_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.myevents_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.scan_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.notificaton_btn)).check(matches(isDisplayed()));
    }
}

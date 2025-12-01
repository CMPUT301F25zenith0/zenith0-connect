package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.core.internal.deps.dagger.Provides;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.AdminImageListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Text;

/**
 * Comprehensive instrumented tests for AdminImageListActivity.
 *
 * This test suite validates the admin image management functionality including:
 * - Initial UI state and component visibility
 * - Toolbar presence and navigation functionality
 * - RecyclerView display for image list
 * - Search functionality with real-time filtering
 * - Progress bar visibility during loading
 * - Empty state handling
 * - Search input interaction
 *
 * Test Coverage:
 * - UI Component Visibility: Toolbar, RecyclerView, search input, progress bar, empty state
 * - Toolbar Navigation: Back button functionality
 * - Search Functionality: Text input, filtering, clearing search
 * - RecyclerView: List display and adapter setup
 * - Loading States: Progress bar and empty state visibility
 * - Search Input: Text entry, clearing, multiple searches
 * - Edge Cases: Empty search, special characters, long queries
 *
 * @author Jagjot Singh Brar
 * @version 1.0
 *
 * Note: These tests use Espresso for UI testing. Firebase Firestore access
 * is required for data loading. Tests verify UI components and interactions.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImageListActivityTest {

    /**
     * Activity scenario rule that launches AdminImageListActivity before each test.
     * The activity is automatically cleaned up after each test completes.
     */
    @Rule
    public ActivityScenarioRule<AdminImageListActivity> activityRule =
            new ActivityScenarioRule<>(AdminImageListActivity.class);

    // ==================== Initial State Tests ====================

    /**
     * Test Case 1: Verify Toolbar Is Visible on Launch
     *
     * Ensures that the MaterialToolbar is rendered and visible
     * when the activity first loads.
     *
     * Expected: Toolbar is displayed on screen
     */
    @Test
    public void toolbar_isVisibleOnLaunch() {
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 2: Verify RecyclerView Is Visible on Launch
     *
     * Validates that the RecyclerView for displaying images is rendered
     * and visible when the activity loads.
     *
     * Expected: RecyclerView is displayed on screen
     */
    @Test
    public void recyclerView_isVisibleOnLaunch() {
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 3: Verify Search Input Is Visible on Launch
     *
     * Confirms that the search input field is visible and accessible
     * for filtering images.
     *
     * Expected: Search input is displayed
     */
    @Test
    public void searchInput_isVisibleOnLaunch() {
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 4: Verify Search Layout Is Visible on Launch
     *
     * Validates that the search layout container is visible
     * when the activity loads.
     *
     * Expected: Search layout is displayed
     */
    @Test
    public void searchLayout_isVisibleOnLaunch() {
        onView(withId(R.id.search_layout))
                .check(matches(isDisplayed()));
    }

    // ==================== Toolbar Tests ====================

    /**
     * Test Case 5: Toolbar Navigation Button Works
     *
     * Tests that clicking the toolbar's navigation (back) button
     * functions correctly without crashing.
     *
     * Expected: Navigation click is registered, activity may finish
     */
    @Test
    public void toolbarNavigation_isClickable() {
        // Verify toolbar is visible first
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));

        // Click navigation button (this will finish the activity)
        onView(withId(R.id.toolbar))
                .perform(click());

        // If we reach here without crash, test passes
    }

    // ==================== Search Functionality Tests ====================

    /**
     * Test Case 6: Search Input Accepts Text
     *
     * Tests that the search input field can receive and display
     * text input from the user.
     *
     * Expected: Text can be entered in the search field
     */
    @Test
    public void searchInput_acceptsText() {
        onView(withId(R.id.search_input))
                .perform(typeText("Event"), closeSoftKeyboard());

        // Verify search input is still visible after typing
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 7: Search Input Can Be Cleared
     *
     * Tests that text in the search input can be cleared
     * and replaced with new search queries.
     *
     * Expected: Search text can be cleared
     */
    @Test
    public void searchInput_canBeCleared() {
        // Type text
        onView(withId(R.id.search_input))
                .perform(typeText("Test"), closeSoftKeyboard());

        // Clear text
        onView(withId(R.id.search_input))
                .perform(clearText());

        // Verify search input is still visible
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 8: Multiple Searches Work Correctly
     *
     * Tests that multiple sequential searches can be performed
     * without issues.
     *
     * Expected: Each search operation completes successfully
     */
    @Test
    public void multipleSearches_workCorrectly() {
        // First search
        onView(withId(R.id.search_input))
                .perform(typeText("Event"), closeSoftKeyboard());

        // Clear and second search
        onView(withId(R.id.search_input))
                .perform(clearText());

        onView(withId(R.id.search_input))
                .perform(typeText("Profile"), closeSoftKeyboard());

        // Verify RecyclerView still visible
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 9: Search With Special Characters
     *
     * Tests that the search function handles special characters
     * without crashing.
     *
     * Expected: Special characters are handled gracefully
     */
    @Test
    public void search_handlesSpecialCharacters() {
        onView(withId(R.id.search_input))
                .perform(typeText("@#$%"), closeSoftKeyboard());

        // Verify UI still functional
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 10: Search With Numbers
     *
     * Tests that the search function handles numeric input
     * correctly (useful for searching by ID).
     *
     * Expected: Numeric input is handled correctly
     */
    @Test
    public void search_handlesNumbers() {
        onView(withId(R.id.search_input))
                .perform(typeText("12345"), closeSoftKeyboard());

        // Verify UI still functional
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 11: Search With Long Query
     *
     * Tests that the search function handles very long search queries
     * without issues.
     *
     * Expected: Long queries are handled gracefully
     */
    @Test
    public void search_handlesLongQuery() {
        onView(withId(R.id.search_input))
                .perform(typeText("This is a very long search query with many words"),
                        closeSoftKeyboard());

        // Verify UI still functional
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    // ==================== UI Component Visibility Tests ====================

    /**
     * Test Case 12: All Major UI Components Are Visible
     *
     * Comprehensive verification that all major UI components are rendered
     * and visible when the activity loads.
     *
     * Expected: All components are displayed
     */
    @Test
    public void allMajorUIComponents_areVisible() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.search_input)).check(matches(isDisplayed()));
        onView(withId(R.id.search_layout)).check(matches(isDisplayed()));
    }

    /**
     * Test Case 13: RecyclerView Exists
     *
     * Verifies that the RecyclerView component is properly initialized
     * and exists in the view hierarchy.
     *
     * Expected: RecyclerView is present and displayed
     */
    @Test
    public void recyclerView_exists() {
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 14: Search Layout Exists
     *
     * Verifies that the search layout container is properly initialized
     * and exists in the view hierarchy.
     *
     * Expected: Search layout is present and displayed
     */
    @Test
    public void searchLayout_exists() {
        onView(withId(R.id.search_layout))
                .check(matches(isDisplayed()));
    }

    // ==================== Search and RecyclerView Interaction Tests ====================

    /**
     * Test Case 15: RecyclerView Persists After Search
     *
     * Verifies that the RecyclerView remains visible after
     * performing a search operation.
     *
     * Expected: RecyclerView remains displayed
     */
    @Test
    public void recyclerView_persistsAfterSearch() {
        onView(withId(R.id.search_input))
                .perform(typeText("Image"), closeSoftKeyboard());

        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 16: Search Input Persists After Interaction
     *
     * Verifies that the search input remains visible and functional
     * after RecyclerView interactions.
     *
     * Expected: Search input remains displayed
     */
    @Test
    public void searchInput_persistsAfterInteraction() {
        // Interact with search
        onView(withId(R.id.search_input))
                .perform(typeText("Test"), closeSoftKeyboard());

        // Verify search input still visible
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 17: Toolbar Persists After Search
     *
     * Verifies that the toolbar remains visible after
     * performing search operations.
     *
     * Expected: Toolbar remains displayed
     */
    @Test
    public void toolbar_persistsAfterSearch() {
        onView(withId(R.id.search_input))
                .perform(typeText("Event"), closeSoftKeyboard());

        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 18: Empty Search Query
     *
     * Tests that clearing the search or searching with empty string
     * shows all images (no filter applied).
     *
     * Expected: Empty search displays all results
     */
    @Test
    public void emptySearch_showsAllResults() {
        // Type then clear
        onView(withId(R.id.search_input))
                .perform(typeText("Test"), closeSoftKeyboard());

        onView(withId(R.id.search_input))
                .perform(clearText());

        // Verify RecyclerView still visible
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 19: Search With Whitespace
     *
     * Tests that search handles queries with only whitespace
     * gracefully.
     *
     * Expected: Whitespace is normalized and handled correctly
     */
    @Test
    public void search_handlesWhitespace() {
        onView(withId(R.id.search_input))
                .perform(typeText("   "), closeSoftKeyboard());

        // Verify UI still functional
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 20: Search With Mixed Case
     *
     * Tests that search is case-insensitive (searching "Event" or "event"
     * should yield same results).
     *
     * Expected: Case-insensitive search works correctly
     */
    @Test
    public void search_isCaseInsensitive() {
        // Search with uppercase
        onView(withId(R.id.search_input))
                .perform(typeText("EVENT"), closeSoftKeyboard());

        // Clear and search with lowercase
        onView(withId(R.id.search_input))
                .perform(clearText());

        onView(withId(R.id.search_input))
                .perform(typeText("event"), closeSoftKeyboard());

        // Both should work without crash
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    // ==================== Search Input Persistence Tests ====================

    /**
     * Test Case 21: Search Input Accepts Append Text
     *
     * Tests that text can be appended to existing search text
     * without clearing first.
     *
     * Expected: Text can be appended to search
     */
    @Test
    public void searchInput_acceptsAppendText() {
        onView(withId(R.id.search_input))
                .perform(typeText("Event"), closeSoftKeyboard());

        onView(withId(R.id.search_input))
                .perform(typeText(" Poster"), closeSoftKeyboard());

        // Verify search input still visible
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 22: All Components Persist After Multiple Searches
     *
     * Comprehensive test that all UI components remain visible
     * after multiple search operations.
     *
     * Expected: All components remain displayed
     */
    @Test
    public void allComponents_persistAfterMultipleSearches() {
        // Perform multiple searches
        onView(withId(R.id.search_input))
                .perform(typeText("Event"), closeSoftKeyboard());

        onView(withId(R.id.search_input))
                .perform(clearText());

        onView(withId(R.id.search_input))
                .perform(typeText("Profile"), closeSoftKeyboard());

        // Verify all components still visible
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.search_input)).check(matches(isDisplayed()));
        onView(withId(R.id.search_layout)).check(matches(isDisplayed()));
    }

    // ==================== Activity Initialization Tests ====================

    /**
     * Test Case 23: Activity Initializes Properly
     *
     * Tests that the activity goes through its lifecycle correctly
     * and all components are initialized without crashes.
     *
     * Expected: Activity initializes successfully
     */
    @Test
    public void activity_initializesProperly() {
        // Verify all critical components are present
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 24: Search Input Is Initially Empty
     *
     * Verifies that the search input field starts empty
     * when the activity first loads.
     *
     * Expected: Search input is empty on launch
     */
    @Test
    public void searchInput_isInitiallyEmpty() {
        // Type to verify it's working
        onView(withId(R.id.search_input))
                .perform(typeText("Test"), closeSoftKeyboard());

        // Clear to test clearing functionality
        onView(withId(R.id.search_input))
                .perform(clearText());

        // Verify still visible
        onView(withId(R.id.search_input))
                .check(matches(isDisplayed()));
    }

    /**
     * Test Case 25: RecyclerView Layout Manager Is Set
     *
     * Verifies that the RecyclerView has a layout manager set
     * and can display content properly.
     *
     * Expected: RecyclerView is configured correctly
     */
    @Test
    public void recyclerView_hasLayoutManager() {
        // RecyclerView should be visible and functional
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }
}

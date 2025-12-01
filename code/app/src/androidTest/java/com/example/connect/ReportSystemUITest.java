package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;

import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.activities.AdminReportActivity;

import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for the Admin Reporting System.
 *
 * <p>This class uses Espresso to verify the UI behavior of the {@link AdminReportActivity}.
 * It covers the following critical user flows:
 * <ul>
 * <li>Filtering the report list via the search bar.</li>
 * <li>Verifying the existence of buttons within RecyclerView items.</li>
 * <li>Opening the "Details" modal dialog.</li>
 * <li>Dismissing the modal dialog and returning to the main list.</li>
 * </ul>
 *
 * <p>Note: To-Test, run the app and login with admin account **IMPORTANT
 */
@RunWith(AndroidJUnit4.class)
public class ReportSystemUITest {

    // Launches AdminReportActivity before every test
    @Rule
    public ActivityScenarioRule<AdminReportActivity> activityRule =
            new ActivityScenarioRule<>(AdminReportActivity.class);

     /**
     * TEST 1: The Search Logic Test
     * Verifies that typing text filters the RecyclerView.
     */
     @Test
     public void testSearchFilterUpdatesList() {
         try { Thread.sleep(3000); } catch (InterruptedException e) {}

         // replace text with gibberish
         onView(withId(R.id.search_input))
                 .perform(replaceText("XJZ_RANDOM_STRING_999"), closeSoftKeyboard());

         // Check for PARTIAL match (Safer)
         // This ensures it passes whether it says "No reports found." or "No reports found matching..."
         onView(withId(R.id.tv_empty_state))
                 .check(matches(isDisplayed()))
                 .check(matches(withText(org.hamcrest.Matchers.containsString("No reports found"))));
     }

    /**
     * TEST 2: Admin List Element Visibility
     * Verifies that the specific UI components of a list item are displayed.
     * (Requires at least one report to be in the database).
     */
    @Test
    public void testReportItemElements() {
        // Wait for data to load (Thread.sleep is bad practice but useful for simple verification)
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));

        // We check if the "Details" button exists on the screen
        onView(withText("Details")).check(matches(isDisplayed()));
        onView(withText("Resolve")).check(matches(isDisplayed()));
    }

    /**
     * TEST 3: Dialog Interaction
     * Clicks "Details" and ensures the dialog opens.
     */
    @Test
    public void testOpenDetailsDialog() {
        // Wait for list
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // Click Details
        onView(withId(R.id.recycler_view))
                .perform(androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.btn_report_details)));

        // Check if Dialog Title appears
        onView(withId(R.id.tv_detail_severity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * TEST 4: Dialog Dissmisal
     * Clicks "Details" and ensures the dialog opens.
     */
    @Test
    public void testCancelDismissesDialog() {
        // Wait for data to load
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // Click "Details" (This is on the Main Screen, so no change here)
        onView(withId(R.id.recycler_view))
                .perform(androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.btn_report_details)));

        // Verify the Dialog is open
        onView(withId(R.id.tv_detail_severity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Click the "Cancel" button inside the dialog
        onView(withId(R.id.btn_detail_cancel))
                .inRoot(isDialog())
                .perform(click());

        // Verify the Dialog is GONE
        // We check that the text does not exist anywhere anymore
        onView(withId(R.id.tv_detail_severity)).check(doesNotExist());

        // Verify the Main Screen is visible again
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
    }
}
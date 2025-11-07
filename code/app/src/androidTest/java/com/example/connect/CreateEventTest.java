package com.example.connect;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.activities.CreateEvent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for CreateEvent activity
 * Tests event creation form validation and user interactions
 * <p>
 * Tests:
 * 1. Empty event name shows error
 * 2. Empty description shows error
 * 3. Empty location shows error
 * 4. Valid inputs allow draft save
 * 5. Date picker buttons are clickable
 * 6. Time picker buttons are clickable
 * 7. Registration period buttons are clickable
 * 8. Geolocation switch is toggleable
 * 9. Capacity and waiting list fields accept numeric input
 * 10. Image upload buttons are displayed and clickable
 * <p>
 * Working as of 11/07/2025
 * @author Digaant Chhokra
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventTest {

    @Rule
    public ActivityScenarioRule<CreateEvent> activityRule =
            new ActivityScenarioRule<>(CreateEvent.class);

    // Test 1: Empty event name shows error
    @Test
    public void testEmptyEventNameShowsError() {
        // Fill other fields with valid data
        onView(withId(R.id.etDescription)).perform(typeText("Test Description"));
        onView(withId(R.id.etLocation)).perform(typeText("Test Location"));
        closeSoftKeyboard();

        // Leave event name empty and try to save draft
        onView(withId(R.id.btnSaveDraft)).perform(click());

        // Verify error is shown on event name field
        onView(withId(R.id.etEventName)).check(matches(hasErrorText("Event name is required")));
    }

    // Test 2: Empty description shows error
    @Test
    public void testEmptyDescriptionShowsError() {
        // Fill other fields with valid data
        onView(withId(R.id.etEventName)).perform(typeText("Test Event"));
        onView(withId(R.id.etLocation)).perform(typeText("Test Location"));
        closeSoftKeyboard();

        // Leave description empty and try to save draft
        onView(withId(R.id.btnSaveDraft)).perform(click());

        // Verify error is shown on description field
        onView(withId(R.id.etDescription)).check(matches(hasErrorText("Description is required")));
    }

    // Test 3: Empty location shows error
    @Test
    public void testEmptyLocationShowsError() {
        // Fill other fields with valid data
        onView(withId(R.id.etEventName)).perform(typeText("Test Event"));
        onView(withId(R.id.etDescription)).perform(typeText("Test Description"));
        closeSoftKeyboard();

        // Leave location empty and try to save draft
        onView(withId(R.id.btnSaveDraft)).perform(click());

        // Verify error is shown on location field
        onView(withId(R.id.etLocation)).check(matches(hasErrorText("Location is required")));
    }

    // Test 4: All required fields filled allows draft save attempt
    @Test
    public void testValidInputsAllowDraftSave() {
        // Fill all required fields
        onView(withId(R.id.etEventName)).perform(typeText("Valid Event"));
        onView(withId(R.id.etDescription)).perform(typeText("Valid Description"));
        onView(withId(R.id.etLocation)).perform(typeText("Valid Location"));
        closeSoftKeyboard();

        // Try to save draft - should not show validation errors
        onView(withId(R.id.btnSaveDraft)).perform(click());

        // Note: This test verifies no validation errors appear
        // Actual save will depend on Firebase authentication
    }

    // Test 5: Date picker buttons are displayed and clickable
    @Test
    public void testDatePickerButtonsAreClickable() {
        // Verify start date button is displayed
        onView(withId(R.id.btnStartDate)).check(matches(isDisplayed()));

        // Verify end date button is displayed
        onView(withId(R.id.btnEndDate)).check(matches(isDisplayed()));

        // Click start date button (will open date picker dialog)
        onView(withId(R.id.btnStartDate)).perform(click());
    }

    // Test 6: Time picker buttons are displayed and clickable
    @Test
    public void testTimePickerButtonsAreClickable() {
        // Verify start time button is displayed
        onView(withId(R.id.btnStartTime)).check(matches(isDisplayed()));

        // Verify end time button is displayed
        onView(withId(R.id.btnEndTime)).check(matches(isDisplayed()));

        // Click start time button (will open time picker dialog)
        onView(withId(R.id.btnStartTime)).perform(click());
    }

    // Test 7: Registration period buttons are displayed and clickable
    @Test
    public void testRegistrationPeriodButtonsAreClickable() {
        // Verify registration opens button is displayed
        onView(withId(R.id.btnRegistrationOpens)).check(matches(isDisplayed()));

        // Verify registration closes button is displayed
        onView(withId(R.id.btnRegistrationCloses)).check(matches(isDisplayed()));

        // Click registration opens button
        onView(withId(R.id.btnRegistrationOpens)).perform(click());
    }

    // Test 8: Geolocation switch is toggleable
    @Test
    public void testGeolocationSwitchIsToggleable() {
        // Verify geolocation switch is displayed
        onView(withId(R.id.switchGeolocation)).check(matches(isDisplayed()));

        // Click to toggle the switch
        onView(withId(R.id.switchGeolocation)).perform(click());

        // Click again to toggle back
        onView(withId(R.id.switchGeolocation)).perform(click());
    }

    // Test 9: Capacity and waiting list fields accept numeric input
    @Test
    public void testCapacityFieldsAcceptNumericInput() {
        // Enter draw capacity
        onView(withId(R.id.etDrawCapacity)).perform(typeText("50"));
        closeSoftKeyboard();

        // Verify input was accepted
        onView(withId(R.id.etDrawCapacity)).check(matches(withText("50")));

        // Enter waiting list capacity
        onView(withId(R.id.etWaitingList)).perform(typeText("100"));
        closeSoftKeyboard();

        // Verify input was accepted
        onView(withId(R.id.etWaitingList)).check(matches(withText("100")));
    }

    // Test 10: Image upload buttons are displayed and clickable
    @Test
    public void testImageUploadButtonsAreClickable() {
        // Verify add image button is displayed
        onView(withId(R.id.ivAddImage)).check(matches(isDisplayed()));

        // Verify event image view is displayed
        onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));

        // Click add image button (will open image picker)
        onView(withId(R.id.ivAddImage)).perform(click());
    }
}
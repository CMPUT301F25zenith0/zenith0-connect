package com.example.connect;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.activities.CreateAcctActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * UI test to verify input boxes show error with incorrect input durring account creation
 * <p>
 * Tests:
 * 1. Invalid email
 * 2. Mis-matched password
 * 3. Invalid phone number
 * <p>
 * Working as of 07/11/2025
 * @author Aakansh Chatterjee
 */

@RunWith(AndroidJUnit4.class)
public class AccountCreationInputValidationTest {
    @Rule
    public ActivityScenarioRule<CreateAcctActivity> activityRule = new ActivityScenarioRule<>(CreateAcctActivity.class);

    // Invalid Email test
    @Test
    public void testInvalidEmailShowsError() {
        // Fill out dummy information --> To isolate email
        onView(withId(R.id.et_full_name)).perform(typeText("John Doe"));
        onView(withId(R.id.et_display_name)).perform(typeText("johnny"));
        onView(withId(R.id.et_mobile_num)).perform(typeText("1234567890"));
        onView(withId(R.id.et_password)).perform(typeText("abcdef"));
        onView(withId(R.id.et_confirm_pass)).perform(typeText("abcdef"));
        closeSoftKeyboard();

        // Actual test --> no @ symbol
        onView(withId(R.id.et_email_profile)).perform(typeText("invalidEmail"));
        closeSoftKeyboard();

        onView(withId(R.id.btn_create_acct)).perform(click());
        // Email box is changed with this error
        onView(withId(R.id.et_email_profile)).check(matches(hasErrorText("Please enter a valid email")));
    }

    // Passwords do not match
    @Test
    public void testPasswordMismatchShowsError() {
        // Fill out dummy information --> To isolate email
        onView(withId(R.id.et_full_name)).perform(typeText("John Doe"));
        onView(withId(R.id.et_display_name)).perform(typeText("johnny"));
        onView(withId(R.id.et_mobile_num)).perform(typeText("1234567890"));
        onView(withId(R.id.et_email_profile)).perform(typeText("test@gmail.com"));
        closeSoftKeyboard();

        // Actual Test -- to completely different passwords
        onView(withId(R.id.et_password)).perform(typeText("abcdef"));
        onView(withId(R.id.et_confirm_pass)).perform(typeText("ghijkl"));
        closeSoftKeyboard();

        onView(withId(R.id.btn_create_acct)).perform(click());
        // Confirm pass input is changed with this erro
        onView(withId(R.id.et_confirm_pass)).check(matches(hasErrorText("Passwords do not match")));

    }

    // Phone Number Invalid
    @Test
    public void testInvalidPhoneNumShowsError() {
        // Fill out dummy information --> To isolate email
        onView(withId(R.id.et_full_name)).perform(typeText("John Doe"));
        onView(withId(R.id.et_display_name)).perform(typeText("johnny"));
        onView(withId(R.id.et_password)).perform(typeText("abcdef"));
        onView(withId(R.id.et_confirm_pass)).perform(typeText("abcdef"));
        onView(withId(R.id.et_email_profile)).perform(typeText("test@gmail.com"));
        closeSoftKeyboard();

        // Actual Test -- undersized password -> 8 digits, should be 10
        onView(withId(R.id.et_mobile_num)).perform(typeText("12345678"));
        closeSoftKeyboard();
        onView(withId(R.id.btn_create_acct)).perform(click());

        // Mobile phone input box shows this error
        onView(withId(R.id.et_mobile_num)).check(matches(hasErrorText("Please enter a valid mobile number (at least 10 digits)")));

    }
}

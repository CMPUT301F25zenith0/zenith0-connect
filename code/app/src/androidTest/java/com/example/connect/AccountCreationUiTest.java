package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.activities.CreateAcctActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI test to verify Account Creation screen display all neseccary input fields
 * Working as of 07/11/2025
 * @author Aakansh Chatterjee
 */
@RunWith(AndroidJUnit4.class)
public class AccountCreationUiTest {
    @Rule
    public ActivityScenarioRule<CreateAcctActivity> activityRule = new ActivityScenarioRule<>(CreateAcctActivity.class);

    // Testing to see all input feilds appear on screen
    @Test
    public void testAllInputFieldsVisible() {
        onView(withId(R.id.et_full_name)).check(matches(isDisplayed()));
        onView(withId(R.id.et_display_name)).check(matches(isDisplayed()));
        onView(withId(R.id.et_email_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.et_password)).check(matches(isDisplayed()));
        onView(withId(R.id.et_confirm_pass)).check(matches(isDisplayed()));
        onView(withId(R.id.et_mobile_num)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_acct)).check(matches(isDisplayed()));
    }


}

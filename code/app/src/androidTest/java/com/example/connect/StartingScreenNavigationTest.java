package com.example.connect;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.MainActivity;
import com.example.connect.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI test to verify navigation from the open screen (MainActivity)
 * to the Login and Create Account screens, and back navigation works correctly.
 * <p>
 * Tests:
 *  1. Launching MainActivity shows the correct buttons.
 *  2. Clicking "Log In" opens the Login screen.
 *  3. Clicking back from Login returns to the open screen.
 *  4. Clicking "Create Account" opens the Create Account screen.
 *  5. Clicking back from Create Account returns to the open screen.
 * <p>
 * Working as of 07/11/2025
 * @author Aakansh Chatterjee
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartingScreenNavigationTest {

    @Before
    public void setup() {
        // Launch MainActivity before each test
        ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testMainScreenVisible() {
        // Check that the open screen buttons are displayed
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
        onView(withId(R.id.create_acct_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToLoginAndBack() {
        // Click the "Log In" button
        onView(withId(R.id.btn_login)).perform(click());

        // Verify Login screen is displayed
        onView(withText("Log In")).check(matches(isDisplayed()));

        // Press back to return to open screen
        // This assumes that the individual swiped the screen to go back --> so we isolate the usage of the back button testing
        Espresso.pressBack();

        // Check that open screen is visible again
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateToCreateAccountAndBack() {
        // Click the "Create Account" button
        onView(withId(R.id.create_acct_btn)).perform(click());

        // Verify Create Account screen is displayed (by unique text or ID)
        // Replace with an element from CreateAcctActivity (e.g., field or button)
        onView(withText("Create Account")).check(matches(isDisplayed()));

        // Press back to return to open screen
        // This assumes that the individual swiped the screen to go back --> so we isolate the usage of the back button testing
        Espresso.pressBack();

        // Check that open screen is visible again
        onView(withId(R.id.create_acct_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButtonNavigationLogin() {
        // Click the "Log In" button
        onView(withId(R.id.btn_login)).perform(click());

        // Isolated test for back button
        onView(withId(R.id.back_btn)).perform(click());

        // Check that open screen is visible again
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }
    @Test
    public void testBackButtonNavigationCreate() {
        // Click the "Create Account" button
        onView(withId(R.id.create_acct_btn)).perform(click());

        // Isolated test for back button
        onView(withId(R.id.back_btn)).perform(click());

        // Check that open screen is visible again
        onView(withId(R.id.create_acct_btn)).check(matches(isDisplayed()));
    }
}

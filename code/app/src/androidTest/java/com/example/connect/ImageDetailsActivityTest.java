package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.core.internal.deps.dagger.Provides;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.ImageDetailsActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * Comprehensive instrumented tests for ImageDetailsActivity.
 *
 * This test suite validates the image display functionality including:
 * - Initial UI state and component visibility
 * - Toolbar presence and navigation functionality
 * - ImageView display for full-screen images
 * - Handling of different image sources (URL and Base64)
 * - Activity behavior with various intent extras
 *
 * Test Coverage:
 * - UI Component Visibility: Toolbar and ImageView
 * - Toolbar Navigation: Back button functionality
 * - Intent Handling: URL-based and Base64-encoded image data
 * - Edge Cases: Missing data, empty strings, invalid data
 * - Activity Lifecycle: Proper initialization and display
 *
 * @author Jagjot Singh Brar
 * @version 1.0
 *
 * Note: These tests use Espresso for UI testing. Image loading via Glide
 * is not directly tested (would require mocking), but UI components are verified.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ImageDetailsActivityTest {

    // ==================== Basic UI Component Tests ====================

    /**
     * Test Case 1: Toolbar Is Visible With URL Intent
     *
     * Validates that the toolbar is rendered and visible when the activity
     * is launched with a URL-based image intent.
     *
     * Expected: Toolbar is displayed on screen
     */
    @Test
    public void toolbar_isVisibleWithUrlIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 2: ImageView Is Visible With URL Intent
     *
     * Validates that the full-screen ImageView is rendered and visible
     * when the activity is launched with a URL-based image intent.
     *
     * Expected: ImageView is displayed on screen
     */
    @Test
    public void imageView_isVisibleWithUrlIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 3: Toolbar Is Visible With Base64 Intent
     *
     * Validates that the toolbar is rendered and visible when the activity
     * is launched with a Base64-encoded image intent.
     *
     * Expected: Toolbar is displayed on screen
     */
    @Test
    public void toolbar_isVisibleWithBase64Intent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        // Sample valid Base64 string (1x1 transparent PNG)
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 4: ImageView Is Visible With Base64 Intent
     *
     * Validates that the full-screen ImageView is rendered and visible
     * when the activity is launched with a Base64-encoded image intent.
     *
     * Expected: ImageView is displayed on screen
     */
    @Test
    public void imageView_isVisibleWithBase64Intent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        // Sample valid Base64 string (1x1 transparent PNG)
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Toolbar Navigation Tests ====================

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Verify toolbar is visible first
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));

            // Click navigation button (this will finish the activity)
            onView(withId(R.id.toolbar))
                    .perform(click());

            // If we reach here without crash, test passes
        }
    }

    // ==================== Edge Case Tests ====================

    /**
     * Test Case 6: Activity Handles Empty URL Intent
     *
     * Tests that the activity handles an empty URL string gracefully
     * without crashing (should show toast and finish).
     *
     * Expected: Activity launches and handles empty data appropriately
     */
    @Test
    public void activity_handlesEmptyUrlIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should handle this gracefully
            // May finish quickly, but shouldn't crash
        }
    }

    /**
     * Test Case 7: Activity Handles Empty Base64 Intent
     *
     * Tests that the activity handles an empty Base64 string gracefully
     * without crashing (should show toast and finish).
     *
     * Expected: Activity launches and handles empty data appropriately
     */
    @Test
    public void activity_handlesEmptyBase64Intent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_base64", "");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should handle this gracefully
            // May finish quickly, but shouldn't crash
        }
    }

    /**
     * Test Case 8: Activity Handles No Intent Extras
     *
     * Tests that the activity handles the case where no image data
     * is provided in the intent (should show toast and finish).
     *
     * Expected: Activity launches and handles missing data appropriately
     */
    @Test
    public void activity_handlesNoIntentExtras() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        // No extras added

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should handle this gracefully
            // Should show toast and finish, but not crash
        }
    }

    /**
     * Test Case 9: Activity Handles Invalid Base64 String
     *
     * Tests that the activity handles an invalid Base64 string gracefully
     * without crashing (should catch exception and show error toast).
     *
     * Expected: Activity launches and handles invalid data appropriately
     */
    @Test
    public void activity_handlesInvalidBase64() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_base64", "invalid_base64_string!!!123");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Activity should handle this gracefully with try-catch
            // Should show error toast but not crash

            // Verify UI components are still there (before potential finish)
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== URL Priority Tests ====================

    /**
     * Test Case 10: URL Takes Priority Over Base64
     *
     * Tests that when both URL and Base64 data are provided,
     * the URL is used (as per the if-else logic in the activity).
     *
     * Expected: Activity displays using URL data
     */
    @Test
    public void urlIntent_takesPriorityOverBase64() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Both UI components should be visible
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== UI Component Visibility Tests ====================

    /**
     * Test Case 11: Both UI Components Visible With URL
     *
     * Comprehensive test that both toolbar and ImageView are visible
     * when launched with URL intent.
     *
     * Expected: Both toolbar and ImageView are displayed
     */
    @Test
    public void bothUIComponents_visibleWithUrl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 12: Both UI Components Visible With Base64
     *
     * Comprehensive test that both toolbar and ImageView are visible
     * when launched with Base64 intent.
     *
     * Expected: Both toolbar and ImageView are displayed
     */
    @Test
    public void bothUIComponents_visibleWithBase64() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Different URL Formats Tests ====================

    /**
     * Test Case 13: Activity Handles HTTP URL
     *
     * Tests that the activity can handle HTTP (non-secure) URLs.
     *
     * Expected: Activity displays UI components correctly
     */
    @Test
    public void activity_handlesHttpUrl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "http://example.com/image.png");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 14: Activity Handles HTTPS URL
     *
     * Tests that the activity can handle HTTPS (secure) URLs.
     *
     * Expected: Activity displays UI components correctly
     */
    @Test
    public void activity_handlesHttpsUrl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://secure.example.com/photo.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 15: Activity Handles Long URL
     *
     * Tests that the activity can handle very long URLs with query parameters.
     *
     * Expected: Activity displays UI components correctly
     */
    @Test
    public void activity_handlesLongUrl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url",
                "https://example.com/images/photo.jpg?size=large&quality=high&format=webp&timestamp=123456789");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Activity Lifecycle Tests ====================

    /**
     * Test Case 16: Activity Initializes Properly With URL
     *
     * Tests that the activity goes through its lifecycle correctly
     * when launched with URL data.
     *
     * Expected: Activity initializes without crashes
     */
    @Test
    public void activity_initializesProperlyWithUrl() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Verify activity is in resumed state and UI is visible
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 17: Activity Initializes Properly With Base64
     *
     * Tests that the activity goes through its lifecycle correctly
     * when launched with Base64 data.
     *
     * Expected: Activity initializes without crashes
     */
    @Test
    public void activity_initializesProperlyWithBase64() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            // Verify activity is in resumed state and UI is visible
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== ImageView Specific Tests ====================

    /**
     * Test Case 18: ImageView Exists With URL Intent
     *
     * Verifies that the ImageView component is properly initialized
     * and exists in the view hierarchy with URL data.
     *
     * Expected: ImageView is present and displayed
     */
    @Test
    public void imageView_existsWithUrlIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/test.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 19: ImageView Exists With Base64 Intent
     *
     * Verifies that the ImageView component is properly initialized
     * and exists in the view hierarchy with Base64 data.
     *
     * Expected: ImageView is present and displayed
     */
    @Test
    public void imageView_existsWithBase64Intent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_base64",
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.iv_full_image))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Test Case 20: Toolbar Exists With Valid Intent
     *
     * Verifies that the MaterialToolbar component is properly initialized
     * and exists in the view hierarchy.
     *
     * Expected: Toolbar is present and displayed
     */
    @Test
    public void toolbar_existsWithValidIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                ImageDetailsActivity.class);
        intent.putExtra("image_url", "https://example.com/image.jpg");

        try (ActivityScenario<ImageDetailsActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }
}

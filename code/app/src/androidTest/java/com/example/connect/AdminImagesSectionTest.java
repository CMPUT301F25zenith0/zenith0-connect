package com.example.connect;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.connect.activities.AdminDashboardActivity;
import com.example.connect.activities.AdminImageListActivity;
import com.example.connect.activities.ImageDetailsActivity;
import com.example.connect.adapters.AdminImageAdapter;
import com.example.connect.testing.TestHooks;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented coverage for the admin Images section:
 * 1. Dashboard navigation
 * 2. Image listing
 * 3. Searching by display name
 * 4. Deleting an image
 * 5. Opening the full image detail screen
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesSectionTest {

    private static final String TAG = "AdminImagesTest";

    @Before
    public void setUp() {
        TestHooks.setUiTestMode(true);
        Intents.init();
    }

    @After
    public void tearDown() {
        TestHooks.setUiTestMode(false);
        Intents.release();
    }

    @Test
    public void testDashboardNavigatesToImagesSection() {
        Intent resultIntent = new Intent();
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        intending(hasComponent(AdminImageListActivity.class.getName())).respondWith(result);

        try (ActivityScenario<AdminDashboardActivity> scenario =
                     ActivityScenario.launch(AdminDashboardActivity.class)) {

            onView(withId(R.id.card_manage_images)).check(matches(isDisplayed()));
            onView(withId(R.id.card_manage_images)).perform(click());

            intended(hasComponent(AdminImageListActivity.class.getName()));
            logSuccess("Verified dashboard navigation to admin images list.");
        }
    }

    @Test
    public void testAdminImageList_DisplaysImages() {
        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            scenario.onActivity(activity -> activity.populateImagesForTests(createSampleImages()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withText("Gala Poster")).check(matches(isDisplayed()));
            onView(withText("Profile: Alex")).check(matches(isDisplayed()));
            logSuccess("Verified admin image list displays injected images.");
        }
    }

    @Test
    public void testAdminImageList_SearchByName() {
        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            scenario.onActivity(activity -> activity.populateImagesForTests(createSampleImages()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.search_input)).perform(replaceText("Profile"));
            closeSoftKeyboard();

            onView(withText("Profile: Alex")).check(matches(isDisplayed()));
            onView(withText("Gala Poster")).check(doesNotExist());
            logSuccess("Verified search filters images by name text.");
        }
    }

    @Test
    public void testAdminImageList_DeleteImage() {
        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            List<AdminImageAdapter.ImageItem> images = new ArrayList<>();
            images.add(buildImage("img-1", "https://example.com/img.jpg", "Event Poster",
                    "event-1", "Delete Poster"));
            scenario.onActivity(activity -> activity.populateImagesForTests(images));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Delete Poster")),
                            clickChildViewWithId(R.id.btn_delete)
                    ));

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withText("Delete Poster")).check(doesNotExist());
            logSuccess("Verified delete button removes image row.");
        }
    }

    @Test
    public void testAdminImageList_OpensImageDetails() {
        Intent resultIntent = new Intent();
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        intending(hasComponent(ImageDetailsActivity.class.getName())).respondWith(result);

        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            List<AdminImageAdapter.ImageItem> images = new ArrayList<>();
            images.add(buildImage("img-detail", "https://example.com/full.jpg",
                    "Event Poster", "event-7", "Detail Poster"));
            scenario.onActivity(activity -> activity.populateImagesForTests(images));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Detail Poster")), click()));
        }

        intended(hasComponent(ImageDetailsActivity.class.getName()));
        intended(hasExtra("image_url", "https://example.com/full.jpg"));
        logSuccess("Verified clicking image opens ImageDetailsActivity with URL.");
    }

    private List<AdminImageAdapter.ImageItem> createSampleImages() {
        List<AdminImageAdapter.ImageItem> images = new ArrayList<>();
        images.add(buildImage("poster-1", "https://example.com/poster.jpg",
                "Event Poster", "event-100", "Gala Poster"));
        images.add(buildImage("profile-1", "https://example.com/profile.jpg",
                "Profile Picture", "user-200", "Profile: Alex"));
        return images;
    }

    private AdminImageAdapter.ImageItem buildImage(String id,
                                                   String url,
                                                   String type,
                                                   String relatedId,
                                                   String displayName) {
        return new AdminImageAdapter.ImageItem(id, url, type, relatedId, displayName);
    }

    private static ViewAction clickChildViewWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on child view with id";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View child = view.findViewById(id);
                if (child != null) {
                    child.performClick();
                }
            }
        };
    }

    private void logSuccess(String message) {
        Log.i(TAG, message);
    }
}


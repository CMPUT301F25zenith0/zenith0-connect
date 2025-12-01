package com.example.connect;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import android.util.Log;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.connect.activities.AdminImageListActivity;
import com.example.connect.adapters.AdminImageAdapter;
import com.example.connect.testing.TestHooks;

import androidx.test.espresso.matcher.ViewMatchers.Visibility;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented coverage for the admin Images section:
 * 1. Image listing
 * 2. Searching by display name
 * 3. Deleting an image
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminImagesSectionTest {

    private static final String TAG = "AdminImagesTest";

    @Before
    public void setUp() {
        TestHooks.setUiTestMode(true);
    }

    @After
    public void tearDown() {
        TestHooks.setUiTestMode(false);
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
    public void testAdminImageList_DeleteLastImageShowsEmptyState() {
        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            List<AdminImageAdapter.ImageItem> singleton = new ArrayList<>();
            singleton.add(buildImage("poster-final", "https://example.com/final.jpg",
                    "Event Poster", "event-final", "Final Poster"));
            scenario.onActivity(activity -> activity.populateImagesForTests(singleton));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.tv_empty_state))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Final Poster")),
                            clickChildViewWithId(R.id.btn_delete)
                    ));

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withId(R.id.tv_empty_state))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            logSuccess("Verified empty state shows once the last image is deleted.");
        }
    }

    @Test
    public void testAdminImageList_DeleteOneImageKeepsOthersVisible() {
        try (ActivityScenario<AdminImageListActivity> scenario =
                     ActivityScenario.launch(AdminImageListActivity.class)) {
            scenario.onActivity(activity -> activity.populateImagesForTests(createSampleImages()));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withText("Gala Poster")).check(matches(isDisplayed()));
            onView(withText("Profile: Alex")).check(matches(isDisplayed()));

            onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText("Gala Poster")),
                            clickChildViewWithId(R.id.btn_delete)
                    ));

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            onView(withText("Gala Poster")).check(doesNotExist());
            onView(withText("Profile: Alex")).check(matches(isDisplayed()));
            onView(withId(R.id.tv_empty_state))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
            logSuccess("Verified deleting one image keeps remaining rows visible.");
        }
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


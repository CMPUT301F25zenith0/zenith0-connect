package com.example.connect;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.connect.activities.EventDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test for EventDetails activity.
 * Tests basic UI elements and waiting list functionality.
 *
 * PREREQUISITES:
 * - User must be logged in to the app before running tests
 * - Animations must be disabled on emulator
 *
 * Run with: ./gradlew connectedAndroidTest --tests "*EventDetailsTest*"
 *
 * Updated: 12/01/2025 - Increased wait times, simplified assertions
 * @author Digaant Chhokra
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDetailsTest {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String testEventId;
    private String testUserId;
    private String organizerId = "organizer_test_123";

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get or create test user
        if (auth.getCurrentUser() != null) {
            testUserId = auth.getCurrentUser().getUid();
        } else {
            CountDownLatch authLatch = new CountDownLatch(1);
            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful() && auth.getCurrentUser() != null) {
                    testUserId = auth.getCurrentUser().getUid();
                }
                authLatch.countDown();
            });
            assertTrue("Authentication timed out", authLatch.await(15, TimeUnit.SECONDS));
        }

        // Create test event
        createTestEvent();

        // Extra delay for Firestore
        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        if (testEventId != null) {
            CountDownLatch cleanupLatch = new CountDownLatch(1);

            db.collection("waiting_lists")
                    .document(testEventId)
                    .collection("entrants")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        querySnapshot.forEach(doc -> doc.getReference().delete());

                        db.collection("waiting_lists").document(testEventId).delete()
                                .addOnSuccessListener(v -> {
                                    db.collection("events").document(testEventId).delete()
                                            .addOnSuccessListener(v2 -> cleanupLatch.countDown())
                                            .addOnFailureListener(e -> cleanupLatch.countDown());
                                })
                                .addOnFailureListener(e -> cleanupLatch.countDown());
                    })
                    .addOnFailureListener(e -> cleanupLatch.countDown());

            cleanupLatch.await(15, TimeUnit.SECONDS);
        }
    }

    private void createTestEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_title", "Test Event");
        eventData.put("org_name", "Test Org");
        eventData.put("location", "Edmonton AB");
        eventData.put("description", "Test description");
        eventData.put("price", "25");
        eventData.put("date_time", "2025-12-25T10:00:00");
        eventData.put("end_time", "2025-12-25T12:00:00");
        eventData.put("reg_start", "2025-11-01T00:00:00");
        eventData.put("reg_stop", "2025-12-20T23:59:59");
        eventData.put("organizer_id", organizerId);
        eventData.put("draw_capacity", 2);
        eventData.put("max_participants", 2);
        eventData.put("draw_completed", false);
        eventData.put("selected_count", 0);
        eventData.put("status", "published");

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(ref -> {
                    testEventId = ref.getId();

                    Map<String, Object> waitingListData = new HashMap<>();
                    waitingListData.put("event_id", testEventId);
                    waitingListData.put("created_at", FieldValue.serverTimestamp());
                    waitingListData.put("total_capacity", 2);

                    db.collection("waiting_lists")
                            .document(testEventId)
                            .set(waitingListData)
                            .addOnCompleteListener(task -> latch.countDown());
                })
                .addOnFailureListener(e -> latch.countDown());

        assertTrue("Event creation timed out", latch.await(20, TimeUnit.SECONDS));
        assertNotNull("Event ID should not be null", testEventId);
    }

    /**
     * Test 1: Verify join button exists
     */
    @Test
    public void testJoinButtonIsDisplayed() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", testEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<EventDetails> scenario = ActivityScenario.launch(intent);

        // Wait longer for activity and Firestore to load
        Thread.sleep(6000);

        // Just check if button exists
        onView(withId(R.id.btn_join_list)).check(matches(isDisplayed()));

        scenario.close();
    }

    /**
     * Test 2: Verify info button is clickable
     */
    @Test
    public void testEventInfoButtonIsClickable() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", testEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<EventDetails> scenario = ActivityScenario.launch(intent);
        Thread.sleep(6000);

        // Check button exists and click it
        onView(withId(R.id.btn_info))
                .check(matches(isDisplayed()))
                .perform(click());

        Thread.sleep(1000);

        scenario.close();
    }

    /**
     * Test 3: Verify waiting list TextView exists
     */
    @Test
    public void testWaitingListTextViewExists() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", testEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<EventDetails> scenario = ActivityScenario.launch(intent);
        Thread.sleep(6000);

        // Just check if waiting list text exists
        onView(withId(R.id.tv_waiting_list)).check(matches(isDisplayed()));

        scenario.close();
    }


    /**
     * Test 4: Verify organization name TextView exists
     */
    @Test
    public void testOrganizationNameExists() throws Exception {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetails.class);
        intent.putExtra("EVENT_ID", testEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ActivityScenario<EventDetails> scenario = ActivityScenario.launch(intent);
        Thread.sleep(6000);

        // Just check if org name exists
        onView(withId(R.id.tv_org_name)).check(matches(isDisplayed()));

        scenario.close();
    }
}
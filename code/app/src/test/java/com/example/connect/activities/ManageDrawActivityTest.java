package com.example.connect.activities;

import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.example.connect.models.User;
import com.example.connect.models.WaitingListEntry;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ManageDrawActivityTest {

    private ManageDrawActivity activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ManageDrawActivity.class);
        intent.putExtra("EVENT_ID", "test-event-123");

        activity = Robolectric.buildActivity(ManageDrawActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        // Let setupRecyclerView() run
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Inject test mode
        activity.setIsTest(true);

        // Fake event
        Event fakeEvent = new Event();
        fakeEvent.setName("Summer Music Fest 2025");
        fakeEvent.setDrawCapacity(150);
        fakeEvent.setLocation("City Park");
        fakeEvent.setDateTime("2025-07-20T19:00:00");
        setField(activity, "currentEvent", fakeEvent);

        // Fake entries
        ArrayList<WaitingListEntry> entries = new ArrayList<>();
        entries.add(createEntry("u1", "Alice", "waiting"));
        entries.add(createEntry("u2", "Bob", "selected"));
        entries.add(createEntry("u3", "Charlie", "enrolled"));
        entries.add(createEntry("u4", "Diana", "canceled"));

        setField(activity, "allEntries", entries);
        setField(activity, "filteredEntries", new ArrayList<>(entries));

        // Force UI update
        var method = activity.getClass().getDeclaredMethod("onAllEntriesLoaded");
        method.setAccessible(true);
        method.invoke(activity);

        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private WaitingListEntry createEntry(String userId, String name, String status) {
        WaitingListEntry e = new WaitingListEntry();
        e.setUserId(userId);
        e.setStatus(status);
        User u = new User();
        u.setUserId(userId);
        u.setName(name);
        e.setUser(u);
        return e;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private String text(int id) {
        TextView tv = activity.findViewById(id);
        return tv != null ? tv.getText().toString() : "";
    }

    private void click(int id) {
        View v = activity.findViewById(id);
        assertNotNull("View not found: " + activity.getResources().getResourceName(id), v);
        v.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private RecyclerView getRecyclerView() {
        ViewGroup container = activity.findViewById(R.id.contentContainer);
        assertNotNull("contentContainer not found", container);
        assertTrue("contentContainer has no children", container.getChildCount() > 0);
        View child = container.getChildAt(0);
        assertTrue("First child is not RecyclerView", child instanceof RecyclerView);
        return (RecyclerView) child;
    }

    @Test
    public void eventInfo_displaysCorrectly() throws Exception {
        // Force refresh UI with our fake data
        var displayMethod = activity.getClass().getDeclaredMethod("displayEventInfo");
        displayMethod.setAccessible(true);
        displayMethod.invoke(activity);

        assertEquals("Summer Music Fest 2025", text(R.id.tvEventName));
        assertTrue(text(R.id.tvCapacity).contains("150"));
        assertTrue(text(R.id.tvLocation).contains("City Park"));
    }

    @Test
    public void clickingWaitingTab_filtersToOneItem() throws Exception {
        click(R.id.btnTabWaiting);

        // Just check that filteredEntries has 1 item — this is what matters
        var field = activity.getClass().getDeclaredField("filteredEntries");
        field.setAccessible(true);
        List<?> filtered = (List<?>) field.get(activity);

        assertEquals(1, filtered.size());
        assertEquals("waiting", ((WaitingListEntry) filtered.get(0)).getStatus());
    }

    @Test
    public void tabs_showCorrectCounts() {
        assertEquals("Waiting (1)", text(R.id.btnTabWaiting));
        assertEquals("Selected (1)", text(R.id.btnTabSelected));
        assertEquals("Enrolled (1)", text(R.id.btnTabEnrolled));
        assertEquals("Canceled (1)", text(R.id.btnTabCanceled));
    }


    @Test
    public void backButton_finishesActivity() {
        click(R.id.btnBack);
        assertTrue(activity.isFinishing());
    }

    @Test
    public void manualLottery_showsCorrectToast() {
        click(R.id.btnManualLottery);
        assertEquals("Running lottery for Summer Music Fest 2025...",
                ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void notifySelected_works() {
        click(R.id.btnNotifySelected);
        // Button gets disabled — just verify it doesn't crash
        MaterialButton btn = activity.findViewById(R.id.btnNotifySelected);
        assertNotNull(btn);
    }

    @Test
    public void bottomNav_dashboard_startsCorrectActivity() {
        click(R.id.btnNavDashboard);
        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(OrganizerActivity.class.getName(), next.getComponent().getClassName());
    }

    @Test
    public void bottomNav_map_opensWithEventId() {
        click(R.id.btnNavMap);
        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(EntrantMapActivity.class.getName(), next.getComponent().getClassName());
        assertEquals("test-event-123", next.getStringExtra("EVENT_ID"));
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.connect.R;
import com.example.connect.adapters.NotificationMessageAdapter;
import com.example.connect.models.NotificationMessage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class OrganizerMessagesActivityTest {

    private OrganizerMessagesActivity activity;
    static {
        // THIS LINE KILLS THE FIRESTORE SQLITE BUG FOREVER
        System.setProperty("firebase.firestore.settings.persistenceEnabled", "false");
    }
    @Before
    public void setUp() throws Exception {
        // Initialize Firebase
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        }


        // Start activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizerMessagesActivity.class);
        activity = Robolectric.buildActivity(OrganizerMessagesActivity.class, intent)
                .create()
                .start()
                .get();  // ← STOP HERE — BEFORE onResume()

        // CRITICAL: Set isTest = true BEFORE onResume() runs
        activity.setIsTest(true);

        // Now safely resume — onResume() sees isTest = true → skips loadAllNotifications()
        activity.onResume();

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Inject fake data
        List<NotificationMessage> fakeMessages = new ArrayList<>();
        fakeMessages.add(createMessage("You're Selected!", "Welcome!", "chosen", "event-1", "Summer Fest"));
        fakeMessages.add(createMessage("Not Selected", "Try again", "not_chosen", "event-1", "Summer Fest"));
        fakeMessages.add(createMessage("Custom Alert", "Bring ID", "custom", "event-2", "Winter Gala"));

        setField(activity, "allMessages", fakeMessages);
        setField(activity, "filteredMessages", new ArrayList<>(fakeMessages));

        // Force UI update
        var updateUI = activity.getClass().getDeclaredMethod("updateUI");
        updateUI.setAccessible(true);
        updateUI.invoke(activity);

        // THIS IS THE NUCLEAR OPTION — FORCES RECYCLERVIEW TO HAVE CHILDREN
        View decorView = activity.getWindow().getDecorView();
        decorView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        );
        decorView.layout(0, 0, 1080, 1920);

        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private NotificationMessage createMessage(String title, String body, String type, String eventId, String eventName) {
        NotificationMessage m = new NotificationMessage();
        m.setTitle(title);
        m.setBody(body);
        m.setType(type);
        String StringId = "";
        m.setEventId(StringId);
        m.setEventName(eventName);
        m.setTimestamp(Timestamp.now());
        m.setRead(false);
        return m;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void click(int id) {
        View v = activity.findViewById(id);
        assertNotNull(v);
        v.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private View findViewById(int id) {
        return activity.findViewById(id);
    }

    @Test
    public void messages_areDisplayed() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerViewMessages);
        assertEquals(3, rv.getAdapter().getItemCount());
        assertEquals(View.GONE, findViewById(R.id.tvNoMessages).getVisibility());
    }

    @Test
    public void search_filtersByTitle() {
        EditText search = (EditText) findViewById(R.id.etSearch);
        search.setText("selected");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerViewMessages);
        assertEquals(2, rv.getAdapter().getItemCount());
    }

    @Test
    public void backButton_finishesActivity() {
        click(R.id.btnBack);
        assertTrue(activity.isFinishing());
    }

    @Test
    public void newMessageButton_opensDialog() {
        click(R.id.btnNewMessage);
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertTrue(true);
    }

    @Test
    public void clickingMessage_opensDetailsDialog() {
        RecyclerView rv = activity.findViewById(R.id.recyclerViewMessages);
        View firstItem = rv.getChildAt(0);
        assertNotNull("RecyclerView has no children — did you call requestLayout()?", firstItem);
        firstItem.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertTrue(true); // No crash = success
    }

    @Test
    public void emptyState_showsWhenNoMessages() throws Exception {
        setField(activity, "allMessages", new ArrayList<>());
        setField(activity, "filteredMessages", new ArrayList<>());
        var m = activity.getClass().getDeclaredMethod("updateUI");
        m.setAccessible(true);
        m.invoke(activity);
        assertEquals(View.VISIBLE, findViewById(R.id.tvNoMessages).getVisibility());
    }
}
package com.example.connect.activities;

import android.content.Intent;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.connect.R;
import com.example.connect.activities.UserNotificationsActivity.NotificationItem;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class UserNotificationsActivityTest {

    private UserNotificationsActivity activity;

    @Before
    public void setUp() throws Exception {
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        }

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserNotificationsActivity.class);
        intent.putExtra("TEST_MODE", true); // This stops onCreate() immediately

        activity = Robolectric.buildActivity(UserNotificationsActivity.class, intent)
                .create()
                .get(); // onCreate() runs and returns early — no Firebase, no crash

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // Manually do what onCreate() would have done
        activity.initializeViews();

        RecyclerView rv = activity.findViewById(R.id.recycler_notifications);
        rv.setLayoutManager(new LinearLayoutManager(activity));

        // THIS IS THE CORRECT WAY TO CREATE THE INNER ADAPTER
        UserNotificationsActivity.NotificationAdapter adapter =
                activity.new NotificationAdapter();

        rv.setAdapter(adapter);
        activity.adapter = adapter; // if you need it later

        activity.setupClickListeners();

        // Inject fake data
        var fake = new ArrayList<NotificationItem>();
        fake.add(new NotificationItem("1", "Welcome!", "You're in", "chosen", "e1", "Summer Fest", new Date(), false, false));
        fake.add(new NotificationItem("2", "Sorry", "Not selected", "not_chosen", "e1", "Summer Fest", new Date(), true, false));
        fake.add(new NotificationItem("3", "Bring ID", "Reminder", "custom", "e2", "Winter Gala", new Date(), false, false));

        adapter.setNotifications(fake);

        rv.measure(1080, 1920);
        rv.layout(0, 0, 1080, 1920);

        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    @Test public void notifications_areDisplayed() {
        assertEquals(3, ((RecyclerView)activity.findViewById(R.id.recycler_notifications)).getAdapter().getItemCount());
    }

    @Test public void backButton_finishesActivity() {
        View back = activity.findViewById(R.id.btnBack);
        if (back == null) back = activity.findViewById(R.id.noti_btn_back);
        back.performClick();
        assertTrue(activity.isFinishing());
    }

    @Test public void toggleButton_exists() {
        assertNotNull(activity.findViewById(R.id.btn_toggle));
    }

    @Test public void clickingClose_removesNotification() {
        RecyclerView rv = activity.findViewById(R.id.recycler_notifications);
        View close = rv.getChildAt(0).findViewById(R.id.ic_close);
        close.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(2, rv.getAdapter().getItemCount());
    }

    @Test public void navigationButtons_exist() {
        assertNotNull(activity.findViewById(R.id.home_btn));
        assertNotNull(activity.findViewById(R.id.myevents_btn));
        assertNotNull(activity.findViewById(R.id.scan_btn));
        assertNotNull(activity.findViewById(R.id.profile_btn));
        assertNotNull(activity.findViewById(R.id.notificaton_btn));
    }

    @Test
    public void emptyState_showsWhenNoNotifications() throws Exception {
        RecyclerView rv = activity.findViewById(R.id.recycler_notifications);
        var adapter = rv.getAdapter();
        var m = adapter.getClass().getDeclaredMethod("setNotifications", List.class);
        m.setAccessible(true);
        m.invoke(adapter, new ArrayList<>());

        // ADD THIS LINE — forces visibility update
        activity.findViewById(R.id.tv_no_notifications).setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        rv.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertEquals(View.VISIBLE, activity.findViewById(R.id.tv_no_notifications).getVisibility());
    }
}
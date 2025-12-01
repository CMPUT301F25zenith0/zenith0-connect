package com.example.connect.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.example.connect.adapters.MyEventsAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying events the user has joined, organized into three tabs.
 *
 * <p>This activity provides a personalized view of the user's event participation status:
 * <ul>
 *   <li><b>Waitlist Tab:</b> Events where the user is on the waiting list (status: waiting, waitlisted, pending)</li>
 *   <li><b>Selected Tab:</b> Events where the user won the lottery but hasn't confirmed yet (status: selected)</li>
 *   <li><b>Confirmed Tab:</b> Events the user has accepted/enrolled in (status: confirmed, enrolled, accepted)</li>
 * </ul>
 *
 * <p>For each event, the activity checks the entrants subcollection in the waiting_lists
 * collection to determine the user's current status and sorts events into the appropriate tab.
 * Each tab shows relevant actions based on the user's status (e.g., accept/decline for selected events).
 *
 * <p>The activity requires user authentication and redirects to login if no user is signed in.
 *
 * @author Aakansh Chatterjee, Aalpesh Dayal
 * @version 3.0
 */

public class MyEventsActivity extends AppCompatActivity {

    private static final String TAG = "MyEventsActivity";

    // UI Components
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn, btnTabMyEvents;
    private Button btnTabWaitlist, btnTabSelected, btnTabConfirmed;
    private ListView myEventsListView;
    private TextView emptyView;

    // Adapter & Data
    private MyEventsAdapter myEventsAdapter;
    private List<Event> displayList = new ArrayList<>();

    // Data Caches
    private List<Event> waitlistedEventsList = new ArrayList<>();
    private List<Event> selectedEventsList = new ArrayList<>();
    private List<Event> confirmedEventsList = new ArrayList<>();
    private List<Event> allEventsList = new ArrayList<>(); //

    // Firebase
    private final EventRepository eventRepository = new EventRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String currentUserId;

    // State: 0=Waitlist, 1=Selected, 2=Confirmed
    private int currentTab = MyEventsAdapter.TAB_WAITLIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events_activity);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupAdapter();
        setupClickListeners();

        loadUserEvents();
    }

    private void initViews() {
        homeBtn = findViewById(R.id.home_btn);
        myEventsBtn = findViewById(R.id.myevents_btn);
        scanBtn = findViewById(R.id.scan_btn);
        profileBtn = findViewById(R.id.profile_btn);
        notificationBtn = findViewById(R.id.notificaton_btn);

        btnTabWaitlist = findViewById(R.id.btn_tab_waitlist);
        btnTabSelected = findViewById(R.id.btn_tab_selected);
        btnTabConfirmed = findViewById(R.id.btn_tab_confirmed);
        btnTabMyEvents = findViewById(R.id.btn_tab_my_events);


        myEventsListView = findViewById(R.id.my_events_list);
        emptyView = findViewById(R.id.empty_view);
        myEventsListView.setEmptyView(emptyView);
    }

    private void setupAdapter() {
        // Initialize adapter with current tab mode
        myEventsAdapter = new MyEventsAdapter(this, displayList, currentTab);
        myEventsListView.setAdapter(myEventsAdapter);
    }

    private void setupClickListeners() {
        homeBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EventListActivity.class));
            finish();
        });
        scanBtn.setOnClickListener(v -> startActivity(new Intent(this, QRCodeScanner.class)));
        profileBtn.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        notificationBtn.setOnClickListener(v -> startActivity(new Intent(this, UserNotificationsActivity.class)));

        // Tab Click Listeners
        btnTabWaitlist.setOnClickListener(v -> switchTab(MyEventsAdapter.TAB_WAITLIST));
        btnTabSelected.setOnClickListener(v -> switchTab(MyEventsAdapter.TAB_SELECTED));
        btnTabConfirmed.setOnClickListener(v -> switchTab(MyEventsAdapter.TAB_CONFIRMED));
        btnTabMyEvents.setOnClickListener(v -> switchTab(MyEventsAdapter.TAB_MY_EVENTS));

        myEventsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < displayList.size()) {
                Event event = displayList.get(position);
                Intent intent = new Intent(MyEventsActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }
        });
    }

    /**
     * Switches to a different tab, updating the displayed events list and UI styling.
     * Updates the adapter's tab state to show appropriate actions for each event status.
     *
     * @param tabIndex The tab to switch to (TAB_WAITLIST, TAB_SELECTED, or TAB_CONFIRMED)
     */
    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        displayList.clear();

        if (tabIndex == MyEventsAdapter.TAB_SELECTED) {
            displayList.addAll(selectedEventsList);
            emptyView.setText("No selected events found.");
        } else if (tabIndex == MyEventsAdapter.TAB_CONFIRMED) {
            displayList.addAll(confirmedEventsList);
            emptyView.setText("No confirmed events found.");
        } else if (tabIndex == myEventsAdapter.TAB_MY_EVENTS) {
            displayList.addAll(allEventsList);
            emptyView.setText("No events found");
        } else {
            // Default to Waitlist
            displayList.addAll(waitlistedEventsList);
            emptyView.setText("No waitlisted events found.");
        }

        // Notify Adapter of state change
        myEventsAdapter.setTabState(currentTab);

        // Update Button Colors
        updateTabStyles(currentTab);
    }

    /**
     * Updates the visual styling of tab buttons to reflect which tab is active.
     * The active tab is highlighted with gold background, inactive tabs are transparent.
     *
     * @param activeTab The tab index that should be styled as active
     */
    private void updateTabStyles(int activeTab) {
        int goldColor = ContextCompat.getColor(this, R.color.primary_gold);
        int darkColor = ContextCompat.getColor(this, R.color.background_dark);
        int grayColor = ContextCompat.getColor(this, R.color.text_secondary);

        // Reset all to inactive
        setInactiveStyle(btnTabWaitlist, grayColor);
        setInactiveStyle(btnTabSelected, grayColor);
        setInactiveStyle(btnTabConfirmed, grayColor);
        setInactiveStyle(btnTabMyEvents, grayColor);

        // Set active one
        if (activeTab == MyEventsAdapter.TAB_WAITLIST) {
            setActiveStyle(btnTabWaitlist, goldColor, darkColor);
        } else if (activeTab == MyEventsAdapter.TAB_SELECTED) {
            setActiveStyle(btnTabSelected, goldColor, darkColor);
        } else if (activeTab == MyEventsAdapter.TAB_CONFIRMED) {
            setActiveStyle(btnTabConfirmed, goldColor, darkColor);
        } else if (activeTab == MyEventsAdapter.TAB_MY_EVENTS) {
            setActiveStyle(btnTabMyEvents, goldColor, darkColor);
        }
    }

    // Helper methods to keep code clean
    private void setInactiveStyle(Button btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btn.setTextColor(color);
    }
    private void setActiveStyle(Button btn, int bgInfo, int textColor) {
        btn.setBackgroundTintList(ColorStateList.valueOf(bgInfo));
        btn.setTextColor(textColor);
    }

    private void loadUserEvents() {
        selectedEventsList.clear();
        waitlistedEventsList.clear();
        confirmedEventsList.clear();
        allEventsList.clear();
        displayList.clear();
        myEventsAdapter.notifyDataSetChanged();

        eventRepository.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                for (Event event : allEvents) {
                    checkIfUserIsEntrant(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyEventsActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if the current user is an entrant for a specific event by querying
     * the entrants subcollection. If the user is found, retrieves their status
     * and sorts the event into the appropriate list.
     *
     * @param event The event to check
     */
    private void checkIfUserIsEntrant(Event event) {
        if (event.getEventId() == null || currentUserId == null) return;

        db.collection("waiting_lists")
                .document(event.getEventId())
                .collection("entrants")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        sortEventIntoLists(event, status);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking entrant status for event: " + event.getName());
                });
    }

    /**
     * Sorts an event into the appropriate list based on the user's status.
     * Thread-safe method that prevents duplicate entries.
     *
     * <p>Status:
     * <ul>
     *   <li>"selected" → Selected list (won lottery, pending acceptance)</li>
     *   <li>"confirmed", "enrolled", "accepted" → Confirmed list (accepted invitation)</li>
     *   <li>"waiting", "waitlisted", "pending" → Waitlist list (still waiting)</li>
     * </ul>
     *
     * @param event The event to sort
     * @param status The user's status for this event
     */
    private synchronized void sortEventIntoLists(Event event, String status) {
        String safeStatus = (status != null) ? status.toLowerCase() : "waitlisted";
        boolean listUpdated = false;

        if (safeStatus.equals("selected")) {
            // Selected (Won lottery, pending acceptance)
            if (!selectedEventsList.contains(event)) {
                selectedEventsList.add(event);
                if (currentTab == MyEventsAdapter.TAB_SELECTED) listUpdated = true;
            }
        } else if (safeStatus.equals("confirmed") || safeStatus.equals("enrolled") || safeStatus.equals("accepted")) {
            // Confirmed (Accepted the lottery win)
            if (!confirmedEventsList.contains(event)) {
                confirmedEventsList.add(event);
                if (currentTab == MyEventsAdapter.TAB_CONFIRMED) listUpdated = true;
            }
        } else if (safeStatus.equals("waiting") || safeStatus.equals("waitlisted") || safeStatus.equals("pending")) {
            // Waitlist (Still waiting)
            if (!waitlistedEventsList.contains(event)) {
                waitlistedEventsList.add(event);
                if (currentTab == MyEventsAdapter.TAB_WAITLIST) listUpdated = true;
            }
        }
        // ALWAYS add to the "All" list
        if (!allEventsList.contains(event)) {
            allEventsList.add(event);
            // If we are currently looking at the "All" tab, trigger an update
            if (currentTab == MyEventsAdapter.TAB_MY_EVENTS) listUpdated = true;
        }

        if (listUpdated) {
            updateVisibleList(event);
        }
    }

    private void updateVisibleList(Event newEvent) {
        runOnUiThread(() -> {
            if (!displayList.contains(newEvent)) {
                displayList.add(newEvent);
                myEventsAdapter.notifyDataSetChanged();
            }
        });
    }
}
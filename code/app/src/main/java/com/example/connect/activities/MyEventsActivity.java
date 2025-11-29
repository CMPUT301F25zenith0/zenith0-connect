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

public class MyEventsActivity extends AppCompatActivity {

    private static final String TAG = "MyEventsActivity";

    // UI Components
    private Button scanBtn, profileBtn, homeBtn, myEventsBtn, notificationBtn;
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

        myEventsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < displayList.size()) {
                Event event = displayList.get(position);
                Intent intent = new Intent(MyEventsActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", event.getEventId());
                startActivity(intent);
            }
        });
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        displayList.clear();

        if (tabIndex == MyEventsAdapter.TAB_SELECTED) {
            displayList.addAll(selectedEventsList);
            emptyView.setText("No selected events found.");
        } else if (tabIndex == MyEventsAdapter.TAB_CONFIRMED) {
            displayList.addAll(confirmedEventsList);
            emptyView.setText("No confirmed events found.");
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

    private void updateTabStyles(int activeTab) {
        int goldColor = ContextCompat.getColor(this, R.color.primary_gold);
        int darkColor = ContextCompat.getColor(this, R.color.background_dark);
        int grayColor = ContextCompat.getColor(this, R.color.text_secondary);

        // Reset all to inactive first
        btnTabWaitlist.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnTabWaitlist.setTextColor(grayColor);

        btnTabSelected.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnTabSelected.setTextColor(grayColor);

        btnTabConfirmed.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        btnTabConfirmed.setTextColor(grayColor);

        // Set active one
        if (activeTab == MyEventsAdapter.TAB_WAITLIST) {
            btnTabWaitlist.setBackgroundTintList(ColorStateList.valueOf(goldColor));
            btnTabWaitlist.setTextColor(darkColor);
        } else if (activeTab == MyEventsAdapter.TAB_SELECTED) {
            btnTabSelected.setBackgroundTintList(ColorStateList.valueOf(goldColor));
            btnTabSelected.setTextColor(darkColor);
        } else if (activeTab == MyEventsAdapter.TAB_CONFIRMED) {
            btnTabConfirmed.setBackgroundTintList(ColorStateList.valueOf(goldColor));
            btnTabConfirmed.setTextColor(darkColor);
        }
    }

    private void loadUserEvents() {
        selectedEventsList.clear();
        waitlistedEventsList.clear();
        confirmedEventsList.clear();
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
package com.example.connect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Displays detailed information about a single event to administrators.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private View contentGroup;
    private ListenerRegistration waitlistListener;
    private String currentEventId;

    private TextView tvTitle;
    private TextView tvOrganizer;
    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvPrice;
    private TextView tvDescription;
    private TextView tvRegWindow;
    private TextView tvWaitingList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_details);

        db = FirebaseFirestore.getInstance();
        initViews();

        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (currentEventId == null || currentEventId.isEmpty()) {
            Toast.makeText(this, "Event reference missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent(currentEventId);
        listenForWaitlist(currentEventId);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
            toolbar.setNavigationIconTint(getResources().getColor(R.color.f3));
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        progressBar = findViewById(R.id.progress_bar);
        contentGroup = findViewById(R.id.content_group);

        ImageView heroImage = findViewById(R.id.hero_image);
        tvTitle = findViewById(R.id.tv_event_title);
        tvOrganizer = findViewById(R.id.tv_event_organizer);
        tvDate = findViewById(R.id.tv_event_date);
        tvLocation = findViewById(R.id.tv_event_location);
        tvPrice = findViewById(R.id.tv_event_price);
        tvDescription = findViewById(R.id.tv_event_description);
        tvRegWindow = findViewById(R.id.tv_reg_window);
        tvWaitingList = findViewById(R.id.tv_waiting_list);
    }

    private void loadEvent(String eventId) {
        progressBar.setVisibility(View.VISIBLE);
        contentGroup.setVisibility(View.GONE);

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::bindEvent)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Unable to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindEvent(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(valueOrFallback(doc.getString("event_title"), "Unnamed Event"));
        tvOrganizer.setText(valueOrFallback(doc.getString("org_name"), "Unknown Organizer"));
        tvDate.setText(valueOrFallback(doc.getString("date_time"), "Date TBD"));
        tvLocation.setText(valueOrFallback(doc.getString("location"), "Location TBD"));

        Object price = doc.get("price");
        tvPrice.setText(price != null ? "$" + price : "Free");

        tvDescription.setText(valueOrFallback(doc.getString("description"), "No description available."));

        String regStart = doc.getString("reg_start");
        String regEnd = doc.getString("reg_stop");
        if (regStart != null || regEnd != null) {
            tvRegWindow.setText(
                    String.format("Registration: %s - %s",
                            valueOrFallback(regStart, "TBD"),
                            valueOrFallback(regEnd, "TBD"))
            );
        } else {
            tvRegWindow.setText("Registration window not configured");
        }

        // Waitlist text updated by live listener
        tvWaitingList.setText("Live Waitlist: --");

        progressBar.setVisibility(View.GONE);
        contentGroup.setVisibility(View.VISIBLE);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private void listenForWaitlist(String eventId) {
        if (eventId == null) return;

        if (waitlistListener != null) {
            waitlistListener.remove();
        }

        waitlistListener = db.collection("waiting_lists")
                .document(eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }
                    int count = 0;
                    if (snapshot != null && snapshot.exists()) {
                        java.util.List<String> entries = (java.util.List<String>) snapshot.get("entries");
                        count = entries != null ? entries.size() : 0;
                    }
                    tvWaitingList.setText(formatLiveWaitlist(count));
                });
    }

    private String formatLiveWaitlist(int count) {
        return "Live Waitlist: " + count + " entrant" + (count == 1 ? "" : "s");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitlistListener != null) {
            waitlistListener.remove();
            waitlistListener = null;
        }
    }
}


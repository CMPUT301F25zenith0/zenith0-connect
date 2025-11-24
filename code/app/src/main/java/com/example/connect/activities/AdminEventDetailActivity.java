package com.example.connect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays detailed information about a single event to administrators.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private View contentGroup;

    private TextView tvTitle;
    private TextView tvOrganizer;
    private TextView tvStatus;
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

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event reference missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent(eventId);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        progressBar = findViewById(R.id.progress_bar);
        contentGroup = findViewById(R.id.content_group);

        tvTitle = findViewById(R.id.tv_event_title);
        tvOrganizer = findViewById(R.id.tv_event_organizer);
        tvStatus = findViewById(R.id.tv_status);
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
        tvStatus.setText(valueOrFallback(doc.getString("status"), "Draft"));
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

        Long waiting = doc.getLong("waiting_list");
        tvWaitingList.setText("Waiting list entries: " + (waiting != null ? waiting : 0));

        progressBar.setVisibility(View.GONE);
        contentGroup.setVisibility(View.VISIBLE);
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}


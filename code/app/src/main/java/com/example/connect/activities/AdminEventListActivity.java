package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminEventAdapter;
import com.example.connect.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminEventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AdminEventAdapter adapter;
    private FirebaseFirestore db;
    private TextInputEditText searchInput;
    private View searchLayout;
    private final List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            loadEvents();
        } catch (Exception e) {
            Log.e("AdminEventList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Close activity to prevent stuck state
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Manage Events");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        searchLayout = findViewById(R.id.search_layout);
        searchInput = findViewById(R.id.search_input);

        if (searchLayout != null) {
            searchLayout.setVisibility(View.VISIBLE);
        }

        if (searchInput != null) {
            searchInput.setHint("Search events");
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterEvents(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminEventAdapter(this::deleteEvent, this::openEventDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void openEventDetails(Event event) {
        Intent intent = new Intent(this, EventDetails.class);
        intent.putExtra("EVENT_ID", event.getEventId());
        intent.putExtra("IS_ADMIN_VIEW", true);
        startActivity(intent);
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        events.add(event);
                    }

                    allEvents.clear();
                    allEvents.addAll(events);
                    applyCurrentFilter();

                    if (allEvents.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminEventList", "Error loading events", e);
                });
    }

    private void applyCurrentFilter() {
        String query = searchInput != null && searchInput.getText() != null
                ? searchInput.getText().toString()
                : "";
        filterEvents(query);
    }

    private void filterEvents(String query) {
        if (adapter == null) return;

        if (query == null) {
            query = "";
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Event> filtered = new ArrayList<>();
        if (lowerQuery.isEmpty()) {
            filtered.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                String name = event.getName() != null ? event.getName().toLowerCase() : "";
                String organizer = event.getOrganizerId() != null ? event.getOrganizerId().toLowerCase() : "";
                if (name.contains(lowerQuery) || organizer.contains(lowerQuery)) {
                    filtered.add(event);
                }
            }
        }

        adapter.setEvents(filtered);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void deleteEvent(Event event) {
        if (event.getEventId() == null)
            return;

        // Confirm deletion (Optional: Add a dialog here)
        // For now, direct delete

        db.collection("events").document(event.getEventId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    loadEvents(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

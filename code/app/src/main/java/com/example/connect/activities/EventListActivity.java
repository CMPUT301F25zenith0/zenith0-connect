package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventListAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View progress;
    private TextView empty;
    private TextInputEditText searchEditText;

    private EventListAdapter adapter;
    private final EventRepository repo = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recycler       = findViewById(R.id.events_recycler);
        progress       = findViewById(R.id.events_progress);
        empty          = findViewById(R.id.events_empty);
        searchEditText = findViewById(R.id.search_edit_text);
        
        // Setup menu button (hamburger menu)
        android.widget.ImageButton btnMenu = findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                // TODO: Open navigation drawer or menu
                android.util.Log.d("EventListActivity", "Menu button clicked");
            });
        }

        // Check if critical views are found
        if (recycler == null) {
            android.util.Log.e("EventListActivity", "RecyclerView not found!");
            finish();
            return;
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(new EventListAdapter.Listener() {
            @Override public void onDetails(Event e) {
                // Navigate to Event Details screen
                Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                intent.putExtra("event", e);
                startActivity(intent);
            }
            @Override public void onJoin(Event e) {
                // TODO: Implement join waitlist functionality
                Toast.makeText(EventListActivity.this, "Join waitlist functionality coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(adapter);

        // Setup search functionality
        if (searchEditText != null) {
            // Filter as user types
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s == null ? "" : s.toString());
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            // Handle search button click
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            });
        }

        loadJoinable();
    }

    private void loadJoinable() {
        // Add null checks for views
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
        if (empty != null) {
            empty.setVisibility(View.GONE);
        }

        repo.fetchJoinableEvents(new EventRepository.EventsCallback() {
            @Override 
            public void onSuccess(List<Event> events) {
                runOnUiThread(() -> {
                    if (progress != null) {
                        progress.setVisibility(View.GONE);
                    }
                    if (adapter != null) {
                        adapter.submit(events);
                    }
                    if (empty != null) {
                        empty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
            
            @Override 
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (progress != null) {
                        progress.setVisibility(View.GONE);
                    }
                    if (empty != null) {
                        String errorMsg = getString(R.string.events_error_generic);
                        if (e != null && e.getMessage() != null) {
                            errorMsg += "\n" + e.getMessage();
                        }
                        empty.setText(errorMsg);
                        empty.setVisibility(View.VISIBLE);
                    }
                    android.util.Log.e("EventsRepo", "Failed to load events", e);
                });
            }
        });
    }
}

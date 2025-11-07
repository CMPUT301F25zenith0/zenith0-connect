package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventListAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View progress;
    private TextView empty;
    private TextInputEditText searchEditText;
    private ImageButton btnMenu, btnProfile;
    private Chip chipInterest, chipDate, chipLocation, chipClearFilters;

    private EventListAdapter adapter;
    private final EventRepository repo = new EventRepository();

    // Filter state variables
    private String selectedDate = "";
    private String selectedInterest = "";
    private String selectedLocation = "";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recycler = findViewById(R.id.events_recycler);
        progress = findViewById(R.id.events_progress);
        empty = findViewById(R.id.events_empty);
        searchEditText = findViewById(R.id.search_edit_text);
        btnMenu = findViewById(R.id.btn_menu);
        btnProfile = findViewById(R.id.btn_profile);
        chipInterest = findViewById(R.id.chip_interest);
        chipDate = findViewById(R.id.chip_date);
        chipLocation = findViewById(R.id.chip_location);
        chipClearFilters = findViewById(R.id.chip_clear_filters);

        if (recycler == null) {
            Log.e("EventListActivity", "RecyclerView not found!");
            finish();
            return;
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(new EventListAdapter.Listener() {
            @Override public void onDetails(Event e) {
                Intent intent = new Intent(EventListActivity.this, EventDetails.class);
                intent.putExtra("EVENT_ID", e.getEventId());
                startActivity(intent);
            }
            @Override public void onJoin(Event e) {
                Toast.makeText(EventListActivity.this, "Join waitlist functionality coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(adapter);

        setupClickListeners();
        loadJoinable();
    }

    private void setupClickListeners() {
        // Menu button
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Log.d("EventListActivity", "Menu button clicked");
            });
        }

        // Profile button
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            });
        }

        // Search filter
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    applyAllFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            });
        }

        // Date filter chip
        if (chipDate != null) {
            chipDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                        EventListActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String selectedDateStr = String.format("%04d-%02d-%02d", 
                                    selectedYear, selectedMonth + 1, selectedDay);
                            selectedDate = selectedDateStr;
                            chipDate.setText(selectedDateStr);
                            chipDate.setChecked(true);
                            applyAllFilters();
                        },
                        year, month, day
                );
                picker.show();
            });

            chipDate.setOnLongClickListener(v -> {
                selectedDate = "";
                chipDate.setText("Date");
                chipDate.setChecked(false);
                applyAllFilters();
                return true;
            });
        }

        // Interest filter chip
        if (chipInterest != null) {
            chipInterest.setOnClickListener(v -> {
                final android.widget.EditText input = new android.widget.EditText(EventListActivity.this);
                input.setHint("Enter an interest (e.g., Music, Tech, Sports)");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
                builder.setTitle("Filter by Interest");
                builder.setView(input);

                builder.setPositiveButton("Apply", (dialog, which) -> {
                    String userInterest = input.getText().toString().trim();
                    if (!userInterest.isEmpty()) {
                        selectedInterest = userInterest;
                        chipInterest.setText(userInterest);
                        chipInterest.setChecked(true);
                        applyAllFilters();
                    } else {
                        Toast.makeText(EventListActivity.this, "Please enter an interest", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            });

            chipInterest.setOnLongClickListener(v -> {
                selectedInterest = "";
                chipInterest.setText("Interest");
                chipInterest.setChecked(false);
                applyAllFilters();
                return true;
            });
        }

        // Location filter chip
        if (chipLocation != null) {
            chipLocation.setOnClickListener(v -> {
                final android.widget.EditText input = new android.widget.EditText(EventListActivity.this);
                input.setHint("Enter a location");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EventListActivity.this);
                builder.setTitle("Filter by Location");
                builder.setView(input);

                builder.setPositiveButton("Apply", (dialog, which) -> {
                    String userLocation = input.getText().toString().trim();
                    if (!userLocation.isEmpty()) {
                        selectedLocation = userLocation;
                        chipLocation.setText(userLocation);
                        chipLocation.setChecked(true);
                        applyAllFilters();
                    } else {
                        Toast.makeText(EventListActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            });

            chipLocation.setOnLongClickListener(v -> {
                selectedLocation = "";
                chipLocation.setText("Location");
                chipLocation.setChecked(false);
                applyAllFilters();
                return true;
            });
        }

        // Clear filters chip
        if (chipClearFilters != null) {
            chipClearFilters.setOnClickListener(v -> {
                clearAllFilters();
            });
        }
    }

    private void loadJoinable() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (empty != null) empty.setVisibility(View.GONE);

        repo.getAllEvents(new EventRepository.EventCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                runOnUiThread(() -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (adapter != null) adapter.submit(events);
                    if (empty != null) empty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
                    applyAllFilters();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (empty != null) {
                        String errorMsg = getString(R.string.events_error_generic);
                        if (e != null && e.getMessage() != null) errorMsg += "\n" + e.getMessage();
                        empty.setText(errorMsg);
                        empty.setVisibility(View.VISIBLE);
                    }
                    Log.e("EventsRepo", "Failed to load events", e);
                });
            }
        });
    }

    private void applyAllFilters() {
        if (adapter == null) return;

        // Start with all events
        List<Event> filtered = adapter.getAllEvents();

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            String lowerQuery = currentSearchQuery.toLowerCase();
            List<Event> searchFiltered = new java.util.ArrayList<>();
            for (Event event : filtered) {
                if (matchesSearch(event, lowerQuery)) {
                    searchFiltered.add(event);
                }
            }
            filtered = searchFiltered;
        }

        // Apply date filter
        if (!selectedDate.isEmpty()) {
            List<Event> dateFiltered = new java.util.ArrayList<>();
            for (Event event : filtered) {
                if (event.getDateTime() != null && event.getDateTime().contains(selectedDate)) {
                    dateFiltered.add(event);
                }
            }
            filtered = dateFiltered;
        }

        // Apply interest filter
        if (!selectedInterest.isEmpty()) {
            String lowerInterest = selectedInterest.toLowerCase();
            List<Event> interestFiltered = new java.util.ArrayList<>();
            for (Event event : filtered) {
                if (event.getCategory() != null && 
                    event.getCategory().toLowerCase().contains(lowerInterest)) {
                    interestFiltered.add(event);
                }
            }
            filtered = interestFiltered;
        }

        // Apply location filter
        if (!selectedLocation.isEmpty()) {
            String lowerLocation = selectedLocation.toLowerCase();
            List<Event> locationFiltered = new java.util.ArrayList<>();
            for (Event event : filtered) {
                if (event.getLocation() != null && 
                    event.getLocation().toLowerCase().contains(lowerLocation)) {
                    locationFiltered.add(event);
                }
            }
            filtered = locationFiltered;
        }

        // Update adapter with filtered results
        adapter.submitList(filtered);
    }

    private boolean matchesSearch(Event event, String query) {
        return (event.getName() != null && event.getName().toLowerCase().contains(query)) ||
               (event.getLocation() != null && event.getLocation().toLowerCase().contains(query)) ||
               (event.getCategory() != null && event.getCategory().toLowerCase().contains(query)) ||
               (event.getDescription() != null && event.getDescription().toLowerCase().contains(query));
    }

    private void clearAllFilters() {
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        currentSearchQuery = "";

        selectedDate = "";
        if (chipDate != null) {
            chipDate.setText("Date");
            chipDate.setChecked(false);
        }

        selectedInterest = "";
        if (chipInterest != null) {
            chipInterest.setText("Interest");
            chipInterest.setChecked(false);
        }

        selectedLocation = "";
        if (chipLocation != null) {
            chipLocation.setText("Location");
            chipLocation.setChecked(false);
        }

        if (adapter != null) {
            adapter.clearFilters();
        }

        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJoinable();
    }


}
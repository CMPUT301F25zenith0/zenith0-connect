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
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
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

        recycler = findViewById(R.id.events_recycler);
        progress = findViewById(R.id.events_progress);
        empty = findViewById(R.id.events_empty);
        searchEditText = findViewById(R.id.search_edit_text);

        // Setup menu button
        android.widget.ImageButton btnMenu = findViewById(R.id.btn_menu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    android.util.Log.d("EventListActivity", "Menu button clicked")
            );
        }

        // Check RecyclerView
        if (recycler == null) {
            android.util.Log.e("EventListActivity", "RecyclerView not found!");
            finish();
            return;
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(new EventListAdapter.Listener() {
            @Override public void onDetails(Event e) {
                Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                intent.putExtra("event", e);
                startActivity(intent);
            }

            @Override public void onJoin(Event e) {
                Toast.makeText(EventListActivity.this, "Join waitlist functionality coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(adapter);

        // Search functionality
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s == null ? "" : s.toString());
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

        // Load events
        loadJoinable();

        // ✅ Date filter chip
        Chip dateChip = findViewById(R.id.chip_date);
        if (dateChip != null) {
            // Open DatePickerDialog
            dateChip.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                        EventListActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);

                            // Update chip text & mark checked
                            dateChip.setText(selectedDate);
                            dateChip.setChecked(true);

                            // Filter events
                            if (adapter != null) adapter.filterByDate(selectedDate);
                        },
                        year, month, day
                );
                picker.show();
            });

            // Long press to clear date filter
            dateChip.setOnLongClickListener(v -> {
                dateChip.setText("Date");
                dateChip.setChecked(false);

                // Show all events
                if (adapter != null) adapter.filterByDate(""); // pass empty string to reset
                return true;
            });
        }


        // interest filter button

        // Define interest chip
        Chip interestChip = findViewById(R.id.chip_interest);



        // clear button
        Chip clearChip = findViewById(R.id.chip_clear_filters);
        if (clearChip != null) {
            clearChip.setOnClickListener(v -> {
                // Clear search text
                if (searchEditText != null) {
                    searchEditText.setText("");
                }

                // Reset all other chips (like date, interest, location)
                if (dateChip != null) {  // <-- use existing variable
                    dateChip.setText("Date");
                    dateChip.setChecked(false);
                }

                if (interestChip != null) interestChip.setChecked(false);
                // if (locationChip != null) locationChip.setChecked(false); --- location filter later

                // Reset RecyclerView
                if (adapter != null) adapter.clearFilters();

            });
        }




    }

    private void loadJoinable() {
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (empty != null) empty.setVisibility(View.GONE);

        repo.fetchJoinableEvents(new EventRepository.EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                runOnUiThread(() -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (adapter != null) adapter.submit(events);
                    if (empty != null) empty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (empty != null) {
                        String errorMsg = getString(R.string.events_error_generic);
                        if (e != null && e.getMessage() != null) errorMsg += "\n" + e.getMessage();
                        empty.setText(errorMsg);
                        empty.setVisibility(View.VISIBLE);
                    }
                    android.util.Log.e("EventsRepo", "Failed to load events", e);
                });
            }
        });
    }
}

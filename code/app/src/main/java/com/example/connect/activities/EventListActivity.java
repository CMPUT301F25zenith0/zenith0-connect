package com.example.connect.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventListAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View progress;
    private TextView empty;

    private SearchBar searchBar;     // Material 3 SearchBar
    private SearchView searchView;   // Paired SearchView overlay

    private EventListAdapter adapter;
    private final EventRepository repo = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recycler   = findViewById(R.id.events_recycler);
        progress   = findViewById(R.id.events_progress);
        empty      = findViewById(R.id.events_empty);
        searchBar  = findViewById(R.id.searchBar);
        searchView = findViewById(R.id.search_view);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(new EventListAdapter.Listener() {
            @Override public void onDetails(Event e) { /* TODO: navigate */ }
            @Override public void onJoin(Event e)    { /* TODO: join */ }
        });
        recycler.setAdapter(adapter);

        // Open the floating SearchView when the bar is tapped
        searchBar.setOnClickListener(v -> searchView.show());

        // Filter as user types
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Optional: submit closes the overlay
        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            searchBar.setText(v.getText());
            searchView.hide();
            return true;
        });

        loadJoinable();
    }

    private void loadJoinable() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        repo.fetchJoinableEvents(new EventRepository.EventsCallback() {
            @Override public void onSuccess(List<Event> events) {
                progress.setVisibility(View.GONE);
                adapter.submit(events);
                empty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                empty.setText(getString(R.string.events_error_generic)
                        + (e != null && e.getMessage() != null ? ("\n" + e.getMessage()) : ""));
                empty.setVisibility(View.VISIBLE);
                android.util.Log.e("EventsRepo", "Failed to load events", e);
            }
        });
    }
}

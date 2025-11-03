package com.example.connect.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.EventListAdapter;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;

import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View progress;
    private TextView empty;
    private EventListAdapter adapter;
    private final EventRepository repo = new EventRepository();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recycler = findViewById(R.id.events_recycler);
        progress = findViewById(R.id.events_progress);
        empty = findViewById(R.id.events_empty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(event -> {
            // TODO: Navigate to Event details screen when you build it
            // For now, no-op.
        });
        recycler.setAdapter(adapter);

        loadJoinable();
    }

    private void loadJoinable() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        repo.fetchJoinableEvents(new EventRepository.EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                progress.setVisibility(View.GONE);
                adapter.submit(events);
                empty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                empty.setText(getString(R.string.events_error_generic));
                empty.setVisibility(View.VISIBLE);
            }
        });
    }
}

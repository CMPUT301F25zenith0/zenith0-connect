package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.OrganizerEventAdapter;
import com.example.connect.models.DashboardEvent;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OrganizerActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private OrganizerEventAdapter adapter;

    private final List<DashboardEvent> all = new ArrayList<>();
    private final List<DashboardEvent> visible = new ArrayList<>();

    private MaterialButton btnTotal, btnOpen, btnClosed, btnDrawn;
    private MaterialButton btnNewEvent, navDash, navMsg, navMap, navProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        // Views
        recycler    = findViewById(R.id.recyclerViewEvents);
        btnNewEvent = findViewById(R.id.btnNewEvent);
        btnTotal    = findViewById(R.id.btnTotalEvents);
        btnOpen     = findViewById(R.id.btnOpen);
        btnClosed   = findViewById(R.id.btnClosed);
        btnDrawn    = findViewById(R.id.btnDrawn);
        navDash     = findViewById(R.id.btnNavDashboard);
        navMsg      = findViewById(R.id.btnNavMessage);
        navMap      = findViewById(R.id.btnNavMap);
        navProfile  = findViewById(R.id.btnNavProfile);

        // Adapter FIRST (so we can set it right away)
        adapter = new OrganizerEventAdapter(visible, new OrganizerEventAdapter.OnRowAction() {
            @Override
            public void onEdit(DashboardEvent e) {
                Intent i = new Intent(OrganizerActivity.this, EditEventActivity.class);
                i.putExtra("eventId", e.getId());
                startActivity(i);
            }

            @Override
            public void onDetails(DashboardEvent e) {
                Intent i = new Intent(OrganizerActivity.this, EventDetailsActivity.class);
                i.putExtra("eventId", e.getId());
                startActivity(i);
            }

            @Override
            public void onManageDraw(DashboardEvent e) {
                Intent i = new Intent(OrganizerActivity.this, ManageDrawActivity.class);
                i.putExtra("eventId", e.getId());
                startActivity(i);
            }

            @Override
            public void onExportCsv(DashboardEvent e) {
                toast("Export CSV for: " + e.getTitle());
                // TODO: implement CSV export/share
            }
        });

        // Recycler (use the FIELD, do not shadow it)
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        recycler.setNestedScrollingEnabled(true);   // allow dashboard to scroll
        recycler.setHasFixedSize(false);

        // Seed demo data and show default list
        seed();
        applyFilter(null);

        // Filters
        btnTotal.setOnClickListener(v -> applyFilter(null));
        btnOpen.setOnClickListener(v -> applyFilter(DashboardEvent.Status.OPEN));
        btnClosed.setOnClickListener(v -> applyFilter(DashboardEvent.Status.CLOSED));
        btnDrawn.setOnClickListener(v -> applyFilter(DashboardEvent.Status.DRAWN));

        // Top-right New Event (placeholder)
        btnNewEvent.setOnClickListener(v -> toast("New Event (TODO)"));

        // Bottom nav
        navDash.setOnClickListener(v -> { /* already here */ });
        navMsg.setOnClickListener(v -> startActivity(new Intent(this, MessagesActivity.class)));
        navMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void seed() {
        all.clear();
        all.add(new DashboardEvent("1", "Campus Games Night", "Board games & snacks", DashboardEvent.Status.OPEN));
        all.add(new DashboardEvent("2", "Hackathon", "48h build sprint", DashboardEvent.Status.CLOSED));
        all.add(new DashboardEvent("3", "Marathon Lottery", "Pick runners", DashboardEvent.Status.DRAWN));
        all.add(new DashboardEvent("4", "Art Workshop", "Watercolor basics", DashboardEvent.Status.OPEN));
        all.add(new DashboardEvent("5", "Robotics Demo", "Lab tour", DashboardEvent.Status.CLOSED));
    }

    private void applyFilter(@Nullable DashboardEvent.Status status) {
        visible.clear();
        for (DashboardEvent e : all) {
            if (status == null || e.getStatus() == status) {
                visible.add(e);
            }
        }
        adapter.submit(visible);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}

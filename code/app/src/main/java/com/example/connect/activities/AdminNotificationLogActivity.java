package com.example.connect.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AdminNotificationLogActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private android.widget.ProgressBar progressBar;
    private TextView tvEmptyState;
    private com.example.connect.adapters.AdminNotificationAdapter adapter;
    private com.google.firebase.firestore.FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadNotificationLogs();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Notification Logs");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new com.example.connect.adapters.AdminNotificationAdapter();
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotificationLogs() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        tvEmptyState.setVisibility(android.view.View.GONE);

        // Try with orderBy first, fallback to unordered if index missing
        db.collection("notification_logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    java.util.List<java.util.Map<String, Object>> logs = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        java.util.Map<String, Object> data = document.getData();
                        data.put("documentId", document.getId()); // Add document ID for reference
                        logs.add(data);
                    }

                    // Sort manually if needed (fallback)
                    if (!logs.isEmpty()) {
                        logs.sort((a, b) -> {
                            com.google.firebase.Timestamp tsA = (com.google.firebase.Timestamp) a.get("timestamp");
                            com.google.firebase.Timestamp tsB = (com.google.firebase.Timestamp) b.get("timestamp");
                            if (tsA == null || tsB == null) return 0;
                            return tsB.compareTo(tsA); // Descending
                        });
                    }

                    if (logs.isEmpty()) {
                        tvEmptyState.setText("No notification logs found");
                        tvEmptyState.setVisibility(android.view.View.VISIBLE);
                    } else {
                        adapter.setNotifications(logs);
                    }
                })
                .addOnFailureListener(e -> {
                    // If orderBy fails (likely missing index), try without ordering
                    android.util.Log.w("AdminNotifLog", "Ordered query failed, trying unordered", e);
                    db.collection("notification_logs")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                java.util.List<java.util.Map<String, Object>> logs = new java.util.ArrayList<>();
                                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    java.util.Map<String, Object> data = document.getData();
                                    data.put("documentId", document.getId());
                                    logs.add(data);
                                }

                                // Sort manually by timestamp
                                if (!logs.isEmpty()) {
                                    logs.sort((a, b) -> {
                                        com.google.firebase.Timestamp tsA = (com.google.firebase.Timestamp) a.get("timestamp");
                                        com.google.firebase.Timestamp tsB = (com.google.firebase.Timestamp) b.get("timestamp");
                                        if (tsA == null || tsB == null) return 0;
                                        return tsB.compareTo(tsA); // Descending
                                    });
                                }

                                if (logs.isEmpty()) {
                                    tvEmptyState.setText("No notification logs found");
                                    tvEmptyState.setVisibility(android.view.View.VISIBLE);
                                } else {
                                    adapter.setNotifications(logs);
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                android.widget.Toast
                                        .makeText(this, "Error loading logs: " + e2.getMessage(), android.widget.Toast.LENGTH_LONG)
                                        .show();
                                android.util.Log.e("AdminNotifLog", "Error loading logs", e2);
                                tvEmptyState.setText("Error loading notification logs");
                                tvEmptyState.setVisibility(android.view.View.VISIBLE);
                            });
                });
    }
}

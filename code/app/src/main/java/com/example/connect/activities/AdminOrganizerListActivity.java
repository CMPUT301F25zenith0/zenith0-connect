package com.example.connect.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminProfileAdapter;
import com.example.connect.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminOrganizerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AdminProfileAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            loadOrganizers();
        } catch (Exception e) {
            Log.e("AdminOrgList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Manage Organizers");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(this::deleteOrganizer, this::openOrganizerDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void openOrganizerDetails(User user) {
        if (user.getUserId() == null)
            return;

        android.content.Intent intent = new android.content.Intent(this, ProfileActivity.class);
        intent.putExtra("user_id_admin_view", user.getUserId());
        intent.putExtra("IS_ADMIN_VIEW", true);
        startActivity(intent);
    }

    private void loadOrganizers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        // For now, we'll fetch all users and filter locally or just show all users as
        // potential organizers
        // Ideally, we would query users where role == "organizer" or similar
        // Since the schema isn't fully clear on roles, I'll fetch all users for now
        // TODO: Refine query to only show organizers

        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
                        // Optional: Filter logic here
                        users.add(user);
                    }

                    if (users.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setUsers(users);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading organizers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminOrgList", "Error loading organizers", e);
                });
    }

    private void deleteOrganizer(User user) {
        if (user.getUserId() == null)
            return;

        String userId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        // 1. Find all events created by this organizer
        db.collection("events")
                .whereEqualTo("organizer_id", userId)
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    // Create a batch for atomic operations if possible, but for now we'll do it
                    // sequentially or parallel
                    // Since we might have many events, we'll just iterate and delete.
                    // A batch has a limit of 500 operations.

                    List<com.google.android.gms.tasks.Task<Void>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                        String eventId = eventDoc.getId();
                        // Delete event
                        tasks.add(db.collection("events").document(eventId).delete());
                        // Delete waiting list
                        tasks.add(db.collection("waiting_lists").document(eventId).delete());
                    }

                    // Wait for all event deletions to complete
                    com.google.android.gms.tasks.Tasks.whenAll(tasks)
                            .addOnSuccessListener(aVoid -> {
                                // 2. Delete the user account
                                db.collection("accounts").document(userId)
                                        .delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Organizer and their events removed",
                                                    Toast.LENGTH_SHORT).show();
                                            loadOrganizers(); // Refresh list
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Error removing organizer account: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error removing organizer's events: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error finding organizer's events: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}

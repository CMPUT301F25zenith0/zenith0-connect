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

public class AdminProfileListActivity extends AppCompatActivity {

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
            loadProfiles();
        } catch (Exception e) {
            Log.e("AdminProfileList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Manage Profiles");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(this::deleteProfile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadProfiles() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
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
                    Toast.makeText(this, "Error loading profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminProfileList", "Error loading profiles", e);
                });
    }

    private void deleteProfile(User user) {
        if (user.getUserId() == null)
            return;

        String userId = user.getUserId();

        // 1. Delete events organized by this user
        db.collection("events")
                .whereEqualTo("org_name", userId) // Assuming org_name stores userId based on Event model
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();
                        // Delete the event
                        db.collection("events").document(eventId).delete();
                        // Delete the waiting list for this event
                        db.collection("waiting_lists").document(eventId).delete();
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminProfileList", "Error deleting organized events", e));

        // 2. Remove user from all waiting lists they joined
        db.collection("waiting_lists")
                .whereArrayContains("entries", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();
                        db.collection("waiting_lists").document(eventId)
                                .update("entries", com.google.firebase.firestore.FieldValue.arrayRemove(userId));
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminProfileList", "Error removing user from waiting lists", e));

        // 3. Delete the user profile
        db.collection("accounts").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile and related data deleted", Toast.LENGTH_SHORT).show();
                    loadProfiles(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

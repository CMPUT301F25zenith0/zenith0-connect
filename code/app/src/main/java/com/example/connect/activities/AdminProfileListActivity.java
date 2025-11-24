package com.example.connect.activities;

import android.content.Intent;
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
        adapter = new AdminProfileAdapter(this::deleteProfile, this::openProfileDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void openProfileDetails(User user) {
        if (user.getUserId() == null)
            return;

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id_admin_view", user.getUserId());
        intent.putExtra("IS_ADMIN_VIEW", true);
        startActivity(intent);
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
                        // Only show users who are NOT admins and NOT disabled
                        if (!document.contains("admin") &&
                                (document.getBoolean("disabled") == null || !document.getBoolean("disabled"))) {
                            User user = document.toObject(User.class);
                            user.setUserId(document.getId());
                            users.add(user);
                        }
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
        progressBar.setVisibility(View.VISIBLE);

        // Step 1: Mark user as disabled in Firestore
        db.collection("accounts").document(userId)
                .update("disabled", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminProfileList", "User marked as disabled");

                    // Step 2: Delete events organized by this user and their waiting lists
                    db.collection("events")
                            .whereEqualTo("organizer_id", userId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String eventId = document.getId();
                                    // Delete the event
                                    db.collection("events").document(eventId).delete();
                                    // Delete the waiting list for this event
                                    db.collection("waiting_lists").document(eventId).delete();
                                }

                                // Step 3: Remove user from all waiting lists they joined
                                db.collection("waiting_lists")
                                        .whereArrayContains("entries", userId)
                                        .get()
                                        .addOnSuccessListener(waitingListSnapshots -> {
                                            for (QueryDocumentSnapshot doc : waitingListSnapshots) {
                                                db.collection("waiting_lists").document(doc.getId())
                                                        .update("entries", com.google.firebase.firestore.FieldValue.arrayRemove(userId));
                                            }

                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "User account disabled successfully", Toast.LENGTH_SHORT).show();
                                            loadProfiles(); // Refresh list (user will disappear)
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Error removing user from waiting lists", Toast.LENGTH_SHORT).show();
                                            Log.e("AdminProfileList", "Error removing user from waiting lists", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error deleting organized events", Toast.LENGTH_SHORT).show();
                                Log.e("AdminProfileList", "Error deleting organized events", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error disabling user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminProfileList", "Error disabling user", e);
                });
    }
}
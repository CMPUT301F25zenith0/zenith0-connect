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
        setContentView(R.layout.activity_admin_list);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadOrganizers();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Organizers");
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(this::deleteOrganizer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadOrganizers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        // For now, we'll fetch all users and filter locally or just show all users as
        // potential organizers
        // Ideally, we would query users where role == "organizer" or similar
        // Since the schema isn't fully clear on roles, I'll fetch all users for now
        // TODO: Refine query to only show organizers

        db.collection("users")
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

        db.collection("users").document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Organizer removed", Toast.LENGTH_SHORT).show();
                    loadOrganizers(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error removing organizer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

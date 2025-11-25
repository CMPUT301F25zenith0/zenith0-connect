package com.example.connect.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import java.util.Locale;

public class AdminOrganizerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etSearch;
    private AdminProfileAdapter adapter;
    private FirebaseFirestore db;
    private List<User> allOrganizers = new ArrayList<>(); // Stores the original, full list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            setupSearch();
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
        etSearch = findViewById(R.id.et_search);
    }

    private void setupRecyclerView() {
        adapter = new AdminProfileAdapter(this::deleteOrganizer, this::openOrganizerDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // setups search functionality on page
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger the filter logic every time the text changes
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Called with every adjustment to maintain live filtering of list
    private void filterList(String searchText) {
        String query = searchText.toLowerCase(Locale.getDefault()).trim();
        List<User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // If the search bar is empty, show the full list
            filteredList.addAll(allOrganizers);
        } else {
            for (User user : allOrganizers) {

                // Use the single 'name' field from the User model
                String fullName = user.getName() != null ? user.getName().toLowerCase(Locale.getDefault()) : "";
                String userId = user.getUserId() != null ? user.getUserId().toLowerCase(Locale.getDefault()) : "";

                // Check if the query is in the full name OR the user ID
                if (fullName.contains(query) || userId.contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        // Update the RecyclerView adapter with the filtered list
        adapter.setUsers(filteredList);

        // Update the empty state TextView visibility
        if (filteredList.isEmpty()) {
            String emptyMessage = query.isEmpty() ? "No organizers found." : "No organizers found matching \"" + searchText + "\".";
            tvEmptyState.setText(emptyMessage);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
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

        db.collection("accounts")
                .whereEqualTo("organizer", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allOrganizers.clear(); // Clear old data
                    List<User> currentOrganizers = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
                        currentOrganizers.add(user);
                    }

                    // Store the full list
                    allOrganizers.addAll(currentOrganizers);

                    // Initialize the adapter with the full list
                    filterList(etSearch.getText().toString());

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading organizers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminOrgList", "Error loading organizers", e);
                    // Ensure empty state is shown on failure
                    tvEmptyState.setText("Failed to load organizers.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                });

    }
    // Slightly modified deleteOrganizer
    private void deleteOrganizer(User user) {
        if (user.getUserId() == null) return;

        String userId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        // 1. Find all events created by this organizer
        db.collection("events")
                .whereEqualTo("organizer_id", userId)
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    // Create a list to hold all Firestore deletion tasks
                    List<com.google.android.gms.tasks.Task<Void>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                        String eventId = eventDoc.getId();

                        // Add task to delete the event
                        tasks.add(db.collection("events").document(eventId).delete());

                        // Add task to delete the associated waiting list
                        tasks.add(db.collection("waiting_lists").document(eventId).delete());
                    }

                    // Wait for all event and waiting list deletions to complete
                    com.google.android.gms.tasks.Tasks.whenAll(tasks)
                            .addOnSuccessListener(aVoid -> {
                                // 2. All events/waiting lists deleted. Now delete the user account
                                db.collection("accounts").document(userId)
                                        .delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Organizer and their events successfully removed",
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
                                // This usually fails if one of the batched delete operations failed
                                Toast.makeText(this, "Error removing organizer's events. Event deletion aborted.",
                                        Toast.LENGTH_SHORT).show();
                                Log.e("AdminOrgList", "Error deleting events in batch", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error finding organizer's events: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
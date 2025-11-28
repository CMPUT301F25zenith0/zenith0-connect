package com.example.connect.activities;

import android.content.Intent;
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
import com.example.connect.utils.NotificationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProfileListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etSearch;
    private View searchLayout;
    private AdminProfileAdapter adapter;
    private FirebaseFirestore db;
    private List<User> allProfiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            setupSearch();
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


        searchLayout = findViewById(R.id.search_layout);

        etSearch = findViewById(R.id.search_input);

        if (searchLayout != null) {
            searchLayout.setVisibility(View.VISIBLE);
        }
        // Update the hint for context
        if (etSearch != null) {
            etSearch.setHint("Search by name, email or ID");
        }
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

    // Setup TextWatcher
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

    // Filters the list based on search input (Name, ID, Email)
    private void filterList(String searchText) {
        String query = searchText.toLowerCase(Locale.getDefault()).trim();
        List<User> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allProfiles);
        } else {
            for (User user : allProfiles) {
                String name = user.getName() != null ? user.getName().toLowerCase(Locale.getDefault()) : "";
                String userId = user.getUserId() != null ? user.getUserId().toLowerCase(Locale.getDefault()) : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase(Locale.getDefault()) : "";

                // Check if the query is in any of chosen fields
                if (name.contains(query) || userId.contains(query) || email.contains(query)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.setUsers(filteredList);

        // Update the empty state TextView
        if (filteredList.isEmpty()) {
            String emptyMessage = query.isEmpty() ? "No profiles found." : "No profiles found matching \"" + searchText + "\".";
            tvEmptyState.setText(emptyMessage);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadProfiles() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    allProfiles.clear(); // Clear previous data

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Only show users who are NOT admins and NOT disabled
                        if (!document.contains("admin") &&
                                (document.getBoolean("disabled") == null || !document.getBoolean("disabled"))) {
                            User user = document.toObject(User.class);
                            user.setUserId(document.getId());
                            allProfiles.add(user); // Add to the master list
                        }
                    }

                    // Display the initial list or the filtered list if the search bar already has text
                    filterList(etSearch.getText().toString());

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminProfileList", "Error loading profiles", e);
                    // Ensure empty state is shown on failure
                    tvEmptyState.setText("Failed to load profiles.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
    }

    private void deleteProfile(User user) {
        if (user.getUserId() == null)
            return;

        String userId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        NotificationHelper notificationHelper = new NotificationHelper();

        // Step 1: Mark user as disabled in Firestore
        db.collection("accounts").document(userId)
                .update("disabled", true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminProfileList", "User marked as disabled");

                    // Step 2: Delete events organized by this user and notify participants
                    db.collection("events")
                            .whereEqualTo("organizer_id", userId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                                    String eventId = eventDoc.getId();
                                    String eventTitle = eventDoc.getString("event_title");
                                    if (eventTitle == null) eventTitle = "An event";

                                    // Collect users to notify
                                    List<String> usersToNotify = new ArrayList<>();

                                    // Get waiting list users
                                    String finalEventTitle = eventTitle;
                                    db.collection("waiting_lists").document(eventId).get()
                                            .addOnSuccessListener(waitingDoc -> {
                                                if (waitingDoc.exists()) {
                                                    List<String> entries = (List<String>) waitingDoc.get("entries");
                                                    if (entries != null) {
                                                        usersToNotify.addAll(entries);
                                                    }
                                                }

                                                // Get chosen entrants
                                                List<String> chosenList = (List<String>) eventDoc.get("chosen_entrants");
                                                if (chosenList != null) {
                                                    usersToNotify.addAll(chosenList);
                                                }

                                                // Get enrolled users
                                                List<String> enrolledList = (List<String>) eventDoc.get("enrolled_users");
                                                if (enrolledList != null) {
                                                    usersToNotify.addAll(enrolledList);
                                                }

                                                // Remove duplicates
                                                List<String> uniqueUsers = new ArrayList<>(new java.util.HashSet<>(usersToNotify));

                                                // Send notification
                                                if (!uniqueUsers.isEmpty()) {
                                                    String notifTitle = "Event Cancelled ❌";
                                                    String notifBody = "Unfortunately, \"" + finalEventTitle +
                                                            "\" has been cancelled.";

                                                    notificationHelper.notifyCustom(
                                                            eventId,
                                                            uniqueUsers,
                                                            finalEventTitle,
                                                            new NotificationHelper.NotificationCallback() {
                                                                @Override
                                                                public void onSuccess(String message) {
                                                                    Log.d("AdminProfileList", "Cancellation notifications sent");
                                                                }

                                                                @Override
                                                                public void onFailure(String error) {
                                                                    Log.e("AdminProfileList", "Failed to send notifications: " + error);
                                                                }
                                                            },
                                                            notifTitle,
                                                            notifBody
                                                    );
                                                }
                                            });

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
                                            Toast.makeText(this, "User account disabled successfully. Event participants notified.", Toast.LENGTH_SHORT).show();
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
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
import com.example.connect.utils.NotificationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrganizerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etSearch;
    private View searchLayout;
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


        searchLayout = findViewById(R.id.search_layout);

        etSearch = findViewById(R.id.search_input);

        if (searchLayout != null) {
            searchLayout.setVisibility(View.VISIBLE);
        }
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

                        // Only add non-disabled accounts
                        Boolean disabled = document.getBoolean("disabled");
                        if (disabled == null || !disabled) {
                            currentOrganizers.add(user);
                        }
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
                    tvEmptyState.setText("Failed to load organizers.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
    }

    private void deleteOrganizer(User user) {
        if (user.getUserId() == null) return;

        String userId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        NotificationHelper notificationHelper = new NotificationHelper();

        // 1. Find all events created by this organizer
        db.collection("events")
                .whereEqualTo("organizer_id", userId)
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    List<com.google.android.gms.tasks.Task<Void>> tasks = new ArrayList<>();

                    // Notify users for each event before deletion
                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                        String eventId = eventDoc.getId();
                        String eventTitle = eventDoc.getString("event_title");
                        if (eventTitle == null) eventTitle = "An event";

                        // --- Notification Logic (Same as before) ---
                        List<String> usersToNotify = new ArrayList<>();
                        String finalEventTitle = eventTitle;

                        // We fetch this purely for notification purposes
                        db.collection("waiting_lists").document(eventId).get()
                                .addOnSuccessListener(waitingDoc -> {
                                    if (waitingDoc.exists()) {
                                        List<String> entries = (List<String>) waitingDoc.get("entries");
                                        if (entries != null) usersToNotify.addAll(entries);
                                    }
                                    List<String> chosenList = (List<String>) eventDoc.get("chosen_entrants");
                                    if (chosenList != null) usersToNotify.addAll(chosenList);
                                    List<String> enrolledList = (List<String>) eventDoc.get("enrolled_users");
                                    if (enrolledList != null) usersToNotify.addAll(enrolledList);

                                    List<String> uniqueUsers = new ArrayList<>(new java.util.HashSet<>(usersToNotify));

                                    if (!uniqueUsers.isEmpty()) {
                                        String notifTitle = "Event Cancelled ❌";
                                        String notifBody = "Unfortunately, \"" + finalEventTitle + "\" has been cancelled by the organizer.";

                                        notificationHelper.notifyCustom(
                                                eventId, uniqueUsers, finalEventTitle,
                                                new NotificationHelper.NotificationCallback() {
                                                    @Override public void onSuccess(String message) { Log.d("AdminOrgList", "Notif sent"); }
                                                    @Override public void onFailure(String error) { Log.e("AdminOrgList", "Notif failed"); }
                                                },
                                                notifTitle, notifBody, "event_Cancelled"
                                        );
                                    }
                                });

                        // --- Deletion Logic ---

                        // 1. Task to delete the Event document
                        tasks.add(db.collection("events").document(eventId).delete());

                        // 2. Task to delete Waiting List AND Entrants sub-collection
                        DocumentReference wlRef = db.collection("waiting_lists").document(eventId);

                        // We create a chained task: Fetch Entrants -> Batch Delete -> Return Task
                        com.google.android.gms.tasks.Task<Void> deleteWaitlistTask = wlRef.collection("entrants").get()
                                .continueWithTask(task -> {
                                    WriteBatch batch = db.batch();

                                    // If fetch succeeded, add entrant docs to delete batch
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        for (DocumentSnapshot entrantDoc : task.getResult()) {
                                            batch.delete(entrantDoc.getReference());
                                        }
                                    }

                                    // Always add the parent waiting list document to the batch
                                    batch.delete(wlRef);

                                    // Commit the batch (returns a Task<Void>)
                                    return batch.commit();
                                });

                        // Add this complex task to the list
                        tasks.add(deleteWaitlistTask);
                    }
                    // --- Part B: Remove User from OTHER Waiting Lists (Collection Group) ---
                    // We add this as a separate task to the list
                    com.google.android.gms.tasks.Task<Void> removeUserFromWaitlistsTask = db.collectionGroup("entrants")
                            .whereEqualTo("user_id", userId) // IMPORTANT: Must match your Firestore Index field name (snake_case vs camelCase)
                            .get()
                            .continueWithTask(task -> {
                                if (!task.isSuccessful() || task.getResult() == null) {
                                    // If query fails, we return a successful null task so we don't block the whole process
                                    Log.e("AdminOrgList", "Failed to query entrants group", task.getException());
                                    return com.google.android.gms.tasks.Tasks.forResult(null);
                                }

                                WriteBatch batch = db.batch();
                                // Add every instance of this user in any waitlist to the delete batch
                                for (DocumentSnapshot doc : task.getResult()) {
                                    batch.delete(doc.getReference());
                                }
                                return batch.commit();
                            });

                    tasks.add(removeUserFromWaitlistsTask);

                    // Wait for all event and waiting list deletions (including subcollections) to complete
                    com.google.android.gms.tasks.Tasks.whenAll(tasks)
                            .addOnSuccessListener(aVoid -> {
                                // 2. All events/waiting lists deleted. Now disable the user account
                                db.collection("accounts").document(userId)
                                        .update("disabled", true)
                                        .addOnSuccessListener(aVoid2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Organizer account disabled and events removed.", Toast.LENGTH_SHORT).show();
                                            loadOrganizers(); // Refresh list
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this, "Error disabling account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error removing events.", Toast.LENGTH_SHORT).show();
                                Log.e("AdminOrgList", "Error deleting events/waitlists", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error finding events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
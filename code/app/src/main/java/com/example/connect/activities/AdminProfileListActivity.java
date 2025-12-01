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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for administrators to view and manage user profiles in the system.
 *
 * <p>This activity displays a searchable list of all user accounts (excluding admins) with the ability to:
 * <ul>
 *   <li>View all active user profiles (non-disabled, non-admin accounts)</li>
 *   <li>Search users by name, email, or user ID in real-time</li>
 *   <li>View detailed profile information for a specific user</li>
 *   <li>Disable user accounts (which cascades to handle their events and waitlist entries)</li>
 * </ul>
 *
 * <p><b>Critical Deletion Behavior:</b> When a user profile is deleted, the system:
 * <ol>
 *   <li>Marks the user account as disabled (soft delete)</li>
 *   <li>If the user is an organizer: notifies all participants and deletes their events</li>
 *   <li>Removes associated waiting lists and entrant subcollections for their events</li>
 *   <li>Removes the user from any waiting lists they joined as an entrant (using collectionGroup query)</li>
 * </ol>
 *
 * @author Vansh Taneja, Aakansh Chatterjee
 * @version 2.0
 */

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

    /**
     * Opens the profile detail view for a specific user.
     *
     * @param user The user whose profile should be viewed
     */
    private void openProfileDetails(User user) {
        if (user.getUserId() == null)
            return;

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id_admin_view", user.getUserId());
        intent.putExtra("IS_ADMIN_VIEW", true);
        startActivity(intent);
    }

    /**
     * Sets up the search functionality with a text watcher for real-time filtering.
     * Triggers filtering on every text change in the search input.
     */
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

    /**
     * Filters the profile list based on a search query.
     * Searches through user names, user IDs, and email addresses (case-insensitive).
     * Updates the RecyclerView and empty state message based on results.
     *
     * @param searchText The search query to filter by
     */
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

    /**
     * Loads all active user profiles from Firestore.
     * Excludes admin accounts and disabled accounts from the list.
     * Shows a progress indicator while loading and applies the current search filter
     * to the results.
     */
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

    /**
     * Disables a user account and removes all associated data.
     *
     * <p>This method performs a comprehensive cleanup process:
     * <ol>
     *   <li>Marks the user account as disabled (soft delete)</li>
     *   <li>If the user organized any events:
     *       <ul>
     *         <li>Sends cancellation notifications to all participants</li>
     *         <li>Deletes all event documents</li>
     *         <li>Removes waiting lists and entrant subcollections</li>
     *       </ul>
     *   </li>
     *   <li>Removes the user from any waiting lists they joined as an entrant
     *       (uses collectionGroup query on "entrants")</li>
     * </ol>
     *
     * <p><b>Important:</b> The collectionGroup query requires a Firestore composite index
     * on the entrants collection with the user_id field. If the index is missing, Firestore
     * will log an error with a link to create it.
     *
     * @param user The user profile to delete
     */
    private void deleteProfile(User user) {
        if (user.getUserId() == null)
            return;

        String userId = user.getUserId();
        progressBar.setVisibility(View.VISIBLE);

        NotificationHelper notificationHelper = new NotificationHelper();

        // Step 1: Mark user as disabled in Firestore
        db.collection("accounts").document(userId)
                .update("disabled", true)
                .addOnSuccessListener(aVoidDisabled -> {
                    Log.d("AdminProfileList", "User marked as disabled");

                    // Step 2: Delete events organized by this user and notify participants
                    db.collection("events")
                            .whereEqualTo("organizer_id", userId)
                            .get()
                            .addOnSuccessListener(eventSnapshots -> {

                                // 2a. Handle events organized by this user (Notifications + Deletion)
                                for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                                    String eventId = eventDoc.getId();
                                    String eventTitle = eventDoc.getString("event_title");
                                    if (eventTitle == null) eventTitle = "An event";

                                    // --- Notification Logic Starts ---
                                    List<String> usersToNotify = new ArrayList<>();
                                    String finalEventTitle = eventTitle;

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
                                                    String notifBody = "Unfortunately, \"" + finalEventTitle + "\" has been cancelled.";
                                                    notificationHelper.notifyCustom(eventId, uniqueUsers, finalEventTitle, new NotificationHelper.NotificationCallback() {
                                                        @Override
                                                        public void onSuccess(String message) { Log.d("AdminProfileList", "Notif sent"); }
                                                        @Override
                                                        public void onFailure(String error) { Log.e("AdminProfileList", "Notif failed: " + error); }
                                                    }, notifTitle, notifBody, "event_Cancelled");
                                                }
                                            });
                                    // --- Notification Logic Ends ---

                                    // Delete the event document
                                    db.collection("events").document(eventId).delete();

                                    // Delete the waiting list and its subcollection for this event
                                    DocumentReference waitListRef = db.collection("waiting_lists").document(eventId);
                                    waitListRef.collection("entrants").get()
                                            .addOnSuccessListener(entrantSnapshots -> {
                                                WriteBatch subBatch = db.batch();
                                                for (DocumentSnapshot entrantDoc : entrantSnapshots) {
                                                    subBatch.delete(entrantDoc.getReference());
                                                }
                                                subBatch.delete(waitListRef); // Delete parent waiting list doc
                                                subBatch.commit();
                                            });
                                }

                                // Step 3: Remove this user from ANY waiting list they have joined
                                // STRATEGY: Collection Group Query on "entrants"
                                db.collectionGroup("entrants")
                                        .whereEqualTo("user_id", userId) //
                                        .get()
                                        .addOnSuccessListener(entrantGroupSnapshots -> {

                                            WriteBatch batch = db.batch();

                                            // Add every entrant doc found to the delete batch
                                            for (DocumentSnapshot doc : entrantGroupSnapshots) {
                                                batch.delete(doc.getReference());

                                            }

                                            // Commit the batch delete
                                            batch.commit()
                                                    .addOnSuccessListener(aVoidBatch -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(this, "User disabled and removed from all waiting lists.", Toast.LENGTH_SHORT).show();
                                                        loadProfiles(); // Refresh list
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(this, "User disabled, but error removing from lists.", Toast.LENGTH_SHORT).show();
                                                        Log.e("AdminProfileList", "Batch delete failed", e);
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            // IMPORTANT: If this fails, check Logcat for a link to create the Index!
                                            Log.e("AdminProfileList", "Error querying entrants collection group. MISSING INDEX?", e);
                                            Toast.makeText(this, "Error accessing entrant lists (Check Logs)", Toast.LENGTH_SHORT).show();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e("AdminProfileList", "Error finding organized events", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error disabling user", Toast.LENGTH_SHORT).show();
                });
    }

}
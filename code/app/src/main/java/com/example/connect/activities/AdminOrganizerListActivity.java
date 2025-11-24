package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminOrganizerAdapter;
import com.example.connect.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity for admin to browse and remove organizers.
 * Displays a list of all users who have created events (organizers).
 * Supports searching/filtering organizers by name, email, or phone.
 * Allows deletion of organizers which will cascade delete all their events,
 * waiting lists, and related data from both Firestore and Firebase Authentication.
 *
 * Implements US 03.07.01: Admin removes organizers
 *
 * @author Zenith Team
 * @version 1.0
 */
public class AdminOrganizerListActivity extends AppCompatActivity {

    /** RecyclerView to display the list of organizers */
    private RecyclerView recyclerViewOrganizers;
    
    /** Search input field for filtering organizers */
    private TextInputEditText etSearch;
    
    /** Adapter for managing organizer items in the RecyclerView */
    private AdminOrganizerAdapter organizerAdapter;
    
    /** Complete list of all organizers loaded from Firestore */
    private List<User> allOrganizers;
    
    /** Filtered list of organizers based on search query */
    private List<User> filteredOrganizers;
    
    /** Firebase Firestore database instance */
    private FirebaseFirestore db;
    
    /** Firebase Functions instance for calling cloud functions */
    private FirebaseFunctions functions;

    /**
     * Called when the activity is first created.
     * Initializes the activity, sets up the layout, initializes Firebase Firestore,
     * and sets up all UI components and event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_organizer_list);

        db = FirebaseFirestore.getInstance();
        functions = FirebaseFunctions.getInstance();
        allOrganizers = new ArrayList<>();
        filteredOrganizers = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadOrganizers();
    }

    /**
     * Initializes all UI components from the layout.
     * Sets up the RecyclerView, search field, and toolbar with back navigation.
     */
    private void initViews() {
        recyclerViewOrganizers = findViewById(R.id.recyclerViewOrganizers);
        etSearch = findViewById(R.id.etSearchOrganizers);

        // Setup toolbar back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    /**
     * Sets up the RecyclerView with the organizer adapter.
     * Configures the adapter with click listeners for viewing and deleting organizers.
     * Uses LinearLayoutManager for vertical scrolling and optimizes for performance.
     */
    private void setupRecyclerView() {
        organizerAdapter = new AdminOrganizerAdapter(filteredOrganizers, new AdminOrganizerAdapter.OnOrganizerClickListener() {
            @Override
            public void onOrganizerClick(User organizer) {
                // Handle organizer click - could navigate to detailed view
                Toast.makeText(AdminOrganizerListActivity.this, "Organizer: " + organizer.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to organizer detail view if needed
            }

            @Override
            public void onOrganizerDelete(User organizer) {
                // Show confirmation dialog before deleting
                confirmDeleteOrganizer(organizer);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewOrganizers.setLayoutManager(layoutManager);
        recyclerViewOrganizers.setAdapter(organizerAdapter);

        // Optimize RecyclerView for smooth scrolling
        recyclerViewOrganizers.setHasFixedSize(true);
        recyclerViewOrganizers.setItemAnimator(null); // Disable animations for smoother scrolling
    }

    /**
     * Sets up the search functionality to filter organizers in real-time.
     * Listens for text changes in the search field and filters the organizer list accordingly.
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrganizers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all organizers from Firestore.
     * Finds all users who have created events by querying the events collection
     * for unique organizer IDs (from both org_name and organizer_id fields).
     * Then fetches their user details from the accounts collection.
     * Displays an error message if the loading fails.
     */
    private void loadOrganizers() {
        Log.d("AdminOrganizerList", "Loading organizers from Firestore...");
        
        // First, get all unique organizer IDs from events collection
        Set<String> organizerIds = new HashSet<>();
        
        // Query events by org_name field
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String orgName = document.getString("org_name");
                        String organizerId = document.getString("organizer_id");
                        
                        if (orgName != null && !orgName.isEmpty()) {
                            organizerIds.add(orgName);
                        }
                        if (organizerId != null && !organizerId.isEmpty()) {
                            organizerIds.add(organizerId);
                        }
                    }
                    
                    Log.d("AdminOrganizerList", "Found " + organizerIds.size() + " unique organizer IDs");
                    
                    if (organizerIds.isEmpty()) {
                        Toast.makeText(this, "No organizers found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Now fetch user details for each organizer ID
                    fetchOrganizerDetails(new ArrayList<>(organizerIds));
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error loading events to find organizers", e);
                    Toast.makeText(this, "Error loading organizers: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches user details for organizer IDs from the accounts collection.
     * Queries Firestore for each organizer ID, extracts user information,
     * and creates User objects. Skips admin accounts. Updates the UI when all
     * fetches are complete.
     *
     * @param organizerIds List of organizer user IDs to fetch from Firestore
     */
    private void fetchOrganizerDetails(List<String> organizerIds) {
        allOrganizers.clear();
        final int[] completedFetches = {0};
        final int totalFetches = organizerIds.size();
        
        if (totalFetches == 0) {
            updateUI();
            return;
        }
        
        for (String organizerId : organizerIds) {
            db.collection("accounts").document(organizerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Skip admin accounts
                            Boolean isAdmin = documentSnapshot.getBoolean("admin");
                            if (isAdmin == null || !isAdmin) {
                                String name = documentSnapshot.getString("full_name");
                                String email = documentSnapshot.getString("email");
                                String phone = documentSnapshot.getString("mobile_num");
                                String profileImageUrl = documentSnapshot.getString("profile_image_url");
                                
                                User organizer = new User(organizerId,
                                        name != null ? name : "Unknown",
                                        email != null ? email : "",
                                        phone != null ? phone : "",
                                        profileImageUrl);
                                
                                allOrganizers.add(organizer);
                            }
                        }
                        
                        completedFetches[0]++;
                        if (completedFetches[0] >= totalFetches) {
                            updateUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminOrganizerList", "Error fetching organizer: " + organizerId, e);
                        completedFetches[0]++;
                        if (completedFetches[0] >= totalFetches) {
                            updateUI();
                        }
                    });
        }
    }

    /**
     * Updates the UI with loaded organizers.
     * Clears the filtered list, adds all loaded organizers, and notifies the adapter
     * to refresh the RecyclerView display. Shows a message if no organizers are found.
     */
    private void updateUI() {
        filteredOrganizers.clear();
        filteredOrganizers.addAll(allOrganizers);
        organizerAdapter.notifyDataSetChanged();
        
        Log.d("AdminOrganizerList", "Loaded " + allOrganizers.size() + " organizers");
        
        if (allOrganizers.isEmpty()) {
            Toast.makeText(this, "No organizers found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Filters organizers based on search query.
     * Searches in organizer name, email, and phone number fields (case-insensitive).
     * If query is empty, shows all organizers. Updates the RecyclerView adapter
     * after filtering.
     *
     * @param query The search query string to filter organizers by
     */
    private void filterOrganizers(String query) {
        filteredOrganizers.clear();

        if (query.isEmpty()) {
            filteredOrganizers.addAll(allOrganizers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User organizer : allOrganizers) {
                boolean matchesName = organizer.getName() != null &&
                        organizer.getName().toLowerCase().contains(lowerQuery);
                boolean matchesEmail = organizer.getEmail() != null &&
                        organizer.getEmail().toLowerCase().contains(lowerQuery);
                boolean matchesPhone = organizer.getPhone() != null &&
                        organizer.getPhone().contains(query);

                if (matchesName || matchesEmail || matchesPhone) {
                    filteredOrganizers.add(organizer);
                }
            }
        }

        organizerAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a confirmation dialog before deleting an organizer.
     * Displays the organizer name and warns about permanent deletion of all related data
     * including all events created by the organizer.
     * User can confirm or cancel the deletion.
     *
     * @param organizer The organizer to delete
     */
    private void confirmDeleteOrganizer(User organizer) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Organizer")
                .setMessage("Are you sure you want to delete " + organizer.getName() + "'s organizer account? " +
                        "This will permanently remove the organizer, all their events, participants, " +
                        "waiting lists, and other related information from Firestore and Firebase Authentication. " +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes an organizer and all related data from Firestore and Firebase Authentication.
     * Performs cascade deletion of:
     * - Organizer account document from accounts collection
     * - All events created by the organizer
     * - All waiting lists and subcollections related to those events
     * - User from waiting lists and event waiting lists
     * - User from Firebase Authentication
     * 
     * Implements US 03.07.01: Admin removes organizers
     * 
     * Updates the UI after successful deletion and shows appropriate error messages
     * if deletion fails.
     *
     * @param organizer The organizer to delete
     */
    private void deleteOrganizer(User organizer) {
        String userIdToDelete = organizer.getUserId();
        Log.d("AdminOrganizerList", "Delete button clicked for organizer: " + organizer.getName());
        Log.d("AdminOrganizerList", "Organizer userId: " + userIdToDelete);

        if (userIdToDelete == null || userIdToDelete.isEmpty()) {
            Log.e("AdminOrganizerList", "❌ Invalid user ID - cannot delete");
            Toast.makeText(this, "Error: Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AdminOrganizerList", "✅ Starting deletion for userId: " + userIdToDelete);
        Toast.makeText(this, "Deleting organizer...", Toast.LENGTH_SHORT).show();

        // Delete account first for immediate feedback, then do cascade cleanup
        deleteAccountAndAuth(userIdToDelete, organizer);

        // Perform cascade deletion in background (doesn't block UI update)
        cascadeDeleteOrganizerData(userIdToDelete, () -> {
            Log.d("AdminOrganizerList", "✅ Background cascade deletion completed");
        });
    }

    /**
     * Performs cascade deletion of all organizer-related data.
     * Deletes all events created by the organizer and all related data.
     *
     * @param organizerId The organizer's user ID to delete
     * @param onComplete Callback when cascade deletion completes
     */
    private void cascadeDeleteOrganizerData(String organizerId, Runnable onComplete) {
        Log.d("AdminOrganizerList", "Starting cascade deletion for organizer: " + organizerId);

        // Delete all events created by this organizer
        deleteOrganizerEvents(organizerId, () -> {
            // Remove user from waiting lists
            removeUserFromWaitingLists(organizerId, () -> {
                // Remove user from event waiting lists
                removeUserFromEventWaitingLists(organizerId, () -> {
                    // Remove from other collections
                    removeUserFromOtherCollections(organizerId, onComplete);
                });
            });
        });
    }

    /**
     * Deletes all events created by an organizer and all related data.
     * This includes event documents, waiting lists, participants, etc.
     *
     * @param organizerId The organizer's user ID
     * @param onComplete Callback when deletion completes
     */
    private void deleteOrganizerEvents(String organizerId, Runnable onComplete) {
        Log.d("AdminOrganizerList", "Deleting all events for organizer: " + organizerId);

        // Find all events where org_name OR organizer_id matches the organizer ID
        final int[] completedQueries = {0};
        final List<QueryDocumentSnapshot> allEventsToDelete = new ArrayList<>();
        final int totalQueries = 2;

        Runnable checkQueriesComplete = () -> {
            completedQueries[0]++;
            if (completedQueries[0] >= totalQueries) {
                // Now delete all found events
                deleteFoundEvents(allEventsToDelete, organizerId, onComplete);
            }
        };

        // Query by org_name
        db.collection("events")
                .whereEqualTo("org_name", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allEventsToDelete.add(doc);
                    }
                    checkQueriesComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error querying events by org_name", e);
                    checkQueriesComplete.run();
                });

        // Query by organizer_id
        db.collection("events")
                .whereEqualTo("organizer_id", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Add only if not already in list (avoid duplicates)
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docId = doc.getId();
                        boolean alreadyExists = false;
                        for (QueryDocumentSnapshot existing : allEventsToDelete) {
                            if (existing.getId().equals(docId)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (!alreadyExists) {
                            allEventsToDelete.add(doc);
                        }
                    }
                    checkQueriesComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error querying events by organizer_id", e);
                    checkQueriesComplete.run();
                });
    }

    /**
     * Deletes all events found for an organizer.
     *
     * @param eventsToDelete List of event documents to delete
     * @param organizerId The organizer's user ID
     * @param onComplete Callback when deletion completes
     */
    private void deleteFoundEvents(List<QueryDocumentSnapshot> eventsToDelete, String organizerId, Runnable onComplete) {
        int totalEvents = eventsToDelete.size();

        if (totalEvents == 0) {
            Log.d("AdminOrganizerList", "No events found for organizer");
            onComplete.run();
            return;
        }

        Log.d("AdminOrganizerList", "Found " + totalEvents + " events to delete for organizer");

        final int[] deleteCount = {0};

        for (QueryDocumentSnapshot eventDoc : eventsToDelete) {
            String eventId = eventDoc.getId();

            // Delete waiting_lists document for this event if it exists
            db.collection("waiting_lists").document(eventId).delete()
                    .addOnCompleteListener(task -> {
                        // Continue regardless of success
                    });

            // Delete event subcollections first (waitingList, participants, etc.)
            deleteEventSubcollections(eventId, () -> {
                // Then delete the event document itself
                eventDoc.getReference()
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("AdminOrganizerList", "Deleted event: " + eventId);
                            deleteCount[0]++;
                            if (deleteCount[0] >= totalEvents) {
                                Log.d("AdminOrganizerList", "All organizer events deleted");
                                onComplete.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AdminOrganizerList", "Error deleting event: " + eventId, e);
                            deleteCount[0]++;
                            if (deleteCount[0] >= totalEvents) {
                                onComplete.run();
                            }
                        });
            });
        }
    }

    /**
     * Deletes all subcollections of an event (waitingList, participants, etc.).
     *
     * @param eventId The event ID
     * @param onComplete Callback when deletion completes
     */
    private void deleteEventSubcollections(String eventId, Runnable onComplete) {
        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] deleteCount = {0};
                    final int totalDocs = queryDocumentSnapshots.size();

                    if (totalDocs == 0) {
                        onComplete.run();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference()
                                .delete()
                                .addOnCompleteListener(task -> {
                                    deleteCount[0]++;
                                    if (deleteCount[0] >= totalDocs) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error deleting event subcollections", e);
                    onComplete.run();
                });
    }

    /**
     * Removes user ID from all waiting_lists collection documents.
     *
     * @param userIdToDelete The user ID to remove
     * @param onComplete Callback when operation completes
     */
    private void removeUserFromWaitingLists(String userIdToDelete, Runnable onComplete) {
        db.collection("waiting_lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] updateCount = {0};
                    final int totalDocs = queryDocumentSnapshots.size();

                    if (totalDocs == 0) {
                        onComplete.run();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> entries = (List<String>) document.get("entries");
                        if (entries != null && entries.contains(userIdToDelete)) {
                            document.getReference()
                                    .update("entries", FieldValue.arrayRemove(userIdToDelete))
                                    .addOnCompleteListener(task -> {
                                        updateCount[0]++;
                                        if (updateCount[0] >= totalDocs) {
                                            onComplete.run();
                                        }
                                    });
                        } else {
                            updateCount[0]++;
                            if (updateCount[0] >= totalDocs) {
                                onComplete.run();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error querying waiting_lists", e);
                    onComplete.run();
                });
    }

    /**
     * Removes user from events/{eventId}/waitingList subcollections.
     *
     * @param userIdToDelete The user ID to remove
     * @param onComplete Callback when operation completes
     */
    private void removeUserFromEventWaitingLists(String userIdToDelete, Runnable onComplete) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] deleteCount = {0};
                    final int totalEvents = queryDocumentSnapshots.size();

                    if (totalEvents == 0) {
                        onComplete.run();
                        return;
                    }

                    for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        String eventId = eventDoc.getId();
                        eventDoc.getReference()
                                .collection("waitingList")
                                .document(userIdToDelete)
                                .delete()
                                .addOnCompleteListener(task -> {
                                    deleteCount[0]++;
                                    if (deleteCount[0] >= totalEvents) {
                                        onComplete.run();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error querying events", e);
                    onComplete.run();
                });
    }

    /**
     * Removes user from other collections (messages, notifications, etc.).
     *
     * @param userIdToDelete The user ID to remove
     * @param onComplete Callback when operation completes
     */
    private void removeUserFromOtherCollections(String userIdToDelete, Runnable onComplete) {
        // TODO: Add cascade deletion for:
        // - Messages sent by the user
        // - Notifications related to the user
        // - Any other user-related data

        Log.d("AdminOrganizerList", "Other collections cleanup completed");
        onComplete.run();
    }

    /**
     * Deletes the account document and Firebase Auth account.
     *
     * @param userIdToDelete The user ID to delete
     * @param organizer The organizer profile (for display purposes)
     */
    private void deleteAccountAndAuth(String userIdToDelete, User organizer) {
        Log.d("AdminOrganizerList", "Attempting to delete account document: " + userIdToDelete);

        db.collection("accounts").document(userIdToDelete)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w("AdminOrganizerList", "Account document does not exist: " + userIdToDelete);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
                            removeFromLocalLists(userIdToDelete);
                        });
                        return;
                    }

                    Log.d("AdminOrganizerList", "Account document exists, proceeding with deletion");

                    // Delete Firestore account document
                    db.collection("accounts").document(userIdToDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AdminOrganizerList", "✅ Account document deleted from Firestore");

                                // Verify deletion
                                db.collection("accounts").document(userIdToDelete)
                                        .get()
                                        .addOnSuccessListener(verifySnapshot -> {
                                            if (!verifySnapshot.exists()) {
                                                Log.d("AdminOrganizerList", "✅ Verified: Account document successfully deleted");
                                            } else {
                                                Log.w("AdminOrganizerList", "⚠️ Warning: Account document still exists after deletion");
                                            }

                                            // Delete from Firebase Auth
                                            deleteFirebaseAuthUser(userIdToDelete);

                                            // Remove from local lists and refresh UI
                                            runOnUiThread(() -> {
                                                removeFromLocalLists(userIdToDelete);
                                                Toast.makeText(this, "Organizer deleted successfully", Toast.LENGTH_SHORT).show();
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("AdminOrganizerList", "Error verifying deletion", e);
                                            // Still proceed with UI update
                                            deleteFirebaseAuthUser(userIdToDelete);
                                            runOnUiThread(() -> {
                                                removeFromLocalLists(userIdToDelete);
                                                Toast.makeText(this, "Organizer deleted (verification failed)", Toast.LENGTH_SHORT).show();
                                            });
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminOrganizerList", "❌ Error deleting account document: " + e.getMessage(), e);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error deleting organizer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrganizerList", "Error checking if account exists", e);
                    // Try to delete anyway
                    db.collection("accounts").document(userIdToDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                deleteFirebaseAuthUser(userIdToDelete);
                                runOnUiThread(() -> {
                                    removeFromLocalLists(userIdToDelete);
                                    Toast.makeText(this, "Organizer deleted", Toast.LENGTH_SHORT).show();
                                });
                            })
                            .addOnFailureListener(e2 -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            });
                });
    }

    /**
     * Removes organizer from local lists and updates UI.
     *
     * @param userIdToDelete The ID of the organizer to remove from local lists
     */
    private void removeFromLocalLists(String userIdToDelete) {
        int initialSize = allOrganizers.size();
        allOrganizers.removeIf(organizer -> organizer.getUserId() != null && organizer.getUserId().equals(userIdToDelete));
        filteredOrganizers.removeIf(organizer -> organizer.getUserId() != null && organizer.getUserId().equals(userIdToDelete));

        int removedCount = initialSize - allOrganizers.size();
        Log.d("AdminOrganizerList", "Removed " + removedCount + " organizer(s) from local lists");

        organizerAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes user from Firebase Authentication.
     * For admin deleting other users, this requires Admin SDK (server-side).
     * Attempts to call a Cloud Function if available, otherwise logs the requirement.
     *
     * @param userId The user ID to delete from Auth
     */
    private void deleteFirebaseAuthUser(String userId) {
        Log.d("AdminOrganizerList", "Attempting to delete user from Firebase Auth: " + userId);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Check if user is deleting themselves
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUid().equals(userId)) {
            // User is deleting themselves - can use client SDK
            auth.getCurrentUser().delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AdminOrganizerList", "✅ User deleted from Firebase Auth (self-delete)");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminOrganizerList", "Error deleting user from Auth (self-delete): " + e.getMessage(), e);
                    });
        } else {
            // Admin deleting another user - requires Admin SDK
            // Try to call Cloud Function if available
            callDeleteUserCloudFunction(userId);
        }
    }

    /**
     * Calls a Cloud Function to delete a user from Firebase Auth.
     * The Cloud Function must use Firebase Admin SDK to delete the user.
     *
     * IMPORTANT: You must deploy the Cloud Function first!
     * See CLOUD_FUNCTION_SETUP.md for deployment instructions.
     *
     * @param userId The user ID to delete
     */
    private void callDeleteUserCloudFunction(String userId) {
        Log.d("AdminOrganizerList", "🔵 Attempting to delete user via Cloud Function: " + userId);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("AdminOrganizerList", "❌ No authenticated user - cannot call Cloud Function");
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: Not authenticated", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        String currentUserUid = auth.getCurrentUser().getUid();
        Log.d("AdminOrganizerList", "Current admin user: " + currentUserUid);

        // Prepare data for Cloud Function
        Map<String, Object> data = new HashMap<>();
        data.put("uid", userId);

        Log.d("AdminOrganizerList", "Calling Cloud Function 'deleteUser' with data: " + data.toString());

        // Call the Cloud Function
        functions.getHttpsCallable("deleteUser")
                .call(data)
                .addOnSuccessListener(result -> {
                    Log.d("AdminOrganizerList", "✅✅✅ Cloud Function call SUCCESSFUL!");
                    Object resultData = result.getData();
                    if (resultData != null) {
                        Log.d("AdminOrganizerList", "Function result: " + resultData.toString());
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ User deleted from Authentication", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    String errorCode = "";
                    if (e instanceof com.google.firebase.functions.FirebaseFunctionsException) {
                        com.google.firebase.functions.FirebaseFunctionsException fe =
                                (com.google.firebase.functions.FirebaseFunctionsException) e;
                        errorCode = fe.getCode().toString();
                        Log.e("AdminOrganizerList", "❌ Cloud Function error code: " + errorCode);
                    }

                    Log.e("AdminOrganizerList", "❌❌❌ Error calling Cloud Function: " + errorMessage, e);
                    Log.e("AdminOrganizerList", "Full exception: " + e.getClass().getName());

                    // Check for specific error types
                    if (errorMessage != null) {
                        if (errorMessage.contains("NOT_FOUND") ||
                                errorMessage.contains("not found") ||
                                errorCode.contains("NOT_FOUND")) {
                            Log.w("AdminOrganizerList", "⚠️⚠️⚠️ Cloud Function 'deleteUser' NOT DEPLOYED!");
                            Log.w("AdminOrganizerList", "⚠️ Please deploy the Cloud Function first.");
                            Log.w("AdminOrganizerList", "⚠️ See CLOUD_FUNCTION_SETUP.md for instructions.");
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "⚠️ Cloud Function not deployed!\nOrganizer deleted from Firestore only.\nDeploy function to delete from Auth.",
                                        Toast.LENGTH_LONG).show();
                            });
                        } else if (errorMessage.contains("permission-denied") ||
                                errorCode.contains("PERMISSION_DENIED")) {
                            Log.e("AdminOrganizerList", "❌ Permission denied - check if user is admin");
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "Permission denied. Check admin status.",
                                        Toast.LENGTH_LONG).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "Error deleting from Auth:\n" + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "Error calling Cloud Function. Check logs.",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
}


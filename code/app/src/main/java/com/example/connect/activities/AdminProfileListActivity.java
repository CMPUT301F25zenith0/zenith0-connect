package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminProfileAdapter;
import com.example.connect.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for admin to browse and view all user profiles.
 * Displays a list of all non-admin users with their basic information.
 * Supports searching/filtering profiles by name, email, or phone.
 * 
 * Implements US 03.05.01: Admin browses profiles
 * 
 * @author Zenith Team
 * @version 1.0
 */
public class AdminProfileListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProfiles;
    private TextInputEditText etSearch;
    private AdminProfileAdapter profileAdapter;
    private List<User> allProfiles;
    private List<User> filteredProfiles;
    private FirebaseFirestore db;
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_list);

        db = FirebaseFirestore.getInstance();
        functions = FirebaseFunctions.getInstance();
        allProfiles = new ArrayList<>();
        filteredProfiles = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupSearch();
        loadProfiles();
    }

    /**
     * Initializes all UI components from the layout.
     */
    private void initViews() {
        recyclerViewProfiles = findViewById(R.id.recyclerViewProfiles);
        etSearch = findViewById(R.id.etSearchProfiles);
        
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
     * Sets up the RecyclerView with the profile adapter.
     */
    private void setupRecyclerView() {
        profileAdapter = new AdminProfileAdapter(filteredProfiles, new AdminProfileAdapter.OnProfileClickListener() {
            @Override
            public void onProfileClick(User profile) {
                // Handle profile click - could navigate to detailed view
                Toast.makeText(AdminProfileListActivity.this, "Profile: " + profile.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Navigate to AdminProfileDetailActivity if needed
            }

            @Override
            public void onProfileDelete(User profile) {
                // Show confirmation dialog before deleting
                confirmDeleteProfile(profile);
            }
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewProfiles.setLayoutManager(layoutManager);
        recyclerViewProfiles.setAdapter(profileAdapter);
        
        // Optimize RecyclerView for smooth scrolling
        recyclerViewProfiles.setHasFixedSize(true);
        recyclerViewProfiles.setItemAnimator(null); // Disable animations for smoother scrolling
    }

    /**
     * Sets up the search functionality to filter profiles.
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProfiles(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Loads all user profiles from Firestore (excluding admin accounts).
     */
    private void loadProfiles() {
        db.collection("accounts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProfiles.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip admin accounts
                        Boolean isAdmin = document.getBoolean("admin");
                        if (isAdmin != null && isAdmin) {
                            continue;
                        }
                        
                        // Extract user data
                        String userId = document.getId();
                        String name = document.getString("full_name");
                        String email = document.getString("email");
                        String phone = document.getString("mobile_num");
                        String profileImageUrl = document.getString("profile_image_url");
                        
                        // Create User object
                        User user = new User(userId, 
                                name != null ? name : "Unknown",
                                email != null ? email : "",
                                phone != null ? phone : "",
                                profileImageUrl);
                        
                        allProfiles.add(user);
                    }
                    
                    // Update filtered list and adapter
                    filteredProfiles.clear();
                    filteredProfiles.addAll(allProfiles);
                    profileAdapter.notifyDataSetChanged();
                    
                    Log.d("AdminProfileList", "Loaded " + allProfiles.size() + " profiles");
                    
                    if (allProfiles.isEmpty()) {
                        Toast.makeText(this, "No profiles found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfileList", "Error loading profiles", e);
                    Toast.makeText(this, "Error loading profiles: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Filters profiles based on search query.
     * Searches in name, email, and phone number.
     * 
     * @param query The search query string
     */
    private void filterProfiles(String query) {
        filteredProfiles.clear();
        
        if (query.isEmpty()) {
            filteredProfiles.addAll(allProfiles);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allProfiles) {
                boolean matchesName = user.getName() != null && 
                        user.getName().toLowerCase().contains(lowerQuery);
                boolean matchesEmail = user.getEmail() != null && 
                        user.getEmail().toLowerCase().contains(lowerQuery);
                boolean matchesPhone = user.getPhone() != null && 
                        user.getPhone().contains(query);
                
                if (matchesName || matchesEmail || matchesPhone) {
                    filteredProfiles.add(user);
                }
            }
        }
        
        profileAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a confirmation dialog before deleting a profile.
     * 
     * @param profile The user profile to delete
     */
    private void confirmDeleteProfile(User profile) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + profile.getName() + "'s profile? " +
                        "This will permanently remove all user data including events (if organizer), " +
                        "waiting lists, and other related information. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProfile(profile))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a user profile with cascade deletion of all related data.
     * Implements US 03.02.01: Admin removes profiles
     * 
     * @param profile The user profile to delete
     */
    private void deleteProfile(User profile) {
        String userIdToDelete = profile.getUserId();
        Log.d("AdminProfileList", "Delete button clicked for profile: " + profile.getName());
        Log.d("AdminProfileList", "Profile userId: " + userIdToDelete);
        
        if (userIdToDelete == null || userIdToDelete.isEmpty()) {
            Log.e("AdminProfileList", "❌ Invalid user ID - cannot delete");
            Toast.makeText(this, "Error: Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AdminProfileList", "✅ Starting deletion for userId: " + userIdToDelete);
        Toast.makeText(this, "Deleting profile...", Toast.LENGTH_SHORT).show();

        // Check if user is an organizer by checking if they have any events
        // Check both org_name and organizer_id fields
        db.collection("events")
                .whereEqualTo("org_name", userIdToDelete)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final boolean isOrganizerFromOrgName = !queryDocumentSnapshots.isEmpty();
                    
                    // Also check organizer_id field if org_name didn't match
                    if (!isOrganizerFromOrgName) {
                        db.collection("events")
                                .whereEqualTo("organizer_id", userIdToDelete)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    boolean isOrganizer = !queryDocumentSnapshots2.isEmpty();
                                    proceedWithDeletion(userIdToDelete, profile, isOrganizer);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("AdminProfileList", "Error checking organizer_id", e);
                                    proceedWithDeletion(userIdToDelete, profile, false);
                                });
                    } else {
                        proceedWithDeletion(userIdToDelete, profile, isOrganizerFromOrgName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfileList", "Error checking if organizer", e);
                    // Proceed with deletion anyway (assume not organizer)
                    proceedWithDeletion(userIdToDelete, profile, false);
                });
    }

    /**
     * Proceeds with cascade deletion after checking if user is organizer.
     * We delete the account first for immediate feedback, then do cascade cleanup.
     */
    private void proceedWithDeletion(String userIdToDelete, User profile, boolean isOrganizer) {
        Log.d("AdminProfileList", "Proceeding with deletion. UserId: " + userIdToDelete + ", IsOrganizer: " + isOrganizer);
        
        if (isOrganizer) {
            Log.d("AdminProfileList", "User is an organizer, will delete all their events");
        }
        
        // Delete account first for immediate feedback, then do cascade cleanup in background
        deleteAccountAndAuth(userIdToDelete, profile);
        
        // Perform cascade deletion in background (doesn't block UI update)
        cascadeDeleteUserData(userIdToDelete, isOrganizer, () -> {
            Log.d("AdminProfileList", "✅ Background cascade deletion completed");
        });
    }

    /**
     * Checks if a user is an organizer by checking if they have created any events.
     * This is done asynchronously during cascade deletion.
     * 
     * @param userId The user ID to check
     * @return true if user is an organizer, false otherwise
     */
    private boolean checkIfOrganizer(String userId) {
        // We'll check this during cascade deletion by querying events
        // For now return false, actual check happens in deleteOrganizerEvents
        return false;
    }

    /**
     * Performs cascade deletion of all user-related data.
     * If user is an organizer, also deletes all their events and related data.
     * 
     * @param userIdToDelete The user ID to delete
     * @param isOrganizer Whether the user is an organizer
     * @param onComplete Callback when cascade deletion completes
     */
    private void cascadeDeleteUserData(String userIdToDelete, boolean isOrganizer, Runnable onComplete) {
        Log.d("AdminProfileList", "Starting cascade deletion for userId: " + userIdToDelete);
        
        // Use a simpler approach: run all operations and use a timeout to ensure completion
        // Track completion of all operations
        final int[] completedOperations = {0};
        final int totalOperations = isOrganizer ? 4 : 3; // More operations if organizer
        
        Runnable checkCompletion = () -> {
            synchronized (completedOperations) {
                completedOperations[0]++;
                Log.d("AdminProfileList", "Operation completed. Progress: " + completedOperations[0] + "/" + totalOperations);
                if (completedOperations[0] >= totalOperations) {
                    Log.d("AdminProfileList", "✅ All cascade deletion operations completed");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        
        // 1. Remove user from waiting_lists collection
        removeUserFromWaitingLists(userIdToDelete, checkCompletion);
        
        // 2. Remove user from events/{eventId}/waitingList subcollections
        removeUserFromEventWaitingLists(userIdToDelete, checkCompletion);
        
        // 3. If organizer, delete all their events and related data
        if (isOrganizer) {
            deleteOrganizerEvents(userIdToDelete, checkCompletion);
        } else {
            checkCompletion.run(); // Skip if not organizer
        }
        
        // 4. Remove user from other collections (messages, notifications, etc.)
        removeUserFromOtherCollections(userIdToDelete, checkCompletion);
        
        // Safety timeout: ensure completion callback is called even if operations hang
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            synchronized (completedOperations) {
                if (completedOperations[0] < totalOperations) {
                    Log.w("AdminProfileList", "Cascade deletion timeout - proceeding with account deletion anyway");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        }, 10000); // 10 second timeout
    }

    /**
     * Removes user ID from all waiting_lists collection documents.
     */
    private void removeUserFromWaitingLists(String userIdToDelete, Runnable onComplete) {
        Log.d("AdminProfileList", "Removing user from waiting_lists: " + userIdToDelete);
        db.collection("waiting_lists")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] updateCount = {0};
                    final int totalDocs = queryDocumentSnapshots.size();
                    
                    if (totalDocs == 0) {
                        Log.d("AdminProfileList", "No waiting_lists documents found");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        return;
                    }
                    
                    Log.d("AdminProfileList", "Processing " + totalDocs + " waiting_lists documents");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> entries = (List<String>) document.get("entries");
                        if (entries != null && entries.contains(userIdToDelete)) {
                            document.getReference()
                                    .update("entries", FieldValue.arrayRemove(userIdToDelete))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AdminProfileList", "Removed user from waiting_list: " + document.getId());
                                        synchronized (updateCount) {
                                            updateCount[0]++;
                                            if (updateCount[0] >= totalDocs) {
                                                Log.d("AdminProfileList", "Completed waiting_lists cleanup");
                                                if (onComplete != null) {
                                                    onComplete.run();
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("AdminProfileList", "Error removing user from waiting_list: " + document.getId(), e);
                                        synchronized (updateCount) {
                                            updateCount[0]++;
                                            if (updateCount[0] >= totalDocs) {
                                                if (onComplete != null) {
                                                    onComplete.run();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            synchronized (updateCount) {
                                updateCount[0]++;
                                if (updateCount[0] >= totalDocs) {
                                    Log.d("AdminProfileList", "Completed waiting_lists cleanup (no matches)");
                                    if (onComplete != null) {
                                        onComplete.run();
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfileList", "Error querying waiting_lists", e);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    /**
     * Removes user from events/{eventId}/waitingList subcollections.
     */
    private void removeUserFromEventWaitingLists(String userIdToDelete, Runnable onComplete) {
        Log.d("AdminProfileList", "Removing user from event waitingList subcollections: " + userIdToDelete);
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] deleteCount = {0};
                    final int totalEvents = queryDocumentSnapshots.size();
                    
                    if (totalEvents == 0) {
                        Log.d("AdminProfileList", "No events found");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        return;
                    }
                    
                    Log.d("AdminProfileList", "Processing " + totalEvents + " events for waitingList cleanup");
                    
                    for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        String eventId = eventDoc.getId();
                        eventDoc.getReference()
                                .collection("waitingList")
                                .document(userIdToDelete)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("AdminProfileList", "Removed user from event waitingList: " + eventId);
                                    synchronized (deleteCount) {
                                        deleteCount[0]++;
                                        if (deleteCount[0] >= totalEvents) {
                                            Log.d("AdminProfileList", "Completed event waitingList cleanup");
                                            if (onComplete != null) {
                                                onComplete.run();
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Document might not exist, which is fine
                                    if (e.getMessage() == null || !e.getMessage().contains("NOT_FOUND")) {
                                        Log.e("AdminProfileList", "Error removing from event waitingList: " + eventId, e);
                                    }
                                    synchronized (deleteCount) {
                                        deleteCount[0]++;
                                        if (deleteCount[0] >= totalEvents) {
                                            if (onComplete != null) {
                                                onComplete.run();
                                            }
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfileList", "Error querying events", e);
                    if (onComplete != null) {
                        onComplete.run();
                    }
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
        Log.d("AdminProfileList", "Deleting all events for organizer: " + organizerId);
        
        // Find all events where org_name OR organizer_id matches the organizer ID
        // We need to query both fields separately as Firestore doesn't support OR queries easily
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
                    Log.e("AdminProfileList", "Error querying events by org_name", e);
                    checkQueriesComplete.run();
                });
        
        // Query by organizer_id
        db.collection("events")
                .whereEqualTo("organizer_id", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Add only if not already in list (avoid duplicates by checking event ID)
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
                    Log.e("AdminProfileList", "Error querying events by organizer_id", e);
                    checkQueriesComplete.run();
                });
    }

    /**
     * Deletes all events found for an organizer.
     */
    private void deleteFoundEvents(List<QueryDocumentSnapshot> eventsToDelete, String organizerId, Runnable onComplete) {
        int totalEvents = eventsToDelete.size();
        
        if (totalEvents == 0) {
            Log.d("AdminProfileList", "No events found for organizer");
            onComplete.run();
            return;
        }
        
        Log.d("AdminProfileList", "Found " + totalEvents + " events to delete for organizer");
        
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
                            Log.d("AdminProfileList", "Deleted event: " + eventId);
                            deleteCount[0]++;
                            if (deleteCount[0] >= totalEvents) {
                                Log.d("AdminProfileList", "All organizer events deleted");
                                onComplete.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AdminProfileList", "Error deleting event: " + eventId, e);
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
        // Delete waitingList subcollection
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
                    Log.e("AdminProfileList", "Error deleting event subcollections", e);
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
        
        Log.d("AdminProfileList", "Other collections cleanup completed");
        onComplete.run();
    }

    /**
     * Deletes the account document and Firebase Auth account.
     * 
     * @param userIdToDelete The user ID to delete
     * @param profile The user profile (for display purposes)
     */
    private void deleteAccountAndAuth(String userIdToDelete, User profile) {
        Log.d("AdminProfileList", "Attempting to delete account document: " + userIdToDelete);
        
        // First verify the document exists
        db.collection("accounts").document(userIdToDelete)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.w("AdminProfileList", "Account document does not exist: " + userIdToDelete);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
                            // Still remove from local lists
                            removeFromLocalLists(userIdToDelete);
                        });
                        return;
                    }
                    
                    Log.d("AdminProfileList", "Account document exists, proceeding with deletion");
                    
                    // Delete Firestore account document
                    db.collection("accounts").document(userIdToDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AdminProfileList", "✅ Account document deleted from Firestore");
                                
                                // Verify deletion
                                db.collection("accounts").document(userIdToDelete)
                                        .get()
                                        .addOnSuccessListener(verifySnapshot -> {
                                            if (!verifySnapshot.exists()) {
                                                Log.d("AdminProfileList", "✅ Verified: Account document successfully deleted");
                                            } else {
                                                Log.w("AdminProfileList", "⚠️ Warning: Account document still exists after deletion");
                                            }
                                            
                                            // Delete from Firebase Auth
                                            deleteFirebaseAuthUser(userIdToDelete);
                                            
                                            // Remove from local lists and refresh UI
                                            runOnUiThread(() -> {
                                                removeFromLocalLists(userIdToDelete);
                                                Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("AdminProfileList", "Error verifying deletion", e);
                                            // Still proceed with UI update
                                            deleteFirebaseAuthUser(userIdToDelete);
                                            runOnUiThread(() -> {
                                                removeFromLocalLists(userIdToDelete);
                                                Toast.makeText(this, "Profile deleted (verification failed)", Toast.LENGTH_SHORT).show();
                                            });
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminProfileList", "❌ Error deleting account document: " + e.getMessage(), e);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Error deleting profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfileList", "Error checking if account exists", e);
                    // Try to delete anyway
                    db.collection("accounts").document(userIdToDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                deleteFirebaseAuthUser(userIdToDelete);
                                runOnUiThread(() -> {
                                    removeFromLocalLists(userIdToDelete);
                                    Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
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
     * Removes profile from local lists and updates UI.
     */
    private void removeFromLocalLists(String userIdToDelete) {
        int initialSize = allProfiles.size();
        allProfiles.removeIf(user -> user.getUserId() != null && user.getUserId().equals(userIdToDelete));
        filteredProfiles.removeIf(user -> user.getUserId() != null && user.getUserId().equals(userIdToDelete));
        
        int removedCount = initialSize - allProfiles.size();
        Log.d("AdminProfileList", "Removed " + removedCount + " profile(s) from local lists");
        
        profileAdapter.notifyDataSetChanged();
    }
    
    /**
     * Deletes user from Firebase Authentication.
     * For admin deleting other users, this requires Admin SDK (server-side).
     * Attempts to call a Cloud Function if available, otherwise logs the requirement.
     * 
     * @param userId The user ID to delete from Auth
     */
    private void deleteFirebaseAuthUser(String userId) {
        Log.d("AdminProfileList", "Attempting to delete user from Firebase Auth: " + userId);
        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        // Check if user is deleting themselves
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUid().equals(userId)) {
            // User is deleting themselves - can use client SDK
            auth.getCurrentUser().delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AdminProfileList", "✅ User deleted from Firebase Auth (self-delete)");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminProfileList", "Error deleting user from Auth (self-delete): " + e.getMessage(), e);
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
        Log.d("AdminProfileList", "🔵 Attempting to delete user via Cloud Function: " + userId);
        
        // Get current user's ID token for authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("AdminProfileList", "❌ No authenticated user - cannot call Cloud Function");
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: Not authenticated", Toast.LENGTH_SHORT).show();
            });
            return;
        }
        
        String currentUserUid = auth.getCurrentUser().getUid();
        Log.d("AdminProfileList", "Current admin user: " + currentUserUid);
        
        // Prepare data for Cloud Function
        Map<String, Object> data = new HashMap<>();
        data.put("uid", userId);
        
        Log.d("AdminProfileList", "Calling Cloud Function 'deleteUser' with data: " + data.toString());
        
        // Call the Cloud Function
        functions.getHttpsCallable("deleteUser")
                .call(data)
                .addOnSuccessListener(result -> {
                    Log.d("AdminProfileList", "✅✅✅ Cloud Function call SUCCESSFUL!");
                    Object resultData = result.getData();
                    if (resultData != null) {
                        Log.d("AdminProfileList", "Function result: " + resultData.toString());
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
                        Log.e("AdminProfileList", "❌ Cloud Function error code: " + errorCode);
                    }
                    
                    Log.e("AdminProfileList", "❌❌❌ Error calling Cloud Function: " + errorMessage, e);
                    Log.e("AdminProfileList", "Full exception: " + e.getClass().getName());
                    
                    // Check for specific error types
                    if (errorMessage != null) {
                        if (errorMessage.contains("NOT_FOUND") || 
                            errorMessage.contains("not found") ||
                            errorCode.contains("NOT_FOUND")) {
                            Log.w("AdminProfileList", "⚠️⚠️⚠️ Cloud Function 'deleteUser' NOT DEPLOYED!");
                            Log.w("AdminProfileList", "⚠️ Please deploy the Cloud Function first.");
                            Log.w("AdminProfileList", "⚠️ See CLOUD_FUNCTION_SETUP.md for instructions.");
                            runOnUiThread(() -> {
                                Toast.makeText(this, 
                                    "⚠️ Cloud Function not deployed!\nUser deleted from Firestore only.\nDeploy function to delete from Auth.", 
                                    Toast.LENGTH_LONG).show();
                            });
                        } else if (errorMessage.contains("permission-denied") || 
                                   errorCode.contains("PERMISSION_DENIED")) {
                            Log.e("AdminProfileList", "❌ Permission denied - check if user is admin");
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


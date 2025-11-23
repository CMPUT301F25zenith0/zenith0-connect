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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_list);

        db = FirebaseFirestore.getInstance();
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
        profileAdapter = new AdminProfileAdapter(filteredProfiles, profile -> {
            // Handle profile click - could navigate to detailed view
            Toast.makeText(this, "Profile: " + profile.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to AdminProfileDetailActivity if needed
        });
        
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProfiles.setAdapter(profileAdapter);
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
}


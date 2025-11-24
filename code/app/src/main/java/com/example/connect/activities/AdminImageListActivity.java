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
import com.example.connect.adapters.AdminImageAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminImageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AdminImageAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            loadImages();
        } catch (Exception e) {
            Log.e("AdminImageList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Manage Images");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(this::deleteImage, this::openImageDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void openImageDetails(AdminImageAdapter.ImageItem image) {
        android.content.Intent intent = new android.content.Intent(this, ImageDetailsActivity.class);
        if (image.url != null && (image.url.startsWith("http") || image.url.startsWith("https"))) {
            intent.putExtra("image_url", image.url);
        } else {
            intent.putExtra("image_base64", image.url);
        }
        startActivity(intent);
    }

    private void loadImages() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        List<AdminImageAdapter.ImageItem> images = new ArrayList<>();

        // 1. Fetch Event Posters
        db.collection("events")
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    for (QueryDocumentSnapshot doc : eventSnapshots) {
                        String imageUrl = doc.getString("imageUrl");
                        String imageBase64 = doc.getString("image_base64");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            images.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    imageUrl,
                                    "Event Poster",
                                    doc.getId()));
                        } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                            images.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    imageBase64,
                                    "Event Poster",
                                    doc.getId()));
                        }
                    }

                    // 2. Fetch Profile Pictures from "accounts" collection
                    db.collection("accounts")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                for (QueryDocumentSnapshot doc : userSnapshots) {
                                    // Use correct field name based on User model annotation
                                    String profileUrl = doc.getString("profile_image_url");
                                    if (profileUrl != null && !profileUrl.isEmpty()) {
                                        images.add(new AdminImageAdapter.ImageItem(
                                                doc.getId(),
                                                profileUrl,
                                                "Profile Picture",
                                                doc.getId()));
                                    }
                                }

                                progressBar.setVisibility(View.GONE);
                                if (images.isEmpty()) {
                                    tvEmptyState.setVisibility(View.VISIBLE);
                                } else {
                                    adapter.setImages(images);
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e("AdminImageList", "Error loading user images", e);
                                Toast.makeText(this, "Error loading user images", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("AdminImageList", "Error loading event images", e);
                    Toast.makeText(this, "Error loading event images", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteImage(AdminImageAdapter.ImageItem image) {
        if (image.type.equals("Event Poster")) {
            // Delete both possible fields for events
            Map<String, Object> updates = new HashMap<>();
            updates.put("imageUrl", null);
            updates.put("image_base64", null);

            db.collection("events").document(image.id)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                        loadImages(); // Refresh
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error removing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Delete profile image
            db.collection("accounts").document(image.id)
                    .update("profile_image_url", null)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                        loadImages(); // Refresh
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error removing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

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
import java.util.List;

public class AdminImageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private AdminImageAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadImages();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Images");
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(this::deleteImage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
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
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            images.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    imageUrl,
                                    "Event Poster",
                                    doc.getId()));
                        }
                    }

                    // 2. Fetch Profile Pictures
                    db.collection("users")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                for (QueryDocumentSnapshot doc : userSnapshots) {
                                    String profileUrl = doc.getString("profileImageUrl");
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
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("AdminImageList", "Error loading event images", e);
                });
    }

    private void deleteImage(AdminImageAdapter.ImageItem image) {
        // Determine collection based on type
        String collection = image.type.equals("Event Poster") ? "events" : "users";
        String field = image.type.equals("Event Poster") ? "imageUrl" : "profileImageUrl";

        // Update document to remove image URL
        db.collection(collection).document(image.id)
                .update(field, null) // Or "" depending on your schema preference
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                    loadImages(); // Refresh
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error removing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

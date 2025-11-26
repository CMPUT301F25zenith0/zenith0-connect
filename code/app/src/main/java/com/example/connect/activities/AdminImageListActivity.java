package com.example.connect.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminImageAdapter;
import com.example.connect.testing.TestHooks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText searchInput;
    private View searchLayout;
    private final List<AdminImageAdapter.ImageItem> allImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();

            if (TestHooks.isUiTestMode()) {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                loadImages();
            }
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
        searchLayout = findViewById(R.id.search_layout);
        searchInput = findViewById(R.id.search_input);

        if (searchLayout != null) {
            searchLayout.setVisibility(View.VISIBLE);
        }

        if (searchInput != null) {
            searchInput.setHint("Search images");
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterImages(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
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
        intent.putExtra("image_title", image.displayName != null ? image.displayName : image.type);
        startActivity(intent);
    }

    private void loadImages() {
        if (TestHooks.isUiTestMode()) {
            return;
        }

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

                        String eventTitle = doc.getString("event_title");
                        if (eventTitle == null || eventTitle.isEmpty()) {
                            eventTitle = doc.getString("name");
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            images.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    imageUrl,
                                    "Event Poster",
                                    doc.getId(),
                                    eventTitle));
                        } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                            images.add(new AdminImageAdapter.ImageItem(
                                    doc.getId(),
                                    imageBase64,
                                    "Event Poster",
                                    doc.getId(),
                                    eventTitle));
                        }
                    }

                    // 2. Fetch Profile Pictures from "accounts" collection
                    db.collection("accounts")
                            .get()
                            .addOnSuccessListener(userSnapshots -> {
                                for (QueryDocumentSnapshot doc : userSnapshots) {
                                    // Use correct field name based on User model annotation
                                    String profileUrl = doc.getString("profile_image_url");
                            String displayName = doc.getString("display_name");
                            if (displayName == null || displayName.isEmpty()) {
                                displayName = doc.getString("full_name");
                            }
                                    if (profileUrl != null && !profileUrl.isEmpty()) {
                                        images.add(new AdminImageAdapter.ImageItem(
                                                doc.getId(),
                                                profileUrl,
                                                "Profile Picture",
                                        doc.getId(),
                                        displayName));
                                    }
                                }

                                progressBar.setVisibility(View.GONE);
                        updateImages(images);
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

    private void updateImages(List<AdminImageAdapter.ImageItem> images) {
        allImages.clear();
        allImages.addAll(images);
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        String query = searchInput != null && searchInput.getText() != null
                ? searchInput.getText().toString()
                : "";
        filterImages(query);
    }

    private void filterImages(String query) {
        if (adapter == null)
            return;

        if (query == null) {
            query = "";
        }

        String lowerQuery = normalize(query);
        List<AdminImageAdapter.ImageItem> filtered = new ArrayList<>();
        if (lowerQuery.isEmpty()) {
            filtered.addAll(allImages);
        } else {
            for (AdminImageAdapter.ImageItem image : allImages) {
                if (buildSearchSource(image).contains(lowerQuery)) {
                    filtered.add(image);
                }
            }
        }

        adapter.setImages(filtered);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private String buildSearchSource(AdminImageAdapter.ImageItem image) {
        StringBuilder builder = new StringBuilder();
        if (image.displayName != null) {
            builder.append(image.displayName).append(" ");
        }
        if (image.relatedId != null) {
            builder.append(image.relatedId);
        }
        return normalize(builder.toString());
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    private void deleteImage(AdminImageAdapter.ImageItem image) {
        if (TestHooks.isUiTestMode()) {
            allImages.removeIf(item -> item.id.equals(image.id));
            applyCurrentFilter();
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            return;
        }

        if (image.type.equals("Event Poster")) {
            // Delete both possible fields for events
            Map<String, Object> updates = new HashMap<>();
            updates.put("imageUrl", null);
            updates.put("image_base64", null);

            db.collection("events").document(image.id)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                        loadImages(); // Refresh
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error removing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @VisibleForTesting
    public void populateImagesForTests(List<AdminImageAdapter.ImageItem> images) {
        allImages.clear();
        if (images != null) {
            allImages.addAll(images);
        }
        progressBar.setVisibility(View.GONE);
        tvEmptyState.setVisibility(allImages.isEmpty() ? View.VISIBLE : View.GONE);
        applyCurrentFilter();
    }
}

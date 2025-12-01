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


/**
 * Activity for administrators to view and manage all images in the system.
 *
 * <p>This activity displays a searchable list of images from two sources:
 * <ul>
 *   <li>Event posters from the events collection</li>
 *   <li>Profile pictures from the accounts collection</li>
 * </ul>
 *
 * <p>Administrators can search for images by display name or related ID, view
 * full-size images, and delete images from the system. Deleting an image removes
 * it from Firestore leaving event intact
 *
 * @author Vansh Taneja, Sai Vashnavi Jattu
 * @version 2.0
 */

public class AdminImageListActivity extends AppCompatActivity {

    // UI components
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

            boolean shouldUseNetwork = !TestHooks.isUiTestMode();
            if (shouldUseNetwork) {
                db = FirebaseFirestore.getInstance();
            }

            initViews();
            setupRecyclerView();

            if (shouldUseNetwork) {
                loadImages();
            } else {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(allImages.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e("AdminImageList", "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initializes all UI components including the toolbar, search bar, and empty state view.
     * Sets up the search functionality with a text watcher for real-time filtering.
     */
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

    /**
     * Initializes all UI components including the toolbar, search bar, and empty state view.
     * Sets up the search functionality with a text watcher for real-time filtering.
     */
    private void setupRecyclerView() {
        adapter = new AdminImageAdapter(this::deleteImage, this::openImageDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        if (TestHooks.isUiTestMode() && recyclerView.getItemAnimator() != null) {
            recyclerView.setItemAnimator(null); // keep Espresso checks deterministic
        }
    }


    /**
     * Opens the full-size image detail view for a specific image.
     * Handles both URL-based and base64 encoded images.
     *
     * @param image The image item to view in detail
     */
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

    /**
     * Loads all images from Firestore including event posters and user profile pictures.
     * Fetches from both the "events" and "accounts" collections and combines the results.
     */
    private void loadImages() {
        if (TestHooks.isUiTestMode() || db == null) {
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

    /**
     * Updates the image list with new data and applies the current search filter.
     *
     * @param images The list of images to display
     */
    private void updateImages(List<AdminImageAdapter.ImageItem> images) {
        allImages.clear();
        allImages.addAll(images);
        applyCurrentFilter();
    }

    /**
     * Applies the current search filter to the image list.
     * Used after loading images to maintain the search state.
     */
    private void applyCurrentFilter() {
        String query = searchInput != null && searchInput.getText() != null
                ? searchInput.getText().toString()
                : "";
        filterImages(query);
    }

    /**
     * Filters the image list based on a search query.
     * Searches through display names and related IDs (case-insensitive).
     * Updates the RecyclerView and empty state visibility based on results.
     *
     * @param query The search query to filter by
     */
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

    /**
     * Builds a searchable string from an image item's properties.
     * Combines display name and related ID for comprehensive searching.
     *
     * @param image The image item to create a search string for
     * @return A normalized, searchable string containing the image's metadata
     */
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

    /**
     * Normalizes a string for case-insensitive searching.
     * Converts to lowercase, collapses whitespace, and trims.
     *
     * @param value The string to normalize
     * @return The normalized string, or empty string if input is null
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    /**
     * Deletes an image reference from Firestore.
     * For event posters, removes both imageUrl and image_base64 fields.
     * For profile pictures, removes the profile_image_url field.
     * Refreshes the image list after successful deletion.
     *
     * @param image The image item to delete
     */
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

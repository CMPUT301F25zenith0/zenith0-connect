package com.example.connect.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.AdminReportAdapter;
import com.example.connect.fragments.ReportDetailsDialogFragment;
import com.example.connect.models.Report;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for administrators to view and manage user-submitted reports.
 *
 * <p>This activity displays a searchable list of all reports submitted by users with the ability to:
 * <ul>
 *   <li>View all pending and resolved reports</li>
 *   <li>Search reports by description or reported item ID in real-time</li>
 *   <li>View detailed information about a specific report in a dialog</li>
 *   <li>Mark reports as resolved (which deletes them from the database)</li>
 * </ul>
 *
 * @author Aakansh Chatterjee
 * @version 1.0
 */
public class AdminReportActivity extends AppCompatActivity implements ReportDetailsDialogFragment.ReportResolveListener {

    private static final String TAG = "AdminReportActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etSearch;
    private View searchLayout;
    private AdminReportAdapter adapter;
    private FirebaseFirestore db;
    private final List<Report> allReports = new ArrayList<>(); // Stores the original, full list


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Reusing the list layout with search bar
            setContentView(R.layout.activity_admin_list);

            db = FirebaseFirestore.getInstance();

            initViews();
            setupRecyclerView();
            setupSearch(); // Setup search listener
            loadUserReports();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("User Reports");
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
            etSearch.setHint("Search by description or Event ID");
        }
    }

    private void setupRecyclerView() {
        adapter = new AdminReportAdapter(this::openReportDetails, this::resolveReport);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the search functionality with a text watcher for real-time filtering.
     * Triggers filtering on every text change in the search input.
     */
    private void setupSearch() {
        if (etSearch == null) return;

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
     * Filters the report list based on a search query.
     * Searches through report descriptions and reported item IDs (case-insensitive).
     * Updates the RecyclerView and empty state message based on results.
     *
     * @param searchText The search query to filter by
     */
    private void filterList(String searchText) {
        String query = searchText.toLowerCase(Locale.getDefault()).trim();
        List<Report> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // If the search bar is empty, show the full list
            filteredList.addAll(allReports);
        } else {
            for (Report report : allReports) {

                // Get fields to search against
                String description = report.getDescription() != null ? report.getDescription().toLowerCase(Locale.getDefault()) : "";
                String reportedItemId = report.getReportedItemId() != null ? report.getReportedItemId().toLowerCase(Locale.getDefault()) : "";

                // Check if the query is in the description OR the reported item ID
                if (description.contains(query) || reportedItemId.contains(query)) {
                    filteredList.add(report);
                }
            }
        }

        // Update the RecyclerView adapter with the filtered list
        adapter.setReports(filteredList);

        // Update the empty state TextView visibility
        if (filteredList.isEmpty()) {
            String emptyMessage = query.isEmpty() ? "No reports found." : "No reports found matching \"" + searchText + "\".";
            tvEmptyState.setText(emptyMessage);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }



    private void openReportDetails(Report report) {
        // Create and show the DialogFragment
        ReportDetailsDialogFragment dialog = ReportDetailsDialogFragment.newInstance(report);
        // Set THIS activity as the resolution listener
        dialog.setReportResolveListener(this);
        dialog.show(getSupportFragmentManager(), "ReportDetailsDialog");
    }

    /**
     * Callback method invoked when a report is resolved from the details dialog.
     * Delegates to resolveReport(Report) to handle the actual resolution.
     *
     * @param report The report that was resolved
     */
    @Override
    public void onReportResolved(Report report) {
        // When the button inside the dialog is pressed, this method is called.
        resolveReport(report);
    }


    /**
     * Marks a report as resolved by deleting it from the database.
     * Shows a progress indicator during deletion and refreshes the list upon completion.
     *
     * @param report The report to resolve
     */
    public void resolveReport(Report report) {
        if (report.getReportId() == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // Logic to delete the report from the database
        db.collection("reports").document(report.getReportId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Report Resolved and Deleted.", Toast.LENGTH_SHORT).show();
                    loadUserReports(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error resolving report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads all user reports from Firestore.
     * Shows a progress indicator while loading and applies the current search filter
     * to the results. Displays an empty state message if no reports are found.
     */
    private void loadUserReports() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("reports")
                // You may add a filter here, e.g., .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    List<Report> currentReports = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Report report = document.toObject(Report.class);
                        report.setReportId(document.getId());
                        currentReports.add(report);
                    }

                    // Store the full list
                    allReports.clear();
                    allReports.addAll(currentReports);

                    // Initialize/refresh the adapter with the current filter state
                    filterList(etSearch != null ? etSearch.getText().toString() : "");

                    if (allReports.isEmpty()) {
                        tvEmptyState.setText("No reports found.");
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading reports", e);
                    // Ensure empty state is shown on failure
                    tvEmptyState.setText("Failed to load reports.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
    }

    /**
     * Handles back button press in the toolbar.
     * Closes the activity and returns to the previous screen.
     *
     * @param item The menu item that was selected
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and return to the previous one (Dashboard)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
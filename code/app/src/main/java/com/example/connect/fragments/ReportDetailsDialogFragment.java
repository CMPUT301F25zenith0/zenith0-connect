package com.example.connect.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.connect.R;
import com.example.connect.models.Report;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * DialogFragment for displaying detailed information about a report.
 * Shows report severity, status, description, and reporter information.
 * Allows admins to resolve or dismiss reports.
 */
public class ReportDetailsDialogFragment extends DialogFragment {

    private static final String ARG_REPORT = "report_object";
    private Report currentReport;
    private ReportResolveListener resolveListener;

    public interface ReportResolveListener {
        void onReportResolved(Report report);
    }

    /**
     * Creates a new instance of ReportDetailsDialogFragment.
     *
     * @param report The Report object to display details for
     * @return A new instance of the dialog fragment
     */
    public static ReportDetailsDialogFragment newInstance(Report report) {
        ReportDetailsDialogFragment fragment = new ReportDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORT, report);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentReport = (Report) getArguments().getSerializable(ARG_REPORT);
        }
        // Set style for a standard
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog_Alert);
    }

    // Set the listener from the Activity
    public void setReportResolveListener(ReportResolveListener listener) {
        this.resolveListener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_details_frag, container, false);
    }

    /**
     * Initializes views and populates them with report data.
     * Sets up button listeners for resolving or canceling the report.
     *
     * @param view The created view
     * @param savedInstanceState Saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (currentReport == null) {
            Toast.makeText(getContext(), "Report data missing.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Initialize Views
        TextView tvSeverity = view.findViewById(R.id.tv_detail_severity);
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        TextView tvReporter = view.findViewById(R.id.tv_detail_reporter);
        TextView tvDescription = view.findViewById(R.id.tv_detail_description);
        Button btnResolve = view.findViewById(R.id.btn_detail_resolve);
        Button btnCancel = view.findViewById(R.id.btn_detail_cancel);


        // Populate static data
        tvSeverity.setText(String.format("Severity: %.1f / 5", currentReport.getSeverity_rating()));
        tvStatus.setText(String.format("Status: %s", currentReport.getStatus() != null ? currentReport.getStatus() : "N/A"));
        tvDescription.setText(currentReport.getDescription());

        // Fetch dynamic data
        fetchReporterName(currentReport.getReporter_id(), tvReporter);

        // Setup Actions
        btnCancel.setOnClickListener(v -> dismiss());

        btnResolve.setOnClickListener(v -> {
            if (resolveListener != null) {
                // Delegate the deletion/resolution logic back to the AdminReportActivity
                resolveListener.onReportResolved(currentReport);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Resolution listener not set.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches the reporter's display name from Firestore.
     * Updates the TextView with the reporter's name and ID.
     * If no name is found, displays the ID with an appropriate message.
     *
     * @param reporterId The ID of the user who created the report
     * @param tvReporter The TextView to update with reporter information
     */
    private void fetchReporterName(String reporterId, TextView tvReporter) {
        // 1. Check if ID is null immediately
        if (reporterId == null || reporterId.isEmpty()) {
            tvReporter.setText("Reported by: UNKNOWN (ID is Null)");
            return;
        }

        // Show the ID immediately
        tvReporter.setText(String.format("ID: %s (Loading name...)", reporterId));

        // Enter accounts collection and find the display_name in accordance to the reporterId
        FirebaseFirestore.getInstance().collection("accounts").document(reporterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString("display_name");

                        String displayName = (fetchedName != null) ? fetchedName : "No Name Found";

                        // Display both Name and ID
                        tvReporter.setText(String.format("Reported by: %s\n(ID: %s)", displayName, reporterId));
                    } else {
                        // Document ID was valid, but no user found in DB
                        tvReporter.setText(String.format("User not found in DB\n(ID: %s)", reporterId));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReportDialog", "Error fetching reporter name: " + e.getMessage());
                    tvReporter.setText(String.format("Error fetching name\n(ID: %s)", reporterId));
                });
    }
}
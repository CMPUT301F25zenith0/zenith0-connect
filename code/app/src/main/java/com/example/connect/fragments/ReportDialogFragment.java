package com.example.connect.fragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.connect.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A DialogFragment for filing a report against an event.
 * Collects a short description (max 200 characters) and a severity rating (1-5 stars).
 */
public class ReportDialogFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "event_id";
    private String eventId;

    /**
     * Create a new instance of this dialog fragment.
     * @param eventId The ID of the event being reported.
     * @return A new instance of ReportDialogFragment.
     */
    public static ReportDialogFragment newInstance(String eventId) {
        ReportDialogFragment fragment = new ReportDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure the dialog style is simple and not full-screen unless specified
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for the report form
        return inflater.inflate(R.layout.report_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText etDescription = view.findViewById(R.id.et_report_description);
        final RatingBar ratingBar = view.findViewById(R.id.rb_severity_rating);
        Button btnSubmit = view.findViewById(R.id.btn_report_submit);
        Button btnCancel = view.findViewById(R.id.btn_report_cancel);

        // Set the character limit of 200 characters
        etDescription.setFilters(new InputFilter[] { new InputFilter.LengthFilter(200) });

        // Set up the listeners
        btnSubmit.setOnClickListener(v -> submitReport(etDescription.getText().toString(), ratingBar.getRating()));
        btnCancel.setOnClickListener(v -> dismiss());
    }

    /**
     * Validates input and submits the report data to the Firestore database.
     * @param description The user-provided text description of the report.
     * @param rating The severity rating (1.0 to 5.0).
     */
    private void submitReport(String description, float rating) {
        // Validation: Check if description is present and within limit
        if (description.trim().isEmpty()) {
            Toast.makeText(getContext(), "Please provide a description for the report.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation: Check if a rating has been provided (assuming 0.0 means unrated)
        if (rating < 1.0f) {
            Toast.makeText(getContext(), "Please select a severity rating (at least 1 star).", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // Final check for necessary IDs
        if (userId == null || eventId == null) {
            Toast.makeText(getContext(), "Error: Authentication or Event ID missing.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Prepare data map for Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> report = new HashMap<>();
        report.put("event_id", eventId);
        report.put("reporter_id", userId);
        report.put("description", description.trim());
        report.put("severity_rating", rating);
        report.put("timestamp", FieldValue.serverTimestamp());
        report.put("status", "pending"); // Initial status for review

        // Save to Firestore
        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Report submitted successfully! Thank you.", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit report. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
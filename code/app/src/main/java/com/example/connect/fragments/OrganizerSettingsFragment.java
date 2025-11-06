package com.example.connect.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.connect.R;
import com.example.connect.constants.AppConstants;
import com.example.connect.network.EventRepository;
import com.example.connect.utils.Validator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerSettingsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "ARG_EVENT_ID";

    public static OrganizerSettingsFragment newInstance(@Nullable String eventId) {
        OrganizerSettingsFragment f = new OrganizerSettingsFragment();
        Bundle b = new Bundle();
        if (eventId != null) b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    private String eventId;
    private Integer cachedCapacity;
    private TextView tvEventName, tvCapacity;
    private EditText inputDrawCount;
    private Button btnSave;
    private ProgressBar progress;
    private EventRepository repo;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_organizer_settings, container, false);

        tvEventName    = v.findViewById(R.id.tv_event_name);
        tvCapacity     = v.findViewById(R.id.tv_capacity);
        inputDrawCount = v.findViewById(R.id.input_draw_count);
        btnSave        = v.findViewById(R.id.btn_save_draw_count);
        progress       = v.findViewById(R.id.progress);

        repo = new EventRepository(FirebaseFirestore.getInstance());
        eventId = (getArguments() == null) ? null : getArguments().getString(ARG_EVENT_ID);

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(requireContext(), "Missing eventId", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false);
        } else {
            loadEventHeader();
        }

        btnSave.setOnClickListener(__ -> onSaveClicked());
        return v;
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!busy);
    }

    private void loadEventHeader() {
        setBusy(true);
        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection(AppConstants.COL_EVENTS)
                .document(eventId);

        ref.get()
                .addOnSuccessListener(this::applyHeaderFromSnapshot)
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCompleteListener(__ -> setBusy(false));
    }

    private void applyHeaderFromSnapshot(DocumentSnapshot snap) {
        if (!snap.exists()) {
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(false);
            return;
        }

        String name = snap.getString(AppConstants.F_NAME);
        tvEventName.setText("Event: " + (TextUtils.isEmpty(name) ? "(unnamed)" : name));

        Number cap = (Number) snap.get(AppConstants.F_CAPACITY);
        cachedCapacity = (cap == null) ? null : cap.intValue();
        tvCapacity.setText("Capacity: " + (cachedCapacity == null ? "—" : cachedCapacity));

        Number dc = (Number) snap.get(AppConstants.F_DRAW_COUNT);
        if (dc != null) inputDrawCount.setText(String.valueOf(dc.intValue()));
    }

    private void onSaveClicked() {
        Validator.Result res = Validator.validateDrawCount(
                inputDrawCount.getText().toString(),
                cachedCapacity
        );

        if (!res.ok) {
            inputDrawCount.setError(res.message);
            if (res.value >= 0) inputDrawCount.setText(String.valueOf(res.value));
            return;
        }

        setBusy(true);
        repo.setDrawCountTransactional(eventId, res.value, cachedCapacity)
                .addOnSuccessListener(__ ->
                        Toast.makeText(requireContext(), "Draw count saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCompleteListener(__ -> setBusy(false));
    }
}

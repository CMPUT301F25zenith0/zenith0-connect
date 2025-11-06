package com.example.connect.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shows Manage Draw tabs by inflating your state_* layouts and wiring RecyclerViews
 * to your item_* row layouts. Uses demo data for now (easy to swap to Firestore later).
 */
public class ManageDrawActivity extends AppCompatActivity {

    // Top bar
    private View btnBack;

    // Tabs from activity_manage_draw.xml
    private TextView tabWaiting, tabSelected, tabEnrolled, tabCanceled;

    // Where we inject each tab's layout
    private FrameLayout contentContainer;

    // ---- map your layouts + recycler IDs here ----
    @LayoutRes private static final int LAYOUT_WAITING   = R.layout.state_waiting;
    @LayoutRes private static final int LAYOUT_SELECTED  = R.layout.state_selected;
    @LayoutRes private static final int LAYOUT_ENROLLED  = R.layout.state_enrolled;
    @LayoutRes private static final int LAYOUT_CANCELED  = R.layout.state_canceled;

    private static final int RV_ID_WAITING   = R.id.recyclerWaiting;
    private static final int RV_ID_SELECTED  = R.id.recyclerSelected;
    private static final int RV_ID_ENROLLED  = R.id.recyclerEnrolled;
    private static final int RV_ID_CANCELED  = R.id.recyclerCanceled;

    private enum Tab { WAITING, SELECTED, ENROLLED, CANCELED }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_draw);

        btnBack      = findViewById(R.id.btnBack);
        tabWaiting   = findViewById(R.id.btnTabWaiting);
        tabSelected  = findViewById(R.id.btnTabSelected);
        tabEnrolled  = findViewById(R.id.btnTabEnrolled);
        tabCanceled  = findViewById(R.id.btnTabCanceled);
        contentContainer = findViewById(R.id.contentContainer);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tabWaiting.setOnClickListener(v -> setTab(Tab.WAITING));
        tabSelected.setOnClickListener(v -> setTab(Tab.SELECTED));
        tabEnrolled.setOnClickListener(v -> setTab(Tab.ENROLLED));
        tabCanceled.setOnClickListener(v -> setTab(Tab.CANCELED));

        setTab(Tab.WAITING); // default
    }

    private void setTab(Tab tab) {
        // visual highlight
        styleTab(tabWaiting,  tab == Tab.WAITING);
        styleTab(tabSelected, tab == Tab.SELECTED);
        styleTab(tabEnrolled, tab == Tab.ENROLLED);
        styleTab(tabCanceled, tab == Tab.CANCELED);

        // inflate state layout into container
        contentContainer.removeAllViews();

        int layoutToInflate;
        int recyclerId;

        switch (tab) {
            case SELECTED:
                layoutToInflate = LAYOUT_SELECTED;
                recyclerId = RV_ID_SELECTED;
                break;
            case ENROLLED:
                layoutToInflate = LAYOUT_ENROLLED;
                recyclerId = RV_ID_ENROLLED;
                break;
            case CANCELED:
                layoutToInflate = LAYOUT_CANCELED;
                recyclerId = RV_ID_CANCELED;
                break;
            case WAITING:
            default:
                layoutToInflate = LAYOUT_WAITING;
                recyclerId = RV_ID_WAITING;
                break;
        }

        LayoutInflater.from(this).inflate(layoutToInflate, contentContainer, true);
        RecyclerView rv = contentContainer.findViewById(recyclerId);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            switch (tab) {
                case WAITING:
                    rv.setAdapter(new WaitingAdapter(demoWaiting()));
                    break;
                case SELECTED:
                    rv.setAdapter(new SelectedAdapter(demoSelected()));
                    break;
                case ENROLLED:
                    rv.setAdapter(new EnrolledAdapter(demoEnrolled()));
                    break;
                case CANCELED:
                    rv.setAdapter(new CanceledAdapter(demoCanceled()));
                    break;
            }
        }
    }

    private void styleTab(TextView tv, boolean selected) {
        if (tv == null) return;
        tv.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tv.setBackgroundColor(selected ? 0xFFBFD7D7 : 0x00FFFFFF);
        tv.setTextColor(0xFF000000);
    }

    // ----------------- Demo data (swap with Firestore later) -----------------
    private List<WaitingItem> demoWaiting() {
        return Arrays.asList(
                new WaitingItem("Alice", "Oct 01, 2025"),
                new WaitingItem("Bob", "Oct 02, 2025"),
                new WaitingItem("Charlie", "Oct 03, 2025")
        );
    }

    private List<SelectedItem> demoSelected() {
        return Arrays.asList(
                new SelectedItem("Deepa", "Selected", "Nov 10, 2025"),
                new SelectedItem("Jorge", "Selected", "Nov 12, 2025")
        );
    }

    private List<EnrolledItem> demoEnrolled() {
        return Arrays.asList(
                new EnrolledItem("Kim",  "Enrolled", "Paid",     "Yes"),
                new EnrolledItem("Lee",  "Enrolled", "Unpaid",   "No"),
                new EnrolledItem("Mo",   "Enrolled", "Paid",     "No")
        );
    }

    private List<CanceledItem> demoCanceled() {
        return Arrays.asList(
                new CanceledItem("Pat",  "No payment", "Nov 01, 2025")
        );
    }

    // ----------------- Models -----------------
    private static class WaitingItem {
        final String name, joined;
        WaitingItem(String n, String j) { name = n; joined = j; }
    }
    private static class SelectedItem {
        final String name, status, deadline;
        SelectedItem(String n, String s, String d) { name = n; status = s; deadline = d; }
    }
    private static class EnrolledItem {
        final String name, status, payment, checkIn;
        EnrolledItem(String n, String s, String p, String c) { name=n; status=s; payment=p; checkIn=c; }
    }
    private static class CanceledItem {
        final String name, reason, canceledOn;
        CanceledItem(String n, String r, String c) { name=n; reason=r; canceledOn=c; }
    }

    // ----------------- Adapters (inflate your item_* XMLs) -----------------

    /** Waiting → item_entract_card.xml */
    private static class WaitingAdapter extends RecyclerView.Adapter<WaitingVH> {
        private final List<WaitingItem> items;
        WaitingAdapter(List<WaitingItem> data) { items = new ArrayList<>(data); }

        @NonNull @Override
        public WaitingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrant_card, parent, false);
            return new WaitingVH(v);
        }

        @Override public void onBindViewHolder(@NonNull WaitingVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }
    private static class WaitingVH extends RecyclerView.ViewHolder {
        TextView tvName, tvJoined, tvStatus;
        Button btnDraw, btnSelected, btnNotify;
        WaitingVH(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvEntrantName);
            tvJoined  = itemView.findViewById(R.id.tvJoinedDate);
            tvStatus  = itemView.findViewById(R.id.tvEntrantStatus);
            btnDraw   = itemView.findViewById(R.id.btnDraw);
            btnSelected = itemView.findViewById(R.id.btnSelected);
            btnNotify = itemView.findViewById(R.id.btnNotify);
        }
        void bind(WaitingItem it) {
            if (tvName != null)   tvName.setText(it.name);
            if (tvJoined != null) tvJoined.setText("Joined: " + it.joined);
            if (tvStatus != null) tvStatus.setText("Status: Waiting");
            // TODO: add click listeners for actions
        }
    }

    /** Selected → item_selected_entrant.xml */
    private static class SelectedAdapter extends RecyclerView.Adapter<SelectedVH> {
        private final List<SelectedItem> items;
        SelectedAdapter(List<SelectedItem> data) { items = new ArrayList<>(data); }

        @NonNull @Override
        public SelectedVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_selected_entrant, parent, false);
            return new SelectedVH(v);
        }

        @Override public void onBindViewHolder(@NonNull SelectedVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }
    private static class SelectedVH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvDeadline;
        View btnRemind, btnCancel, btnReplace;
        SelectedVH(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvName);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            btnRemind  = itemView.findViewById(R.id.btnRemind);
            btnCancel  = itemView.findViewById(R.id.btnCancel);
            btnReplace = itemView.findViewById(R.id.btnReplace);
        }
        void bind(SelectedItem it) {
            if (tvName != null)     tvName.setText(it.name);
            if (tvStatus != null)   tvStatus.setText("Status: " + it.status);
            if (tvDeadline != null) tvDeadline.setText("Deadline: " + it.deadline);
            // TODO: set button actions
        }
    }

    /** Enrolled → item_enrolled_entrant.xml */
    private static class EnrolledAdapter extends RecyclerView.Adapter<EnrolledVH> {
        private final List<EnrolledItem> items;
        EnrolledAdapter(List<EnrolledItem> data) { items = new ArrayList<>(data); }

        @NonNull @Override
        public EnrolledVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_enrolled_entrant, parent, false);
            return new EnrolledVH(v);
        }

        @Override public void onBindViewHolder(@NonNull EnrolledVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }
    private static class EnrolledVH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvPayment, tvCheckIn;
        View btnRemove, btnMarkPaid;
        EnrolledVH(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvName);
            tvStatus  = itemView.findViewById(R.id.tvStatus);
            tvPayment = itemView.findViewById(R.id.tvPayment);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            btnRemove   = itemView.findViewById(R.id.btnRemove);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
        }
        void bind(EnrolledItem it) {
            if (tvName != null)    tvName.setText(it.name);
            if (tvStatus != null)  tvStatus.setText("Status: " + it.status);
            if (tvPayment != null) tvPayment.setText("Payment: " + it.payment);
            if (tvCheckIn != null) tvCheckIn.setText("Check-In: " + it.checkIn);
            // TODO: set button actions
        }
    }

    /** Canceled → item_canceled_entrant.xml */
    private static class CanceledAdapter extends RecyclerView.Adapter<CanceledVH> {
        private final List<CanceledItem> items;
        CanceledAdapter(List<CanceledItem> data) { items = new ArrayList<>(data); }

        @NonNull @Override
        public CanceledVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_canceled_entrant, parent, false);
            return new CanceledVH(v);
        }

        @Override public void onBindViewHolder(@NonNull CanceledVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount() { return items.size(); }
    }
    private static class CanceledVH extends RecyclerView.ViewHolder {
        TextView tvName, tvReasonLabel, tvCanceledOn;
        CanceledVH(@NonNull View itemView) {
            super(itemView);
            tvName       = itemView.findViewById(R.id.tvName);
            tvReasonLabel= itemView.findViewById(R.id.tvReasonLabel);
            tvCanceledOn = itemView.findViewById(R.id.tvCanceledOn);
        }
        void bind(CanceledItem it) {
            if (tvName != null)       tvName.setText(it.name);
            if (tvReasonLabel != null)tvReasonLabel.setText("Reason: " + it.reason);
            if (tvCanceledOn != null) tvCanceledOn.setText("Canceled On: " + it.canceledOn);
        }
    }
}

package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.DashboardEvent;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.VH> {

    public interface OnRowAction {
        void onEdit(DashboardEvent e);
        void onDetails(DashboardEvent e);
        void onManageDraw(DashboardEvent e);
        void onExportCsv(DashboardEvent e);
    }

    private final List<DashboardEvent> data = new ArrayList<>();
    private final OnRowAction actions;

    public OrganizerEventAdapter(List<DashboardEvent> initial, OnRowAction actions) {
        if (initial != null) data.addAll(initial);
        this.actions = actions;
    }

    public void submit(List<DashboardEvent> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_organizer_event, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DashboardEvent e = data.get(pos);
        h.title.setText(e.getTitle());
        h.status.setText(e.getStatus().name());
        h.desc.setText(e.getDescription());

        h.btnEdit.setOnClickListener(v -> actions.onEdit(e));
        h.btnDetails.setOnClickListener(v -> actions.onDetails(e));
        h.btnManageDraw.setOnClickListener(v -> actions.onManageDraw(e));
        h.btnExportCsv.setOnClickListener(v -> actions.onExportCsv(e));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, status, desc;
        View btnEdit, btnDetails, btnManageDraw, btnExportCsv;

        VH(@NonNull View item) {
            super(item);
            // IDs must exist in row_organizer_event.xml
            title = item.findViewById(R.id.tvEventTitle);
            status = item.findViewById(R.id.tvStatusClip);
            desc   = item.findViewById(R.id.tvDescription);

            btnEdit       = item.findViewById(R.id.btnEditEvent);
            btnDetails    = item.findViewById(R.id.btnDetails);
            btnManageDraw = item.findViewById(R.id.btnManageDraw);
            btnExportCsv  = item.findViewById(R.id.btnExportCSV);
        }
    }
}

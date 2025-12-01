package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying and managing user reports in the admin panel.
 * Allows viewing report details and resolving reports.
 */
public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder> {

    private List<Report> reports;
    private final OnReportActionListener detailsListener;
    private final OnReportActionListener resolveListener;

    /**
     * Interface for handling click actions on report items.
     */
    public interface OnReportActionListener {
        void onAction(Report report);
    }

    /**
     * Constructor for the adapter.
     * @param detailsListener Listener for opening report details.
     * @param resolveListener Listener for resolving/deleting the report.
     */
    public AdminReportAdapter(OnReportActionListener detailsListener, OnReportActionListener resolveListener) {
        this.reports = new ArrayList<>();
        this.detailsListener = detailsListener;
        this.resolveListener = resolveListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    /**
     * Updates the data in the adapter and notifies the RecyclerView.
     * @param newReports The new list of reports.
     */
    public void setReports(List<Report> newReports) {
        this.reports = newReports != null ? newReports : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemType;
        private final TextView tvReportId;
        private final TextView tvReason;
        private final Button btnDetails;
        private final Button btnResolve;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemType = itemView.findViewById(R.id.tv_report_item_type);
            tvReportId = itemView.findViewById(R.id.tv_report_item_id);
            tvReason = itemView.findViewById(R.id.tv_report_reason);
            btnDetails = itemView.findViewById(R.id.btn_report_details);
            btnResolve = itemView.findViewById(R.id.btn_report_resolve);
        }

        /**
         * Binds a Report to this ViewHolder's views.
         * Displays the report type, item ID, and reason, and sets up button listeners.
         *
         * @param report The Report to display
         */
        public void bind(final Report report) {
            // Display the type of item reported (e.g., "event", "user", "image")
            tvItemType.setText(String.format("TYPE: %s", report.getItemType() != null ? report.getItemType().toUpperCase() : "N/A"));

            // Display the unique ID of the reported item
            tvReportId.setText(String.format("Event ID: %s...", report.getReportedItemId() != null ? report.getReportedItemId().substring(0, 8) : "N/A"));

            // Display the primary reason
            tvReason.setText(String.format("Reason: %s", report.getReason() != null ? report.getReason() : "No Reason Provided"));

            // Handle the details button click
            btnDetails.setOnClickListener(v -> {
                if (detailsListener != null) {
                    detailsListener.onAction(report);
                }
            });

            // Handle the resolve button click
            btnResolve.setOnClickListener(v -> {
                if (resolveListener != null) {
                    resolveListener.onAction(report);
                }
            });
        }
    }
}
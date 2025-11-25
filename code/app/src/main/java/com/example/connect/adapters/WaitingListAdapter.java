package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.WaitingListEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for displaying entrants in waiting lists
 * Used in ManageDrawActivity to show users in different statuses
 *
 * @author Zenith Team
 * @version 1.0
 */
public class WaitingListAdapter extends ListAdapter<WaitingListEntry, WaitingListAdapter.WaitingListViewHolder> {

    private static final String TAG = "WaitingListAdapter";

    /**
     * Constructor for WaitingListAdapter
     */
    public WaitingListAdapter() {
        super(new DiffUtil.ItemCallback<WaitingListEntry>() {
            @Override
            public boolean areItemsTheSame(@NonNull WaitingListEntry oldItem, @NonNull WaitingListEntry newItem) {
                return oldItem.getUserId() != null && oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull WaitingListEntry oldItem, @NonNull WaitingListEntry newItem) {
                boolean sameUser = (oldItem.getUser() != null && newItem.getUser() != null &&
                        oldItem.getUser().getName().equals(newItem.getUser().getName()));
                boolean sameStatus = oldItem.getStatus().equals(newItem.getStatus());
                return sameUser && sameStatus;
            }
        });
    }

    @NonNull
    @Override
    public WaitingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant_card, parent, false);
        return new WaitingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitingListViewHolder holder, int position) {
        WaitingListEntry entry = getItem(position);
        holder.bind(entry);
    }

    /**
     * ViewHolder for waiting list entrant items
     */
    static class WaitingListViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEntrantName;
        private final TextView tvJoinedDate;
        private final TextView tvEntrantStatus;

        /**
         * Constructor for ViewHolder
         *
         * @param itemView The view for this item
         */
        public WaitingListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
            tvJoinedDate = itemView.findViewById(R.id.tvJoinedDate);
            tvEntrantStatus = itemView.findViewById(R.id.tvEntrantStatus);
        }

        /**
         * Bind waiting list entry data to the view
         *
         * @param entry The waiting list entry to display
         */
        public void bind(WaitingListEntry entry) {
            // Set name from User object
            if (entry.getUser() != null && entry.getUser().getName() != null) {
                tvEntrantName.setText(entry.getUser().getName());
            } else {
                tvEntrantName.setText("Unknown User");
            }

            // Format and set joined date
            if (entry.getJoinedDate() != null) {
                Date date = entry.getJoinedDate().toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvJoinedDate.setText("Joined: " + sdf.format(date));
            } else {
                tvJoinedDate.setText("Joined: Unknown");
            }

            // Set status with proper capitalization and color coding
            String status = entry.getStatus();
            if (status != null && !status.isEmpty()) {
                // Capitalize first letter
                status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                tvEntrantStatus.setText("Status: " + status);

                // Set status color based on type
                int statusColor;
                switch (entry.getStatus().toLowerCase()) {
                    case "waiting":
                        statusColor = 0xFF666666; // Gray
                        break;
                    case "selected":
                        statusColor = 0xFF2196F3; // Blue
                        break;
                    case "enrolled":
                        statusColor = 0xFF4CAF50; // Green
                        break;
                    case "canceled":
                        statusColor = 0xFFFF5252; // Red
                        break;
                    default:
                        statusColor = 0xFF666666; // Default gray
                }
                tvEntrantStatus.setTextColor(statusColor);
            } else {
                tvEntrantStatus.setText("Status: Unknown");
                tvEntrantStatus.setTextColor(0xFF666666);
            }
        }
    }
}
package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class WaitingListAdapter extends ListAdapter<WaitingListEntry, WaitingListAdapter.WaitingListViewHolder> {

    public interface OnSendNotificationClickListener {
        void onSendNotificationClicked(WaitingListEntry entry);
    }

    public interface OnCancelEntrantClickListener {
        void onCancelEntrantClicked(WaitingListEntry entry);
    }

    private OnSendNotificationClickListener notificationClickListener;
    private OnCancelEntrantClickListener cancelClickListener;

    public void setOnSendNotificationClickListener(OnSendNotificationClickListener listener) {
        this.notificationClickListener = listener;
    }

    public void setOnCancelEntrantClickListener(OnCancelEntrantClickListener listener) {
        this.cancelClickListener = listener;
    }

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

        holder.btnSendNotification.setOnClickListener(v -> {
            if (notificationClickListener != null) {
                notificationClickListener.onSendNotificationClicked(entry);
            }
        });

        // Show cancel button only for selected entrants
        if ("selected".equalsIgnoreCase(entry.getStatus())) {
            holder.btnCancelEntrant.setVisibility(View.VISIBLE);
            holder.btnCancelEntrant.setOnClickListener(v -> {
                if (cancelClickListener != null) {
                    cancelClickListener.onCancelEntrantClicked(entry);
                }
            });
        } else {
            holder.btnCancelEntrant.setVisibility(View.GONE);
        }
    }

    static class WaitingListViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEntrantName;
        private final TextView tvJoinedDate;
        private final TextView tvEntrantStatus;
        Button btnSendNotification;
        Button btnCancelEntrant;

        public WaitingListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
            tvJoinedDate = itemView.findViewById(R.id.tvJoinedDate);
            tvEntrantStatus = itemView.findViewById(R.id.tvEntrantStatus);
            btnSendNotification = itemView.findViewById(R.id.btnSendCustomNotification);
            btnCancelEntrant = itemView.findViewById(R.id.btnCancelEntrant); // NEW
        }

        public void bind(WaitingListEntry entry) {
            // Name
            tvEntrantName.setText(entry.getUser() != null && entry.getUser().getName() != null
                    ? entry.getUser().getName()
                    : "Unknown User");

            // Joined Date
            if (entry.getJoinedDate() != null) {
                Date date = entry.getJoinedDate().toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvJoinedDate.setText("Joined: " + sdf.format(date));
            } else {
                tvJoinedDate.setText("Joined: Unknown");
            }

            // Status
            String status = entry.getStatus();
            if (status != null && !status.isEmpty()) {
                status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                tvEntrantStatus.setText("Status: " + status);

                int statusColor;
                switch (entry.getStatus().toLowerCase()) {
                    case "waiting": statusColor = 0xFF666666; break;
                    case "selected": statusColor = 0xFF2196F3; break;
                    case "enrolled": statusColor = 0xFF4CAF50; break;
                    case "canceled": statusColor = 0xFFFF5252; break;
                    default: statusColor = 0xFF666666;
                }
                tvEntrantStatus.setTextColor(statusColor);
            } else {
                tvEntrantStatus.setText("Status: Unknown");
                tvEntrantStatus.setTextColor(0xFF666666);
            }
        }
    }
}

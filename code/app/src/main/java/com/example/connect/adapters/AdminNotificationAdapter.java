package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private List<Map<String, Object>> notifications = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault());

    public void setNotifications(List<Map<String, Object>> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvBody;
        private final TextView tvTimestamp;
        private final TextView tvEventName;
        private final TextView tvRecipient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvBody = itemView.findViewById(R.id.tv_body);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvRecipient = itemView.findViewById(R.id.tv_recipient);
        }

        public void bind(Map<String, Object> notification) {
            String title = (String) notification.get("title");
            String body = (String) notification.get("body");
            String eventName = (String) notification.get("eventName");
            String recipientId = (String) notification.get("recipientId");
            Timestamp timestamp = (Timestamp) notification.get("timestamp");

            tvTitle.setText(title != null ? title : "No Title");
            tvBody.setText(body != null ? body : "No Body");
            tvEventName.setText("Event: " + (eventName != null ? eventName : "Unknown"));
            tvRecipient.setText("To: " + (recipientId != null ? recipientId : "Unknown"));

            if (timestamp != null) {
                tvTimestamp.setText(dateFormat.format(timestamp.toDate()));
            } else {
                tvTimestamp.setText("");
            }
        }
    }
}

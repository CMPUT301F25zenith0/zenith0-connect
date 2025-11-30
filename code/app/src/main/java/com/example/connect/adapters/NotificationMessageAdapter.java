package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.NotificationMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying notification messages in RecyclerView
 */
public class NotificationMessageAdapter extends RecyclerView.Adapter<NotificationMessageAdapter.MessageViewHolder> {

    public List<NotificationMessage> messages = new ArrayList<>();
    private OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(NotificationMessage message);
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.clickListener = listener;
    }

    public void submitList(List<NotificationMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        NotificationMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvBody, tvEvent, tvTimestamp, tvStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMessageTitle);
            tvBody = itemView.findViewById(R.id.tvMessageBody);
            tvEvent = itemView.findViewById(R.id.tvMessageEvent);
            tvTimestamp = itemView.findViewById(R.id.tvMessageTimestamp);
            tvStatus = itemView.findViewById(R.id.tvMessageStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onMessageClick(messages.get(position));
                }
            });
        }

        public void bind(NotificationMessage message) {
            tvTitle.setText(message.getTitle());

            // Truncate body if too long
            String body = message.getBody();
            if (body != null && body.length() > 80) {
                body = body.substring(0, 77) + "...";
            }
            tvBody.setText(body);

            tvEvent.setText(message.getEventName() != null ?
                    message.getEventName() : "Unknown Event");

            // Format timestamp
            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                tvTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
            } else {
                tvTimestamp.setText("Unknown time");
            }

            // Show read status
            if (message.isRead()) {
                tvStatus.setText("✓");
                tvStatus.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_green_dark, null));
            } else {
                tvStatus.setText("○");
                tvStatus.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_orange_dark, null));
            }
        }
    }
}
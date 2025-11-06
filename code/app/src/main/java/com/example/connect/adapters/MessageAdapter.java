package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    // 👇 This defines the Listener interface (fixes your error)
    public interface Listener {
        void onOpen(Message m);
        void onDelete(Message m, int position);
    }

    private final List<Message> data = new ArrayList<>();
    private final Listener listener;

    public MessageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Message> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position < 0 || position >= data.size()) return;
        data.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Message m = data.get(position);
        h.tvTitle.setText(m.getTitle());
        h.tvSubtext.setText(m.getSubtext());
        h.tvDelivered.setText(m.getDelivered());
        h.tvDate.setText(m.getDate());
        h.tvTime.setText(m.getTime());
        h.tvBodyText.setText(m.getBodyText());

        h.root.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(m);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(m, h.getBindingAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView root;
        TextView tvTitle, tvSubtext, tvDelivered, tvDate, tvTime, tvBodyText;
        MaterialButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            root       = (MaterialCardView) itemView;
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvSubtext  = itemView.findViewById(R.id.tvSubtext);
            tvDelivered= itemView.findViewById(R.id.tvDelivered);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvTime     = itemView.findViewById(R.id.tvTime);
            tvBodyText = itemView.findViewById(R.id.tvBodyText);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}

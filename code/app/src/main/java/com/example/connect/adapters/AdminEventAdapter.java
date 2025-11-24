package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private final OnEventDeleteListener deleteListener;

    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    public AdminEventAdapter(OnEventDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvOrganizer;
        private final TextView tvDate;
        private final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvOrganizer = itemView.findViewById(R.id.tv_event_organizer);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Event event) {
            tvName.setText(event.getName());
            // TODO: Fetch organizer name if possible, or just show ID for now
            tvOrganizer.setText("Organizer ID: " + event.getOrganizerId());
            tvDate.setText(event.getDateTime());

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(event);
                }
            });
        }
    }
}

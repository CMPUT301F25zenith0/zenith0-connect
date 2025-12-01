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

/**
 * RecyclerView adapter for displaying events in the admin event management interface.
 *
 * <p>This adapter displays a list of events with administrative controls, showing:
 * <ul>
 *   <li>Event name</li>
 *   <li>Organizer ID</li>
 *   <li>Event date/time</li>
 *   <li>Delete button for removing events</li>
 * </ul>
 *
 * @author Sai Vashnavi Jattu
 * @version 1.0
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private final OnEventDeleteListener deleteListener;
    private final OnEventClickListener clickListener;

    /**
     * Listener interface for handling event deletion requests.
     */
    public interface OnEventDeleteListener {
        void onDelete(Event event);
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Creates a new AdminEventAdapter.
     *
     * @param deleteListener Listener for delete button clicks
     * @param clickListener Listener for event item clicks
     */
    public AdminEventAdapter(OnEventDeleteListener deleteListener, OnEventClickListener clickListener) {
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    /**
     * Updates the list of events displayed by the adapter.
     *
     * @param events The new list of events to display
     */
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

    /**
     * ViewHolder class for event items in the admin list.
     * Handles binding event data to UI components and setting up click listeners.
     */
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

        /**
         * Binds event data to the view components and sets up click listeners.
         *
         * @param event The event to display
         */
        public void bind(Event event) {
            tvName.setText(event.getName());
            tvOrganizer.setText("Organizer ID: " + event.getOrganizerId());
            tvDate.setText(event.getDateTime());

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(event);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onEventClick(event);
                }
            });
        }
    }
}

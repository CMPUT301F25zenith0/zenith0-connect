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

import java.util.List;

/**
 * RecyclerView adapter for displaying events in the admin event list.
 * Handles the display of event information and provides click listeners
 * for viewing event details and deleting events.
 * 
 * @author Zenith Team
 * @version 1.0
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    /** List of events to display in the RecyclerView */
    private List<Event> events;
    
    /** Listener for handling event item clicks and deletions */
    private OnEventClickListener listener;

    /**
     * Interface for handling event item clicks and deletions.
     * Implemented by the activity to respond to user interactions with event items.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event item is clicked.
         *
         * @param event The event that was clicked
         */
        void onEventClick(Event event);
        
        /**
         * Called when the delete button for an event is clicked.
         *
         * @param event The event to be deleted
         */
        void onEventDelete(Event event);
    }

    /**
     * Constructs a new AdminEventAdapter.
     * 
     * @param events List of events to display in the RecyclerView
     * @param listener Listener for handling event item clicks and deletions
     */
    public AdminEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder instance for an event item.
     * Inflates the item layout and returns a new EventViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new EventViewHolder that holds the inflated view
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at the specified position.
     * Populates the ViewHolder with event information and sets up click listeners.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of events in the adapter
     */
    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    /**
     * ViewHolder for event items in the RecyclerView.
     * Holds references to all UI components for a single event item.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the event name */
        private TextView tvEventName;
        
        /** TextView displaying the event location */
        private TextView tvEventLocation;
        
        /** TextView displaying the event date and time */
        private TextView tvEventDateTime;
        
        /** TextView displaying the event category */
        private TextView tvEventCategory;
        
        /** TextView displaying participant count (current/max) */
        private TextView tvEventParticipants;
        
        /** Button for viewing event details */
        private MaterialButton btnView;
        
        /** Button for deleting the event */
        private MaterialButton btnDelete;

        /**
         * Constructs a new EventViewHolder and initializes all view references.
         *
         * @param itemView The root view of the item layout
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventParticipants = itemView.findViewById(R.id.tvEventParticipants);
            btnView = itemView.findViewById(R.id.btnViewEvent);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
        }

        /**
         * Binds event data to the ViewHolder's views.
         * Populates all TextViews with event information and sets up click listeners
         * for the item view and delete button.
         *
         * @param event The event object containing data to display
         * @param listener The listener to handle click events
         */
        void bind(Event event, OnEventClickListener listener) {
            tvEventName.setText(event.getName() != null ? event.getName() : "Untitled Event");
            tvEventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");
            tvEventDateTime.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
            tvEventCategory.setText(event.getCategory() != null ? event.getCategory() : "Uncategorized");
            
            // Display participants info
            String participantsInfo = event.getCurrentParticipants() + "/" + event.getMaxParticipants();
            tvEventParticipants.setText(participantsInfo);
            
            // View button click listener
            btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
            
            // Delete button click listener
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventDelete(event);
                }
            });
        }
    }
}


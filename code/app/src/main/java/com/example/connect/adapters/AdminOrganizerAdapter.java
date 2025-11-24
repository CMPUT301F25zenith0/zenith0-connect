package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * RecyclerView adapter for displaying organizers in the admin organizer list.
 * Handles the display of organizer information and provides click listeners
 * for viewing organizer details and deleting organizers.
 * 
 * @author Zenith Team
 * @version 1.0
 */
public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    /** List of organizers to display in the RecyclerView */
    private List<User> organizers;
    
    /** Listener for handling organizer item clicks and deletions */
    private OnOrganizerClickListener listener;

    /**
     * Interface for handling organizer item clicks and deletions.
     * Implemented by the activity to respond to user interactions with organizer items.
     */
    public interface OnOrganizerClickListener {
        /**
         * Called when an organizer item is clicked.
         *
         * @param organizer The organizer that was clicked
         */
        void onOrganizerClick(User organizer);
        
        /**
         * Called when the delete button for an organizer is clicked.
         *
         * @param organizer The organizer to be deleted
         */
        void onOrganizerDelete(User organizer);
    }

    /**
     * Constructs a new AdminOrganizerAdapter.
     * 
     * @param organizers List of organizers to display in the RecyclerView
     * @param listener Listener for handling organizer item clicks and deletions
     */
    public AdminOrganizerAdapter(List<User> organizers, OnOrganizerClickListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder instance for an organizer item.
     * Inflates the item layout and returns a new OrganizerViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new OrganizerViewHolder that holds the inflated view
     */
    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

    /**
     * Binds organizer data to the ViewHolder at the specified position.
     * Populates the ViewHolder with organizer information and sets up click listeners.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        User organizer = organizers.get(position);
        holder.bind(organizer, listener);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of organizers in the adapter
     */
    @Override
    public int getItemCount() {
        return organizers != null ? organizers.size() : 0;
    }

    /**
     * ViewHolder for organizer items in the RecyclerView.
     * Holds references to all UI components for a single organizer item.
     */
    static class OrganizerViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the organizer name */
        private TextView tvName;
        
        /** TextView displaying the organizer email */
        private TextView tvEmail;
        
        /** TextView displaying the organizer phone number */
        private TextView tvPhone;
        
        /** Button for deleting the organizer */
        private MaterialButton btnDelete;

        /**
         * Constructs a new OrganizerViewHolder and initializes all view references.
         *
         * @param itemView The root view of the item layout
         */
        OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOrganizerName);
            tvEmail = itemView.findViewById(R.id.tvOrganizerEmail);
            tvPhone = itemView.findViewById(R.id.tvOrganizerPhone);
            btnDelete = itemView.findViewById(R.id.btnDeleteOrganizer);
        }

        /**
         * Binds organizer data to the ViewHolder's views.
         * Populates all TextViews with organizer information and sets up click listeners
         * for the item view and delete button.
         *
         * @param organizer The organizer object containing data to display
         * @param listener The listener to handle click events
         */
        void bind(User organizer, OnOrganizerClickListener listener) {
            tvName.setText(organizer.getName() != null ? organizer.getName() : "Unknown");
            tvEmail.setText(organizer.getEmail() != null ? organizer.getEmail() : "No email");
            tvPhone.setText(organizer.getPhone() != null && !organizer.getPhone().isEmpty() 
                    ? organizer.getPhone() : "No phone");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrganizerClick(organizer);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrganizerDelete(organizer);
                }
            });
        }
    }
}


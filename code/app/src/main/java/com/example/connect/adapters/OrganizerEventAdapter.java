package com.example.connect.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;

/**
 * Adapter for displaying organizer's events in a RecyclerView
 * Handles event display with edit, details, manage draw, and export CSV actions
 *
 * @author Zenith Team
 * @version 2.0
 */
public class OrganizerEventAdapter extends ListAdapter<Event, OrganizerEventAdapter.OrganizerEventViewHolder> {

    private static final String TAG = "OrganizerEventAdapter";

    /**
     * Interface for handling organizer event actions
     */
    public interface OrganizerEventListener {
        void onEditEvent(Event event);
        void onViewDetails(Event event);
        void onManageDraw(Event event);
        void onExportCSV(Event event);
        void onImageClick(Event event);
    }

    private final OrganizerEventListener listener;

    /**
     * Constructor for OrganizerEventAdapter
     *
     * @param listener Listener for handling event actions
     */
    public OrganizerEventAdapter(OrganizerEventListener listener) {
        super(new DiffUtil.ItemCallback<Event>() {
            @Override
            public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                return oldItem.getEventId() != null && oldItem.getEventId().equals(newItem.getEventId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrganizerEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_organizer_event, parent, false);
        return new OrganizerEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerEventViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bind(event, listener);
    }

    /**
     * ViewHolder for organizer event items
     */
    static class OrganizerEventViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivEventImage;
        private final ImageView ivAddIcon;
        private final TextView tvEventTitle;
        private final TextView tvStatusClip;
        private final TextView tvDescription;
        private final MaterialButton btnEditEvent;
        private final MaterialButton btnDetails;
        private final MaterialButton btnManageDraw;
        private final MaterialButton btnExportCSV;

        public OrganizerEventViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            ivAddIcon = itemView.findViewById(R.id.ivAddIcon);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvStatusClip = itemView.findViewById(R.id.tvStatusClip);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnManageDraw = itemView.findViewById(R.id.btnManageDraw);
            btnExportCSV = itemView.findViewById(R.id.btnExportCSV);
        }

        public void bind(Event event, OrganizerEventListener listener) {
            // Set event title
            tvEventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");

            // Set status clip (based on event status or dates)
            String status = determineEventStatus(event);
            tvStatusClip.setText(status);

            // Set description (truncate if too long)
            String description = event.getDescription();
            if (description != null && description.length() > 50) {
                description = description.substring(0, 47) + "...";
            }
            tvDescription.setText(description != null ? description : "No description");

            // Load event image
            if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(event.getImageBase64(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bmp != null) {
                        ivEventImage.setImageBitmap(bmp);
                        ivAddIcon.setVisibility(View.GONE);
                    } else {
                        ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery);
                        ivAddIcon.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding image: " + e.getMessage());
                    ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    ivAddIcon.setVisibility(View.VISIBLE);
                }
            } else {
                ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery);
                ivAddIcon.setVisibility(View.VISIBLE);
            }

            // Clear old listeners to prevent conflicts
            btnEditEvent.setOnClickListener(null);
            btnDetails.setOnClickListener(null);
            btnManageDraw.setOnClickListener(null);
            btnExportCSV.setOnClickListener(null);

            // Set up button click listeners
            btnEditEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditEvent(event);
                }
            });

            btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(event);
                }
            });

            btnManageDraw.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onManageDraw(event);
                }
            });

            btnExportCSV.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExportCSV(event);
                }
            });

            // Image click listener (for changing/adding image)
            ivEventImage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(event);
                }
            });

            ivAddIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(event);
                }
            });
        }

        /**
         * Determines the status of an event based on its dates
         *
         * @param event The event to check
         * @return Status string (Open, Closed, Draft, etc.)
         */
        private String determineEventStatus(Event event) {
            String regStart = event.getRegStart();
            String regStop = event.getRegStop();

            if (regStart == null || regStart.isEmpty()) {
                return "Draft";
            }
            return "Open";
        }
    }
}
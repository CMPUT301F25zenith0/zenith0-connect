package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    public interface Listener {
        void onDetails(Event e);
        void onJoin(Event e);
    }

    private final Listener listener;
    private List<Event> allEvents = new ArrayList<>();

    public EventListAdapter(Listener listener) {
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
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_layout, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = getItem(position);
        holder.bind(event, listener);
    }

    public void submit(List<Event> events) {
        allEvents = events != null ? new ArrayList<>(events) : new ArrayList<>();
        submitList(allEvents);
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            submitList(allEvents);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            if (matchesSearch(event, lowerQuery)) {
                filtered.add(event);
            }
        }
        submitList(filtered);
    }

    public void filterByDate(String date) {
        if (date == null || date.isEmpty()) {
            submitList(allEvents);
            return;
        }

        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getDateTime() != null && event.getDateTime().contains(date)) {
                filtered.add(event);
            }
        }
        submitList(filtered);
    }

    public void filterByInterest(String interest) {
        if (interest == null || interest.isEmpty()) {
            submitList(allEvents);
            return;
        }

        String lowerInterest = interest.toLowerCase();
        List<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getCategory() != null && event.getCategory().toLowerCase().contains(lowerInterest)) {
                filtered.add(event);
            }
        }
        submitList(filtered);
    }

    public void clearFilters() {
        submitList(allEvents);
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(allEvents);
    }

    public List<Event> getCurrentList() {
        List<Event> current = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            current.add(getItem(i));
        }
        return current;
    }

    private boolean matchesSearch(Event event, String query) {
        return (event.getName() != null && event.getName().toLowerCase().contains(query)) ||
               (event.getLocation() != null && event.getLocation().toLowerCase().contains(query)) ||
               (event.getCategory() != null && event.getCategory().toLowerCase().contains(query)) ||
               (event.getDescription() != null && event.getDescription().toLowerCase().contains(query));
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final TextView eventTitle;
        private final TextView eventDateTime;
        private final TextView eventLocation;
        private final TextView eventPrice;
        private final Button btnViewDetails;
        private final Button btnJoinWaitlist;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventPrice = itemView.findViewById(R.id.eventPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnJoinWaitlist = itemView.findViewById(R.id.btnJoinWaitlist);
        }

        public void bind(Event event, Listener listener) {
            eventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");
            eventDateTime.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
            eventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");
            eventPrice.setText(event.getPrice() != null ? event.getPrice() : "Free");

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                // TODO: Load image with Glide or Picasso
                eventImage.setImageResource(R.drawable.placeholder_img);
            } else {
                eventImage.setImageResource(R.drawable.placeholder_img);
            }

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetails(event);
                }
            });

            btnJoinWaitlist.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJoin(event);
                }
            });
        }
    }
}

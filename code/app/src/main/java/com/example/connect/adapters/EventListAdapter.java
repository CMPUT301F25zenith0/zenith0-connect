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

/**
 * Event list adapter is a recycle view adapter fo display a list of event
 * objects
 * <p>
 * Using listAdapter and diffUtil to handle list updates, filtering, and data
 * changes without reloading the entire dataset.
 * </p>
 * <p>
 * This adapter supports filtering events by search query, date, and interest
 * category.
 * It also provides listener callbacks for user interactions such as viewing
 * event details or joining an event waitlist.
 * </p>
 * 
 * @author Zenith team
 * @version 2.0
 */
public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    /**
     * Called when the user selects an event to view its details.
     * Or join the waitlist directly from the listView
     *
     * @param e is the event that was selected
     */
    public interface Listener {
        // TODO - Complete the functionality of join from this button
        void onDetails(Event e);

        void onJoin(Event e);
    }

    private final Listener listener;
    private List<Event> allEvents = new ArrayList<>();

    /**
     * Constructs a new EventList Adapter
     *
     * @param listener the listener that handles user interactions with event items
     */
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
        private final TextView eventLocation;
        private final TextView eventDateDay;
        private final TextView eventDateMonth;
        private final Button btnEventDetails;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDateDay = itemView.findViewById(R.id.eventDateDay);
            eventDateMonth = itemView.findViewById(R.id.eventDateMonth);
            btnEventDetails = itemView.findViewById(R.id.btnEventDetails);
        }

        public void bind(Event event, Listener listener) {
            eventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");
            eventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");

            // Parse and set date
            if (event.getDateTime() != null && !event.getDateTime().isEmpty()) {
                String[] dateParts = parseDate(event.getDateTime());
                eventDateDay.setText(dateParts[0]);
                eventDateMonth.setText(dateParts[1]);
            } else {
                eventDateDay.setText("--");
                eventDateMonth.setText("TBD");
            }

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                // TODO: Load image with Glide or Picasso
                eventImage.setImageResource(R.drawable.placeholder_img);
            } else {
                eventImage.setImageResource(R.drawable.placeholder_img);
            }

            // Event Details button click
            btnEventDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetails(event);
                }
            });

            // Item click listener is handled by the RecyclerView or parent
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetails(event);
                }
            });
        }

        private String[] parseDate(String dateString) {
            String day = "01";
            String month = "JAN";
            try {
                String[] parts = dateString.split("[/\\s,-]+");
                if (parts.length >= 2) {
                    if (parts[0].matches("\\d+")) {
                        day = parts[0];
                        // If second part is a number, convert to month name
                        if (parts[1].matches("\\d+")) {
                            int monthNum = Integer.parseInt(parts[1]);
                            String[] monthNames = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };
                            if (monthNum >= 1 && monthNum <= 12) {
                                month = monthNames[monthNum - 1];
                            }
                        } else {
                            month = parts[1].substring(0, Math.min(3, parts[1].length())).toUpperCase();
                        }
                    } else if (parts[1].matches("\\d+")) {
                        month = parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase();
                        day = parts[1];
                    }
                }
            } catch (Exception e) {
            }
            return new String[] { day, month };
        }
    }
}

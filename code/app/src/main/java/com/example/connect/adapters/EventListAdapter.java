package com.example.connect.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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

import javax.annotation.meta.When;

/**
 * Event list adapter is a recycle view adapter fo display a list of event objects
 * <p>
 * Using listAdapter and diffUtil to handle list updates, filtering, and data changes without reloading the entire dataset.
 * </p>
 * <p>
 * This adapter supports filtering events by search query, date, and interest category.
 * It also provides listener callbacks for user interactions such as viewing event details or joining an event waitlist.
 * </p>
 * @author Zenith team
 * @version 2.0
 */
public class EventListAdapter extends ListAdapter<Event, EventListAdapter.EventViewHolder> {

    private static final String TAG = "EventListAdapter";

    /**
     * Called when the user selects an event to view its details.
     * Or join the waitlist directly from the listView
     *
     * @param e is the event that was selected
     */
    public interface Listener {
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
        Log.d(TAG, "EventListAdapter created with listener: " + (listener != null));
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
        Log.d(TAG, "Binding event at position " + position + ": " + (event != null ? event.getName() : "null"));
        holder.bind(event, listener);
    }

    public void submit(List<Event> events) {
        allEvents = events != null ? new ArrayList<>(events) : new ArrayList<>();
        submitList(new ArrayList<>(allEvents));
        Log.d(TAG, "Submitted " + allEvents.size() + " events to adapter");
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            submitList(new ArrayList<>(allEvents));
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
            submitList(new ArrayList<>(allEvents));
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
            submitList(new ArrayList<>(allEvents));
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
        submitList(new ArrayList<>(allEvents));
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
        private static final String TAG = "EventViewHolder";

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

            Log.d(TAG, "ViewHolder created - buttons found: " +
                    (btnViewDetails != null) + ", " + (btnJoinWaitlist != null));
        }

        public void bind(Event event, Listener listener) {
            Log.d(TAG, "Binding event: " + (event != null ? event.getName() : "null"));
            Log.d(TAG, "Listener is: " + (listener != null ? "present" : "NULL"));

            eventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");
            eventDateTime.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
            eventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");

            String formattedPrice = priceFormat(event.getPrice());
            eventPrice.setText(formattedPrice);
            if (formattedPrice.equals("Free")) {
                eventPrice.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                eventPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.primary_gold));
            }

            // Handle image
            if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(event.getImageBase64(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bmp != null) {
                        eventImage.setImageBitmap(bmp);
                    } else {
                        eventImage.setImageResource(R.drawable.placeholder_img);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding image: " + e.getMessage());
                    eventImage.setImageResource(R.drawable.placeholder_img);
                }
            } else {
                eventImage.setImageResource(R.drawable.placeholder_img);
            }

            // Set up button clicks
            btnViewDetails.setOnClickListener(v -> {
                Log.d(TAG, "View Details clicked for: " + event.getName());
                if (listener != null) {
                    Log.d(TAG, "Calling listener.onDetails()");
                    listener.onDetails(event);
                } else {
                    Log.e(TAG, "Listener is NULL - cannot handle click!");
                }
            });

            btnJoinWaitlist.setOnClickListener(v -> {
                Log.d(TAG, "Join Waitlist clicked for: " + event.getName());
                if (listener != null) {
                    Log.d(TAG, "Calling listener.onJoin()");
                    listener.onJoin(event);
                } else {
                    Log.e(TAG, "Listener is NULL - cannot handle click!");
                }
            });

            Log.d(TAG, "Bind complete for: " + event.getName());
        }

        /**
         * Helper method to format price string.
         * returns "Free" if value is 0 or empty, otherwise returns formatted currency (e.g., "$10.00")
         **/
        private static String priceFormat(String priceStr){
            if (priceStr == null || priceStr.trim().isEmpty()) {
                return "Free";
            }

            try {
                // Remove everything that isn't a number or a decimal point
                // This handles cases like "$50", "USD 50", or just "50"
                String cleanPrice = priceStr.replaceAll("[^\\d.]", "");

                if (cleanPrice.isEmpty()) {
                    return "Free";
                }

                // Parse to double
                double priceValue = Double.parseDouble(cleanPrice);

                // 3. Check value
                if (priceValue <= 0) {
                    return "Free";
                } else {
                    // Format to 2 decimal places
                    return String.format("$%.2f", priceValue);
                }
            } catch (NumberFormatException e) {
                // If parsing fails (e.g. text is "Donation only"), return original text
                return priceStr;
            }
        }
    }
}
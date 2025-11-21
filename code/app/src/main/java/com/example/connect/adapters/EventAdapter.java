package com.example.connect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.connect.R;
import com.example.connect.models.Event;

import java.util.List;

/**
 * Custom arrayAdapter used to populate the event listview
 * <p>
 * It inflates a custom layout for each event item and binds the event data
 * (title, date/time, location, price, and image) to its corresponding view
 * components.
 * </p>
 * <p>
 * Each list item also contains two buttons:
 * <ul>
 * <li><b>View Details</b> — navigates to the event details screen.</li>
 * TODO: Change the flow of join Waitlist
 * <li><b>Join Waitlist</b> — navigates to the event details screen where users
 * can join the waitlist.</li>
 * </ul>
 * </p>
 * 
 * @author Zenith team
 * @version 3.0
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final List<Event> events;

    /**
     * Constructs a new eventAdapter
     *
     * @param context the current context (usually the Activity where this adapter
     *                is used)
     * @param events  the list of event objects to display in the list
     */

    public EventAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    /**
     * Provides a view for each item in the dataset.
     * <p>
     * This method uses the ViewHolder pattern to optimize performance
     * by reusing existing views rather than inflating new ones each time.
     * </p>
     *
     * @param position    the position of the item within the dataset
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent view that this view will eventually be attached
     *                    to
     * @return the fully populated view for the current event item
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_item_layout, parent, false);
            holder = new ViewHolder();
            holder.eventImage = convertView.findViewById(R.id.eventImage);
            holder.eventTitle = convertView.findViewById(R.id.eventTitle);
            holder.eventLocation = convertView.findViewById(R.id.eventLocation);
            holder.eventDateDay = convertView.findViewById(R.id.eventDateDay);
            holder.eventDateMonth = convertView.findViewById(R.id.eventDateMonth);
            holder.btnEventDetails = convertView.findViewById(R.id.btnEventDetails);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = events.get(position);

        // Set event details
        holder.eventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");
        holder.eventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");

        // Parse and set date
        if (event.getDateTime() != null && !event.getDateTime().isEmpty()) {
            String[] dateParts = parseDate(event.getDateTime());
            holder.eventDateDay.setText(dateParts[0]);
            holder.eventDateMonth.setText(dateParts[1]);
        } else {
            holder.eventDateDay.setText("--");
            holder.eventDateMonth.setText("TBD");
        }

        // Load image if available
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            // TODO: Use Glide or Picasso to load image
            holder.eventImage.setImageResource(R.drawable.placeholder_img);
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_img);
        }

        // Event Details button click
        holder.btnEventDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.connect.activities.EventDetails.class);
            intent.putExtra("event_id", event.getEventId());
            context.startActivity(intent);
        });

        return convertView;
    }

    /**
     * Helper to parse date string into Day and Month.
     * Assumes format like "dd/MM/yyyy" or "MMM dd, yyyy" or similar.
     * For now, simple heuristic or fallback.
     *
     * @param dateString the raw date string from the event
     * @return an array where [0] is day, [1] is month (uppercase 3-letter
     *         abbreviation)
     */
    private String[] parseDate(String dateString) {
        String day = "01";
        String month = "JAN";

        try {
            // Split by common delimiters
            String[] parts = dateString.split("[/\\s,-]+");
            if (parts.length >= 2) {
                // Heuristic: if first part is a number, assume dd/MM or dd MMM
                if (parts[0].matches("\\d+")) {
                    day = parts[0];
                    // If second part is a number, it's MM; if text, it's month name
                    if (parts[1].matches("\\d+")) {
                        // Convert month number to name
                        int monthNum = Integer.parseInt(parts[1]);
                        String[] monthNames = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };
                        if (monthNum >= 1 && monthNum <= 12) {
                            month = monthNames[monthNum - 1];
                        }
                    } else {
                        // It's already a month name, take first 3 chars and uppercase
                        month = parts[1].substring(0, Math.min(3, parts[1].length())).toUpperCase();
                    }
                } else if (parts[1].matches("\\d+")) {
                    // Format is MMM dd
                    month = parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase();
                    day = parts[1];
                }
            }
        } catch (Exception e) {
            // Fallback to defaults
        }

        return new String[] { day, month };
    }

    /**
     * Updates the adapter with a new list of events and refreshes the view.
     *
     * @param newEvents the new list of event objects to display
     */
    public void updateEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    /**
     * A static inner class used to cache view references for each list item.
     */
    static class ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventLocation;
        TextView eventDateDay;
        TextView eventDateMonth;
        Button btnEventDetails;
    }
}
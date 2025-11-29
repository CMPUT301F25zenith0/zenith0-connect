package com.example.connect.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.connect.R;
import com.example.connect.models.Event;

import java.util.List;

/**
 * Custom arrayAdapter used to populate the event listview
 * <p>
 * It inflates a custom layout for each event item and binds the event data
 * (title, date/time, location, price, and image) to its corresponding view components.
 * </p>
 * <p>
 * Each list item also contains two buttons:
 * <ul>
 *     <li><b>View Details</b> — navigates to the event details screen.</li>
 *     TODO: Change the flow of join Waitlist
 *     <li><b>Join Waitlist</b> — navigates to the event details screen where users can join the waitlist.</li>
 * </ul>
 * </p>
 * @author Zenith team
 * @version 3.0
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final List<Event> events;

    /**
     * Constructs a new eventAdapter
     *
     * @param context the current context (usually the Activity where this adapter is used)
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
     * @param parent      the parent view that this view will eventually be attached to
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
            holder.eventDateTime = convertView.findViewById(R.id.eventDateTime);
            holder.eventLocation = convertView.findViewById(R.id.eventLocation);
            holder.eventPrice = convertView.findViewById(R.id.eventPrice);
            holder.btnViewDetails = convertView.findViewById(R.id.btnViewDetails);
            holder.btnJoinWaitlist = convertView.findViewById(R.id.btnJoinWaitlist);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = events.get(position);

        // Set event details
        holder.eventTitle.setText(event.getName() != null ? event.getName() : "Untitled Event");
        holder.eventDateTime.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
        holder.eventLocation.setText(event.getLocation() != null ? event.getLocation() : "Location TBD");
        holder.eventPrice.setText(event.getPrice() != null ? event.getPrice() : "Free");

        // Load the poster image (URL > Base64 > placeholder fallback)
        bindEventImage(holder.eventImage, event);

        // View Details button click
        holder.btnViewDetails.setOnClickListener(v -> {
            // Navigate to event details activity
            Intent intent = new Intent(context, com.example.connect.activities.EventDetails.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            context.startActivity(intent);
        });

        // Join Waitlist button click
        holder.btnJoinWaitlist.setOnClickListener(v -> {
            // Navigate to event details activity (where users can join waitlist)
            Intent intent = new Intent(context, com.example.connect.activities.EventDetails.class);
            intent.putExtra("EVENT_ID", event.getEventId());
            context.startActivity(intent);
        });

        return convertView;
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
     * A static inner class used to cache view references for each list item, minimizing repeated calls to that find views and improving scroll performance.
     */
    static class ViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventDateTime;
        TextView eventLocation;
        TextView eventPrice;
        Button btnViewDetails;
        Button btnJoinWaitlist;
    }

    private void bindEventImage(ImageView imageView, Event event) {
        String imageUrl = event.getImageUrl();
        String imageBase64 = event.getImageBase64();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.placeholder_img)
                    .centerCrop()
                    .into(imageView);
            return;
        }

        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return;
                }
            } catch (IllegalArgumentException ignored) {
                // fall through to placeholder
            }
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.placeholder_img);
    }
}
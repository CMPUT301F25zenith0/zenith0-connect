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
 * Adapter for displaying Event objects in a ListView
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final List<Event> events;

    public EventAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

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

        // Load image if available
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            // TODO: Use Glide or Picasso to load image
            // Glide.with(context).load(event.getImageUrl()).into(holder.eventImage);
            holder.eventImage.setImageResource(R.drawable.placeholder_img);
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_img);
        }

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
     * Update the adapter's data
     */
    public void updateEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder pattern for efficient view recycling
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
}
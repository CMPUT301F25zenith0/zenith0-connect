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

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.VH> {

    public interface Listener {
        void onDetails(Event e);
        void onJoin(Event e);
    }

    private final Listener listener;
    private final List<Event> items = new ArrayList<>();
    private List<Event> all = new ArrayList<>();

    public EventListAdapter(Listener l) { this.listener = l; }

    public void submit(List<Event> events) {
        items.clear();
        items.addAll(events);
        all = new ArrayList<>(events);
        notifyDataSetChanged();
    }

    /** simple text filter on title */
    public void filter(String query) {
        items.clear();
        if (query == null || query.trim().isEmpty()) {
            items.addAll(all);
        } else {
            String q = query.toLowerCase();
            for (Event e : all) {
                if ((e.getName() != null && e.getName().toLowerCase().contains(q))) {
                    items.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = items.get(pos);
        h.title.setText(e.getName() != null ? e.getName() : "Event Title");
        
        // Date & Time
        String dateTime = "";
        if (e.getDate() != null && !e.getDate().isEmpty()) {
            dateTime = e.getDate();
            if (e.getTime() != null && !e.getTime().isEmpty()) {
                dateTime = h.itemView.getContext().getString(R.string.event_date_time_fmt, e.getDate(), e.getTime());
            }
        }
        h.dateAndTime.setText(dateTime);
        
        // Location
        if (e.getLocation() != null && !e.getLocation().isEmpty()) {
            h.location.setText(h.itemView.getContext().getString(R.string.event_location_fmt, e.getLocation()));
            h.location.setVisibility(android.view.View.VISIBLE);
        } else {
            h.location.setVisibility(android.view.View.GONE);
        }
        
        // Price
        if (e.getPrice() != null && !e.getPrice().isEmpty()) {
            h.price.setText(h.itemView.getContext().getString(R.string.event_price_fmt, e.getPrice()));
            h.price.setVisibility(android.view.View.VISIBLE);
        } else {
            h.price.setVisibility(android.view.View.GONE);
        }

        h.btnDetails.setOnClickListener(v -> listener.onDetails(e));
        h.btnJoin.setOnClickListener(v -> listener.onJoin(e));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, dateAndTime, location, price;
        MaterialButton btnDetails, btnJoin;
        android.widget.ImageView eventImage;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            dateAndTime = v.findViewById(R.id.tvDateAndTime);
            location = v.findViewById(R.id.tvLocation);
            price = v.findViewById(R.id.tvPrice);
            eventImage = v.findViewById(R.id.event_image);
            btnDetails = v.findViewById(R.id.btnDetails);
            btnJoin    = v.findViewById(R.id.btnJoin);
        }
    }

    // filter by Date function

    public void filterByDate(String selectedDate) {
        items.clear();
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            items.addAll(all);
        } else {
            for (Event e : all) {
                if (e.getDate() != null && e.getDate().equals(selectedDate)) {
                    items.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    // interest filter

    // Filter by Interest (compares interest string with event name)
    public void filterByInterest(String interest) {
        items.clear();
        if (interest == null || interest.trim().isEmpty()) {
            items.addAll(all);
        } else {
            String lowerInterest = interest.toLowerCase();
            for (Event e : all) {
                if (e.getName() != null && e.getName().toLowerCase().contains(lowerInterest)) {
                    items.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    // clear filter
    public void clearFilters() {
        items.clear();
        items.addAll(all); // Reset to full list
        notifyDataSetChanged();
    }




}

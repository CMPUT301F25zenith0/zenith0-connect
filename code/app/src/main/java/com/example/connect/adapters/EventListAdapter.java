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
        h.title.setText(e.getName());
        h.date.setText(h.itemView.getContext().getString(R.string.event_date_fmt, e.getDate()));
        h.reg.setText(h.itemView.getContext().getString(R.string.event_reg_fmt,
                e.getRegOpens(), e.getRegCloses()));

        h.btnDetails.setOnClickListener(v -> listener.onDetails(e));
        h.btnJoin.setOnClickListener(v -> listener.onJoin(e));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, date, reg;
        MaterialButton btnDetails, btnJoin;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            date  = v.findViewById(R.id.tvDate);
            reg   = v.findViewById(R.id.tvReg);
            btnDetails = v.findViewById(R.id.btnDetails);
            btnJoin    = v.findViewById(R.id.btnJoin);
        }
    }
}

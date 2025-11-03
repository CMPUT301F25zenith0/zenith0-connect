package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.VH> {

    public interface OnEventClick {
        void onClick(Event event);
    }

    private final List<Event> items = new ArrayList<>();
    private final OnEventClick click;

    public EventListAdapter(OnEventClick click) {
        this.click = click;
    }

    public void submit(List<Event> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = items.get(pos);
        h.title.setText(e.getName());
        String subtitle = (e.getDate() != null ? e.getDate() : "")
                + ((e.getRegCloses() != null) ? ("  ·  Reg closes: " + e.getRegCloses()) : "");
        h.subtitle.setText(subtitle.trim());
        h.card.setOnClickListener(v -> {
            if (click != null) click.onClick(e);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        TextView title, subtitle;
        VH(@NonNull View v) {
            super(v);
            card = (CardView) v;
            title = v.findViewById(R.id.event_title);
            subtitle = v.findViewById(R.id.event_subtitle);
        }
    }
}

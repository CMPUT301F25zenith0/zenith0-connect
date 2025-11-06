package com.example.connect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.adapters.MessageAdapter;
import com.example.connect.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private static final String TAG = "MessagesActivity";

    private RecyclerView recycler;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages); // make sure name matches XML

        // ----- Back button -----
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "btnBack not found. Check activity_messages.xml id.");
        }

        // ----- RecyclerView -----
        recycler = findViewById(R.id.recyclerViewMessages);
        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(this));

            adapter = new MessageAdapter(new MessageAdapter.Listener() {
                @Override
                public void onOpen(Message m) {
                    Toast.makeText(MessagesActivity.this, "Open: " + m.getTitle(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDelete(Message m, int position) {
                    adapter.removeAt(position);
                    Toast.makeText(MessagesActivity.this, "Deleted: " + m.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });

            recycler.setAdapter(adapter);
            adapter.submit(seedMessages());
        } else {
            Log.e(TAG, "recyclerViewMessages not found. Check activity_messages.xml id.");
        }

        // ----- Bottom Navigation -----
        safeClick(R.id.btnNavDashboard, v -> {
            startActivity(new Intent(this, OrganizerActivity.class));
            finish();
        });

        safeClick(R.id.btnNavMap, v ->
                startActivity(new Intent(this, MapActivity.class)));

        safeClick(R.id.btnNavProfile, v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        safeClick(R.id.btnNavMessage, v ->
                Toast.makeText(this, "Already on Messages", Toast.LENGTH_SHORT).show());
    }

    // Helper for safe button linking
    private void safeClick(int id, View.OnClickListener l) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(l);
        else Log.e(TAG, "Missing view id in layout: " + id);
    }

    // ----- Dummy Messages -----
    private List<Message> seedMessages() {
        List<Message> list = new ArrayList<>();
        list.add(new Message("1", "Campus Admin", "System Notice", "Delivered", "Nov 6, 2025", "10:41 AM", "Welcome to the event lottery platform."));
        list.add(new Message("2", "Hackathon Bot", "Deadline Reminder", "Delivered", "Nov 5, 2025", "8:15 PM", "Submissions close at 11:59 PM tonight."));
        list.add(new Message("3", "Marathon Team", "Selection Update", "Delivered", "Nov 4, 2025", "4:22 PM", "You have been moved to Selected. Please confirm by Friday."));
        list.add(new Message("4", "Art Workshop", "Materials List", "Delivered", "Nov 3, 2025", "1:05 PM", "Bring brushes, watercolor paper, and tape."));
        return list;
    }
}

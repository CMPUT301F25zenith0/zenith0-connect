package com.example.connect.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.example.connect.utils.LotteryManager;
import com.example.connect.utils.NotificationActionsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import android.widget.ArrayAdapter;

public class MyEventsAdapter extends ArrayAdapter<Event> {

    public static final int TAB_WAITLIST = 0;
    public static final int TAB_SELECTED = 1;
    public static final int TAB_CONFIRMED = 2;

    private int currentTabMode;
    private Context context;
    private FirebaseFirestore db;
    private LotteryManager lotteryManager;

    public MyEventsAdapter(Context context, List<Event> events, int tabMode) {
        super(context, 0, events);
        this.context = context;
        this.currentTabMode = tabMode;
        this.db = FirebaseFirestore.getInstance();
        this.lotteryManager = new LotteryManager();
    }

    public void setTabState(int tabMode) {
        this.currentTabMode = tabMode;
        notifyDataSetChanged();
    }

    private static String priceFormat(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) return "Free";
        try {
            String cleanPrice = priceStr.replaceAll("[^\\d.]", "");
            if (cleanPrice.isEmpty()) return "Free";
            double priceValue = Double.parseDouble(cleanPrice);
            return priceValue <= 0 ? "Free" : String.format("$%.2f", priceValue);
        } catch (NumberFormatException e) {
            return priceStr;
        }
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_event_card, parent, false);
        }

        TextView title = convertView.findViewById(R.id.tv_event_title);
        TextView time = convertView.findViewById(R.id.tv_event_time);
        TextView price = convertView.findViewById(R.id.tv_event_price);
        ImageView eventImage = convertView.findViewById(R.id.iv_event_image);

        if (event != null) {
            title.setText(event.getName());
            time.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
            price.setText(priceFormat(event.getPrice()));

            if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(event.getImageBase64(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bmp != null) eventImage.setImageBitmap(bmp);
                    else eventImage.setImageResource(android.R.drawable.ic_menu_gallery);
                } catch (Exception e) {
                    eventImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                eventImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Buttons
        MaterialButton btnAccept = convertView.findViewById(R.id.btn_accept);
        MaterialButton btnCancel = convertView.findViewById(R.id.btn_cancel);
        MaterialButton btnLeave = convertView.findViewById(R.id.btn_leave);

        btnAccept.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnLeave.setVisibility(View.GONE);

        if (currentTabMode == TAB_SELECTED) {
            btnAccept.setVisibility(View.VISIBLE);
            btnAccept.setText("Accept");

            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Decline");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));

        } else if (currentTabMode == TAB_CONFIRMED) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Cancel");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));

        } else { // WAITLIST
            btnLeave.setVisibility(View.VISIBLE);
            btnLeave.setText("Leave List");
            btnLeave.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnLeave.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));
        }

        String currentUserId = getCurrentUserId();

        // Accept
        btnAccept.setOnClickListener(v -> {
            if (event == null || event.getEventId() == null || currentUserId == null) return;
            NotificationActionsHelper.acceptInvitation(context, currentUserId, event.getEventId(), event.getName());
            remove(event);
            notifyDataSetChanged();
        });

        // Cancel / Decline
        btnCancel.setOnClickListener(v -> {
            if (event == null || event.getEventId() == null || currentUserId == null) return;
            NotificationActionsHelper.declineInvitation(context, currentUserId, event.getEventId(), event.getName(), lotteryManager);
            remove(event);
            notifyDataSetChanged();
        });

        // Leave waiting list
        btnLeave.setOnClickListener(v -> {
            if (event == null || event.getEventId() == null || currentUserId == null) return;

            db.collection("waiting_lists")
                    .document(event.getEventId())
                    .collection("entrants")
                    .document(currentUserId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Left waiting list", Toast.LENGTH_SHORT).show();
                        remove(event);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error leaving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        return convertView;
    }
}

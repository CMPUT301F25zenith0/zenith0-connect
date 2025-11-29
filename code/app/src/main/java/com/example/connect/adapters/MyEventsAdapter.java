package com.example.connect.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyEventsAdapter extends ArrayAdapter<Event> {

    private static final String TAG = "MyEventsAdapter";

    // TAB CONSTANTS
    public static final int TAB_WAITLIST = 0;
    public static final int TAB_SELECTED = 1;
    public static final int TAB_CONFIRMED = 2;

    private int currentTabMode;
    private Context context;

    public MyEventsAdapter(Context context, List<Event> events, int tabMode) {
        super(context, 0, events);
        this.context = context;
        this.currentTabMode = tabMode;
    }

    public void setTabState(int tabMode) {
        this.currentTabMode = tabMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_event_card, parent, false);
        }

        // Bind Data
        TextView title = convertView.findViewById(R.id.tv_event_title);
        TextView time = convertView.findViewById(R.id.tv_event_time);
        TextView price = convertView.findViewById(R.id.tv_event_price);
        ImageView eventImage = convertView.findViewById(R.id.iv_event_image);

        if (event != null) {
            title.setText(event.getName());
            time.setText(event.getDateTime() != null ? event.getDateTime() : "TBD");
            price.setText(event.getPrice() != null ? "$" + event.getPrice() : "Free");

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

        // Button Logic
        MaterialButton btnAccept = convertView.findViewById(R.id.btn_accept);
        MaterialButton btnCancel = convertView.findViewById(R.id.btn_cancel);

        if (currentTabMode == TAB_SELECTED) {
            // SELECTED: Accept or Decline
            btnAccept.setVisibility(View.VISIBLE);
            btnAccept.setText("Accept");

            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Decline");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));

        } else if (currentTabMode == TAB_CONFIRMED) {
            // CONFIRMED: Can only Cancel Attendance
            btnAccept.setVisibility(View.GONE);

            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Cancel");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));

        } else {
            // WAITLIST: Can only Leave List
            btnAccept.setVisibility(View.GONE);

            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Leave List");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));
        }

        // 3. Click Listeners
        btnAccept.setOnClickListener(v -> {
            // TODO: Update status to 'confirmed' in DB
        });

        btnCancel.setOnClickListener(v -> {
            // TODO: Update status in DB
        });

        return convertView;
    }
}
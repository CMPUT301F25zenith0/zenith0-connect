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
    private FirebaseFirestore db; // Added instance variable for efficiency

    public MyEventsAdapter(Context context, List<Event> events, int tabMode) {
        super(context, 0, events);
        this.context = context;
        this.currentTabMode = tabMode;
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore once
    }

    public void setTabState(int tabMode) {
        this.currentTabMode = tabMode;
        notifyDataSetChanged();
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

            String formatterPrice = priceFormat(event.getPrice());
            price.setText(formatterPrice);

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
        MaterialButton btnLeave = convertView.findViewById(R.id.btn_leave);

        // Reset visibility first to avoid recycling issues
        btnAccept.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnLeave.setVisibility(View.GONE);

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
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText("Cancel");
            btnCancel.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnCancel.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));

        } else {
            // WAITLIST: Can only Leave List
            btnLeave.setVisibility(View.VISIBLE);

            btnLeave.setText("Leave List");
            btnLeave.setTextColor(ContextCompat.getColor(context, R.color.mist_pink));
            btnLeave.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mist_pink)));
        }

        // --- Click Listeners ---

        btnAccept.setOnClickListener(v -> {
            // TODO: Update status to 'confirmed' in DB
        });

        btnCancel.setOnClickListener(v -> {
            // TODO: Update status in DB
        });

        // LEAVE WAITLIST LOGIC
        btnLeave.setOnClickListener(v -> {
            if (event == null || event.getEventId() == null) return;

            // Get current user ID from Firebase Auth
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = (currentUser != null) ? currentUser.getUid() : null;

            if (userId == null) {
                Toast.makeText(context, "Please sign in to leave the waiting list", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventId = event.getEventId();

            // Check if user is in the waiting list
            db.collection("waiting_lists")
                    .document(eventId)
                    .collection("entrants")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(context, "You're not on the waiting list", Toast.LENGTH_SHORT).show();
                            // Optional: Remove from UI anyway since they aren't in DB
                            remove(event);
                            notifyDataSetChanged();
                            return;
                        }

                        // Remove user from waiting list
                        db.collection("waiting_lists")
                                .document(eventId)
                                .collection("entrants")
                                .document(userId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Left waiting list", Toast.LENGTH_SHORT).show();

                                    // Remove the item from the Adapter's list and refresh UI
                                    remove(event);
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error leaving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error checking status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return convertView;
    }
}
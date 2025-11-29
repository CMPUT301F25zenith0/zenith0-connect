package com.example.connect.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.connect.R;
import com.example.connect.models.Event;
import com.example.connect.utils.LocationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            // Join waitlist directly without navigating to event details
            joinWaitingList(event.getEventId());
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

    /**
     * Adds the current (logged in) user to the event's waiting list in Firestore.
     * Checks total_capacity (waiting list limit) before adding to prevent exceeding the limit.
     * If total_capacity is null or 0, allows unlimited entries.
     */
    private void joinWaitingList(String eventId) {
        if (eventId == null) {
            Toast.makeText(context, "Error: Event ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user ID from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(context, "Please sign in to join the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if user is the organizer first
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if user is the organizer
                    String organizerId = eventDoc.getString("organizer_id");
                    if (organizerId != null && organizerId.equals(userId)) {
                        Toast.makeText(context, "Organizers cannot join their own event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // US 02.02.02: Check if event requires geolocation
                    Boolean requireGeo = eventDoc.getBoolean("require_geolocation");
                    final boolean needsLocation = requireGeo != null && requireGeo;

                    // Check waiting list capacity
                    db.collection("waiting_lists")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(waitingListDoc -> {
                                // Get total_capacity from waiting_lists collection
                                final Long totalCapacity = waitingListDoc.exists()
                                        ? waitingListDoc.getLong("total_capacity")
                                        : null;

                                // Check if user already in waiting list
                                db.collection("waiting_lists")
                                        .document(eventId)
                                        .collection("entrants")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener(entrantDoc -> {
                                            if (entrantDoc.exists()) {
                                                Toast.makeText(context, "You're already on the waiting list",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Count ALL entrants for capacity check
                                            db.collection("waiting_lists")
                                                    .document(eventId)
                                                    .collection("entrants")
                                                    .get()
                                                    .addOnSuccessListener(querySnapshot -> {
                                                        int currentSize = querySnapshot.size();

                                                        // Only check limit if total_capacity is set and > 0
                                                        // null or 0 = unlimited waiting list
                                                        if (totalCapacity != null && totalCapacity > 0
                                                                && currentSize >= totalCapacity) {
                                                            Toast.makeText(context,
                                                                    "Waiting list is full (" + totalCapacity + " entrants)",
                                                                    Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        // All checks passed - add user to waiting list
                                                        // US 02.02.02: Capture location if required
                                                        if (needsLocation) {
                                                            captureLocationAndAdd(eventId, userId, totalCapacity);
                                                        } else {
                                                            addUserToWaitingList(eventId, userId, totalCapacity, null, null);
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Error checking waiting list: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error checking your status: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error accessing waiting list: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error accessing event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Captures location and then adds user to waiting list
     */
    private void captureLocationAndAdd(String eventId, String userId, Long totalCapacity) {
        LocationHelper locationHelper = new LocationHelper(context);
        
        // Check if permission is already granted
        if (locationHelper.hasLocationPermission()) {
            // Permission already granted, get location
            locationHelper.getLastLocation((latitude, longitude) -> {
                if (latitude != null && longitude != null) {
                    Log.d("EventAdapter", "Location captured: " + latitude + ", " + longitude);
                    addUserToWaitingList(eventId, userId, totalCapacity, latitude, longitude);
                } else {
                    Toast.makeText(context, "Unable to get location. Please enable location services.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Permission not granted - navigate to EventDetails where permission can be properly requested
            Toast.makeText(context, "Location permission required. Opening event details...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, com.example.connect.activities.EventDetails.class);
            intent.putExtra("EVENT_ID", eventId);
            context.startActivity(intent);
        }
    }

    /**
     * Helper method to add user to waiting list subcollection.
     * Ensures waiting list document exists before adding entrant.
     * US 02.02.02: Includes location data if provided.
     */
    private void addUserToWaitingList(String eventId, String userId, Long totalCapacity, Double latitude, Double longitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ensure the waiting list document exists
        Map<String, Object> waitingListData = new HashMap<>();
        waitingListData.put("event_id", eventId);
        waitingListData.put("created_at", FieldValue.serverTimestamp());
        waitingListData.put("total_capacity", totalCapacity); // Preserve capacity

        db.collection("waiting_lists")
                .document(eventId)
                .set(waitingListData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Now add user to entrants subcollection
                    Map<String, Object> entrantData = new HashMap<>();
                    entrantData.put("user_id", userId);
                    entrantData.put("status", "waiting");
                    entrantData.put("joined_date", FieldValue.serverTimestamp());
                    
                    // US 02.02.02: Add location data if available
                    if (latitude != null && longitude != null) {
                        entrantData.put("latitude", latitude);
                        entrantData.put("longitude", longitude);
                        entrantData.put("location_captured_at", FieldValue.serverTimestamp());
                    }

                    db.collection("waiting_lists")
                            .document(eventId)
                            .collection("entrants")
                            .document(userId)
                            .set(entrantData)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(context, "Joined waiting list", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error joining: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error creating waiting list: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
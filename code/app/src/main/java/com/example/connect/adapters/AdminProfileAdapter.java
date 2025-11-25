package com.example.connect.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final OnProfileClickListener profileClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(User user);
    }

    public interface OnProfileClickListener {
        void onProfileClick(User user);
    }

    public AdminProfileAdapter(OnDeleteClickListener deleteListener, OnProfileClickListener profileClickListener) {
        this.deleteListener = deleteListener;
        this.profileClickListener = profileClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProfile;
        private final TextView tvEventName;
        private final TextView tvEventOrganizer;
        private final TextView tvEventDate;
        private final Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventOrganizer = itemView.findViewById(R.id.tv_event_organizer);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(User user) {
            // Set user name
            tvEventName.setText(user.getName() != null ? user.getName() : "Unknown User");

            // Set user email
            tvEventOrganizer.setText(user.getEmail() != null ? user.getEmail() : "");

            // Set user ID or other info
            tvEventDate.setText(user.getUserId() != null ? "ID: " + user.getUserId() : "");

            // Load profile image from Base64 string
            if (!TextUtils.isEmpty(user.getProfileImageUrl())) {
                try {
                    Bitmap bitmap = decodeBase64ToBitmap(user.getProfileImageUrl());
                    if (bitmap != null) {
                        ivProfile.setImageBitmap(bitmap);
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } catch (Exception e) {
                    // If decoding fails, use placeholder
                    ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                // Use placeholder if no image data
                ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> profileClickListener.onProfileClick(user));
            btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(user));
        }

        /**
         * Decodes a Base64 string to a Bitmap
         * @param base64String The Base64 encoded string
         * @return Decoded Bitmap or null if decoding fails
         */
        private Bitmap decodeBase64ToBitmap(String base64String) {
            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
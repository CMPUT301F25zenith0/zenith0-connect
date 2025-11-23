package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connect.R;
import com.example.connect.models.User;

import java.util.List;

/**
 * RecyclerView adapter for displaying user profiles in the admin profile list.
 * 
 * @author Zenith Team
 * @version 1.0
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    private List<User> profiles;
    private OnProfileClickListener listener;

    /**
     * Interface for handling profile item clicks.
     */
    public interface OnProfileClickListener {
        void onProfileClick(User profile);
    }

    /**
     * Constructs a new AdminProfileAdapter.
     * 
     * @param profiles List of user profiles to display
     * @param listener Listener for profile item clicks
     */
    public AdminProfileAdapter(List<User> profiles, OnProfileClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User profile = profiles.get(position);
        holder.bind(profile, listener);
    }

    @Override
    public int getItemCount() {
        return profiles != null ? profiles.size() : 0;
    }

    /**
     * ViewHolder for profile items.
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvEmail;
        private TextView tvPhone;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProfileName);
            tvEmail = itemView.findViewById(R.id.tvProfileEmail);
            tvPhone = itemView.findViewById(R.id.tvProfilePhone);
        }

        void bind(User profile, OnProfileClickListener listener) {
            tvName.setText(profile.getName() != null ? profile.getName() : "Unknown");
            tvEmail.setText(profile.getEmail() != null ? profile.getEmail() : "No email");
            tvPhone.setText(profile.getPhone() != null && !profile.getPhone().isEmpty() 
                    ? profile.getPhone() : "No phone");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProfileClick(profile);
                }
            });
        }
    }
}


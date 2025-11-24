package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connect.R;
import com.example.connect.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnProfileDeleteListener deleteListener;

    public interface OnProfileDeleteListener {
        void onDelete(User user);
    }

    public AdminProfileAdapter(OnProfileDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
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
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvRole;
        private final ShapeableImageView ivProfilePic;
        private final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            ivProfilePic = itemView.findViewById(R.id.iv_profile_pic);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(User user) {
            String name = user.getName() != null ? user.getName() : "";
            tvName.setText(name.trim().isEmpty() ? "Unknown User" : name.trim());
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");

            // Determine role (simplified logic)
            // In a real app, you might check specific flags or collections
            tvRole.setText("User ID: " + user.getUserId());

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(ivProfilePic);
            } else {
                ivProfilePic.setImageResource(R.drawable.ic_profile_placeholder);
            }

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(user);
                }
            });
        }
    }
}

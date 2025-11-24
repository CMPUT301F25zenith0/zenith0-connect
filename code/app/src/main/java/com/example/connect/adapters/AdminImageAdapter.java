package com.example.connect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connect.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ViewHolder> {

    // Helper class to represent an image item
    public static class ImageItem {
        public String id;
        public String url;
        public String type; // "Event Poster" or "Profile Picture"
        public String relatedId; // Event ID or User ID

        public ImageItem(String id, String url, String type, String relatedId) {
            this.id = id;
            this.url = url;
            this.type = type;
            this.relatedId = relatedId;
        }
    }

    private List<ImageItem> images = new ArrayList<>();
    private final OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onDelete(ImageItem image);
    }

    public AdminImageAdapter(OnImageDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem image = images.get(position);
        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvType;
        private final TextView tvId;
        private final MaterialButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvType = itemView.findViewById(R.id.tv_image_type);
            tvId = itemView.findViewById(R.id.tv_image_id);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(ImageItem image) {
            tvType.setText(image.type);
            tvId.setText("ID: " + image.relatedId);

            Glide.with(itemView.getContext())
                    .load(image.url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivImage);

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(image);
                }
            });
        }
    }
}

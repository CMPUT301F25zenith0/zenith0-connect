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

/**
 * RecyclerView adapter for displaying and managing images in the admin panel.
 * Supports both event posters and profile pictures with delete functionality.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ViewHolder> {

    /**
     * Represents an image item to be displayed in the admin panel.
     */
    public static class ImageItem {
        public String id;
        public String url;
        public String type; // "Event Poster" or "Profile Picture"
        public String relatedId; // Event ID or User ID
        public String displayName;

        /**
         * Creates a new ImageItem.
         *
         * @param id Unique identifier for the image
         * @param url Image URL or Base64 string
         * @param type Type of image
         * @param relatedId Related entity ID
         * @param displayName Display name for the image
         */
        public ImageItem(String id, String url, String type, String relatedId, String displayName) {
            this.id = id;
            this.url = url;
            this.type = type;
            this.relatedId = relatedId;
            this.displayName = displayName;
        }
    }

    private List<ImageItem> images = new ArrayList<>();
    private final OnImageDeleteListener deleteListener;
    private final OnImageClickListener clickListener;

    public interface OnImageDeleteListener {
        void onDelete(ImageItem image);
    }

    public interface OnImageClickListener {
        void onImageClick(ImageItem image);
    }

    public AdminImageAdapter(OnImageDeleteListener deleteListener, OnImageClickListener clickListener) {
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
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

    /**
     * ViewHolder for displaying individual image items.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvType;
        private final TextView tvId;
        private final MaterialButton btnDelete;

        /**
         * Creates a new ViewHolder.
         *
         * @param itemView The view for this ViewHolder
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvType = itemView.findViewById(R.id.tv_image_type);
            tvId = itemView.findViewById(R.id.tv_image_id);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        /**
         * Binds an ImageItem to this ViewHolder's views.
         * Loads the image using Glide from either a URL or Base64 string.
         *
         * @param image The ImageItem to display
         */
        public void bind(ImageItem image) {
            if (image.displayName != null && !image.displayName.isEmpty()) {
                tvType.setText(image.displayName);
            } else {
                tvType.setText(image.type);
            }
            tvId.setText("ID: " + image.relatedId);

            if (image.url != null && (image.url.startsWith("http") || image.url.startsWith("https"))) {
                // Load URL
                Glide.with(itemView.getContext())
                        .load(image.url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(ivImage);
            } else if (image.url != null) {
                // Try to load as Base64
                try {
                    byte[] decodedString = android.util.Base64.decode(image.url, android.util.Base64.DEFAULT);
                    Glide.with(itemView.getContext())
                            .load(decodedString)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.stat_notify_error)
                            .into(ivImage);
                } catch (Exception e) {
                    ivImage.setImageResource(android.R.drawable.stat_notify_error);
                }
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(image);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onImageClick(image);
                }
            });
        }
    }
}

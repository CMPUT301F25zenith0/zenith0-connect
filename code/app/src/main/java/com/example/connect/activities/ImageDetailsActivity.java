package com.example.connect.activities;

import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.connect.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.Instant;

public class ImageDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ImageView ivFullImage = findViewById(R.id.iv_full_image);

        if (toolbar != null) {
            String title = getIntent().getStringExtra("image_title");
            if (title != null && !title.isEmpty()) {
                toolbar.setTitle(title);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        String imageUrl = getIntent().getStringExtra("image_url");
        String imageBase64 = getIntent().getStringExtra("image_base64");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivFullImage);
        } else if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(ivFullImage);
            } catch (Exception e) {
                Toast.makeText(this, "Error decoding image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image data found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

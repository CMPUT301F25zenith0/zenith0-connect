package com.example.connect.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;
import com.google.android.material.button.MaterialButton;

public class MapActivity extends AppCompatActivity {

    private MaterialButton btnBack, btnZoomIn, btnZoomOut;
    private TextView tvEntrantsInView, tvWithinZone, tvOutsideZone;

    private int zoomLevel = 10; // just a dummy number for demo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // VERY IMPORTANT: point to your XML EXACTLY
        setContentView(R.layout.activity_extrant_map_view);

        // Sanity signal so we know this Activity actually launched
        Toast.makeText(this, "MapActivity opened (placeholder map)", Toast.LENGTH_SHORT).show();

        // Bind views by IDs that exist in your XML
        btnBack = findViewById(R.id.btnBack);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        tvEntrantsInView = findViewById(R.id.tvEntrantsInView);
        tvWithinZone = findViewById(R.id.tvWithinZone);
        tvOutsideZone = findViewById(R.id.tvOutsideZone);

        // Back -> finish this screen
        btnBack.setOnClickListener(v -> finish());

        // Dummy zoom handlers (no Google map attached)
        btnZoomIn.setOnClickListener(v -> {
            zoomLevel++;
            Toast.makeText(this, "Zoom: " + zoomLevel, Toast.LENGTH_SHORT).show();
        });
        btnZoomOut.setOnClickListener(v -> {
            zoomLevel = Math.max(1, zoomLevel - 1);
            Toast.makeText(this, "Zoom: " + zoomLevel, Toast.LENGTH_SHORT).show();
        });

        // Put some demo stats so you can see updates on screen
        tvEntrantsInView.setText("Entrants In View:  12");
        tvWithinZone.setText("Within Allowed Zone:  9");
        tvOutsideZone.setText("Outside Zone:  3");
    }
}


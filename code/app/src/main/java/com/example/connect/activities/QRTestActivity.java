package com.example.connect.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.connect.R;

/**
 * Test activity for QR code generation
 */
public class QRTestActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private EditText eventIdEditText;
    private Button generateQrButton;
    private Button generateColoredQrButton;
    private TextView qrDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_test);

        // Initialize views
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        eventIdEditText = findViewById(R.id.eventIdEditText);
        generateQrButton = findViewById(R.id.generateQrButton);
        generateColoredQrButton = findViewById(R.id.generateColoredQrButton);
        qrDataTextView = findViewById(R.id.qrDataTextView);

        // Set default event ID for quick testing
        eventIdEditText.setText("event_12345");

        // Generate standard QR code --> going to use specific event id from database
        generateQrButton.setOnClickListener(v -> {
            String eventId = eventIdEditText.getText().toString().trim();

            if (TextUtils.isEmpty(eventId)) {
                Toast.makeText(this, "Please enter an Event ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate QR code
            Bitmap qrCode = QRCodeCreation.generateEventQRCode(eventId);

            if (qrCode != null) {
                qrCodeImageView.setImageBitmap(qrCode);

                // Display the encoded data
                String qrData = QRCodeCreation.generateEventQRCodeData(eventId);
                qrDataTextView.setText("QR Code Data: " + qrData);

                Toast.makeText(this, "QR Code generated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        // Generate colored QR code
        generateColoredQrButton.setOnClickListener(v -> {
            String eventId = eventIdEditText.getText().toString().trim();

            if (TextUtils.isEmpty(eventId)) {
                Toast.makeText(this, "Please enter an Event ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate colored QR code (blue on white)
            Bitmap coloredQrCode = QRCodeCreation.generateColoredEventQRCode(
                    eventId,
                    400,
                    400,
                    Color.parseColor("#1976D2"), // Blue
                    Color.WHITE
            );

            if (coloredQrCode != null) {
                qrCodeImageView.setImageBitmap(coloredQrCode);

                // Display the encoded data
                String qrData = QRCodeCreation.generateEventQRCodeData(eventId);
                qrDataTextView.setText("QR Code Data: " + qrData);

                Toast.makeText(this, "Colored QR Code generated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
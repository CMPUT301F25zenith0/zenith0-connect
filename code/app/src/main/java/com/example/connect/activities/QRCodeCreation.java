package com.example.connect.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeCreation {

    /**
     * Generates a QR code bitmap for an event
     * @param eventId The unique identifier for the event
     * @param width The width of the QR code in pixels
     * @param height The height of the QR code in pixels
     * @return Bitmap of the QR code, which can be saved into the event. OR null if generation fails
     */
    public static Bitmap generateEventQRCode(String eventId, int width, int height) {
        try {
            // Create the event signup URL
            // Modify this URL to match your app's deep link or web URL structure
            String eventSignupUrl = "myapp://event/signup?eventId=" + eventId;
            // Alternative for web URL: "https://yourapp.com/event/signup?eventId=" + eventId

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    eventSignupUrl,
                    BarcodeFormat.QR_CODE,
                    width,
                    height
            );

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a QR code with default size (400x400)
     * @param eventId The unique identifier for the event
     * @return Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateEventQRCode(String eventId) {
        return generateEventQRCode(eventId, 400, 400);
    }

    /**
     * Generates a QR code with custom colors
     * @param eventId The unique identifier for the event
     * @param width The width of the QR code in pixels
     * @param height The height of the QR code in pixels
     * @param foregroundColor The color of the QR code pattern
     * @param backgroundColor The background color
     * @return Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateColoredEventQRCode(String eventId, int width, int height,
                                                    int foregroundColor, int backgroundColor) {
        try {
            String eventSignupUrl = "myapp://event/signup?eventId=" + eventId;

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(eventSignupUrl, BarcodeFormat.QR_CODE, width, height);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? foregroundColor : backgroundColor);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a QR code as a String (for storing in database)
     * @param eventId The unique identifier for the event
     * @return The URL string that the QR code encodes
     */
    public static String generateEventQRCodeData(String eventId) {
        return "myapp://event/signup?eventId=" + eventId;
    }
}

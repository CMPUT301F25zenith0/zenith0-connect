package com.example.connect.activities;

import androidx.appcompat.app.AppCompatActivity;

public class QRCodeScanner extends AppCompatActivity {

    // Rough Planning / Skeleton

    // Need to Decide how the QR code implementation is going to work in the first place --> Google has some libraries we can pull from
        // QR code either has custom URL scheme OR a direct universal app link
        // Direct universal app link, which gives us a name
        // Using name of the Event we can find and display it
    // This linking needs to be done at the creation of the URl
    // So need to complete the URL creation first


    // Need to figure out how to use the androids camera --> So Camera Permissions implementation
        // FOUND** Android QR scanner
            // MLkit or ZXing
            // Flutter?


// Theory code from  --> https://www.youtube.com/watch?v=jtT60yFPelI
    // public void scanCode() {
    // ScanOptions options = new Scanoptions();
    // options.setPrompt("Volume up to flash on");
    // options.setBeepEnabled(True);
    // options.setOrientationLocked(true),
    // options.setCaptureActivity(CaptureAct.class) --> Create the CaptureAct Class


    // https://github.com/hackstarsj/Android_Barcode_Generator_Scanner
    // Code / Tutorial for QR scanner and camera permissions




}

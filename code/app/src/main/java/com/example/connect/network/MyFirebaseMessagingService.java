package com.example.connect.network;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.connect.utils.NotificationTokenManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received: " + remoteMessage.getData());
        // TODO: show notification in UI
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);
        // Save token to Firestore
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            NotificationTokenManager.updateToken();
        }

    }
}
package com.example.connect.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationTokenManager {

    private static final String TAG = "NotificationTokenManager";

    public static void updateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    saveTokenToFirestore(token);
                });
    }

    private static void saveTokenToFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated"))
                .addOnFailureListener(e -> Log.w(TAG, "Token update failed", e));
    }
}
//package com.example.connect.utils;
//
//import android.util.Log;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.messaging.FirebaseMessaging;
//
///**
// * This file is deprecated, it was originally being used to manage the firebase cloud messages tokens that connect it with the database
// * however this required backend server to fully work
// */
//public class NotificationTokenManager {
//
//    public static void updateToken() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            Log.w("FCM", "No user logged in, skipping token update.");
//            return;
//        }
//
//        FirebaseMessaging.getInstance().getToken()
//                .addOnSuccessListener(token -> {
//                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    FirebaseFirestore.getInstance()
//                            .collection("accounts") // your Firestore collection
//                            .document(uid)
//                            .update("fcmToken", token)
//                            .addOnSuccessListener(aVoid ->
//                                    Log.d("FCM", "Token saved: " + token))
//                            .addOnFailureListener(e ->
//                                    Log.e("FCM", "Failed to save token", e));
//                })
//                .addOnFailureListener(e -> Log.e("FCM", "Failed to get token", e));
//    }
//}
package com.example.connect.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.connect.utils.LotteryManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationActionsHelper {

    // Decline invitation
    public static void declineInvitation(Context context, String userId, String eventId, String eventName, LotteryManager lotteryManager) {
        if (userId == null || eventId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "canceled");
        updates.put("canceled_date", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Invitation declined successfully", Toast.LENGTH_SHORT).show();

                    // Trigger replacement lottery
                    if (lotteryManager != null) {
                        lotteryManager.performReplacementLottery(eventId, eventName, 1, new LotteryManager.LotteryCallback() {
                            @Override
                            public void onSuccess(int selectedCount, int waitingListCount) {}
                            @Override
                            public void onFailure(String error) {}
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to decline: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Accept invitation
    public static void acceptInvitation(Context context, String userId, String eventId, String eventName) {
        if (userId == null || eventId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "enrolled");
        updates.put("enrolled_date", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("waiting_lists")
                .document(eventId)
                .collection("entrants")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Invitation accepted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to accept: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

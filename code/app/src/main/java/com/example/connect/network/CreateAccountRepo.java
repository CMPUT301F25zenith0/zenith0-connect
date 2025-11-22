package com.example.connect.network;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class handling Firebase operations for account creation.
 * Separates business logic from UI for better testability.
 */
public class CreateAccountRepo {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public CreateAccountRepo(FirebaseAuth auth, FirebaseFirestore firestore) {
        this.mAuth = auth;
        this.db = firestore;
    }

    /**
     * Interface for callbacks from registration operations
     */
    public interface RegistrationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Registers a new user with Firebase Auth and saves profile to Firestore
     *
     * @param email User's email
     * @param password User's password
     * @param fullName User's full name
     * @param displayName User's display name
     * @param mobileNumber User's mobile number (optional)
     * @param callback Callback for success/failure
     */
    public void registerUser(String email, String password, String fullName,
                             String displayName, String mobileNumber,
                             RegistrationCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserToDatabase(user.getUid(), fullName, displayName,
                                email, mobileNumber, callback);
                    } else {
                        callback.onFailure("Error creating account");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Account creation failed: " + e.getMessage());
                });
    }


    /**
     * Saves user profile data to Firestore
     */
    private void saveUserToDatabase(String userId, String fullName, String displayName,
                                    String email, String mobileNumber,
                                    RegistrationCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("full_name", fullName);
        user.put("display_name", displayName);
        user.put("email", email);
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            user.put("mobile_num", mobileNumber);
        }
        user.put("created_at", System.currentTimeMillis());
        
        // Initialize activity tracking fields
        long currentTimestamp = System.currentTimeMillis();
        user.put("is_active", false); // New users start as inactive until they log in
        user.put("last_active_timestamp", currentTimestamp);

        db.collection("accounts").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to save user data: " + e.getMessage());
                });
    }
}
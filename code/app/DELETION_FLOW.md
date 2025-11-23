# Profile Deletion Flow - Complete Process

## Overview
This document explains the complete flow when an admin clicks the "Delete" button on a user profile.

---

## Step-by-Step Flow

### 1️⃣ **User Clicks Delete Button**
**Location:** `item_admin_profile.xml` → `btnDeleteProfile` button
**Code:** `AdminProfileAdapter.java` → `btnDelete.setOnClickListener()`

```java
btnDelete.setOnClickListener(v -> {
    if (listener != null) {
        listener.onProfileDelete(profile);  // Calls the listener
    }
});
```

**What happens:**
- Delete button is clicked on a profile card
- The click listener triggers `onProfileDelete()` method

---

### 2️⃣ **Adapter Calls Activity Method**
**Location:** `AdminProfileAdapter.java` → `OnProfileClickListener` interface
**Code:** `AdminProfileListActivity.java` → `setupRecyclerView()`

```java
profileAdapter = new AdminProfileAdapter(filteredProfiles, new OnProfileClickListener() {
    @Override
    public void onProfileDelete(User profile) {
        confirmDeleteProfile(profile);  // Shows confirmation dialog
    }
});
```

**What happens:**
- Adapter calls the listener's `onProfileDelete()` method
- This triggers `confirmDeleteProfile()` in the Activity

---

### 3️⃣ **Confirmation Dialog Appears**
**Location:** `AdminProfileListActivity.java` → `confirmDeleteProfile()`

```java
private void confirmDeleteProfile(User profile) {
    new android.app.AlertDialog.Builder(this)
        .setTitle("Delete Profile")
        .setMessage("Are you sure you want to delete " + profile.getName() + "'s profile? " +
                "This will permanently remove all user data including events (if organizer), " +
                "waiting lists, and other related information. This action cannot be undone.")
        .setPositiveButton("Delete", (dialog, which) -> deleteProfile(profile))
        .setNegativeButton("Cancel", null)
        .show();
}
```

**What happens:**
- Shows a confirmation dialog with warning message
- User can click "Delete" to confirm or "Cancel" to abort
- If "Delete" is clicked, calls `deleteProfile(profile)`

---

### 4️⃣ **Check if User is Organizer**
**Location:** `AdminProfileListActivity.java` → `deleteProfile()`

```java
private void deleteProfile(User profile) {
    String userIdToDelete = profile.getUserId();
    
    // Check if user has created events (is organizer)
    db.collection("events")
        .whereEqualTo("org_name", userIdToDelete)
        .limit(1)
        .get()
        // ... also checks organizer_id field
        // Then calls proceedWithDeletion()
}
```

**What happens:**
- Validates the user ID
- Checks if user is an organizer by querying events collection
- Checks both `org_name` and `organizer_id` fields
- Calls `proceedWithDeletion()` with organizer status

---

### 5️⃣ **Delete Account from Firestore**
**Location:** `AdminProfileListActivity.java` → `proceedWithDeletion()` → `deleteAccountAndAuth()`

```java
private void proceedWithDeletion(String userIdToDelete, User profile, boolean isOrganizer) {
    // Delete account first for immediate feedback
    deleteAccountAndAuth(userIdToDelete, profile);
    
    // Cascade deletion in background
    cascadeDeleteUserData(userIdToDelete, isOrganizer, () -> {
        Log.d("AdminProfileList", "✅ Background cascade deletion completed");
    });
}
```

**What happens:**
- Calls `deleteAccountAndAuth()` to delete from Firestore immediately
- Starts cascade deletion in background (doesn't block UI)

---

### 6️⃣ **Delete from Firestore & Verify**
**Location:** `AdminProfileListActivity.java` → `deleteAccountAndAuth()`

```java
private void deleteAccountAndAuth(String userIdToDelete, User profile) {
    // 1. Check if document exists
    db.collection("accounts").document(userIdToDelete).get()
        .addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 2. Delete the document
                db.collection("accounts").document(userIdToDelete).delete()
                    .addOnSuccessListener(aVoid -> {
                        // 3. Verify deletion
                        db.collection("accounts").document(userIdToDelete).get()
                            .addOnSuccessListener(verifySnapshot -> {
                                if (!verifySnapshot.exists()) {
                                    Log.d("AdminProfileList", "✅ Verified: Account deleted");
                                }
                                
                                // 4. Delete from Firebase Auth
                                deleteFirebaseAuthUser(userIdToDelete);
                                
                                // 5. Update UI
                                removeFromLocalLists(userIdToDelete);
                                Toast.makeText(this, "Profile deleted successfully", ...);
                            });
                    });
            }
        });
}
```

**What happens:**
1. ✅ Checks if account document exists
2. ✅ Deletes document from `accounts` collection
3. ✅ Verifies deletion was successful
4. ✅ Calls `deleteFirebaseAuthUser()` to delete from Authentication
5. ✅ Removes from local lists and updates UI

---

### 7️⃣ **Delete from Firebase Authentication**
**Location:** `AdminProfileListActivity.java` → `deleteFirebaseAuthUser()`

```java
private void deleteFirebaseAuthUser(String userId) {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    
    if (auth.getCurrentUser().getUid().equals(userId)) {
        // User deleting themselves - use client SDK
        auth.getCurrentUser().delete()...
    } else {
        // Admin deleting another user - use Cloud Function
        callDeleteUserCloudFunction(userId);
    }
}
```

**What happens:**
- If user is deleting themselves: Uses Firebase Auth client SDK
- If admin is deleting another user: Calls Cloud Function `deleteUser`

---

### 8️⃣ **Call Cloud Function (if needed)**
**Location:** `AdminProfileListActivity.java` → `callDeleteUserCloudFunction()`

```java
private void callDeleteUserCloudFunction(String userId) {
    Map<String, Object> data = new HashMap<>();
    data.put("uid", userId);
    
    functions.getHttpsCallable("deleteUser")
        .call(data)
        .addOnSuccessListener(result -> {
            Log.d("AdminProfileList", "✅ User deleted from Authentication");
            Toast.makeText(this, "User deleted from Authentication", ...);
        })
        .addOnFailureListener(e -> {
            // Handle errors (function not deployed, permission denied, etc.)
        });
}
```

**What happens:**
- Calls Firebase Cloud Function `deleteUser`
- Function uses Admin SDK to delete user from Authentication
- Shows success/error message based on result

---

### 9️⃣ **Cascade Deletion (Background)**
**Location:** `AdminProfileListActivity.java` → `cascadeDeleteUserData()`

```java
private void cascadeDeleteUserData(String userIdToDelete, boolean isOrganizer, Runnable onComplete) {
    // 1. Remove user from waiting_lists collection
    removeUserFromWaitingLists(userIdToDelete, checkCompletion);
    
    // 2. Remove user from events/{eventId}/waitingList subcollections
    removeUserFromEventWaitingLists(userIdToDelete, checkCompletion);
    
    // 3. If organizer, delete all their events
    if (isOrganizer) {
        deleteOrganizerEvents(userIdToDelete, checkCompletion);
    }
    
    // 4. Remove from other collections
    removeUserFromOtherCollections(userIdToDelete, checkCompletion);
}
```

**What happens (runs in background):**
1. ✅ Removes user ID from all `waiting_lists` documents
2. ✅ Deletes user documents from all `events/{eventId}/waitingList` subcollections
3. ✅ If organizer: Deletes all events created by the user
4. ✅ Cleans up other related data

---

## Complete Flow Diagram

```
[User Clicks Delete Button]
         ↓
[Adapter: onProfileDelete()]
         ↓
[Activity: confirmDeleteProfile()]
         ↓
[Confirmation Dialog Shows]
         ↓
[User Clicks "Delete" in Dialog]
         ↓
[Activity: deleteProfile()]
         ↓
[Check if User is Organizer]
         ↓
[Activity: proceedWithDeletion()]
         ↓
    ┌─────────────────┐
    │                 │
    ↓                 ↓
[Delete from      [Cascade Deletion
 Firestore]       (Background)]
    ↓
[Verify Deletion]
    ↓
[Delete from Auth]
    ↓
[Call Cloud Function]
    ↓
[Update UI]
    ↓
[Show Success Toast]
```

---

## What Gets Deleted

### ✅ **Always Deleted:**
1. Account document from `accounts` collection (Firestore)
2. User from Firebase Authentication (via Cloud Function)
3. User ID from `waiting_lists` collection
4. User documents from `events/{eventId}/waitingList` subcollections

### ✅ **If User is Organizer:**
5. All events created by the user
6. All subcollections of those events (waitingList, etc.)
7. Corresponding `waiting_lists` documents for those events

---

## Error Handling

- **Invalid User ID:** Shows error toast, stops deletion
- **Document Not Found:** Logs warning, still updates UI
- **Cloud Function Not Deployed:** Shows warning, deletes from Firestore only
- **Permission Denied:** Shows error, logs details
- **Network Errors:** Shows error message, logs full exception

---

## Logging

All steps are logged with tags:
- `AdminProfileList` - Main activity logs
- `✅` - Success indicators
- `❌` - Error indicators
- `⚠️` - Warning indicators

Check Logcat for detailed flow tracking.


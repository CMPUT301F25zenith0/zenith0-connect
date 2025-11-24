# 🚨 CRITICAL: Deploy Cloud Function for Auth Deletion

## Why This Is Needed

When an admin deletes a user, the user is removed from **Firestore** but **NOT from Firebase Authentication** because the Cloud Function is not deployed yet.

**The Cloud Function MUST be deployed for Auth deletion to work!**

---

## Quick Setup (5-10 minutes)

### Prerequisites
- Node.js installed (download from https://nodejs.org/)
- Firebase project access

### Step 1: Install Firebase CLI
Open PowerShell/Command Prompt and run:
```bash
npm install -g firebase-tools
```

### Step 2: Login to Firebase
```bash
firebase login
```
This will open a browser for authentication.

### Step 3: Navigate to Project Root
```bash
cd C:\Users\hp\AndroidStudioProjects\zenith0-connect\code
```

### Step 4: Initialize Firebase Functions
```bash
firebase init functions
```

**When prompted:**
- ✅ Use an existing project → Select your Firebase project
- ✅ Language: **JavaScript**
- ✅ ESLint: **No** (or Yes if you want)
- ✅ Install dependencies: **Yes**

This will create a `functions` folder.

### Step 5: Copy Function Code

**Copy the entire content from `cloud-function-deleteUser.js`** and replace the content in:
```
functions/index.js
```

The file should contain:
```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

exports.deleteUser = functions.https.onCall(async (data, context) => {
  // ... (full code from cloud-function-deleteUser.js)
});
```

### Step 6: Deploy the Function
```bash
firebase deploy --only functions:deleteUser
```

**Expected output:**
```
✔  functions[deleteUser(us-central1)] Successful create operation.
✔  Deploy complete!
```

---

## Verify Deployment

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Functions** → You should see `deleteUser` listed
4. Try deleting a user from the app
5. Check Firebase Console → **Authentication** → **Users** → The user should be deleted!

---

## Testing

After deployment, delete a user and check **Logcat** in Android Studio:

✅ **Success:**
```
✅✅✅ Cloud Function returned: {success=true, message=...}
✅✅✅ User [USER_ID] removed from Authentication -> Users
```

❌ **If function not deployed:**
```
⚠️⚠️⚠️ Cloud Function 'deleteUser' NOT DEPLOYED!
```

---

## Troubleshooting

### "Function not found" error
- ✅ Make sure you ran: `firebase deploy --only functions:deleteUser`
- ✅ Check Firebase Console → Functions to see if `deleteUser` is listed
- ✅ Wait 1-2 minutes after deployment (functions take time to propagate)

### "Permission denied" error
- ✅ Verify your admin account has `admin: true` in Firestore `accounts` collection
- ✅ Check Firebase Console → Functions → `deleteUser` → Logs for errors

### Function deployed but still not working
- ✅ Check Firebase Console → Functions → `deleteUser` → Logs
- ✅ Verify the function code matches `cloud-function-deleteUser.js`
- ✅ Make sure you're using the correct Firebase project

### "firebase: command not found"
- ✅ Make sure Node.js is installed: `node --version`
- ✅ Reinstall Firebase CLI: `npm install -g firebase-tools`

---

## Need Help?

1. Check **Firebase Console** → **Functions** to see deployed functions
2. Check **Logcat** in Android Studio for detailed error messages
3. Check **Firebase Console** → **Functions** → **Logs** for server-side errors

---

## After Deployment

Once deployed, when an admin deletes a user:
1. ✅ User deleted from **Firestore** `accounts` collection
2. ✅ User deleted from **Firebase Authentication**
3. ✅ All related data cascade deleted (events, waiting lists, etc.)

**The user will be completely removed from your Firebase project!**


# Quick Deploy Guide - Delete User Cloud Function

## ⚠️ IMPORTANT: The Cloud Function MUST be deployed for Auth deletion to work!

If you see users still in Firebase Authentication after deleting them, it means the Cloud Function is not deployed yet.

## Quick Setup (5 minutes)

### Step 1: Install Firebase CLI
```bash
npm install -g firebase-tools
```

### Step 2: Login
```bash
firebase login
```

### Step 3: Initialize Functions (if not done)
```bash
firebase init functions
```
- Select your project: **ZenithConnect**
- Language: **JavaScript**
- Install dependencies: **Yes**

### Step 4: Copy Function Code

Copy the entire content from `cloud-function-deleteUser.js` and paste it into:
```
functions/index.js
```

Your `functions/index.js` should look like:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

exports.deleteUser = functions.https.onCall(async (data, context) => {
  // ... (paste code from cloud-function-deleteUser.js)
});
```

### Step 5: Deploy
```bash
firebase deploy --only functions:deleteUser
```

### Step 6: Verify
After deployment, you should see:
```
✔  functions[deleteUser(us-central1)] Successful create operation.
```

## Testing

After deployment, try deleting a user again. Check Logcat for:
- `✅✅✅ Cloud Function call SUCCESSFUL!` - means it worked!
- `⚠️⚠️⚠️ Cloud Function 'deleteUser' NOT DEPLOYED!` - means function not found

## Troubleshooting

### "Function not found" error
- Make sure you deployed: `firebase deploy --only functions:deleteUser`
- Check Firebase Console → Functions to see if `deleteUser` is listed

### "Permission denied" error
- Verify your admin account has `admin: true` in Firestore `accounts` collection
- Check the function logs in Firebase Console

### Function deployed but still not working
- Check Firebase Console → Functions → deleteUser → Logs
- Look for error messages
- Verify the function code is correct

## Need Help?

1. Check Firebase Console → Functions to see deployed functions
2. Check Logcat in Android Studio for detailed error messages
3. Check Firebase Console → Functions → Logs for server-side errors


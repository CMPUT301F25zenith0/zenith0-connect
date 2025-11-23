# Cloud Function Setup for User Deletion

This guide explains how to set up the Cloud Function to delete users from Firebase Authentication.

## Prerequisites

1. Node.js installed (v14 or higher)
2. Firebase CLI installed
3. Firebase project with Functions enabled

## Setup Steps

### 1. Install Firebase CLI (if not already installed)

```bash
npm install -g firebase-tools
```

### 2. Login to Firebase

```bash
firebase login
```

### 3. Initialize Firebase Functions (if not already done)

Navigate to your project root directory and run:

```bash
firebase init functions
```

When prompted:
- Select your Firebase project
- Choose JavaScript (or TypeScript if you prefer)
- Install dependencies: Yes

### 4. Add the Delete User Function

Copy the code from `cloud-function-deleteUser.js` to your `functions/index.js` file.

Your `functions/index.js` should look like this:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

if (!admin.apps.length) {
  admin.initializeApp();
}

exports.deleteUser = functions.https.onCall(async (data, context) => {
  // ... (copy the code from cloud-function-deleteUser.js)
});
```

### 5. Install Dependencies

Navigate to the `functions` directory:

```bash
cd functions
npm install
```

### 6. Deploy the Function

From the project root:

```bash
firebase deploy --only functions:deleteUser
```

Or deploy all functions:

```bash
firebase deploy --only functions
```

### 7. Verify Deployment

After deployment, you should see output like:

```
✔  functions[deleteUser(us-central1)] Successful create operation.
Function URL: https://us-central1-YOUR-PROJECT.cloudfunctions.net/deleteUser
```

## Testing

You can test the function using the Firebase Console:
1. Go to Firebase Console → Functions
2. Click on `deleteUser` function
3. Use the "Test" tab to test with sample data:

```json
{
  "uid": "test-user-id"
}
```

## Security

The function checks:
1. User is authenticated
2. User has `admin: true` in their Firestore `accounts` document

Make sure your Firestore security rules allow admins to be identified:

```javascript
// Firestore Rules Example
match /accounts/{userId} {
  allow read: if request.auth != null && 
    (request.auth.uid == userId || 
     get(/databases/$(database)/documents/accounts/$(request.auth.uid)).data.admin == true);
}
```

## Troubleshooting

### Function not found error
- Make sure the function is deployed: `firebase deploy --only functions`
- Check the function name matches: `deleteUser`

### Permission denied
- Verify the caller has `admin: true` in their Firestore account document
- Check Firestore security rules allow reading the admin status

### User not found
- The user might already be deleted from Authentication
- Check the user ID is correct

## Notes

- The function uses Firebase Admin SDK which has full privileges
- Only authenticated admins can call this function
- The function logs all deletion attempts for audit purposes


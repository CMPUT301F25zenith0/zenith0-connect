/**
 * Firebase Cloud Function to delete a user from Firebase Authentication.
 * 
 * DEPLOYMENT INSTRUCTIONS:
 * 1. Install Firebase CLI: npm install -g firebase-tools
 * 2. Login: firebase login
 * 3. Initialize Functions (if not done): firebase init functions
 * 4. Copy this code to functions/index.js
 * 5. Deploy: firebase deploy --only functions
 * 
 * This function requires:
 * - Firebase Admin SDK (automatically available in Cloud Functions)
 * - The caller must be authenticated and have admin privileges in Firestore
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Admin SDK (automatically done in Cloud Functions)
if (!admin.apps.length) {
  admin.initializeApp();
}

/**
 * Cloud Function to delete a user from Firebase Authentication.
 * Only admins can call this function.
 * 
 * @param {string} data.uid - The user ID to delete
 * @param {object} context - Firebase callable function context
 * @returns {object} Success message
 */
exports.deleteUser = functions.https.onCall(async (data, context) => {
  // Verify user is authenticated
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to delete users'
    );
  }

  const callerUid = context.auth.uid;
  const uidToDelete = data.uid;

  if (!uidToDelete) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'User ID (uid) is required'
    );
  }

  // Verify caller is admin by checking Firestore
  try {
    const callerDoc = await admin.firestore()
      .collection('accounts')
      .doc(callerUid)
      .get();

    if (!callerDoc.exists) {
      throw new functions.https.HttpsError(
        'permission-denied',
        'Caller account not found'
      );
    }

    const callerData = callerDoc.data();
    const isAdmin = callerData && callerData.admin === true;

    if (!isAdmin) {
      throw new functions.https.HttpsError(
        'permission-denied',
        'Only admins can delete users'
      );
    }

    // Delete user from Firebase Authentication
    await admin.auth().deleteUser(uidToDelete);

    console.log(`User ${uidToDelete} deleted from Authentication by admin ${callerUid}`);

    return {
      success: true,
      message: `User ${uidToDelete} successfully deleted from Authentication`
    };

  } catch (error) {
    console.error('Error deleting user:', error);

    // Handle specific Firebase Auth errors
    if (error.code === 'auth/user-not-found') {
      throw new functions.https.HttpsError(
        'not-found',
        'User not found in Authentication'
      );
    }

    // Re-throw HttpsError as-is
    if (error instanceof functions.https.HttpsError) {
      throw error;
    }

    // Handle other errors
    throw new functions.https.HttpsError(
      'internal',
      'An error occurred while deleting the user: ' + error.message
    );
  }
});


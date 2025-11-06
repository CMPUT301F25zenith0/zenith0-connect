//package com.example.connect.network;
//
//import android.util.Log;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import com.example.connect.R;
//import com.example.connect.utils.NotificationTokenManager;
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.os.Build;
//
///**
// * This object is deprecated, was used alongside NotificationListenerService
// */
//public class MyFirebaseMessagingService extends FirebaseMessagingService {
//
//    private static final String CHANNEL_ID = "default";
//
//    @Override
//    public void onNewToken(String token) {
//        super.onNewToken(token);
//        Log.d("FCM", "New token: " + token);
//
//        // Call NotificationTokenManager to save it in Firestore
//        NotificationTokenManager.updateToken();
//    }
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        Log.d("MyFirebaseMsgService", "Message received: " + remoteMessage.getData());
//
//        if (remoteMessage.getNotification() != null) {
//            Log.d("FCM", "Notification title: " + remoteMessage.getNotification().getTitle());
//            Log.d("FCM", "Notification body: " + remoteMessage.getNotification().getBody());
//        }
//
//        Log.d("FCM", "Data payload: " + remoteMessage.getData());
//
//        // Use payload if present, otherwise use default test text
//        String title = "Test Notification";
//        String body = "You got a notification!";
//
//        if (remoteMessage.getNotification() != null) {
//            if (remoteMessage.getNotification().getTitle() != null)
//                title = remoteMessage.getNotification().getTitle();
//            if (remoteMessage.getNotification().getBody() != null)
//                body = remoteMessage.getNotification().getBody();
//        }
//
//        // Always show a notification, even if data is empty
//        showNotification(title, body);
//    }
//
//    public void showNotification(String title, String body) {
//        String CHANNEL_ID = "default";
//
//        // Create notification channel (required for Android 8+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Default Notifications",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
//
//        // Build notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.mipmap.ic_launcher) // must exist in res/mipmap
//                .setContentTitle(title)
//                .setContentText(body)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true);
//
//        // Show notification
//        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
//        manager.notify((int) System.currentTimeMillis(), builder.build());
//    }
//
//}
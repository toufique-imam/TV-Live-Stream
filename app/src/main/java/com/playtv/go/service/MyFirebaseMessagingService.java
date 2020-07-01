package com.playtv.go.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.playtv.go.LoginActivity;
import com.playtv.go.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "Play!TV GO";
    private static final String CHANNEL_NAME = "Play!TV GO";

    private void sendNotification(RemoteMessage remoteMessage) {
        Log.e("Msgbackground", remoteMessage.getData().toString());
        Map data = remoteMessage.getData();
        String title = (String) data.get("title");
        String body = (String) data.get("body");
        Intent resultIntent = new Intent(this, LoginActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(10)
                .setTicker(CHANNEL_NAME)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");
        notificationManager.notify(0, notificationBuilder.build());

    }

    private void sendNotification1(RemoteMessage remoteMessage) {
        Log.e("remoteMessage_OREO", remoteMessage.getData().toString());
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        Intent resultIntent = new Intent(this, LoginActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent);

        oreoNotification.getManager().notify(0, builder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotification1(remoteMessage);
        } else {
            Log.e("CAME HERE", remoteMessage.getData().toString());
            sendNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DeviceTokens");
        ref.child(token).setValue(false);
    }
}

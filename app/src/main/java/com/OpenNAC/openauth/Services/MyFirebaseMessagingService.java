package com.OpenNAC.openauth.Services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.OpenNAC.openauth.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "OpenAuth1";
    private static final String CHANNEL_NAME = "OCF1";
    private static final String CHANNEL_DESC = "OpenCloud Factory1";
    NotificationCompat.Action action, action2;
    Intent intent;
    PendingIntent pendingIntent;
   @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,
                   NotificationManager.IMPORTANCE_DEFAULT);
           channel.setDescription(CHANNEL_DESC);
           NotificationManager manager = getSystemService(NotificationManager.class);
           manager.createNotificationChannel(channel);
       }
       action = new NotificationCompat.Action(R.mipmap.ic_launcher, "YES",pendingIntent);
       action2 = new NotificationCompat.Action(R.mipmap.ic_launcher, "NO",pendingIntent);
       NotificationCompat.Builder mBuilder =
               new NotificationCompat.Builder(this, CHANNEL_ID)
                       .setSmallIcon(R.mipmap.ic_launcher_round)
                       .setContentTitle(remoteMessage.getNotification().getTitle())
                       .setContentText(remoteMessage.getNotification().getBody())
                       .addAction(action)
                       .addAction(action2)
                       .setPriority(NotificationCompat.PRIORITY_DEFAULT);
       NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
       notificationManagerCompat.notify(1,mBuilder.build());
    }

//
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        System.out.println("Token in Messaging service is" + token);
    }

}
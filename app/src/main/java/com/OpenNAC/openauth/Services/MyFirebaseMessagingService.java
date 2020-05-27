package com.OpenNAC.openauth.Services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.OpenNAC.openauth.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.security.SecureRandom;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "OpenAuth1";
    private static final String CHANNEL_NAME = "OCF1";
    private static final String CHANNEL_DESC = "OpenCloud Factory1";
    private static final String SHARED_PREFS = "sharedPrefs", TOKEN_TEXT = "token";
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
//       for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
//           String key = entry.getKey();
//           String value = entry.getValue();
//           Log.d(TAG, "key, " + key + " value " + value);
//       }
       String value = remoteMessage.getData().get(TOKEN_TEXT);
       SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
       SharedPreferences.Editor editor = sharedPreferences.edit();editor.putString(TOKEN_TEXT,value);
       editor.apply();

      // Log.d(TAG,"in fcm message value is: " + value);

       NotificationCompat.Builder mBuilder =
               new NotificationCompat.Builder(this, CHANNEL_ID)
                       .setSmallIcon(R.mipmap.ic_launcher_round)
                       .setContentTitle(remoteMessage.getNotification().getTitle())
                       .setContentText(remoteMessage.getNotification().getBody())
                       .setPriority(NotificationCompat.PRIORITY_DEFAULT);
       NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
       notificationManagerCompat.notify(createRandomCode(7),mBuilder.build());
    }
    public int createRandomCode(int codeLength) {
        char[] chars = "1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return Integer.parseInt(sb.toString());
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
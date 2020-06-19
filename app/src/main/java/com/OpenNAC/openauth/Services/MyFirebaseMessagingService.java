package com.OpenNAC.openauth.Services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.OpenNAC.openauth.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.security.SecureRandom;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID1 = "INFO_NOTIFICATION",
            CHANNEL_ID2 = "ANSWER_NOTIFICATION",
            NULL_ID = "NULL",
            EXTRA_ID = "EXTRA";
    private static final String CHANNEL_NAME1 = "App News",
            CHANNEL_NAME2 = "Important Notification",
            NULL_NAME = "NOT DEFINED",
            EXTRA_NAME = "EXTRA NAME";
    private static final String CHANNEL_DESC1 = "Notifications to show information and news about the application",
            CHANNEL_DESC2 = "Important notifications to ensure access to application resources",
            NULL_DESC = "NULL DESC",
            EXTRA_DESC = "EXTRA DESCRIPTION";
    private static final String SHARED_PREFS = "sharedPrefs",
            CHALLENGE_ID_EXTRA = "challengeId", ACCOUNT_NAME_EXTRA = "accountName",
            HOSTNAME_EXTRA = "hostname", MAC_ADDRESS_EXTRA = "macaddress",
            NET_DEVICE_IP_EXTRA = "netDeviceIp", POLICY_EXTRA = "policy",
            TIME_EXTRA = "time", LOCATION_EXTRA = "location", TITLE_EXTRA = "titles",
            BODY_EXTRA = "bodys";
    NotificationManagerCompat notificationManagerCompat;
    Intent intent = new Intent("com.OpenNAC.openauth_FCM-MESSAGE");
    //mostly background
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        //super.onMessageReceived(remoteMessage);
       createNotificationChannels();
       // get custom data in foreground


       //fore getting all custom data payload
//       for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
//           String key = entry.getKey();
//           String value = entry.getValue();
//           Log.d(TAG, "key, " + key + " value " + value);
//       }
       notificationManagerCompat = NotificationManagerCompat.from(this);
        createNotification(remoteMessage,remoteMessage.getNotification().getChannelId());
        remoteMessage.getNotification();
        //addIntent(intent, remoteMessage);
       }
       public void addIntent(Intent intent,RemoteMessage remoteMessage){
           if(remoteMessage.getData().size()>0) {
               String challengeId = remoteMessage.getData().get(CHALLENGE_ID_EXTRA);
               String accountName = remoteMessage.getData().get(ACCOUNT_NAME_EXTRA);
               String hostname = remoteMessage.getData().get(HOSTNAME_EXTRA);
               String macaddress = remoteMessage.getData().get(MAC_ADDRESS_EXTRA);
               String netDeviceIp = remoteMessage.getData().get(NET_DEVICE_IP_EXTRA);
               String policy = remoteMessage.getData().get(POLICY_EXTRA);
               String time = remoteMessage.getData().get(TIME_EXTRA);
               String location = remoteMessage.getData().get(LOCATION_EXTRA);
               String titles = remoteMessage.getData().get(TITLE_EXTRA);
               String bodys = remoteMessage.getData().get(BODY_EXTRA);
               intent.putExtra(CHALLENGE_ID_EXTRA, challengeId);
               intent.putExtra(ACCOUNT_NAME_EXTRA,accountName);
               intent.putExtra(HOSTNAME_EXTRA,hostname);
               intent.putExtra(MAC_ADDRESS_EXTRA,macaddress);
               intent.putExtra(NET_DEVICE_IP_EXTRA,netDeviceIp);
               intent.putExtra(POLICY_EXTRA,policy);
               intent.putExtra(TIME_EXTRA,time);
               intent.putExtra(LOCATION_EXTRA,location);
               intent.putExtra(TITLE_EXTRA,titles);
               intent.putExtra(BODY_EXTRA,bodys);
               LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
               localBroadcastManager.sendBroadcast(intent);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           }
       }
   public void createNotificationChannels(){
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           //OpenAuth1
           NotificationChannel channel = new NotificationChannel(CHANNEL_ID1,CHANNEL_NAME1,
                   NotificationManager.IMPORTANCE_DEFAULT);
           channel.setDescription(CHANNEL_DESC1);
           NotificationManager manager = getSystemService(NotificationManager.class);
           manager.createNotificationChannel(channel);
           //OpenAuth1
           //OpenAuth2
           NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID2,CHANNEL_NAME2,
                   NotificationManager.IMPORTANCE_DEFAULT);
           channel2.setDescription(CHANNEL_DESC2);
           manager.createNotificationChannel(channel2);
           //OpenAuth2
           //null channel
           NotificationChannel channel3 = new NotificationChannel(NULL_ID,NULL_NAME,
                   NotificationManager.IMPORTANCE_DEFAULT);
           channel3.setDescription(NULL_DESC);
           manager.createNotificationChannel(channel3);
           //null channel
           //extra channel
           NotificationChannel channel4 = new NotificationChannel(EXTRA_ID,EXTRA_NAME,
                   NotificationManager.IMPORTANCE_DEFAULT);
           channel4.setDescription(EXTRA_DESC);
           manager.createNotificationChannel(channel4);
       }
   }
   //kind of foreground
   public void createNotification(RemoteMessage remoteMessage, String channel_id){
       System.out.println("fcm cannel id is"+ channel_id);
            if(channel_id.equals(CHANNEL_ID1)){
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this,CHANNEL_ID1)
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(remoteMessage.getNotification().getTitle())
                                .setContentText(remoteMessage.getNotification().getBody())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                notificationManagerCompat.notify(createRandomCode(7), mBuilder.build());
            }
           else if(channel_id.equals(CHANNEL_ID2)){
                addIntent(intent, remoteMessage);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 1,intent,PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this,CHANNEL_ID2)
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(remoteMessage.getNotification().getTitle())
                                .setContentText(remoteMessage.getNotification().getBody())
                                .setAutoCancel(true)
                                .setContentIntent(contentIntent)
                                .setPriority(NotificationCompat.PRIORITY_HIGH);
                notificationManagerCompat.notify(createRandomCode(7), mBuilder.build());
            }
           else if(channel_id.equals(EXTRA_ID)) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this,EXTRA_ID)
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(remoteMessage.getNotification().getTitle())
                                .setContentText(remoteMessage.getNotification().getBody())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                notificationManagerCompat.notify(createRandomCode(7), mBuilder.build());
            }
            else{
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this,NULL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(remoteMessage.getNotification().getTitle())
                                .setContentText(remoteMessage.getNotification().getBody())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                notificationManagerCompat.notify(createRandomCode(7), mBuilder.build());
            }

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
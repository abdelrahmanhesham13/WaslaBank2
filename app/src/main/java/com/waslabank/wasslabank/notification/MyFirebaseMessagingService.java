package com.waslabank.wasslabank.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.waslabank.wasslabank.MyDailyRidesActivity;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.SplashActivity;
import com.waslabank.wasslabank.utils.Helper;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> notificationMessage = remoteMessage.getData();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "0");


        mBuilder.setSmallIcon(R.drawable.ic_notification_image);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(notificationMessage.get("body")));
        mBuilder.setContentTitle(notificationMessage.get("title"));
        mBuilder.setContentText(notificationMessage.get("body"));
        mBuilder.setChannelId("0");
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setDefaults(0);
        mBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.plucky));
        int color = ContextCompat.getColor(this, android.R.color.white);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setColor(color);
            mBuilder.setSmallIcon(R.drawable.ic_notification_image);
        } else {
            mBuilder.setSmallIcon(R.drawable.ic_notification_image);
        }
        Intent resultIntent = new Intent(this, SplashActivity.class);
        Log.d("gotoooooooooooo", "ooo");

        if (notificationMessage.containsKey("targetScreen")) {
            Log.d("gotoooooooooooo", "ooo"+notificationMessage.get("targetScreen"));

            if (notificationMessage.get("targetScreen").equals("chat")) {
                resultIntent = new Intent(this, SplashActivity.class)
                        .putExtra("chat_id",notificationMessage.get("chat_id"))
                        .putExtra("request_id",notificationMessage.get("request_id"))
                        .putExtra("goToChat", true);
            } else if (notificationMessage.get("targetScreen").equals("offer")){
                Log.d("gotoooooooooooo", "onMessageReceived: +notification +++ "+notificationMessage.get("targetScreen"));
                resultIntent = new Intent(this, SplashActivity.class).putExtra("goToNotification", true);

            }else if(notificationMessage.get("targetScreen").equals("request")) {
                Log.d("gotoooooooooooo", "onMessageReceived: +notification");
              //  resultIntent = new Intent(this, SplashActivity.class).putExtra("goToNotification", true);
                resultIntent= new Intent(this, MyDailyRidesActivity.class);

            } else {
                 resultIntent = new Intent(this, SplashActivity.class).putExtra("goToNotification", true);
                // resultIntent = new Intent(this, SplashActivity.class);
            }
        }
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SplashActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("0", "test", NotificationManager.IMPORTANCE_HIGH);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);


        int random = (int) System.currentTimeMillis();
        if (mNotificationManager != null) {
            mNotificationManager.notify(random, mBuilder.build());
        }

    }
}

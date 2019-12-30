package com.waslabank.wasslabank.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.waslabank.wasslabank.ConfirmRideRequest;
import com.waslabank.wasslabank.GroupChatActivity;
import com.waslabank.wasslabank.R;
import com.waslabank.wasslabank.SplashActivity;
import com.waslabank.wasslabank.models.MyRideModel;
import com.waslabank.wasslabank.models.NotificationModel;
import com.waslabank.wasslabank.models.RideModel;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    Connector connector;
    MyRideModel myRideModel;
    RideModel rideModel;

    Intent resultIntent;

    NotificationCompat.Builder mBuilder;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        connector = new Connector(this, new Connector.LoadCallback() {
            @Override
            public void onComplete(String tag, String response) {
                if (Connector.checkStatus(response)) {
                    myRideModel = Connector.getMyRequest(response,MyFirebaseMessagingService.this);
                    if (myRideModel.getUserId().equals(Helper.getUserSharedPreferences(MyFirebaseMessagingService.this).getId())) {
                        resultIntent= new Intent(MyFirebaseMessagingService.this, ConfirmRideRequest.class).putExtra("type","show")
                                .putExtra("from_id",myRideModel.getFromId())
                                .putExtra("user_id",myRideModel.getUserId())
                                .putExtra("request",myRideModel)
                                .putExtra("Status",myRideModel.getStatus())
                                .putExtra("RequestID",myRideModel.getId());
                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyFirebaseMessagingService.this);
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
                    } else {
                        rideModel = Connector.getRequest(response);

                        resultIntent = new Intent(MyFirebaseMessagingService.this, ConfirmRideRequest.class)
                                .putExtra("type", "offer")
                                .putExtra("Status", rideModel.getStatus())
                                .putExtra("RequestID", rideModel.getId())
                                .putExtra("user_id", rideModel.getUserId())
                                .putExtra("from_id", rideModel.getFromId())
                                .putExtra("notification", new NotificationModel("323","3232","accept_offer",rideModel.getUserId(),"gopgge","geopt",rideModel.getStatus(),rideModel.getId(),Helper.getUserSharedPreferences(MyFirebaseMessagingService.this).getId(),"mreg",rideModel.getUser()));

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyFirebaseMessagingService.this);
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
            }
        }, new Connector.ErrorCallback() {
            @Override
            public void onError(VolleyError error) {

            }
        });

        Map<String, String> notificationMessage = remoteMessage.getData();

        mBuilder = new NotificationCompat.Builder(this, "0");


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
         resultIntent = new Intent(this, SplashActivity.class);

        if (notificationMessage.containsKey("targetScreen")) {
            Helper.writeToLog(notificationMessage.get("targetScreen"));

            if (notificationMessage.get("targetScreen").equals("chat")) {
                resultIntent = new Intent(this, SplashActivity.class)
                        .putExtra("chat_id",notificationMessage.get("chat_id"))
                        .putExtra("request_id",notificationMessage.get("request_id"))
                        .putExtra("goToChat", true);
            } else if (notificationMessage.get("targetScreen").equals("offer")){
                resultIntent = new Intent(this, SplashActivity.class).putExtra("goToNotification", true);

            }  else if(notificationMessage.get("targetScreen").equals("request")) {
                connector.getRequest("TAg","https://www.cta3.com/waslabank/api/get_request?id=" + notificationMessage.get("request_id"));
            }  else if (notificationMessage.get("targetScreen").equals("group_message")) {
                Intent intent = new Intent("groupMessage");
                intent.putExtra("user_id",notificationMessage.get("user_id"));
                intent.putExtra("chat_id",notificationMessage.get("chat_id"));
                if (!LocalBroadcastManager.getInstance(this).sendBroadcast(intent)) {
                    sendGroupNotification(notificationMessage.get("chat_id"));
                    return;
                }
            } else {
                 resultIntent = new Intent(this, SplashActivity.class).putExtra("goToNotification", true);
            }
        }

        if (!notificationMessage.get("targetScreen").equals("request")) {
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

    private void sendGroupNotification(String chatId) {
        mBuilder = new NotificationCompat.Builder(this, "0");


        mBuilder.setSmallIcon(R.drawable.ic_notification_image);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("New Group Message"));
        mBuilder.setContentTitle("Group Message");
        mBuilder.setContentText("New Group Message");
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
        resultIntent = new Intent(this, GroupChatActivity.class).putExtra("group_id",chatId);


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

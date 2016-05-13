package com.dropininc.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.utils.Constants;
import com.dropininc.utils.Logs;

import org.json.JSONObject;

public class GcmIntentService extends IntentService {

    private static int NOTIFICATION_ID = 0;
    private NotificationManager mNotificationManager;

    GcmIntentService(String name) {
        super(name);
    }

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logs.log("GcmIntentService", "onHandleIntent");
//        Bundle extras = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        String messageType = gcm.getMessageType(intent);
////        InstanceID instanceID = InstanceID.getInstance(this);
////        try {
////            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
////                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
////            Logs.log("GcmIntentService", token);
////        } catch (Exception e) {
////            Logs.log(e);
////        }
//        if (!extras.isEmpty()) {
//            Logs.log("GcmIntentService", "extras not isEmpty()");
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
////				sendNotification("Send error: " + extras.toString());
//                Logs.log("GcmIntentService", "MESSAGE_TYPE_SEND_ERROR");
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
////				sendNotification("Deleted messages on server: "
////						+ extras.toString());
//                // If it's a regular GCM message, do some work.
//                Logs.log("GcmIntentService", "MESSAGE_TYPE_DELETED");
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//                Logs.log("GcmIntentService", "MESSAGE_TYPE_MESSAGE");
//                // Post notification of received message.
//                sendNotification(extras);
//                Logs.log("GcmIntentService", "Received: " + extras.toString());
//            }else{
//                Logs.log("GcmIntentService", "Received: " + extras.toString());
//            }
//        }else{
//            Logs.log("GcmIntentService", "extras.isEmpty()");
//        }

        parseData(intent);
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseData(Intent intent) {
        Bundle dataBundle = intent.getBundleExtra("data");
        if (dataBundle != null) {
            try {
                Log.d("GcmIntentService", "NotificationTable title: " + dataBundle.getString("title"));
                Log.d("GcmIntentService", "Is Your App Active: " + dataBundle.getBoolean("isActive"));
                Log.d("GcmIntentService", "data additionalData: " + dataBundle.getString("custom"));
                JSONObject customJSON = new JSONObject(dataBundle.getString("custom"));
                if (customJSON.has("a")) {
                    JSONObject additionalData = customJSON.getJSONObject("a");
                    if (additionalData.has("yourCustomKey"))
                        Log.i("OneSignalExample", "additionalData:yourCustomKey: " + additionalData.getString("yourCustomKey"));
                }
//                sendNotification(dataBundle);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void sendNotification(Bundle extras) {
        if (extras.containsKey("code")) {
            int code = Integer.parseInt(extras.getString(NotificationKey.CODE));
            Logs.log("GcmIntentService", "new message with code " + code);
            String message = "";
            boolean isShow = false;
            Intent data = new Intent();
            data.setAction(Constants.NOTIFICATION_FILTER);
            data.putExtras(extras);
            if (extras.containsKey("message")) message = extras.getString("message");
            if (!AppApplication.getInstance().getRunning()) {
                if (!TextUtils.isEmpty(message)) {
                    showNotification(extras, message);
                }
            } else {
                sendBroadcast(data);
            }
        }
    }

    private void showNotification(Bundle bundle, String msg) {
        try {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this).setSmallIcon(getNotificationIcon())
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setContentText(msg);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(Intent.ACTION_MAIN);
            resultIntent.setClass(this, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            resultIntent.putExtras(bundle);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of
            // the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            mBuilder.setVibrate(new long[]{1000, 1000});
            mBuilder.setLights(Color.BLUE, 3000, 3000);
            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(notificationSound);
//            mBuilder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
            mNotificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            NOTIFICATION_ID += 1;
            Logs.log("NOTIFICATION_ID " + NOTIFICATION_ID);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private int getNotificationIcon() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_notification_ll : R.drawable.ic_launcher;
    }
}

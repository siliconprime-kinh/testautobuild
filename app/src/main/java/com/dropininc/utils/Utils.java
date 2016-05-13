package com.dropininc.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.NotificationCode;
import com.dropininc.location.LocationAlarmReceiver;
import com.dropininc.sharepreference.DSharePreference;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by Tam Mai
 */
public class Utils {
    public static final String IMAGE_PATH = "Dropin";


    public static String getImagePath(Context context, Intent data) {
        Uri uri = data.getData();
        String imagePath = "";

        final String[] columns = {MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME};

        Cursor cursor = context.getContentResolver().query(uri, columns, null,
                null, null);
        if (cursor == null || !cursor.moveToFirst())
            return imagePath;

        int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        if (columnIndex == -1) {
            columnIndex = cursor
                    .getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (columnIndex == -1)
                imagePath = "";
        } else {
            imagePath = cursor.getString(columnIndex);
        }
        cursor.close();

        return imagePath;
    }

    public static File getOutputMediaFile() {
        String sCurrentTime = String.valueOf(new Date().getTime());
        String filename = sCurrentTime + ".jpeg";
        File diskCacheDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_PATH);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        File photoFile = new File(diskCacheDir, filename);
        if (photoFile.exists()) {
            photoFile.delete();
        }

        return photoFile;
    }

    public static String getString(TextView textView) {
        return textView.getText().toString();
    }

    public static String formatStringDate(String time) {
        String finalDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date myDate = null;
        try {
            myDate = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, yyyy hh:mma", Locale.getDefault());
        finalDate = timeFormat.format(myDate);

        return finalDate;
    }

    public static String formatStringDate(String format, String time) {
        String finalDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date myDate = null;
        try {
            myDate = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        finalDate = timeFormat.format(myDate);

        return finalDate;
    }

    public static boolean getCameraCapabilities(Context ctx) {
        try {
            boolean allow = false;
            boolean isGet = DSharePreference.isGetCameraCapabilities(ctx);
            if (!isGet) {
                int numberOfCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Camera camera = Camera.open(i);
                        Camera.Parameters cameraParams = camera.getParameters();
                        for (int j = 0; j < cameraParams.getSupportedPictureSizes().size(); j++) {
                            int width = cameraParams.getSupportedPictureSizes().get(j).width;
                            int height = cameraParams.getSupportedPictureSizes().get(j).height;
                            Logs.log("Camera.CameraInfo", "Camera Size: " + width + " x " + height);
                            if (width > 850 && height > 480) {
                                allow = true;
                            }
                        }
                        camera.release();
                    }
                }
                DSharePreference.setCameraCapabilities(ctx, allow);
                DSharePreference.setIsGetCameraCapabilities(ctx, true);
            } else {
                allow = DSharePreference.isCameraCapabilities(ctx);
            }
            return allow;
        } catch (RuntimeException e) {
            Logs.log(e);
            return true;
        } catch (Exception e) {
            Logs.log(e);
            return false;
        }
    }

    public static void sendMessageSwitchModeToViewer(Context mContext, String accountId) {
        try {
            String messageId = UUID.randomUUID().toString();
            JSONObject message = new JSONObject();
            message.put("code", NotificationCode.SWITCH_MODE);
            message.put("accountId", accountId);
            message.put("messageId", messageId);

            Logs.log("PublishMessage", "Switch To Viewer Mode AccountId: " + accountId + " message:" + message);
            ((MainActivity) mContext).publishMessage(accountId, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareTwitter(Context context, String message, Uri myImageUri) {
        TweetComposer.Builder builder = new TweetComposer.Builder(context)
                .text(message);
        if (myImageUri != null)
            builder.image(myImageUri);
        builder.show();
    }

    public static void clearAllNotification(Context context) {
        // Clear all notification
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    /**
     * If the URL is mailto then return Intent with specific email
     *
     * @param url
     * @return
     */
    public static Intent getIntentFromUrl(String url) {
        Intent intent = null;
        if (url.startsWith("mailto:")) {
            String[] data = url.split(":");

            intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[]{data[1]});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        }

        return intent;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static void makeLogForLocation(Location location, float distance, boolean isSend) {
        String date = DateFormat.format("MM/dd/yyyy hh:mm:ss", new Date(System.currentTimeMillis())).toString();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                "dropin_location_log.txt");
        try {
            if (!file.exists()) file.createNewFile();
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.append(date);
            fileWriter.append(" : ");
            fileWriter.append("lat = ");
            fileWriter.append("" + location.getLatitude());
            fileWriter.append("; lng = ");
            fileWriter.append("" + location.getLongitude());
            fileWriter.append("; distance = " + distance);
            fileWriter.append("; accuracy = " + location.getAccuracy());
            fileWriter.append("; is send = " + isSend);
            fileWriter.append("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendLogsIntoEmail(Context context) {
        String filename = "dropin_location_log.txt";
        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {""};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public static void startLocationAlarm(Context context) {
        stopLocationAlarm(context);
        Logs.log("startLocationAlarm");

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LocationAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), 60 * 1000, alarmIntent);
    }

    public static void stopLocationAlarm(Context context) {
        Logs.log("stopLocationAlarm");

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LocationAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.cancel(alarmIntent);
    }

    public static boolean isConnectedMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static String getStringTimeFormat(long timestamp) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        final String timeString =
                new SimpleDateFormat("MMM dd, HH:mm aa").format(cal.getTime());
        return timeString;
    }
}

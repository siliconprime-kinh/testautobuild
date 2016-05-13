package com.dropininc.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dropininc.AppApplication;
import com.dropininc.interfaces.UserType;
import com.dropininc.services.LocationService;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Logs;


public class LocationAlarmReceiver extends BroadcastReceiver {

    private final String TAG = "LocationAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logs.log(TAG, "onReceive");
        if (DSharePreference.getUserType(context) == UserType.OPERATOR
                && !AppApplication.getInstance().isApplicationRunning()
                && !LocationService.isRunning) {
            Logs.log(TAG, "restarting service...");
            Intent locationIntent = new Intent(context, LocationService.class);
            locationIntent.putExtra(LocationService.MODE_EXTRA, LocationManager.MODE.SCHEDULE_MODE);
            context.startService(locationIntent);
        }
    }

}

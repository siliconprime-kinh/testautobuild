package com.dropininc.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.dropininc.AppApplication;
import com.dropininc.interfaces.LocationChanged;
import com.dropininc.location.LocationManager;
import com.dropininc.network.NetworkManager;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Logs;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by QA on 5/2/2016.
 */
public class LocationService extends Service {

    private final String TAG = "LocationService";

    public static final String LOCATION_FILTER = "LOCATION_FILTER";
    public static final String MODE_EXTRA = "MODE_EXTRA";
    public static final String GET_LOCATION_EXTRA = "GET_LOCATION_EXTRA";

    public static boolean isRunning = false;

    private Context mContext;
    private LocationManager mLocationManager;

    private Location lastLocation;
    private String mGigsId = "";
    private int mSettingRadius = 10;
    private int lastStartId = -1;

    private Intent locationIntent = new Intent(LOCATION_FILTER);

    @Inject
    protected NetworkManager networkManager;

    @Override
    public void onCreate() {
        super.onCreate();

        AppApplication.appComponent().inject(this);
        mContext = getApplicationContext();
        Logs.log(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!intent.hasExtra(MODE_EXTRA)) {
            Logs.log(TAG, "no mode set");

            stopSelf();
            return START_STICKY;
        }

        LocationManager.MODE mode = (LocationManager.MODE) intent.getSerializableExtra(MODE_EXTRA);

        Logs.log(TAG, "onStartCommand with mode - " + mode.name());
        //Log.d("KINH", "LocationService:onStart=" + mode+ ".name=" + mode.name());
        switch (mode) {
            case VIEWER_MODE:
            case DROPERATOR_MODE:
                startLocation(mode);
                break;

            case SCHEDULE_MODE:
                mGigsId = AppApplication.getInstance().getCurrentGigsId();
                mSettingRadius = DSharePreference.getSettingRadius(this);
                startLocation(mode);
                break;

            case STREAM_MODE:
                stopLocation();
                break;
        }

        if (lastStartId != -1) {
            stopSelf(lastStartId);
        }
        lastStartId = startId;
        isRunning = true;

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        //Log.e("KINH","LocationService:onDestroy");
        stopLocation();
        isRunning = false;

        Logs.log(TAG, "onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocation(LocationManager.MODE mode) {
        stopLocation();

        mLocationManager = new LocationManager(mContext, mode);
        mLocationManager.setLocationChanged(mLocationChanged);
        mLocationManager.startLocationUpdates();
    }

    private void stopLocation() {
        //Log.e("KINH","LocationService:stopLocation");
        if (mLocationManager != null) {
            mLocationManager.stopLocationUpdates();
            mLocationManager.destroy();
            mLocationManager = null;
        }
    }

    private LocationChanged mLocationChanged = new LocationChanged() {
        @Override
        public void onLocationChanged(Location location) {
            if (mLocationManager == null) return;
            switch (mLocationManager.getCurrentMode()) {
                case VIEWER_MODE:
                    processedViewerMode(location);
                    break;
                case DROPERATOR_MODE:
                    processedDroperatorMode(location);
                    break;

                case SCHEDULE_MODE:
                    processedScheduleMode(location);
                    break;
            }
        }
    };

    private void processedViewerMode(Location location) {
        processedDroperatorMode(location);
        stopSelf();
    }

    private void processedDroperatorMode(Location location) {
        locationIntent.putExtra(GET_LOCATION_EXTRA, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);
    }

    private void processedScheduleMode(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            Logs.log(TAG, "distance between two points is " + distance);
            if (distance >= 500) {
                sendLocationToServer(location);
                lastLocation = location;
            }
        } else lastLocation = location;
    }

    private void sendLocationToServer(Location location) {
        //Log.e("KINH", "BUGBUG>>sendLocationToServer.networkManager=" + (networkManager != null ? "OKOK" : "ERRORR"));
        /*
        https://fabric.io/dropininc/android/apps/com.dropininc/issues/57209c69ffcdc042501950f5
         */
        Logs.log(TAG, "sendLocationToServer");
        if (location != null && networkManager != null) {
            Observable<Object> t = networkManager.location(new LocationRequest(location.getLatitude() + "",
                    location.getLongitude() + "", "50", 100, true, "0", mSettingRadius + "", mGigsId));
            if( t!= null){
                t.subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("location" , networkManager.parseError(throwable));
                });
            }

        } else {
            Logs.log(TAG, "sendLocationToServer cann't process cause location NULL");
        }
    }

}

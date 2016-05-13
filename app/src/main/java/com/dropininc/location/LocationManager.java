package com.dropininc.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.dropininc.interfaces.LocationChanged;
import com.dropininc.utils.Logs;
import com.google.android.gms.location.LocationRequest;

    public class LocationManager implements LocationChanged {

    private final String TAG = "LocationManager";
    private static final int MAX_LOCATION_ACCURACY     = 100;

    public enum MODE { VIEWER_MODE, DROPERATOR_MODE, SCHEDULE_MODE, STREAM_MODE }

    private LocationChanged mLocationChanged;
    private MODE currentMode;

    private LocationDetector mLocationDetector;
    private Handler mHandler;

    private Location currentLocation;
    private Location scheduledLocation;


    public LocationManager(Context context, MODE mode) {
        currentMode = mode;

        int requestInterval = 1000;
        int locationPriority = LocationRequest.PRIORITY_LOW_POWER;

        switch (currentMode) {
            case VIEWER_MODE:
            case DROPERATOR_MODE:
                requestInterval = 10000;
                locationPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                break;

            case SCHEDULE_MODE:
                requestInterval = 1000;
                locationPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                mHandler = new Handler();
                break;
        }
        mLocationDetector = new LocationDetector(context, requestInterval, locationPriority);
        mLocationDetector.setLocationChanged(this);
    }

    public void setLocationChanged(LocationChanged locationChanged) {
        mLocationChanged = locationChanged;
    }

    public MODE getCurrentMode() {
        return currentMode;
    }

    public void startLocationUpdates() {
        Logs.log(TAG, "startLocationUpdates mode = " + currentMode);
        switch (currentMode) {
            case SCHEDULE_MODE:
                mRunnable45.run();

            case VIEWER_MODE:
            case DROPERATOR_MODE:
                mLocationDetector.init();
                break;
        }
    }

    public void stopLocationUpdates() {
        Logs.log(TAG, "stopLocationUpdates mode = " + currentMode);
        switch (currentMode) {
            case SCHEDULE_MODE:
                mHandler.removeCallbacks(mRunnable45);
                mHandler.removeCallbacks(mRunnable50);

            case VIEWER_MODE:
            case DROPERATOR_MODE:
                mLocationDetector.stopLocationUpdates();
                break;
        }
    }

    public void destroy() {
        if (mLocationDetector != null) {
            mLocationDetector.destroy();
            mLocationDetector = null;
        }
    }

    public double getLatitude() {
        return currentLocation != null ? currentLocation.getLatitude() : 0.0;
    }

    public double getLongitude() {
        return currentLocation != null ? currentLocation.getLongitude() : 0.0;
    }

    @Override
    public void onLocationChanged(Location location) {
        Logs.log(TAG, "Accuracy = " + location.getAccuracy());
        if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0
                && location.getAccuracy() <= MAX_LOCATION_ACCURACY) {

            currentLocation = location;

            switch (currentMode) {
                case SCHEDULE_MODE:
                    processedScheduleMode(location);
                    break;

                case VIEWER_MODE:
                case DROPERATOR_MODE:
                    if (mLocationChanged != null) {
                        mLocationChanged.onLocationChanged(location);
                    }
                    break;
            }
        }
    }

    private void processedScheduleMode(Location location) {
        if (scheduledLocation == null ||
                location.getAccuracy() < scheduledLocation.getAccuracy()) {
            scheduledLocation = location;
        }
    }

    private Runnable mRunnable45 = new Runnable() {
        @Override
        public void run() {
            Logs.log(TAG, "Start getting location");

            scheduledLocation = null;
            if (mLocationDetector.isDetectorReady()) mLocationDetector.startLocationUpdates();

            mHandler.postDelayed(mRunnable50, 45 * 1000);
        }
    };

    private Runnable mRunnable50 = new Runnable() {
        @Override
        public void run() {
            Logs.log(TAG, "Stop getting location");

            mLocationDetector.stopLocationUpdates();
            if (scheduledLocation != null && mLocationChanged != null) {
                mLocationChanged.onLocationChanged(scheduledLocation);
            }

            mHandler.postDelayed(mRunnable45, 50 * 1000);
        }
    };
}

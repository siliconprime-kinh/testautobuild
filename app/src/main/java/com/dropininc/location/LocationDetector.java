package com.dropininc.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.dropininc.interfaces.LocationChanged;
import com.dropininc.utils.Constants;
import com.dropininc.utils.Logs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationDetector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String TAG = "LocationDetector";
    private final Context mContext;
    private final int requestInterval;
    private final int locationPriority;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private LocationChanged mLocationChanged;

    public LocationDetector(Context context, int requestInterval, int locationPriority) {
        this.mContext = context;
        this.requestInterval = requestInterval;
        this.locationPriority = locationPriority;
    }

    public void init() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void startLocationUpdates() {
        if (isNeedRequestPermissions()) return;

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    public void destroy() {
        stopLocationUpdates();
       if (mGoogleApiClient != null) {
           mGoogleApiClient.disconnect();
           mGoogleApiClient = null;
       }
    }

    public void setLocationChanged(LocationChanged mLocationChanged) {
        this.mLocationChanged = mLocationChanged;
    }

    public boolean isDetectorReady() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Logs.log(TAG, "onConnected");

        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logs.log(TAG, "onConnectionSuspended " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logs.log(TAG, "onConnectionFailed = " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationChanged.onLocationChanged(location);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(requestInterval);
        mLocationRequest.setFastestInterval(requestInterval / 2);
        mLocationRequest.setPriority(locationPriority);

        startLocationUpdates();
    }

    private boolean isNeedRequestPermissions() {
        Logs.log(TAG, "isNeedRequestPermissions");
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setAction(Constants.GPS_PERMISSION_FILTER);
            mContext.sendBroadcast(intent);
            return true;
        }
        return false;
    }

}

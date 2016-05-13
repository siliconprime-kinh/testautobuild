package com.dropininc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dropininc.AppApplication;
import com.dropininc.utils.Logs;

/**
 * Created by Tam mai
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    private int currentState;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            // Connected
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Logs.log("ConnectionChangeReceiver","TYPE_WIFI");
                if(currentState != 1) {
 					/*kinh logNetWork*/
                    AppApplication.getInstance().logNetwork(networkInfo);
                    AppApplication.getInstance().connectPusher();
                }
                currentState = 1;
            } else {
                Logs.log("ConnectionChangeReceiver","TYPE_3G");
                if(currentState != 2) {
					/*kinh logNetWork*/
                    AppApplication.getInstance().logNetwork(networkInfo);
                    AppApplication.getInstance().connectPusher();
                }
                currentState = 2;
            }
        }else{
            Logs.log("ConnectionChangeReceiver","DISCONNECT");
			  if(currentState != 0){
                /*kinh logNetWork*/
                AppApplication.getInstance().logNetwork(networkInfo);
            }
            // Disconnect
            currentState = 0;
        }
    }
}

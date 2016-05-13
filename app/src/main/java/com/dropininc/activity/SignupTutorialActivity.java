package com.dropininc.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class SignupTutorialActivity extends BaseActivity {

    private GoogleCloudMessaging gcm;
    private String regid;

    int click = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_signup_tutorial);

        setupGCM();

        setupView();
    }

    private void setupView() {
        Button btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("Sign Up - Step", "Started");
                    mixpanel.track("Sign Up - Started", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }
                Intent intent = new Intent(SignupTutorialActivity.this, TermAndConditionActivity.class);
                startActivity(intent);
            }
        });
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("Log in - Started", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                Intent intent = new Intent(SignupTutorialActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        FontUtils.typefaceButton(btnSignup, FontType.LIGHT);
        FontUtils.typefaceButton(btnLogin, FontType.LIGHT);
        TextView splash_mess = (TextView) findViewById(R.id.splash_mess);
        FontUtils.typefaceTextView(splash_mess, FontType.REGULAR);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {
            click++;
            if (click == 3) {
                openDialogAPI();
                click = 0;
            }
        });
    }

    // ------- GCM
    private void setupGCM() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);
            Logs.log("GCM", "Registration ID: " + regid);
            if (TextUtils.isEmpty(regid)) {
                registerInBackground();
            }
        } else {
            Logs.log("GCM", "No valid Google Play Services APK found.");
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(SignupTutorialActivity.this);
                    }
                    regid = gcm.register(Constants.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    DSharePreference.setRegistrationId(SignupTutorialActivity.this, regid);
                    Log.d("GCM", regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Logs.log("GCMMessage", msg + "\n");
            }
        }.execute();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logs.log("checkPlayServices", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        String registrationId = DSharePreference.getRegistrationId(context);
        if (TextUtils.isEmpty(registrationId)) {
            Logs.log("GCM", "Registration not found.");
            return "";
        }
        return registrationId;
    }

    private void openDialogAPI() {
        final CharSequence[] items = {
                "http://apiqa2.dropininc.com/", "http://apidev2.dropininc.com/"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String api = items[item].toString();
                Log.d("API", "select API: " + api);
                DSharePreference.setDebugURL(SignupTutorialActivity.this, api);
                AppApplication.getInstance().initAppComponent();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

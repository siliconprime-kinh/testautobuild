package com.dropininc.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.VerifyModel;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import io.branch.referral.Branch;


public class SplashActivity extends BaseActivity {

    private int DELAY_TIME = 1500;

    public static String referralId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);

        TextView splash_mess = (TextView) findViewById(R.id.splash_mess);
        FontUtils.typefaceTextView(splash_mess, FontType.REGULAR);

        new Handler().postDelayed(() -> {
            if (TextUtils.isEmpty(DSharePreference.getAccessToken(SplashActivity.this))) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("ID", "Not logged");
                    mixpanel.track("App Open", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }
                if (DSharePreference.isFirstTimeUse(SplashActivity.this)) {
                    Intent intent = new Intent(SplashActivity.this, TutorialActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, SignupTutorialActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                try {
                    String profile = DSharePreference.getProfile(context);
                    VerifyModel model = new Gson().fromJson(profile, VerifyModel.class);
                    JSONObject props = new JSONObject();
                    props.put("ID", model.account.id);
                    mixpanel.track("App Open", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                if(ViewerStreamingActivity.isActive != DroperatorStreamingActivity.isActive){
                    if(ViewerStreamingActivity .isActive){
                        intent = new Intent(SplashActivity.this, ViewerStreamingActivity.class);
                    }

                    if(DroperatorStreamingActivity.isActive){
                        intent = new Intent(SplashActivity.this, DroperatorStreamingActivity.class);
                    }
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        }, DELAY_TIME);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                final String myPackageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(myPackageName))
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                else {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + myPackageName));
                }
            }
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.initSession((referringParams, error) -> {
            if (error == null) {
                // params are the deep linked params associated with the link that the user clicked before showing up
                Log.i("BranchConfig", "deep link data: " + referringParams.toString());
                try {
                    JSONObject jo = new JSONObject(referringParams.toString());
                    referralId = jo.optString("referralId", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}

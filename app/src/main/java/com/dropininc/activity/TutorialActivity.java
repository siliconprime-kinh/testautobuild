package com.dropininc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.dropininc.R;
import com.dropininc.adapter.TutorialAdapter;
import com.dropininc.interfaces.FontType;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.taptwo.android.widget.CircleFlowIndicator;
import org.taptwo.android.widget.FlowIndicator;
import org.taptwo.android.widget.ViewFlow;

public class TutorialActivity extends BaseActivity {

    Activity mActivity;

    ViewFlow viewFlow;
    CircleFlowIndicator indic;
    Button btnSkip;
    Button btnDontShowAgain;

    private int currentScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = TutorialActivity.this;
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_tutorial);
        setupView();

        loadTutorial();
    }

    private void setupView() {
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        indic = (CircleFlowIndicator) findViewById(R.id.circleFlowIndicator);
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnDontShowAgain = (Button) findViewById(R.id.btnDontShowAgain);
        btnDontShowAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("Screen", currentScreen + 1);
                    mixpanel.track("Tour - Don't Show Again", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }
                DSharePreference.setIsFirstTimeUse(mActivity, false);
                gotoNextScreen();
            }
        });
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("Screen", currentScreen + 1);
                    mixpanel.track("Tour - Skip", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }
                gotoNextScreen();
            }
        });

        // set font
        FontUtils.typefaceButton(btnSkip, FontType.LIGHT);
        FontUtils.typefaceButton(btnDontShowAgain, FontType.LIGHT);
    }

    private void gotoNextScreen() {
        if (TextUtils.isEmpty(DSharePreference.getAccessToken(mActivity))) {
            Intent intent = new Intent(mActivity, SignupTutorialActivity.class);
            startActivity(intent);
            finish();
        } else {

            Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
            if(ViewerStreamingActivity.isActive != DroperatorStreamingActivity.isActive){
                if(ViewerStreamingActivity .isActive){
                    intent = new Intent(TutorialActivity.this, ViewerStreamingActivity.class);
                }

                if(DroperatorStreamingActivity.isActive){
                    intent = new Intent(TutorialActivity.this, DroperatorStreamingActivity.class);
                }
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }
    }


    private void loadTutorial() {
        try {
            JSONArray arr = new JSONArray();
            arr.put(1);
            arr.put(2);
            arr.put(3);
            TutorialAdapter adapter = new TutorialAdapter(arr, mActivity);
            viewFlow.setAdapter(adapter);
            viewFlow.setFlowIndicator(indic);

            viewFlow.setOnViewSwitchListener(new FlowIndicator() {
                @Override
                public void setViewFlow(ViewFlow view) {

                }

                @Override
                public void onScrolled(int h, int v, int oldh, int oldv) {

                }

                @Override
                public void onSwitched(View view, int position) {
                    currentScreen = position;
                    try {
                        JSONObject props = new JSONObject();
                        props.put("Screen", position + 1);
                        mixpanel.track("Tour - Screen", props);
                    } catch (JSONException e) {
                        Logs.log(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

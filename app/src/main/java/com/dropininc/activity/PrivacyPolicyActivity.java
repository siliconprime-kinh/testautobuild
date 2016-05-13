package com.dropininc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.customview.CustomWebView;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Utils;

public class PrivacyPolicyActivity extends BaseActivity implements View.OnClickListener {

    private Activity mActivity;
    private Button btnSkip;
    private Button btnAccept;
    private Button btnDecline;
    private LinearLayout linear_buttom;
    private CustomWebView mWebView;

    private boolean keepScroll;

    private ProgressBar pb_loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = PrivacyPolicyActivity.this;
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_privacy_policy);
        setupView();

        loadContent();
    }

    private void setupView() {
        linear_buttom = (LinearLayout) findViewById(R.id.linear_buttom);
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnDecline = (Button) findViewById(R.id.btnDecline);
        btnAccept = (Button) findViewById(R.id.btnAccept);
        mWebView = (CustomWebView) findViewById(R.id.webView);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);

        btnSkip.setOnClickListener(this);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
        mWebView.setOnScrollListener(new CustomWebView.OnScrollListener() {
            @Override
            public void onScrollDown() {
//                if (!keepScroll) {
//                    btnSkip.setVisibility(View.GONE);
//                    linear_buttom.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onScrollUp() {
//                if (!keepScroll) {
//                    btnSkip.setVisibility(View.VISIBLE);
//                    linear_buttom.setVisibility(View.GONE);
//                }
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                pb_loading.setVisibility(View.GONE);
//                Handler lHandler = new Handler();
//                lHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mWebView.scrollTo(0, 10);
//                    }
//                }, 200);
//                if (!mWebView.isOnScrollChanged()) {
//                    btnSkip.setVisibility(View.GONE);
//                    linear_buttom.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                btnSkip.setVisibility(View.GONE);
//                linear_buttom.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = Utils.getIntentFromUrl(url);
                if (intent != null) {
                    startActivity(intent);
                    return true;
                }
                return false;
            }

        });

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        FontUtils.typefaceTextView(txtTitle, FontType.LIGHT);

        FontUtils.typefaceButton(btnSkip, FontType.LIGHT);
        FontUtils.typefaceButton(btnAccept, FontType.REGULAR);
        FontUtils.typefaceButton(btnDecline, FontType.LIGHT);
    }

    private void loadContent() {
        pb_loading.setVisibility(View.VISIBLE);
        mWebView.loadUrl(Constants.POLICY_URL);
//        mWebView.loadUrl("http://dropin.cloudapp.net/tos.html");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSkip:
                keepScroll = true;
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.pageDown(true);
                        keepScroll = false;
                    }
                });
                break;
            case R.id.btnAccept:
//                try {
//                    JSONObject props = new JSONObject();
//                    mixpanel.track("Sign Up - TOS Accepted", props);
//                } catch (Exception e) {
//                    Logs.log(e);
//                }
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.btnDecline:
//                try {
//                    JSONObject props = new JSONObject();
//                    mixpanel.track("Sign Up - TOS Declined", props);
//                } catch (Exception e) {
//                    Logs.log(e);
//                }
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}

package com.dropininc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.utils.Constants;
import com.dropininc.utils.Utils;


public class PrivacyFragment extends BaseFragment {
    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    private WebView mWebView;
    ProgressBar pb_loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.term_of_service));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_privacy, container, false);

        initView();
        loadContent();
        return mRootView;
    }

    private void initView() {
        mWebView = (WebView) mRootView.findViewById(R.id.webView);
        pb_loading = (ProgressBar) mRootView.findViewById(R.id.pb_loading);
        pb_loading.setVisibility(View.VISIBLE);
        mWebView.loadUrl(Constants.TOS_URL);
    }

    private void loadContent() {
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                pb_loading.setVisibility(View.GONE);
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
    }


}

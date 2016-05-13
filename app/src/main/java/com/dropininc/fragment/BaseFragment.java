package com.dropininc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View.OnClickListener;

import com.dropininc.AppApplication;
import com.dropininc.BaseSlidingFragmentActivity;
import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.BaseInterface;
import com.dropininc.network.NetworkManager;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import javax.inject.Inject;


public class BaseFragment extends Fragment implements BaseInterface {

    public static final String TAG = InboxFragment.class.getName();

    public Context mContext;
    private String mTitle = "";

    public MixpanelAPI mixpanel;

    @Inject
    protected NetworkManager networkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppApplication.appComponent().inject(this);

        mContext = getActivity();
//        mRequestQueue.getRequestQueue().start();
        mixpanel = MixpanelAPI.getInstance(getActivity(), com.dropininc.Constants.MIXPANEL_TOKEN);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    @Override
    public void showProgressDialog(String message,
                                   OnCancelListener onCancelListener) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showProgressDialog(message, onCancelListener);
        }

    }

    @Override
    public void hideProgressDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.hideProgressDialog();
        }
    }

    @Override
    public void showAlertDialog(String message) {
        showAlertDialog("", message);
    }

    @Override
    public void showAlertDialog(String title, String message) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showAlertDialog(title, message);
        }
    }

    @Override
    public void showAlertDialog(String title, String message, String titleButton) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showAlertDialog(title, message, titleButton);
        }
    }

    @Override
    public void showAlertDialog(String title, String message,
                                OnClickListener onClick) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showAlertDialog(title, message, onClick);
        }

    }

    @Override
    public void showAlertDialogNoButton(String title, String message) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showAlertDialogNoButton(title, message);
        }
    }

    @Override
    public void hideAlertDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.hideAlertDialog();
        }
    }

    @Override
    public void showConfirmDialog(String title, String message,
                                  OnClickListener onOk, OnClickListener onCancel) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showConfirmDialog(title, message, onOk,
                    onCancel);
        }
    }

    @Override
    public void showConfirmDialog(String title, String message, String titleOk,
                                  String titleCancel, OnClickListener onOk, OnClickListener onCancel) {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.showConfirmDialog(title, message, titleOk,
                    titleCancel, onOk, onCancel);
        }
    }

    @Override
    public boolean isOnline() {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            return baseFragmentActivity.isOnline();
        }
        return false;
    }

    @Override
    public void hideSoftKeyboard() {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.hideSoftKeyboard();
        } else {
            Log.d("TAG", "can not close keyboard");
        }
    }

    @Override
    public void hideConfirmDialog() {
        Activity activity = getActivity();
        if (activity instanceof BaseSlidingFragmentActivity) {
            BaseSlidingFragmentActivity baseFragmentActivity = (BaseSlidingFragmentActivity) activity;
            baseFragmentActivity.hideConfirmDialog();
        }
    }

    public void onBackPressed() {
        ((MainActivity) mContext).onBackPressed();
    }
}

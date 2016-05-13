package com.dropininc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.dropininc.AppApplication;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.interfaces.BaseInterface;
import com.dropininc.network.NetworkManager;
import com.dropininc.utils.Logs;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import javax.inject.Inject;


public abstract class BaseActivity extends AppCompatActivity implements BaseInterface {

    AlertDialog alertDialog;
    ProgressDialog progressDialog;
    ConfirmDialog comfirmDialog;
    public boolean isStopActivity = false;
    public MixpanelAPI mixpanel;
    private boolean cancelAllRequestWhenStopActivity = true;
    public Context context;

    @Inject
    protected NetworkManager networkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        AppApplication.appComponent().inject(this);

        mixpanel = MixpanelAPI.getInstance(this, com.dropininc.Constants.MIXPANEL_TOKEN);
    }

    @Override
    protected void onResume() {
        ((AppApplication) getApplication()).setRunning(true);
//        AppApplication.getInstance().connectPusher();
        super.onResume();
    }

    @Override
    protected void onPause() {
        ((AppApplication) getApplication()).setRunning(false);
        super.onPause();
    }

    @Override
    protected void onStart() {
        isStopActivity = false;
        super.onStart();
    }

    @Override
    protected void onStop() {
        isStopActivity = true;
//        AppApplication.getInstance().disconnectPusher();
        super.onStop();
    }

    /**
     * showProgressDialog
     */
    @Override
    public void showProgressDialog(String message,
                                   DialogInterface.OnCancelListener onCancelListener) {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
            }
            progressDialog.setCancelable(false);
            if (!progressDialog.isShowing()) {
                progressDialog.showDialog(message, onCancelListener);
            } else {
                progressDialog.setMessage(message);
            }
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void showAlertDialog(String message) {
        showAlertDialog("", message);
    }

    @Override
    public void showAlertDialog(String title, String message) {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            alertDialog = new AlertDialog(this);
            alertDialog.setTitleDialog(title);
            alertDialog.setMessageDialog(message);
            alertDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void showAlertDialogNoButton(String title, String message) {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            alertDialog = new AlertDialog(this);
            alertDialog.setTitleDialog(title);
            alertDialog.setMessageDialog(message);
            alertDialog.hideButtonCancel();
            alertDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void showAlertDialog(String title, String message, String titleButton) {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            alertDialog = new AlertDialog(this);
            alertDialog.setTitleDialog(title);
            alertDialog.setMessageDialog(message);
            alertDialog.setTitleButton(titleButton);
            alertDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void hideAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void showAlertDialog(String title, String message,
                                OnClickListener onClick) {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                return;
            }
            alertDialog = new AlertDialog(this);
            alertDialog.setTitleDialog(title);
            alertDialog.setMessageDialog(message);
            alertDialog.setButtonClick(onClick);
            alertDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onDestroy() {
        hideProgressDialog();
        hideAlertDialog();
        Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
        // Mint.flush();
        // Mint.closeSession(this);
        mixpanel.flush();
        super.onDestroy();
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
    }

    @Override
    public void showConfirmDialog(String title, String message,
                                  OnClickListener onOk, OnClickListener onCancel) {

        try {
            if (comfirmDialog != null && comfirmDialog.isShowing()) {
                return;
            }
            comfirmDialog = new ConfirmDialog(this);
            comfirmDialog.setTitleDialog(title);
            comfirmDialog.setMessageDialog(message);
            if (onOk != null)
                comfirmDialog.setOkButtonClick(onOk);
            if (onCancel != null)
                comfirmDialog.setCancelButtonClick(onCancel);
            comfirmDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void showConfirmDialog(String title, String message, String titleOk,
                                  String titleCancel, OnClickListener onOk, OnClickListener onCancel) {
        if (isStopActivity) return;//Replace by Thong Nguyen 30/04/2016
        try {
            if (comfirmDialog != null && comfirmDialog.isShowing()) {
                return;
            }
            comfirmDialog = new ConfirmDialog(this);
            comfirmDialog.setTitleDialog(title);
            comfirmDialog.setTitleOk(titleOk);
            comfirmDialog.setTitleCancel(titleCancel);
            comfirmDialog.setMessageDialog(message);
            if (onOk != null)
                comfirmDialog.setOkButtonClick(onOk);
            if (onCancel != null)
                comfirmDialog.setCancelButtonClick(onCancel);
            comfirmDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void hideConfirmDialog() {
        if (comfirmDialog != null) {
            comfirmDialog.dismiss();
            comfirmDialog = null;
        }
    }

    public void setCancelAllRequestWhenStopActivity(boolean flag) {
        cancelAllRequestWhenStopActivity = flag;
    }


}

package com.dropininc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.fragment.MainMenuFragment;
import com.dropininc.interfaces.BaseInterface;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.NavigationDrawerCallBack;
import com.dropininc.interfaces.UserType;
import com.dropininc.network.NetworkManager;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActionBarActivity;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import javax.inject.Inject;


public class BaseSlidingFragmentActivity extends SlidingActionBarActivity implements BaseInterface {

    public boolean isStopActivity = false;
    public Context context;
    public SlidingMenu slidingMenu;
    public MixpanelAPI mixpanel;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private ConfirmDialog confirmDialog;
    private MainMenuFragment LeftMenu;
    private ImageView mImageLeft, mImageLogo, mImageSwitch;
    private RelativeLayout mRelativeLeft, mRelativeLogo, mRelativeRight;
    private TextView mTextViewTitle;
    private boolean cancelAllRequestWhenStopActivity = true;

    @Inject
    protected NetworkManager networkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logs.log("KINH","BaseSlidingFragmentActivity onCreate" );
        AppApplication.appComponent().inject(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        context = this;

        slidingMenu = getSlidingMenu();
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setTouchmodeMarginThreshold(getResources().getDimensionPixelSize(R.dimen.margin_30dp));
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // Menu
        initMenu();

        mixpanel = MixpanelAPI.getInstance(this, com.dropininc.Constants.MIXPANEL_TOKEN);
    }

    private void initMenu() {
        setBehindContentView(R.layout.layout_menu_frame_right);
        LeftMenu = new MainMenuFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame_right, LeftMenu).commit();
    }

    public MainMenuFragment getLeftMenu() {
        return LeftMenu;
    }

    public void setMenuItemSelectedCallBack(NavigationDrawerCallBack callBack) {
        if (LeftMenu != null) {
            LeftMenu.setMenuItemSelectedCallBack(callBack);
        }
    }

    public void setupView() {
        mRelativeLeft = (RelativeLayout) findViewById(R.id.relativeLeft);
        mRelativeLogo = (RelativeLayout) findViewById(R.id.relativeLogo);
        mRelativeRight = (RelativeLayout) findViewById(R.id.relativeRight);

        mImageLeft = (ImageView) findViewById(R.id.btnLeft);
        mImageLogo = (ImageView) findViewById(R.id.btnLogo);
        mImageSwitch = (ImageView) findViewById(R.id.btn_switch);

        mTextViewTitle = (TextView) findViewById(R.id.txtTitle);
    }

    public void addToolBarCustom(boolean showHelpButton) {
        mTextViewTitle.setVisibility(View.GONE);
        mTextViewTitle.setText("");
        mRelativeLogo.setVisibility(View.VISIBLE);
        mRelativeRight.setVisibility(View.VISIBLE);

        // help button
        if (showHelpButton) {
            mRelativeLogo.setVisibility(View.VISIBLE);
        } else {
            mRelativeLogo.setVisibility(View.GONE);
        }
    }

    public void addToolBarCustom(boolean showHelpButton, boolean showUserTypeIcon) {
        mTextViewTitle.setVisibility(View.GONE);
        mTextViewTitle.setText("");

        if (showHelpButton) {
            mRelativeLogo.setVisibility(View.VISIBLE);
        } else {
            mRelativeLogo.setVisibility(View.GONE);
        }

        if (showUserTypeIcon) {
            mRelativeRight.setVisibility(View.VISIBLE);
        } else {
            mRelativeRight.setVisibility(View.GONE);
        }
    }

    public void addToolBarNormal(String title) {
        mTextViewTitle.setVisibility(View.VISIBLE);
        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        FontUtils.typefaceTextView(txtTitle, FontType.LIGHT);
        mTextViewTitle.setText(title);
        mRelativeLogo.setVisibility(View.GONE);
        mRelativeRight.setVisibility(View.GONE);

    }

    public void setActionBarHomeButtonOnClick(View.OnClickListener mListener) {
        mRelativeLeft.setOnClickListener(mListener);
        mImageLeft.setOnClickListener(mListener);
    }

    public void setHelpOnClick(View.OnClickListener mListener) {
        mRelativeLogo.setOnClickListener(mListener);
        mImageLogo.setOnClickListener(mListener);
    }

    public void setSwitchChangeUserType(View.OnClickListener mListener) {
        mRelativeRight.setOnClickListener(mListener);
        setUserType(UserType.VIEWER);
    }

    public void setShowHelpIcon(boolean flag) {
        mRelativeLogo.setVisibility(View.VISIBLE);
    }

    public int getUserType() {
        return AppApplication.getInstance().getUserType();
    }

    public void setUserType(int userType) {
        if (userType == UserType.VIEWER) {
            mImageSwitch.setSelected(false);
        } else {
            mImageSwitch.setSelected(true);
        }
    }

    public void setShowUserTypeIcon(boolean flag) {
        if (flag) {
            mRelativeRight.setVisibility(View.VISIBLE);
        } else {
            mRelativeRight.setVisibility(View.GONE);
        }
    }

    public void setEnableSwitchUserType(boolean enable) {
        mRelativeRight.setEnabled(enable);
        mImageSwitch.setEnabled(enable);
    }

    public void setEnableHelp(boolean enable) {
        mRelativeLogo.setEnabled(enable);
        mImageLogo.setEnabled(enable);
    }

    public View getSwitchView() {
        return mRelativeRight;
    }

    public boolean isMenuShowing() {
        return slidingMenu.isMenuShowing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logs.log("v", "onActivityResult - requestCode: " + requestCode + " - resultCode:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        AppApplication.getInstance().setRunning(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        AppApplication.getInstance().setRunning(false);
        super.onPause();
    }

    @Override
    protected void onStart() {
        isStopActivity = false;
        super.onStart();
        Logs.log("BaseSlidingFragmentActivity", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logs.log("BaseSlidingFragmentActivity", "onRestart");
    }

    @Override
    protected void onStop() {
        /*
        if (cancelAllRequestWhenStopActivity) {
            isStopActivity = true;
        }*/
        isStopActivity = true;
        super.onStop();
        Logs.log("BaseSlidingFragmentActivity", "onStop");
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
        if (progressDialog != null && progressDialog.isShowing()) {
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
    public void hideAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void showAlertDialog(String title, String message,
                                View.OnClickListener onClick) {
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
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onDestroy() {
        Logs.log("KINH","BaseSlidingFragmentActivity onDestroy" );
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
        try {
            if (getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }catch(Exception e){
            Log.e("KINH", "hideSoftKeyboard.Exception=" + e != null ? e.getMessage() : "");
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
                                  View.OnClickListener onOk, View.OnClickListener onCancel) {
        try {
            if (confirmDialog != null && confirmDialog.isShowing()) {
                return;
            }
            confirmDialog = new ConfirmDialog(this);
            confirmDialog.setTitleDialog(title);
            confirmDialog.setMessageDialog(message);
            if (onOk != null)
                confirmDialog.setOkButtonClick(onOk);
            if (onCancel != null)
                confirmDialog.setCancelButtonClick(onCancel);
            confirmDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void showConfirmDialog(String title, String message, String titleOk,
                                  String titleCancel, View.OnClickListener onOk, View.OnClickListener onCancel) {
        try {
            if (confirmDialog != null && confirmDialog.isShowing()) {
                return;
            }
            confirmDialog = new ConfirmDialog(this);
            confirmDialog.setTitleDialog(title);
            confirmDialog.setTitleOk(titleOk);
            confirmDialog.setTitleCancel(titleCancel);
            confirmDialog.setMessageDialog(message);
            if (onOk != null)
                confirmDialog.setOkButtonClick(onOk);
            if (onCancel != null)
                confirmDialog.setCancelButtonClick(onCancel);
            confirmDialog.show();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    @Override
    public void hideConfirmDialog() {
        if (confirmDialog != null) {
            confirmDialog.dismiss();
            confirmDialog = null;
        }
    }

    public void onMixpanelGCMRegistered(String userID, String registrationId) {
        // MixpanelAPI.People people = mMixpanel.getPeople();
        // people.identify(userID);
        // people.setPushRegistrationId(registrationId);
        // people.initPushHandling(Constant.MIXPANEL_PUSH_NOTIFICATION_SENDERID);
    }

    public void onMixpanelGCMUnregistered(String userID) {
        // MixpanelAPI.People people = mMixpanel.getPeople();
        // people.identify(userID);
        // people.clearPushRegistrationId();
    }

    public void setCancelAllRequestWhenStopActivity(boolean flag) {
        cancelAllRequestWhenStopActivity = flag;
    }

}

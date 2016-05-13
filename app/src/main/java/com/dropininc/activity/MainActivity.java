package com.dropininc.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dropininc.AppApplication;
import com.dropininc.BaseSlidingFragmentActivity;
import com.dropininc.R;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.FullScreenDialog;
import com.dropininc.fragment.BaseFragment;
import com.dropininc.fragment.HelpCenterFragment;
import com.dropininc.fragment.HistoryFragment;
import com.dropininc.fragment.InboxFragment;
import com.dropininc.fragment.LogoutFragment;
import com.dropininc.fragment.MapFragment;
import com.dropininc.fragment.PaymentFragment;
import com.dropininc.fragment.PrivacyFragment;
import com.dropininc.fragment.ProfileFragment;
import com.dropininc.fragment.ReferFriendFragment;
import com.dropininc.fragment.SettingFragment;
import com.dropininc.interfaces.Navigation;
import com.dropininc.interfaces.NotificationCode;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.interfaces.OperatorStatus;
import com.dropininc.interfaces.RenderCallback;
import com.dropininc.interfaces.RequestCode;
import com.dropininc.interfaces.UserType;
import com.dropininc.location.LocationManager;
import com.dropininc.message.MessageManager;
import com.dropininc.model.AccountSettingModel;
import com.dropininc.model.DataInputStreamModel;
import com.dropininc.model.LocationModel;
import com.dropininc.model.LogModel;
import com.dropininc.model.LogPusher;
import com.dropininc.model.NotificationModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.ResumeCheckModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.network.request.DeviceTokenRequest;
import com.dropininc.services.LocationService;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.AppVisibilityState;
import com.dropininc.utils.Constants;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.zendesk.sdk.feedback.ui.ContactZendeskFragment;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.model.request.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.model.request.ZendeskFeedbackConfiguration;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.storage.SdkStorage;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends BaseSlidingFragmentActivity implements AppVisibilityState.Listener {
    private final String TAG = "MainActivity";

    private static MainActivity mActivity;

    private Stack<BaseFragment> mStacks;
    private IntentFilter mIntentFilter;
    private IntentFilter mIntentFilterNetwork;
    private IntentFilter mIntentFilterBattery;
    private IntentFilter mIntentFilterGpsPermission;
    private AutoDialog mAutoDialogNetwork;

    private boolean mNavigationEnable = true;
    private boolean mBackPressedEnable = true;
    private int currentMenu = -1;
    private int mSettingRadius;
    private boolean mMainActivityActive = true;
    private boolean mProcessingRequest = false;
    private boolean mEnableStartStream = false;
    private boolean mBatterySwitchUserType = false;
    private boolean mShowDialogBatteryMain = true;
    private boolean mDeviceCharging = false;
    private boolean mCheckLocationService = true;

    private String mMyAccountId = "";
    private String mAccountListenerId = "";

    private int mBatteryLevelMain;

    private LocationModel mLastViewerLocation;
    private LocationModel mLastDroperatorLocation;
    private OperatorModel mDocusignStatus;
    private Map<String, String> mListMessageId;
    private Map<String, String> mMapBatteryMain;

    // facebook
    private CallbackManager callbackManager;

    private boolean mustResumeProfile = false;

    private boolean isProgresssing = false;

    private PrivateChannel privateChannel;
    private boolean mEnRouteMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = MainActivity.this;
        AppApplication.getInstance().setAppicationRunning(true);
        AppApplication.getInstance().addAppVisibilityStateListener(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);
        super.setupView();

        if (getIntent().getExtras() != null) {
            processNotificationIfNeed(getIntent());
        }

        if (savedInstanceState != null) {
            Bundle savedState = savedInstanceState.getBundle("SAVE_STATE");
            if (savedState != null)
                mustResumeProfile = savedState.getBoolean("STOP_AT_PROFILE", false);
        }

        initView();

        mIntentFilter = new IntentFilter(Constants.NOTIFICATION_FILTER);
        mIntentFilterNetwork = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilterBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mIntentFilterGpsPermission = new IntentFilter(Constants.GPS_PERMISSION_FILTER);
        mListMessageId = new HashMap<>();
        mMapBatteryMain = new HashMap<>();

        registerReceiver(mBroadcastReceiver, mIntentFilter);
        registerReceiver(getPermissionReceiver, mIntentFilterGpsPermission);

        AccountSettingModel accountSetting = DSharePreference.getAccountSetting(mActivity);
        if (accountSetting != null)
            mSettingRadius = accountSetting.detectRadius;
        //else // TODO

        String json = DSharePreference.getProfile(mActivity);
        VerifyModel model = new Gson().fromJson(json, VerifyModel.class);
        mMyAccountId = model.account.id;
        AppApplication.getInstance().setAccountId(model.account.id);
        AppApplication.getInstance().addChannel(model.account.id);
        if (model.account != null && model.account.operator != null) {
            Logs.log("Operator Status", model.account.operator.status);
            if (model.account.operator.status.equals("unapproved") || model.account.operator.status.equals(OperatorStatus.CANCELED)) {
                AppApplication.getInstance().setOperator(false);
            } else {
                AppApplication.getInstance().setOperator(true);
            }
        } else {
            AppApplication.getInstance().setOperator(false);
        }
        Logs.log("Flag Operator Status", AppApplication.getInstance().isOperator() + "");
        addChannelListener(mMyAccountId);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LocationModel locationDefault = AppApplication.getInstance().getLocationDefault();
        if (locationDefault == null) {
            locationDefault = DSharePreference.getDefaultLocation(mActivity);
            if (locationDefault == null) {
                locationDefault = new LocationModel();
                locationDefault.latitude = 34.0851002;
                locationDefault.longitude = -118.3768646;
            }
            AppApplication.getInstance().setLocationDefault(locationDefault);
        }

        setupZendesk(model);

        // delay for map init and progress other task
        new Handler().postDelayed(() -> checkGisStatus(), 1500);

        refreshDeviceToken();

        setUserType(AppApplication.getInstance().getUserType());

        if (AppApplication.getInstance() != null && AppApplication.getInstance().getPushIntent() != null) {
            Logs.log("MNotification", "pushIntent != null");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logs.log("MNotification", "sendBroadcast postDelayed");
                    sendBroadcast(AppApplication.getInstance().getPushIntent());
                }
            }, 1000);
        } else {
            Logs.log("MNotification", "pushIntent == null");
        }
    }

    private void checkGisStatus() {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.checkOldGigStatus();
            mapFragment.checkGisStatus();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("TAG", "Main Activity: onSaveInstanceState");
        if (getCurrentFragment() instanceof ProfileFragment) {
            Bundle state = new Bundle();
            state.putBoolean("STOP_AT_PROFILE", true);
            outState.putBundle("SAVE_STATE", state);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkInfoReceiver, mIntentFilterNetwork);
        registerReceiver(mBatteryChangeReceiver, mIntentFilterBattery);
        Utils.clearAllNotification(getApplicationContext());
        AppApplication.getInstance().connectPusher();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkInfoReceiver);
        unregisterReceiver(mBatteryChangeReceiver);
    }

    @Override
    protected void onDestroy() {
        /**bug dialog**/
        if (mDialogIncomingJob != null && mDialogIncomingJob.isShowing()) {
            mDialogIncomingJob.dismiss();
            mDialogIncomingJob = null;
        }
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(getPermissionReceiver);
        AppApplication.getInstance().setAppicationRunning(false);
        removeAllChannel();
        startLocationService();
        mActivity = null;
        super.onDestroy();
    }

    public static MainActivity getInstance() {
        return mActivity;
    }

    public static void setInstance(MainActivity instance) {
        mActivity = instance;
    }

    private void initView() {
        addToolBarCustom(true);
        setActionBarHomeButtonOnClick(v -> {
            if (mNavigationEnable) {
                hideSoftKeyboard();
                toggle();
                getLeftMenu().checkOperatorStatus();
                getLeftMenu().getNotificationCount();
            }
        });
        showKeyHashFacebook();
        setMenuItemSelectedCallBack(position -> {
            toggle();
            switch (position) {
                case Navigation.HOME:
                    if (mBatterySwitchUserType) {
                        setUserType(UserType.VIEWER);
                    }
                    setOnItemClick(Navigation.HOME);
                    break;
                case Navigation.NOTIFICATION:
                    setOnItemClick(Navigation.NOTIFICATION);
                    break;
                case Navigation.PAYMENT:
                    openPayment();
                    break;
                case Navigation.TERM_OF_SERVICE:
                    setOnItemClick(Navigation.TERM_OF_SERVICE);
                    break;
                case Navigation.HISTORY:
                    setOnItemClick(Navigation.HISTORY);
                    break;
                case Navigation.SETTINGS:
                    setOnItemClick(Navigation.SETTINGS);
                    break;
                case Navigation.SUPPORT:
                    setOnItemClick(Navigation.SUPPORT);
                    break;
                case Navigation.LOGOUT:
                    if (!isEnroute()) {
                        setOnItemClick(Navigation.LOGOUT);
                    }
                    break;
                case Navigation.BECOME_OPERATOR:
                    setOnItemClick(Navigation.BECOME_OPERATOR);
                    break;
                case Navigation.REFER_FRIEND:
                    setOnItemClick(Navigation.REFER_FRIEND);
                    break;
            }
        });
        setSwitchChangeUserType(mSwitchUser);
        setHelpOnClick(mHelpOnClick);

        // init tab control
        mStacks = new Stack<>();
        if (mustResumeProfile) {
            BaseFragment fragment = new SettingFragment();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            pushFragments(fragment);
            clearStack();
        } else {
            addToolBarCustom(true);
            setUserType(UserType.VIEWER);
            BaseFragment fragment = MapFragment.getInstance();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Logs.log("MNotification", "main create view complete");
                fragment.setArguments(bundle);
            }
            pushFragments(fragment);
            clearStack();
        }
    }

    private void setupZendesk(final VerifyModel model) {
        try {
//            Logger.setLoggable(true);
            SdkStorage.INSTANCE.init(context);
            SdkStorage.INSTANCE.clearUserData();
            SdkStorage.INSTANCE.settings().deleteStoredSettings();
            ZendeskConfig.INSTANCE.init(this, "https://dropinhelp.zendesk.com", "666bd3939a99d869a1c0d7b4e5f51520ab17808c85d73824", "mobile_sdk_client_1ec034b808a4e29ebce2", new ZendeskCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Logs.log(TAG, "Zendesk init onSuccess : " + result);
                }

                @Override
                public void onError(ErrorResponse error) {
                    Logs.log(TAG, "Zendesk init error: " + error.getReason());
                }
            });
            ZendeskFeedbackConfiguration configuration = new BaseZendeskFeedbackConfiguration() {
                @Override
                public String getRequestSubject() {
                    return "Contact request";
                }
            };
            ZendeskConfig.INSTANCE.setContactConfiguration(configuration);
            Identity identity = new AnonymousIdentity.Builder()
                    .withNameIdentifier(model.account.firstName + " " + model.account.lastName)
                    .withEmailIdentifier(model.account.identities.get(0).value)
                    .build();
            Logs.log(TAG, "EmailIdentifier " + (model.account.identities.get(0).value));
            ZendeskConfig.INSTANCE.setIdentity(identity);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void processNotificationIfNeed(Intent intent) {
        try {
            String fullData = intent.getStringExtra("PUSH_DATA");
            JSONObject bundle = new JSONObject(fullData);
            if (bundle != null && bundle.has("code")) {
                int code = Integer.parseInt(bundle.getString(NotificationKey.CODE));
                switch (code) {
                    case NotificationCode.STREAM_START:
                        break;
                    case NotificationCode.STREAM_FINISH:
                        NotificationModel model = NotificationModel.fromJSON(bundle.toString());
                        // Disconnecting from the GIG channel
                        MessageManager.getInstance().unsubscribeFromGigChannel(model.gigId);
                        break;
                    case NotificationCode.REQUEST_OPERATOR:
                        break;
                    case NotificationCode.ACCEPT_DENY_REQUEST:
                        break;
                    case NotificationCode.DOCUMENT_SIGN:
                        setCheckLocationService(false);
                        break;
                }
            }
        } catch (Exception e) {
            Logs.log(e);
        }
    }


    public void shareTwitter(String mess) {
        Utils.shareTwitter(mActivity, mess, null);
    }

    public void shareFaceBook(String url) {
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
//                showAlertDialog("", getResources().getString(R.string.shared_via_facebook));
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(mActivity.getString(R.string.send_mail_subject))
                    .setContentDescription(
                            String.format(mActivity.getString(R.string.send_mail_body),
                                    url))
                    .setContentUrl(Uri.parse(url))
                    .build();

            shareDialog.show(linkContent);
        } else {
            Logs.log("TAG", "can not share link content facebook");
        }

    }

    private void showKeyHashFacebook() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getApplicationContext().getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (mBackPressedEnable) {
            onMainBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        BaseFragment fragment = getCurrentFragment();
        /*check null*/
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 111) {// email
                showAlertDialog("", getString(R.string.shared_via_email));
            }
            if (requestCode == 112) {// sms
                showAlertDialog("", getString(R.string.shared_via_sms));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_ASK_GPS_PERMISSIONS:
                if (grantResults.length > 1 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    restartLocationService();
                }
                break;
            case Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS:
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment != null && currentFragment instanceof ProfileFragment) {
                    ProfileFragment profileFragment = (ProfileFragment) currentFragment;
                    profileFragment.openCamera();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public BaseFragment getCurrentFragment() {
        if (mActivity == null || mActivity.getSupportFragmentManager() == null) {
            return null;
        }
        return (BaseFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.frame_container);
    }

    public MapFragment getMapFragment() {
        if (getCurrentFragment() != null) {
            if (getCurrentFragment() instanceof MapFragment)
                return (MapFragment) getCurrentFragment();
        }
        return null;
    }

    public void onMainBackPressed() {
        if (mStacks.size() > 0) {
            if (mStacks.size() == 1) {
                finish();
            } else {
                popFragments();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void setOnItemClick(int index) {
        if (currentMenu != index) {
            setCurrentItem(index);
            invalidateOptionsMenu();
        }
    }

    public void setCurrentItem(int index) {
        boolean isChangeFragment = false;
        boolean isClear = true;
        BaseFragment fragment = null;
        switch (index) {
            case Navigation.HOME:
                fragment = getCurrentFragment();
                if (fragment instanceof MapFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    addToolBarCustom(true);
                    setUserType(getUserType());
                    fragment = MapFragment.getInstance();
                }
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("Map", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                break;
            case Navigation.NOTIFICATION:
                fragment = getCurrentFragment();
                if (fragment instanceof InboxFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new InboxFragment();
                    fragment.setTitle(getString(R.string.inbox));
                    addToolBarNormal(getString(R.string.inbox));
                }
                break;
            case Navigation.PAYMENT:
                fragment = getCurrentFragment();
                if (fragment instanceof PaymentFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new PaymentFragment();
                    fragment.setTitle(getString(R.string.payment_title));
                    addToolBarNormal(getString(R.string.payment_title));
                }
                break;
            case Navigation.TERM_OF_SERVICE:
                fragment = getCurrentFragment();
                if (fragment instanceof PrivacyFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new PrivacyFragment();
                    fragment.setTitle(getString(R.string.term_of_service));
                    addToolBarNormal(getString(R.string.term_of_service));
                }
                break;
            case Navigation.HISTORY:
                fragment = getCurrentFragment();
                if (fragment instanceof HistoryFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new HistoryFragment();
                    fragment.setTitle(getString(R.string.history_title));
                    addToolBarNormal(getString(R.string.history_title));
                }
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("History", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                break;
            case Navigation.SETTINGS:
                fragment = getCurrentFragment();
                if (fragment instanceof SettingFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new SettingFragment();
                    fragment.setTitle(getString(R.string.settings));
                    addToolBarNormal(getString(R.string.settings));
                }
                break;
            case Navigation.SUPPORT:
                fragment = getCurrentFragment();
                if (fragment instanceof HelpCenterFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    isClear = false;
                    fragment = new HelpCenterFragment();
                    fragment.setTitle(getString(R.string.help_center));
                    addToolBarNormal(getString(R.string.help_center));
                }
                break;
            case Navigation.LOGOUT:
                fragment = getCurrentFragment();
                if (fragment instanceof LogoutFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    isClear = false;
                    fragment = new LogoutFragment();
                    fragment.setTitle(getString(R.string.logout_title));
                    addToolBarNormal(getString(R.string.logout_title));
                }
                try {
                    JSONObject props = new JSONObject();
                    props.put("Mode", getUserType() == UserType.OPERATOR ? "Droperator" : "Viewer");
                    mixpanel.track("Logout", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                break;
            case Navigation.BECOME_OPERATOR:
                openDroperatorFlow();
                break;
            case Navigation.REFER_FRIEND:
                fragment = getCurrentFragment();
                if (fragment instanceof ReferFriendFragment) {
                    toggle();
                } else {
                    isChangeFragment = true;
                    fragment = new ReferFriendFragment();
                    fragment.setTitle(getString(R.string.refer_a_friend));
                    addToolBarNormal(getString(R.string.refer_a_friend));
                }
                break;
        }
        if (isChangeFragment) {
            pushFragments(fragment);
            if (isClear) {
                clearStack();
            }

        }
    }


    public void clearStack() {
        while (mStacks.size() > 1) {
            BaseFragment fragment = mStacks.get(mStacks.size() - 2);
            mStacks.remove(fragment);
//            fragment = null;
        }
    }

    public void selectCurrentFragment() {
        BaseFragment fragment = mStacks.elementAt(mStacks.size() - 1);
        if (fragment != null && !fragment.isAdded()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public void pushFragments(BaseFragment fragment) {
        Logs.log("TAG", fragment.getClass().getSimpleName());
        mStacks.push(fragment);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void pushExternalFragment(ContactZendeskFragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }


    public void popFragments() {
        BaseFragment fragment = null;
        int size = mStacks.size();
        if (size > 1) {
            fragment = mStacks.elementAt(mStacks.size() - 2);
            mStacks.pop();
        } else {
            fragment = mStacks.elementAt(0);
        }
        if (fragment != null) {
            if (fragment instanceof MapFragment) {
                addToolBarCustom(true);
                if (getLeftMenu() != null) {
                    getLeftMenu().onItemSelected(Navigation.HOME);
                }
                if (mBatterySwitchUserType) {
                    setUserType(UserType.VIEWER);
                    ((MapFragment) fragment).setUserType(UserType.VIEWER);
                }
            } else {
                addToolBarNormal(fragment.getTitle());
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.frame_container, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public void setNavigationEnable(boolean enable) {
        mNavigationEnable = enable;
    }

    public void setBackPressedEnable(boolean enable) {
        mBackPressedEnable = enable;
    }

    public void setEnableStartStream(boolean enable) {
        mEnableStartStream = enable;
    }

    public void resetParams() {
        Logs.log(TAG, "REQUEST_OPERATOR: Reset Param ");
        mBackPressedEnable = true;
        mNavigationEnable = true;
        mMainActivityActive = true;
        mProcessingRequest = false;
        mEnableStartStream = false;
        mAccountListenerId = "";

        mListMessageId.clear();
    }

    public void setIsProgreesing(boolean isProgreesing) {
        this.isProgresssing = isProgreesing;
    }

    private View.OnClickListener mSwitchUser = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (getUserType() == UserType.VIEWER) {
                if (isProgresssing)
                    return;
                // this flag using for prevent multi click switch
                isProgresssing = true;
                if (!mDeviceCharging && mBatteryLevelMain <= 10) {
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setTitleDialog("");
                    mDialog.setMessageDialog(R.string.message_battery_less_than_10);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v1 -> mDialog.dismiss());
                    mDialog.show();
                    isProgresssing = false;
                    return;
                }

                if (Utils.getCameraCapabilities(mActivity)) {
                    MapFragment mapFragment = getMapFragment();
                    if (mapFragment != null) {
//                        (mapFragment).setNeedSwitchMode(true);
                        mapFragment.checkBeforeSwitchUserType();
                        isProgresssing = false;
                    }
                } else {
                    showDialogDeviceInvalid();
                    isProgresssing = false;
                }
            } else {
                setShowHelpIcon(true);
                int userType = UserType.VIEWER;
                MapFragment mapFragment = getMapFragment();
                if (mapFragment != null) {
                    (mapFragment).setUserType(userType);
                    (mapFragment).setRadiusVisibility();
                    (mapFragment).setButtonFindVisibility();
                }
                setUserType(userType);
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("Viewer - Toggled", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
            }
        }
    };

    private void showDialogDeviceInvalid() {
        final AlertDialog alertDialog = new AlertDialog(mActivity);
        alertDialog.setTitleDialog("");
        alertDialog.setMessageDialog(getString(R.string.message_device_invalid));
        alertDialog.setTitleButton(getString(R.string.cancel));
        alertDialog.setLinkClicked(view -> {
            alertDialog.dismiss();
            getLeftMenu().onItemSelected(Navigation.SUPPORT);
            setOnItemClick(Navigation.SUPPORT);
        });
        alertDialog.show();
    }

    public void processMapFragment() {
        setShowHelpIcon(false);
        int userType = UserType.OPERATOR;
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.setUserType(userType);
            mapFragment.setRadiusVisibility();
            mapFragment.setButtonFindVisibility();
            if (DSharePreference.isHelpTutorial(mActivity, userType)) {
                mapFragment.showHelperDialog();
            }
        } else {
            Logs.log(TAG, "processMapFragment not in MapFragment");
        }
    }

    public void openPayment() {
        BaseFragment fragment = new PaymentFragment();
        fragment.setTitle(getString(R.string.payment_title));
        addToolBarNormal(getString(R.string.payment_title));
        pushFragments(fragment);
        try {
            JSONObject props = new JSONObject();
            mixpanel.track("Payment", props);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    public void openDroperatorFlow() {
        startActivity(new Intent(this, DroperatorFlowActivity.class));
    }

    public void showDialogDocumentSignApproved(String userId) {
        /*fix bug https://fabric.io/dropininc/android/apps/com.dropininc/issues/5721aaacffcdc04250207145*/
        if (mActivity == null || !isStopActivity) {
            Logs.log("Activity", "null");
            return;
        }
        if (mProcessingRequest) return;
        String json = DSharePreference.getProfile(mActivity);
        VerifyModel verify = new Gson().fromJson(json, VerifyModel.class);
        if (!TextUtils.isEmpty(userId) && verify.account.id.equalsIgnoreCase(userId)) { // Check UserId of Notification match with current user
            DSharePreference.setOperator(mActivity, true);
            AppApplication.getInstance().setOperator(true);
            final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
            mDialog.setTitleDialog(R.string.docusign_form_approved);
            mDialog.setMessageDialog("");
            mDialog.setOkTitleButton(R.string.droperator_mode);
            mDialog.setCancelTitleButton(R.string.cancel);
            mDialog.setOkButtonClick(view -> {
                mDialog.dismiss();
                MapFragment mapFragment = getMapFragment();
                Logs.log("Docusign", mapFragment.getTitle());
                if (isMenuShowing()) {
                    toggle();
                }
                if (mapFragment != null) {
                    setCheckLocationService(true);
                    mSwitchUser.onClick(getSwitchView());
                } else {
                    setCurrentItem(Navigation.HOME);
                    MapFragment mapFrg = getMapFragment();
                    if (mapFrg != null) {
                        mapFrg.setNeedSwitchMode(true);
                        mapFrg.setRenderCallback(new RenderCallback() {
                            @Override
                            public void onRendered() {
                                setCheckLocationService(true);
                                mSwitchUser.onClick(getSwitchView());
                            }
                        });
                    }
                }
            });
            mDialog.setCancelButtonClick(view -> {
                mDialog.dismiss();
                setCheckLocationService(false);
            });
            mDialog.show();
        }
    }

    private ConfirmDialog mDialogIncomingJob = null;

    private void showDialogIncomingJob(final String gigsId, final String viewerId, final LocationModel locationViewer) {
        /*
        https://fabric.io/dropininc/android/apps/com.dropininc/issues/5721aaacffcdc04250207145
        * */
        if (mActivity == null) {
            Logs.log("Activity", "null");
            Logs.log("REQUEST_OPERATOR", "Fail Activity = null");
            return;
        }

        hideAlertDialog();

        String textDistance = "";
        if (mLastDroperatorLocation != null) {
            Location fromLocation = new Location("");
            fromLocation.setLatitude(mLastDroperatorLocation.latitude);
            fromLocation.setLongitude(mLastDroperatorLocation.longitude);

            Location toLocation = new Location("");
            toLocation.setLatitude(locationViewer.latitude);
            toLocation.setLongitude(locationViewer.longitude);

            DecimalFormat mFormatter = new DecimalFormat("#.#");
            float distance = fromLocation.distanceTo(toLocation);
            distance = (int) (distance * 3.28084);
            if (distance < 1000) {
                textDistance = (int) distance + " Ft";
            } else {
                double mile = distance * 0.000621371;
                textDistance = mFormatter.format(mile) + " Mi";
            }
        }

        Geocoder geocoder = new Geocoder(mActivity);
        String address = "";
        try {
            List<Address> listAddress = geocoder.getFromLocation(locationViewer.latitude, locationViewer.longitude, 1);
            if (listAddress != null && listAddress.size() > 0) {
                Address item = listAddress.get(0);
                int maxIndex = item.getMaxAddressLineIndex();
                if (maxIndex > -1) {
                    for (int i = 0; i <= maxIndex; i++) {
                        if (TextUtils.isEmpty(address)) {
                            address = item.getAddressLine(i);
                        } else {
                            address = address + ", " + item.getAddressLine(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String message = getString(R.string.message_incoming_job);
        message = message + "\n" + getString(R.string.destination)
                + ": %s" + "\n" + getString(R.string.distance) + ": %s";

        message = String.format(message, address, textDistance);
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialogIncomingJob = mDialog;
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(message);
        mDialog.setCancelTitleButton(R.string.cancel_request);
        mDialog.setOkTitleButton(R.string.accept);
        mDialog.setOkButtonClick(v -> {
            try {
                JSONObject props = new JSONObject();
                if (getMapFragment() != null) {
                    props.put("latitude", getMapFragment().getLatitude());
                    props.put("longitude", getMapFragment().getLongitude());
                }
                props.put("Accepted", "Yes");
                mixpanel.track("Droperator - Gig Received", props);
            } catch (Exception e) {
                Logs.log(e);
            }
            try {
                if (getMapFragment() != null) {
                    JSONObject props = new JSONObject();
                    props.put("latitude", getMapFragment().getLatitude());
                    props.put("longitude", getMapFragment().getLongitude());
                    mixpanel.track("Stream - Droperator En Route", props);
                }
            } catch (Exception e) {
                Logs.log(e);
            }
            mDialog.dismiss();
            mDialogIncomingJob = null;
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.acceptRequestViewer(gigsId, locationViewer, "accept");
            } else {
                setCurrentItem(Navigation.HOME);
                final MapFragment mapFrag = getMapFragment();
                if (mapFrag != null) {
                    mapFrag.setRenderCallback(new RenderCallback() {
                        @Override
                        public void onRendered() {
                            mapFrag.acceptRequestViewer(gigsId, locationViewer, "accept");
                        }
                    });
                }
            }
        });
        mDialog.setCancelButtonClick(v -> {
            mDialog.dismiss();
            mDialogIncomingJob = null;
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.acceptRequestViewer(gigsId, locationViewer, "reject");
            } else {
                networkManager.responseViewer(gigsId, "reject")
                        .doAfterTerminate(() -> {

                        })
                        .subscribe(claimModel -> {
                        }, throwable -> {
                            AppApplication.getInstance().logErrorServer("responseViewer/reject", networkManager.parseError(throwable));
                        });
                setProcessingRequest(false);
                setAccountListener("");
            }
            try {
                JSONObject props = new JSONObject();
                if (mapFragment != null) {
                    props.put("latitude", mapFragment.getLatitude());
                    props.put("longitude", mapFragment.getLongitude());
                }
                props.put("Accepted", "No");
                mixpanel.track("Droperator - Gig Received", props);
            } catch (Exception e) {
                Logs.log(e);
            }
            try {
                JSONObject props = new JSONObject();
                props.put("gigsId", gigsId);
                mixpanel.track("Stream - Droperator Cancelled", props);
            } catch (Exception e) {
                Logs.log(e);
            }
        });
        mDialog.show();
        Logs.log("REQUEST_OPERATOR", "Fail Activity = null");
    }

    private void acceptDenyRequest(Bundle bundle) {
        String action = bundle.getString("action");
        if (action.equalsIgnoreCase("accept")) {
            String gigsId = bundle.getString("gigsId");
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.confirmRequest(gigsId);
            }
        } else {
            if (mActivity == null) {
                Logs.log("Activity", "null");
            }
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.resetParamsViewerMode();
            }
            final AlertDialog mDialog = new AlertDialog(mActivity);
            mDialog.setTitleDialog("");
            mDialog.setMessageDialog(R.string.message_deny_request_from_droperator);
            mDialog.setTitleButton(R.string.ok_cap);
            mDialog.setButtonClick(v -> mDialog.dismiss());
            mDialog.show();

            try {
                String gigsId = bundle.getString("gigsId");
                LogModel logModel = new LogModel();
                logModel.message = "Android: " + R.string.message_deny_request_from_droperator;
                logModel.data = "gigsId: " + gigsId + " - action: " + action;
                ((MainActivity) mActivity).logToServer(logModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void navigateViewerStreamActivity(DataInputStreamModel dataInputStream) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            // hide view info
            mapFragment.hideEnrouteInfoView();
            mMainActivityActive = false;
            mapFragment.removeRoutePath();
            AppApplication.getInstance().setCurrentGigsId(dataInputStream.gigsId);
            Intent intent = dataInputStream.toIntent(mActivity, ViewerStreamingActivity.class);
            startActivityForResult(intent, RequestCode.VIEWER_STREAM);
            mapFragment.removeChat();
        } else {
            Logs.log("w", TAG, "navigateViewerStreamActivity - mapFragment null");
        }
    }

    public void navigateOperatorStreamActivity(DataInputStreamModel dataInputStream) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            Intent intent = dataInputStream.toIntent(mActivity, DroperatorStreamingActivity.class);
            startActivityForResult(intent, RequestCode.OPERATOR_STREAM);
            mapFragment.removeChat();
        } else {
            Logs.log("w", TAG, "navigateOperatorStreamActivity - mapFragment null");
        }
    }

    private void operatorCancelRequest(Bundle bundle) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            String message = bundle.getString("message");
            mapFragment.cancelRequest(message);
        }
    }

    private void handlerReceiveLocationFromOperator(Bundle bundle) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            String operatorId = bundle.getString("operatorId");
            String textLocation = bundle.getString("location");
            int radius = Integer.parseInt(bundle.getString("settingRadius"));
            LocationModel location = LocationModel.fromJSON(textLocation);
            mapFragment.receiveLocationMessageFromOperator(location, operatorId, radius);
        }
    }

    private void handlerOperatorEnRoute(String gigId) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.startEnRouteOperator(gigId);
        } else {
            Logs.log("handlerOperatorEnRoute", "not MapFragment");
        }
    }

    private void showChatButtonOnMap(String chanel, String chatName, String chatAvatar, String gigID) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.showChatButton(chanel, chatName, chatAvatar, gigID);
        }
    }

    private void handlerSwitchMode(NotificationModel model) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.removeMarkerOperator(model.accountId);
        }
    }

    public void addChannelListener(ArrayList<String> channels) {
        for (int i = 0; i < channels.size(); i++) {
            String channel = channels.get(i);
            addChannelListener(channel);
        }
    }

    public void addChannelListener(String channel) {
        Log.d("KINH", "Pusher:>>>>>ADD CHANNEL--START = " + channel);
        Logs.log("addChannelListener", "subscribe to the channel " + channel);
        try {
            PrivateChannel privateChannel = AppApplication.getInstance().getPusher().subscribePrivate("private-" + channel);
            privateChannel.bind("gig", mSubscriptionEventListener);
            privateChannel.bind("client-gig", mSubscriptionEventListener);
            AppApplication.getInstance().addChannel(channel);
        } catch (Exception e) {
            Log.d("KINH", "Pusher:>>>>>ADD CHANNEL--ERROR=" + e.getMessage());
            Logs.log(e);
        }
        Log.d("KINH", "Pusher:>>>>>ADD CHANNEL--END");
    }

    public void removeChannelListener(String channel) {
        Logs.log("removeChannelListener", "UNsubscribe from the channel " + channel);
        if (AppApplication.getInstance().getListChannel() != null
                && AppApplication.getInstance().getListChannel().containsKey(channel)) {
            AppApplication.getInstance().getPusher().unsubscribe(channel);
            AppApplication.getInstance().removeChannel(channel);
        }
    }

    public void removeAllChannel() {
        Log.d("KINH", "Pusher:>>>>>REMOVE CHANNEL--START= " + AppApplication.getInstance().getListChannel().keySet().toString());
        Logs.log("removeAllChannel", "UNsubscribe from all channels");
        if (AppApplication.getInstance().getListChannel() != null) {
            for (String c : AppApplication.getInstance().getListChannel().keySet())
                AppApplication.getInstance().getPusher().unsubscribe(c);
            AppApplication.getInstance().getListChannel().clear();
        }
        Log.d("KINH", "Pusher:>>>>>REMOVE CHANNEL--END=");
    }

    public void publishMessage(String channel, JSONObject message) {
        Log.d("KINH", "Pusher.publishMessage.channel=" + channel + ".msg=" + message);
        Logs.log("publishMessage", "privateChannel - publish message \"" + message + "\" to the channel " + channel);
        try {
            privateChannel = AppApplication.getInstance().getPusher().getPrivateChannel("private-" + channel);
            if (privateChannel != null && privateChannel.isSubscribed()) {
                privateChannel.trigger("client-gig", message.toString());
            } else {
                privateChannel = AppApplication.getInstance().getPusher().subscribePrivate("private-" + channel, new PrivateChannelEventListener() {

                    @Override
                    public void onAuthenticationFailure(String s, Exception e) {
                        Log.e("KINH", "Pusher.publishMessage.onAuthenticationFailure=" + s + ".e=" + (e != null ? e.getMessage() : "NULL"));
                        Logs.log("Pusher-publishMessage", String.format("onAuthenticationFailure Authentication failure due to [%s], exception was [%s]", message, e));
                    }

                    @Override
                    public void onSubscriptionSucceeded(String channel) {
                        Log.d("KINH", "Pusher.publishMessage.onSubscriptionSucceeded.channel=" + channel + ".message=" + message.toString());
                        Logs.log("Pusher-publishMessage", "onSubscriptionSucceeded channel " + channel);
                        privateChannel.trigger("client-gig", message.toString());
                    }

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        Log.d("KINH", "Pusher.publishMessage.onEvent.channel=" + channelName + ".eventName=" + eventName + ".data=");
                        Logs.log("Pusher-publishMessage", "onEvent " + channelName + "; message is " + data);
                    }
                });
            }
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    public void setLastViewerLocation(LocationModel location) {
        mLastViewerLocation = location;
    }

    public LocationModel getLastViewerLocation() {
        return mLastViewerLocation;
    }

    public void setLastDroperatorLocation(LocationModel location) {
        mLastDroperatorLocation = location;
    }

    public LocationModel getLastDroperatorLocation() {
        return mLastDroperatorLocation;
    }

    public void setDocusignStatus(OperatorModel docusignStatus) {
        mDocusignStatus = docusignStatus;
    }

    public OperatorModel getDocusignStatus() {
        return mDocusignStatus;
    }

    public void setProcessingRequest(boolean mProcessingRequest) {
        this.mProcessingRequest = mProcessingRequest;
        Logs.log("REQUEST_OPERATOR", "setProcessingRequest: " + mProcessingRequest);
    }

    public boolean getProcessingRequest() {
        return mProcessingRequest;
    }

    public void setAccountListener(String id) {
        mAccountListenerId = id;
    }

    public String getAccountListener() {
        return mAccountListenerId;
    }

    private boolean checkMessageId(String messageId) {
        if (mListMessageId.containsKey(messageId)) return true;
        putMessageId(messageId);
        return false;
    }

    private void putMessageId(String messageId) {
        mListMessageId.put(messageId, messageId);
    }

    public void setSettingRadius(int mile) {
        mSettingRadius = mile;
        AccountSettingModel model = DSharePreference.getAccountSetting(mActivity);
        model.detectRadius = mile;
        DSharePreference.setAccountSetting(mActivity, model);
    }

    public int getSettingRadius() {
        return mSettingRadius;
    }

    public void setMainActivityActive(boolean enable) {
        mMainActivityActive = enable;
    }

    public void setBatterySwitchUserType(boolean mBatterySwitchUserType) {
        this.mBatterySwitchUserType = mBatterySwitchUserType;
    }

    public int getBatteryLevel() {
        return mBatteryLevelMain;
    }

    public void setCheckLocationService(boolean flag) {
        this.mCheckLocationService = flag;
    }

    public boolean isCheckLocationService() {
        return mCheckLocationService;
    }

    public boolean isCheckResumeProfile() {
        return mustResumeProfile;
    }

    public void setCheckResumeProfile(boolean isResume) {
        mustResumeProfile = isResume;
    }

    private void startLocationService() {
        if (getUserType() == UserType.OPERATOR) {
            Intent intent = new Intent(AppApplication.getInstance(), LocationService.class);
            intent.putExtra(LocationService.MODE_EXTRA, LocationManager.MODE.SCHEDULE_MODE);
            AppApplication.getInstance().startService(intent);
        }
    }

    private void restartLocationService() {
        if (getUserType() == UserType.OPERATOR) {
            Intent intent = new Intent(mActivity, LocationService.class);
            int userType = AppApplication.getInstance().getUserType();
            intent.putExtra(LocationService.MODE_EXTRA, userType == UserType.OPERATOR
                    ? LocationManager.MODE.DROPERATOR_MODE
                    : LocationManager.MODE.VIEWER_MODE);
            startService(intent);
        }
    }

    private void refreshDeviceToken() {
        if (!TextUtils.isEmpty(DSharePreference.getRegistrationId(mActivity)))
            Logs.log("MainActivity refreshDeviceToken", DSharePreference.getRegistrationId(mActivity));
        if (!TextUtils.isEmpty(DSharePreference.getAccessToken(mActivity)))
            Logs.log("MainActivity AccessToken", DSharePreference.getAccessToken(mActivity));
        networkManager.addDeviceToken(
                new DeviceTokenRequest(DSharePreference.getRegistrationId(mActivity)))
                .subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("addDeviceToken", networkManager.parseError(throwable));
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    private View.OnClickListener mHelpOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int userType = getUserType();
            int resourceId = R.drawable.ic_help_tutorial;
            if (userType == UserType.OPERATOR) {
                resourceId = R.drawable.ic_help_doper;
            }

            final FullScreenDialog mFullScreenDialog = new FullScreenDialog(mActivity);
            mFullScreenDialog.setBackgroundImage(resourceId);
            mFullScreenDialog.setTextButton(R.string.close);
            mFullScreenDialog.setButtonListener(v1 -> mFullScreenDialog.dismiss());
            mFullScreenDialog.setButtonBottomListener(v1 -> {
                mFullScreenDialog.dismiss();

                HelpCenterFragment helpFragment = new HelpCenterFragment();
                helpFragment.setTitle(getString(R.string.help_center));
                pushFragments(helpFragment);
            });
            mFullScreenDialog.setButtonBottomVisibility(View.VISIBLE);
            mFullScreenDialog.show();
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null && intent.getExtras() != null) {
                    Logs.log("MNotification ShowFrom", "BroadcastReceiver");
                    String fullData = intent.getStringExtra("PUSH_DATA");
                    JSONObject bundle = new JSONObject(fullData);
                    Logs.log("MNotification", bundle.toString());
                    int code = Integer.parseInt(bundle.getString(NotificationKey.CODE));
                    Logs.log("MNotification", "CODE: " + code);
                    Logs.log("MNotification : mMainActivityActive", "" + mMainActivityActive);
                    Logs.log("MNotification : mEnableStartStream", "" + mEnableStartStream);
                    Logs.log("MNotification : UserType", "" + getUserType());
                    Logs.log("MNotification ShowFrom", "BroadcastReceiver");
                    switch (code) {
                        case NotificationCode.STREAM_START:
                            NotificationModel notificationStart = new NotificationModel();
                            notificationStart.data = NotificationModel.Data.fromJSON(bundle.getString("data"));
                            notificationStart.messageId = bundle.getString("messageId");
                            notificationStart.token = bundle.getString("token");
                            notificationStart.apiKey = bundle.getString("apiKey");
                            notificationStart.session = bundle.getString("session");
                            processStreamStart(notificationStart);
                            break;
                        case NotificationCode.STREAM_FINISH:
                            NotificationModel notificationFinish = NotificationModel.fromJSON(bundle.toString());
                            notificationFinish.messageId = bundle.getString("messageId");
                            processStreamFinish(notificationFinish);
                            break;
                        case NotificationCode.REQUEST_OPERATOR:
                            NotificationModel notificationRequestOperator = new NotificationModel();
                            notificationRequestOperator.messageId = bundle.getString("messageId");
                            notificationRequestOperator.data = NotificationModel.Data.fromJSON(bundle.getString("data"));
                            notificationRequestOperator.id = bundle.getString("id");
                            processRequestOperator(notificationRequestOperator);
                            break;
                        case NotificationCode.ACCEPT_DENY_REQUEST:
                            NotificationModel notificationAcceptRequest = new NotificationModel();
                            notificationAcceptRequest.messageId = bundle.getString("messageId");
                            notificationAcceptRequest.data = NotificationModel.Data.fromJSON(bundle.getString("data"));
                            processStreamRequest(notificationAcceptRequest);
                            break;
                        case NotificationCode.EN_ROUTE:
                            NotificationModel notificationEnroute = new NotificationModel();
                            notificationEnroute.messageId = bundle.getString("messageId");
                            notificationEnroute.data = NotificationModel.Data.fromJSON(bundle.getString("data"));
                            processEnroute(notificationEnroute);
                            break;
                        case NotificationCode.OPERATOR_NOT_FOUND:
                            NotificationModel notificationNotFound = new NotificationModel();
                            notificationNotFound.messageId = bundle.getString("messageId");
                            processOperatorNotFound(notificationNotFound);
                            break;
                        case NotificationCode.NEW_OPERATOR:
                            break;
                        case NotificationCode.NEW_LOCATION:
                            processNewOperatorAppear(NotificationModel.fromJSON(fullData));
                            break;
                        case NotificationCode.SWITCH_MODE:
                            break;
                        case NotificationCode.STREAM_EXPIRE:
                            NotificationModel modelExpire = NotificationModel.fromJSON(bundle.toString());
                            modelExpire.messageId = bundle.getString("messageId");
                            processStreamExpire(modelExpire);
                            break;
                        case NotificationCode.VIEWER_REJECTED:
                        case NotificationCode.CANCELED:
                            NotificationModel modelCancel = NotificationModel.fromJSON(bundle.toString());
                            processCancel(modelCancel);
                            break;
                        case NotificationCode.DOCUMENT_SIGN_CANCEL:
                            Logs.log("MNotification ", "DOCUMENT_SIGN_CANCEL");
                            String userId = bundle.getString("accountId");
                            String message = bundle.getString("message");
                            progressDocusignCancel(userId, message);
                            break;
                        case NotificationCode.DOCUMENT_SIGN:
                            Logs.log("MNotification", "DOCUMENT_SIGN");
                            if (getUserType() == UserType.VIEWER && mMainActivityActive) {
                                if (!checkMessageId(bundle.getString("messageId"))) {
                                    showDialogDocumentSignApproved(bundle.getString("accountId"));
                                    // update menu
                                    getLeftMenu().checkOperatorStatus();
                                    Logs.log("MNotification ShowFrom", "BroadcastReceiver");
                                }
                            }
                            break;
                    }
                }
                AppApplication.getInstance().setPushIntent(null);
            } catch (Exception e) {
                Logs.log(e);
            }
        }

    };

    // Listener from Pusher
    private PrivateChannelEventListener mSubscriptionEventListener = new PrivateChannelEventListener() {

        @Override
        public void onEvent(String channel, String eventName, String data) {
            Logs.log("ShowFrom", "mSubscriptionEventListener");
            Logs.log("Pusher-SubscriptionEventListener", "successCallback mMyAccountId= " + mMyAccountId);
            Logs.log("Pusher-SubscriptionEventListener", "successCallback mSubscriptionEventListener channel: " + channel);
            Logs.log("Pusher-SubscriptionEventListener", "successCallback data= " + data);
            final String channelName = channel.replace("private-", "");
            if (data.startsWith("[") && data.endsWith("]"))
                return;
            final NotificationModel model = NotificationModel.fromJSON(data);
            if (model == null)
                return;

            if (model.code == NotificationCode.TESTING_PING)
                Toast.makeText(MainActivity.this, "PING PONG", Toast.LENGTH_SHORT).show();

            LogPusher logPusher = LogPusher.getNotification(model);
            if(logPusher != null){
                networkManager.logPusher(logPusher).subscribe();

            }

            if (!channelName.contains(mMyAccountId) && model.code != NotificationCode.NEW_LOCATION
                    && model.code != NotificationCode.NEW_OPERATOR) {
                Logs.log("SubscriptionEventListener", "Received message for a different recipient!");
                return;
            }

            Logs.log("mSubscriptionEventListener : mMainActivityActive", "" + mMainActivityActive);
            Logs.log("mSubscriptionEventListener : mEnableStartStream", "" + mEnableStartStream);
            Logs.log("mSubscriptionEventListener : UserType", "" + getUserType());

            runOnUiThread(() -> {
                try {
                    LogModel logModel = new LogModel();
                    logModel.level = "debug";
                    String mess = "";
                    switch (model.code) {
                        case NotificationCode.STREAM_START:
                            mess = "process stream ready";
                            processStreamStart(model);
                            break;
                        case NotificationCode.STREAM_FINISH:
                            mess = "StreamFinished";
                            processStreamFinish(model);
                            break;
                        case NotificationCode.REQUEST_OPERATOR:
                            mess = "processIncomingJob";
                            processRequestOperator(model);
                            break;
                        case NotificationCode.ACCEPT_DENY_REQUEST:
                            mess = "PROCESS DROPER ACCEPTED REQUEST";
                            processStreamRequest(model);
                            break;
                        case NotificationCode.EN_ROUTE:
                            mess = "process En Route";
                            processEnroute(model);
                            break;
                        case NotificationCode.OPERATOR_NOT_FOUND:
                            mess = "process Operator not found";
                            processOperatorNotFound(model);
                            break;
                        case NotificationCode.NEW_OPERATOR:
                            logModel = null;
                            break;
                        case NotificationCode.NEW_LOCATION:
                            processNewOperatorAppear(model);
                            logModel = null;
                            break;
                        case NotificationCode.STREAM_EXPIRE:
                            mess = "process Stream Expire";
                            processStreamExpire(model);
                            break;
                        case NotificationCode.VIEWER_REJECTED:
                            mess = "process Stream Rejected";
                            processCancel(model);
                            break;
                        case NotificationCode.CANCELED:
                            mess = "process Stream Cancel";
                            processCancel(model);
                            break;
                        case NotificationCode.SWITCH_MODE:
                            if (getUserType() == UserType.VIEWER && mMainActivityActive && !mProcessingRequest) {
                                if (!checkMessageId(model.messageId)) {
                                    handlerSwitchMode(model);
                                }
                            }
                            logModel = null;
                            break;
                        case NotificationCode.DOCUMENT_SIGN:
                            if (getUserType() == UserType.VIEWER && mMainActivityActive) {
                                if (!checkMessageId(model.messageId)) {
                                    showDialogDocumentSignApproved(model.accountId);
                                    Logs.log("ShowFrom", "SocketCallback");
                                }
                            }
                            logModel = null;
                            break;
                        case NotificationCode.DOCUMENT_SIGN_CANCEL:
                            progressDocusignCancel(model.accountId, model.message);
                            logModel = null;
                            break;
                    }
                    if (logModel != null) {
                        logModel.message = "Android: " + mess + "- Channel: " + channel;
                        logModel.data = data;
                        logToServer(logModel);
                    }
                } catch (Exception e) {
                    Logs.log(e);
                }
            });

        }

        @Override
        public void onAuthenticationFailure(String message, Exception e) {
            Logs.log("Pusher-addChannelListener", String.format("onAuthenticationFailure Authentication failure due to [%s], exception was [%s]", message, e));
        }

        @Override
        public void onSubscriptionSucceeded(String channel) {
            Logs.log("Pusher-addChannelListener", "onSubscriptionSucceeded " + channel);
        }
    };

    /**
     * Stream Start
     */
    private void processStreamStart(NotificationModel notificationModel) {
        Logs.log(TAG, "processStreamStart");
        if (getUserType() == UserType.VIEWER && mMainActivityActive && mEnableStartStream) {
            if (!checkMessageId(notificationModel.messageId)) {
                MapFragment mapFragment = getMapFragment();
                if (mapFragment != null) {
                    DataInputStreamModel dataInputStream = DataInputStreamModel.fromBundle(notificationModel.toBundle());
                    dataInputStream.chatChannel = mapFragment.getChatChannel();
                    dataInputStream.chatName = mapFragment.getChatName();
                    dataInputStream.chatAvatar = mapFragment.getChatAvatar();
                    dataInputStream.chatData = mapFragment.getChatData();
                    navigateViewerStreamActivity(dataInputStream);
                }
            } else {
                Logs.log("processStreamStart", "NO START CAUSE checkMessageId(messageId) TRUE");
            }
        } else {
            Logs.log("processStreamStart", "NO START CAUSE " + getUserType() + " - mMainActivityActive "
                    + mMainActivityActive + " - mEnableStartStream " + mEnableStartStream);
        }
    }

    /**
     * Stream Finish
     */
    private void processStreamFinish(NotificationModel notificationModel) {
        Logs.log(TAG, "processStreamFinish");
        if (!mMainActivityActive && mProcessingRequest) {
            if (!checkMessageId(notificationModel.messageId)) {
                Logs.log("processStreamFinish", "SocketCallback: STREAM_FINISH");
                Bundle extras = new Bundle();
                extras.putInt(NotificationKey.CODE, NotificationCode.STREAM_FINISH);
                extras.putString(NotificationModel.class.getSimpleName(), notificationModel.toJSON());
                Intent intent = new Intent();
                intent.setAction(Constants.NOTIFICATION_STREAM);
                intent.putExtras(extras);
                sendBroadcast(intent);
            } else {
                Logs.log("processStreamFinish", "NO START CAUSE checkMessageId(model.messageId) TRUE");
            }
        } else {
            Logs.log("processStreamFinish", "NO START CAUSE - mMainActivityActive "
                    + mMainActivityActive + " - mProcessingRequest " + mProcessingRequest);
        }
        // Disconnecting from the GIG channel
        MessageManager.getInstance().unsubscribeFromAllGigs();
    }

    /**
     * Stream Expire
     */
    private void processStreamExpire(NotificationModel notificationModel) {
        Logs.log(TAG, "processStreamExpire");
        if (!mMainActivityActive && mProcessingRequest) {
            if (!checkMessageId(notificationModel.messageId)) {
                Bundle extras = new Bundle();
                extras.putInt(NotificationKey.CODE, NotificationCode.STREAM_EXPIRE);
                extras.putString(NotificationModel.class.getSimpleName(), notificationModel.toJSON());
                Intent intent = new Intent();
                intent.setAction(Constants.NOTIFICATION_STREAM);
                intent.putExtras(extras);
                sendBroadcast(intent);
            } else {
                Logs.log("processStreamExpire", "NO START CAUSE checkMessageId(model.messageId) TRUE");
                resetUserState(false);
            }
        } else {
            resetUserState(true);
        }
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.dismissDialog();
            mapFragment.removeTimeOut();
        }
        // Disconnecting from the GIG channel
        MessageManager.getInstance().unsubscribeFromAllGigs();
    }

    /*
    * Process Request Operator
    */
    private void processRequestOperator(NotificationModel notificationModel) {
        Logs.log(TAG, "processRequestOperator");
        if (getUserType() == UserType.OPERATOR && !mProcessingRequest && mMainActivityActive) {
            if (!checkMessageId(notificationModel.messageId)) {
                setProcessingRequest(true);
                Logs.log("processRequestOperator", "set progress true 2");
                setAccountListener(notificationModel.id);
                String viewerId = notificationModel.id;
                String gigsId = notificationModel.data.gig.id;
                LocationModel location = new LocationModel();
                location.latitude = notificationModel.data.gig.latitude;
                location.longitude = notificationModel.data.gig.longitude;
                showDialogIncomingJob(gigsId, viewerId, location);
                Logs.log("processRequestOperator", "showDialogIncomingJob()");
            } else {
                Logs.log("processRequestOperator", "Fail messages ID");
            }
        } else {
            Logs.log("processRequestOperator", "Fail if flag");
        }
    }

    /*
    * Process Accept/Deny Request
    */
    private void processStreamRequest(NotificationModel notificationModel) {
        Logs.log(TAG, "processStreamRequest");
        if (getUserType() == UserType.VIEWER && mProcessingRequest && mMainActivityActive) {
            if (TextUtils.isEmpty(getAccountListener()) && !checkMessageId(notificationModel.messageId)) {
                setAccountListener(notificationModel.id);
                String action = "reject";
                if (notificationModel.data.gig.type.equalsIgnoreCase("real")) {
                    action = "accept";
                }
                Bundle bundle = new Bundle();
                bundle.putString("action", action);
                bundle.putString("gigsId", notificationModel.data.gig.id);
                acceptDenyRequest(bundle);
            } else {
                Logs.log("processStreamRequest", "ACCEPT_DENY_REQUEST FAIL: MessageID exists");
                Logs.log("processStreamRequest", "ACCEPT_DENY_REQUEST FAIL: getAccountListener : " + TextUtils.isEmpty(getAccountListener()));
            }
        } else {
            Logs.log("processStreamRequest", "ACCEPT_DENY_REQUEST Fail: " + mProcessingRequest + " - " + mMainActivityActive);
        }
    }

    /*
    * Process Enroute
    */
    private void processEnroute(NotificationModel notificationModel) {
        Logs.log(TAG, "processEnroute");
        if (getUserType() == UserType.OPERATOR) {
            //Connecting to the GIG channel
            Logs.log("processEnroute", "subscribeToGigChannel id: " + notificationModel.data.gig.id);
            MessageManager.getInstance().subscribeToGigChannel(notificationModel.data.gig.id);
            if (mMainActivityActive && mProcessingRequest) {
                if (!checkMessageId(notificationModel.messageId)) {
                    handlerOperatorEnRoute(notificationModel.data.gig.id);
                    // Create chatName and Chatavartar
                    String chatName = notificationModel.data.gig.customer.firstName + " " + notificationModel.data.gig.customer.lastName;
                    String chatAvatar = "";
                    if (notificationModel.data.gig.customer.profileImage != null && !TextUtils.isEmpty(notificationModel.data.gig.customer.profileImage.location)) {
                        chatAvatar = notificationModel.data.gig.customer.profileImage.location;
                    }
                    // Show chat button
                    showChatButtonOnMap(notificationModel.data.chatChannel, chatName, chatAvatar, notificationModel.data.gig.id);
                } else {
                    Logs.log("processEnroute", "EN_ROUTE: fail for messageId");
                }
            } else {
                Logs.log("processEnroute", "EN_ROUTE: fail for FLAG");
            }
        } else {
            Logs.log("processEnroute", "NOT DO ANYTHINGS CAUSE BY NOT OPERATOR");
        }
    }

    /*
    * Process Operator not found
    */
    private void processOperatorNotFound(NotificationModel notificationModel) {
        Logs.log(TAG, "processOperatorNotFound");
        if (getUserType() == UserType.VIEWER) {
            if (mProcessingRequest && mMainActivityActive) {
                if (!checkMessageId(notificationModel.messageId)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("message", getString(R.string.message_deny_request_from_droperator));
                    operatorCancelRequest(bundle);
                } else {
                    Logs.log("processOperatorNotFound", "fail for messageId");
                }
            } else {
                Logs.log("processOperatorNotFound", "fail for FLAG");
            }
        } else {
            Logs.log("processEnroute", "NOT DO ANYTHINGS CAUSE BY NOT VIEWER");
        }
    }

    /*
    * Process New Operator Appear
    */
    private void processNewOperatorAppear(NotificationModel notificationModel) {
        Logs.log(TAG, "processNewOperatorAppear");
        if (getUserType() == UserType.VIEWER) {
            if (mMainActivityActive) {
                Bundle bundle = new Bundle();
                bundle.putString("operatorId", notificationModel.operatorId);
                bundle.putString("location", notificationModel.location.toJSON());
                bundle.putString("settingRadius", notificationModel.settingRadius);
                handlerReceiveLocationFromOperator(bundle);
            } else {
                Logs.log("processOperatorNotFound", "fail for FLAG");
            }
        } else {
            Logs.log("processEnroute", "NOT DO ANYTHINGS CAUSE BY NOT VIEWER");
        }
    }

    /*
   * Process cancel or reject stream request
   */
    private void processCancel(NotificationModel notificationModel) {
        if (!checkMessageId(notificationModel.messageId)) {
            Logs.log(TAG, "processCancel");
            // Disconnecting from the GIG channel
            MessageManager.getInstance().unsubscribeFromAllGigs();
            resetUserState(true);
        } else {
            Logs.log("processCancel", "fail for messageId");
        }
    }

    private void resetUserState(boolean isNeedShowDialog) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            mapFragment.resetUserState(AppApplication.getInstance().getUserType()
                    == UserType.VIEWER ? RequestCode.VIEWER_STREAM : RequestCode.OPERATOR_STREAM);
        }

        if (isNeedShowDialog) {
            if (!isStopActivity) {
                AlertDialog alertDialog = new AlertDialog(this);
                alertDialog.setMessageDialog(R.string.live_stream_cancelled);
                alertDialog.show();
                alertDialog.setOnDismissListener(dialogInterface -> checkGisStatus());
            }
        }
    }

    private void progressDocusignCancel(String userId, String messages) {
        String mes = getResources().getString(R.string.docusign_form_decline) + messages;
        // update model
        String json = DSharePreference.getProfile(mActivity);
        VerifyModel verify = new Gson().fromJson(json, VerifyModel.class);
        if (!TextUtils.isEmpty(userId) && verify.account.id.equalsIgnoreCase(userId)) { // Check UserId of Notification match with current user
            verify.account.operator.status = OperatorStatus.CANCELED;
            DSharePreference.setProfile(mActivity, verify.toJSON());
            AppApplication.getInstance().setOperator(false);
            DSharePreference.setOperator(mActivity, false);
            showAlertDialog("", mes);
        }
    }

    private BroadcastReceiver mNetworkInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in air plan mode it will be null
            boolean isOnline = (netInfo != null && netInfo.isConnected());
            if (isOnline) {
                if (mAutoDialogNetwork == null) return;
                if (mAutoDialogNetwork.isShowing()) {
                    mAutoDialogNetwork.dismiss();
                }
                mAutoDialogNetwork = null;
                return;
            } else {
                if (mAutoDialogNetwork == null) {
                    mAutoDialogNetwork = new AutoDialog(mActivity);
                    mAutoDialogNetwork.setCancelable(false);
                    mAutoDialogNetwork.setTitleDialog(R.string.network_problem);
                    mAutoDialogNetwork.setMessageDialog(R.string.please_connect_to_continue);
                    mAutoDialogNetwork.setAutoDismiss(false);
                    mAutoDialogNetwork.show();
                    return;
                }
            }
        }
    };

    private BroadcastReceiver mBatteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBatteryLevelMain = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            Logs.log("mBatteryChangeReceiver onReceive", "BatteryLevel: " + mBatteryLevelMain);
            Logs.log("mBatteryChangeReceiver onReceive", "IsCharging: " + isCharging);
            MapFragment mapFragment = getMapFragment();
            if (mapFragment != null) {
                mapFragment.setIsCharging(isCharging);
            }
            mDeviceCharging = isCharging;
            if (!isCharging) {
                if (mapFragment != null) {
                    mapFragment.handlerLowBattery(mBatteryLevelMain);
                } else {
                    if (getUserType() == UserType.VIEWER) return;
                    if (mBatteryLevelMain > 15 || mBatteryLevelMain < 10) return;
                    if (mBatteryLevelMain == 10) {
                        mBatterySwitchUserType = true;
                        return;
                    }

                    if (mShowDialogBatteryMain && !mMapBatteryMain.containsKey("operator_15")) {
                        mShowDialogBatteryMain = false;
                        mMapBatteryMain.put("operator_15", "operator_15");
                        final AlertDialog mDialog = new AlertDialog(mActivity);
                        mDialog.setTitleDialog(R.string.title_low_battery_droperator_15);
                        mDialog.setMessageDialog(R.string.message_low_battery_booted_out);
                        mDialog.setTitleButton(R.string.ok_cap);
                        mDialog.setButtonClick(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
                        mDialog.show();
                    }
                }
            } else {
                if (mBatterySwitchUserType) mBatterySwitchUserType = false;
                if (!mShowDialogBatteryMain) mShowDialogBatteryMain = true;
                if (mMapBatteryMain.containsKey("operator_15"))
                    mMapBatteryMain.remove("operator_15");
                if (mBatteryLevelMain > 15 && mapFragment != null) {
                    mapFragment.resetParamsBattery();
                }
            }
        }
    };

    private BroadcastReceiver getPermissionReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.GPS_PERMISSION_FILTER)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.REQUEST_CODE_ASK_GPS_PERMISSIONS);
            } else if (intent.getAction().equals(Constants.PHOTO_PERMISSION_FILTER)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS);
            } else if (intent.getAction().equals(Constants.MIC_PERMISSION_FILTER)) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        Constants.REQUEST_CODE_ASK_MIC_PERMISSIONS);
            } else if (intent.getAction().equals(Constants.STORE_PERMISSION_FILTER)) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        Constants.REQUEST_CODE_ASK_STORE_PERMISSIONS);
            }
        }
    };

    @Override
    public void onBecameForeground() {
        Logs.log(TAG, "onBecameForeground");
    }

    @Override
    public void onBecameBackground() {
        Logs.log(TAG, "onBecameBackground");
        startLocationService();
    }

    public void logToServer(LogModel model) {
        model.level = AppApplication.getInstance().getAccountId();
        model.data = model.data + " - VERSION: " + getVersionNames();
        networkManager.log(model)
                .subscribe(ignore -> logResponseSuccess(), throwable -> logResponseError());
    }

    public void logResponseSuccess() {
    }

    public void logResponseError() {
    }

    public String getVersionNames() {
        PackageInfo pInfo = null;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        return version;
    }

    public void showDialogResumeEnroute() {
        Logs.log(TAG, "showDialogResumeEnroute");
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null && mapFragment.getResumeCheckData() != null) {
            ResumeCheckModel resumeCheckModel = mapFragment.getResumeCheckData();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showConfirmDialog("", getString(R.string.enroute_resume_popup), getString(R.string.ok), getString(R.string.cancel_gig)
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (getUserType() == UserType.OPERATOR) {
                                        // Draw Viewer on map
                                        mapFragment.setViewerLocation(resumeCheckModel.latitude, resumeCheckModel.longitude);
                                        handlerOperatorEnRoute(resumeCheckModel.id);
                                        String chatName = resumeCheckModel.customer.firstName + " " + resumeCheckModel.customer.lastName;
                                        String chatAvatar = "";
                                        if (resumeCheckModel.customer.profileImage != null && !TextUtils.isEmpty(resumeCheckModel.customer.profileImage.location)) {
                                            chatAvatar = resumeCheckModel.customer.profileImage.location;
                                        }
                                        showChatButtonOnMap(resumeCheckModel.chatChannel, chatName, chatAvatar, resumeCheckModel.id);
                                    } else {
                                        String chatName = resumeCheckModel.operator.firstName + " " + resumeCheckModel.operator.lastName.substring(0, 1);
                                        String chatAvatar = "";
                                        if (resumeCheckModel.operator.profileImage != null && !TextUtils.isEmpty(resumeCheckModel.operator.profileImage.location)) {
                                            chatAvatar = resumeCheckModel.operator.profileImage.location;
                                        }
                                        showChatButtonOnMap(resumeCheckModel.chatChannel, chatName, chatAvatar, resumeCheckModel.id);
                                        mapFragment.addMarkerLocationViewer(resumeCheckModel.latitude, resumeCheckModel.longitude);
                                        mapFragment.setEnRouteMode(true);
                                        mapFragment.showEnrouteInfoView(resumeCheckModel.operator.firstName, resumeCheckModel.operator.lastName, resumeCheckModel.operator.operatorRating, resumeCheckModel.operator.profileImage != null ? resumeCheckModel.operator.profileImage.location : "");
                                        mapFragment.createMainOperatorProcessOnMap(resumeCheckModel.id, resumeCheckModel.operator.firstName, resumeCheckModel.operator.lastName, resumeCheckModel.metaData.operatorLastData.preciseLatitude, resumeCheckModel.metaData.operatorLastData.preciseLongitude);

                                        LocationModel fromLocation = new LocationModel();
                                        fromLocation.latitude = resumeCheckModel.metaData.operatorLastData.preciseLatitude;
                                        fromLocation.longitude = resumeCheckModel.metaData.operatorLastData.preciseLongitude;

                                        LocationModel toLocation = new LocationModel();
                                        toLocation.latitude = resumeCheckModel.latitude;
                                        toLocation.longitude = resumeCheckModel.longitude;

                                        mapFragment.drawMapEnRoute(fromLocation, toLocation);
                                        mapFragment.updateInfoDistanceETA(fromLocation, toLocation);
                                    }
                                    // Connect to gig channel socket
                                    MessageManager.getInstance().subscribeToGigChannel(resumeCheckModel.id);
                                    hideConfirmDialog();
                                }
                            }, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    networkManager.cancelStream(resumeCheckModel.id)
                                            .subscribe(endStreamModel -> {
                                                try {
                                                    LogModel logModel = new LogModel();
                                                    logModel.message = "Android: Viewer - CANCEL GIGS RESPONSE - URL: http://apidev.dropininc.com/gigs/cancel/" + resumeCheckModel.id;
                                                    logModel.data = "DATA: " + endStreamModel.toJSON();
                                                    logToServer(logModel);
                                                } catch (Exception e) {
                                                    Logs.log(e);
                                                }
                                            }, throwable -> {
                                                AppApplication.getInstance().logErrorServer("cancelStream/" + resumeCheckModel.id, networkManager.parseError(throwable));
                                            });
                                    hideConfirmDialog();
                                }
                            });
                }
            });
        } else {
            Logs.log("w", TAG, "mapFragment Null || apFragment.getResumeCheckData() Null");
        }
    }

    public void setEnroute(boolean isEnroute) {
        this.mEnRouteMode = isEnroute;
    }

    public boolean isEnroute() {
        return mEnRouteMode;
    }

    public boolean isDeviceCharging() {
        return mDeviceCharging;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){

            if ( keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                AppApplication.getInstance().adjustVolumeRaise();
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ) {
                AppApplication.getInstance().adjustVolumeLow();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}

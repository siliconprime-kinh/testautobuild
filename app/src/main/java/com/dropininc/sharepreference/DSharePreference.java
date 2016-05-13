package com.dropininc.sharepreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropininc.AppApplication;
import com.dropininc.Constants;
import com.dropininc.interfaces.UserType;
import com.dropininc.model.AccountSettingModel;
import com.dropininc.model.LocationModel;


public class DSharePreference {

    private static final String APPS = "DropIn";
    private static final String IS_FIRST_TIME_USE = "IS_FIRST_TIME_USE";
    private static final String LOGIN = "login";
    private static final String GCM_REGISTRATION = "gcm_registration";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String PROFILE = "PROFILE";
    private static final String ACCEPT_LOCATION = "ACCEPT_LOCATION";
    private static final String HELP_USER_TUTORIAL = "HELP_USER_TUTORIAL";
    private static final String HELP_OPERATOR_TUTORIAL = "HELP_OPERATOR_TUTORIAL";
    private static final String DEFAULT_LOCATION = "DEFAULT_LOCATION";
    private static final String ALLOW_NOTIFICATION = "ALLOW_NOTIFICATION";
    private static final String ALLOW_LOCATION = "ALLOW_LOCATION";
    private static final String HELP_DROPERATOR_TUTORIAL = "HELP_DROPERATOR_TUTORIAL";
    private static final String HELP_VIEWER_TUTORIAL = "HELP_VIEWER_TUTORIAL";
    private static final String PERMISSION_CAMERA = "PERMISSION_CAMERA";
    private static final String ACCOUNT_SETINNG = "ACCOUNT_SETINNG";
    private static final String IS_OPERATOR = "IS_OPERATOR";
    private static final String IS_CAMERA = "isCameraCapabilities";
    private static final String IS_GET_CAMERA = "isGetCameraCapabilities";
    private static final String MY_REFERRAL_CODE = "myReferralCode";

    private static final String AVATAR_URL = "AVATAR_URL";

    public static String getTMPAvatarUrl(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(AVATAR_URL, "");
    }

    public static void setTMPAvatarUrl(Context mContext, String version) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(AVATAR_URL, version);
        editor.commit();
    }

    public static boolean isGetCameraCapabilities(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(IS_GET_CAMERA, false);
    }

    public static void setIsGetCameraCapabilities(Context mContext, boolean flag) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(IS_GET_CAMERA, flag);
        editor.commit();
    }

    public static boolean isCameraCapabilities(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(IS_CAMERA, false);
    }

    public static void setCameraCapabilities(Context mContext, boolean flag) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(IS_CAMERA, flag);
        editor.commit();
    }

    public static boolean isLogin(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(LOGIN, false);
    }

    public static String getRegistrationId(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS, 0);
        return ref.getString(GCM_REGISTRATION, null);
    }

    public static void setRegistrationId(Context mContext, String registrationId) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS, 0);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(GCM_REGISTRATION, registrationId);
        editor.commit();
    }

    public static void setAccessToken(Context context, String token) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(ACCESS_TOKEN, "");
    }

    public static void setIsFirstTimeUse(Context context, boolean flag) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(IS_FIRST_TIME_USE, flag);
        editor.commit();
    }

    public static boolean isFirstTimeUse(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(IS_FIRST_TIME_USE, true);
    }

    public static String getProfile(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(PROFILE, null);
    }

    public static void setProfile(Context mContext, String profile) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(PROFILE, profile);
        editor.commit();
    }

    public static void setHelpTutorial(Context context, boolean flag, int userType) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        switch (userType) {
            case UserType.VIEWER:
                editor.putBoolean(HELP_USER_TUTORIAL, flag);
                break;
            case UserType.OPERATOR:
                editor.putBoolean(HELP_OPERATOR_TUTORIAL, flag);
                break;
        }
        editor.commit();
    }

    public static boolean isHelpTutorial(Context context, int userType) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        boolean flag = false;
        switch (userType) {
            case UserType.VIEWER:
                flag = ref.getBoolean(HELP_USER_TUTORIAL, true);
                break;
            case UserType.OPERATOR:
                flag = ref.getBoolean(HELP_OPERATOR_TUTORIAL, true);
                break;
        }
        return flag;
    }

    public static LocationModel getDefaultLocation(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return LocationModel.fromJSON(ref.getString(DEFAULT_LOCATION, ""));
    }

    public static void setDefaultLocation(Context mContext, LocationModel location) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(DEFAULT_LOCATION, location.toJSON());
        editor.commit();
    }

    public static void clearAll(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        ref.edit().clear().apply();
    }

    public static boolean isAllowNotification(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(ALLOW_NOTIFICATION, false);
    }

    public static void setAllowNotification(Context mContext, boolean flag) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(ALLOW_NOTIFICATION, flag);
        editor.commit();
    }

    public static boolean isAllowLocation(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(ALLOW_LOCATION, false);
    }

    public static void setAllowLocation(Context mContext, boolean flag) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(ALLOW_LOCATION, flag);
        editor.commit();
    }

    public static void logout(Context mContext) {
        setAccessToken(mContext, "");
        setProfile(mContext, "");
    }

    public static void setIsShowDroperatorHelp(Context context, boolean flag) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(HELP_DROPERATOR_TUTORIAL, flag);
        editor.commit();
    }

    public static boolean isShowDroperatorHelp(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(HELP_DROPERATOR_TUTORIAL, true);
    }

    public static void setIsShowViewerHelp(Context context, boolean flag) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(HELP_VIEWER_TUTORIAL, flag);
        editor.commit();
    }

    public static boolean isShowViewerHelp(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(HELP_VIEWER_TUTORIAL, true);
    }

    public static void setPermissionCamera(Context context, boolean flag) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(PERMISSION_CAMERA, flag);
        editor.commit();
    }

    public static boolean isPermissionCamera(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(PERMISSION_CAMERA, false);
    }

    public static AccountSettingModel getAccountSetting(Context mContext) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return AccountSettingModel.fromJSON(ref.getString(ACCOUNT_SETINNG, ""));
    }

    public static void setAccountSetting(Context mContext, AccountSettingModel account) {
        SharedPreferences ref = mContext.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(ACCOUNT_SETINNG, account.toJSON());
        editor.commit();
    }

    public static void setOperator(Context context, boolean flag) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putBoolean(IS_OPERATOR, flag);
        editor.commit();
    }

    public static boolean isOperator(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getBoolean(IS_OPERATOR, false);
    }

    public static void setDebugURL(Context context, String url) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString("URL", url);
        editor.commit();
    }

    public static String getDebugURL(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString("URL", Constants.SERVER_URL);
    }

    public static void setUserType(Context context, int userType) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putInt("userType", userType);
        editor.commit();
    }

    public static int getUserType(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getInt("userType", UserType.VIEWER);
    }

    public static void setSettingRadius(Context context, int settingRadius) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putInt("settingRadius", settingRadius);
        editor.apply();
    }

    public static int getSettingRadius(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getInt("settingRadius", 10);
    }

    public static void setMyReferralCode(Context context, String code) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(MY_REFERRAL_CODE, code);
        editor.apply();
    }

    public static String getMyReferralCode(Context context) {
        SharedPreferences ref = context.getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(MY_REFERRAL_CODE, "");
    }

    /*Kinh log*/
    private static final String LOG_NETWORK = "log_network";
    private static final String LOG_PUSHER = "log_pusher";
    private static final String LOG_ERROR_SERVER = "log_error_server";

    public static void setLogNetWork(String logNetWork) {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(LOG_NETWORK, logNetWork);
        editor.apply();
    }

    public static String getLogNetWork() {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(LOG_NETWORK, "");
    }

    public static void setLogPuhser(String logPuhser) {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(LOG_PUSHER, logPuhser);
        editor.apply();
    }

    public static String getLogPusher() {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(LOG_PUSHER, "");
    }

    public static void setLogErrorServer(String logErrorServer) {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ref.edit();
        editor.putString(LOG_ERROR_SERVER, logErrorServer);
        editor.apply();
    }

    public static String getLogErrorServer() {
        SharedPreferences ref = AppApplication.getInstance().getSharedPreferences(APPS,
                Context.MODE_PRIVATE);
        return ref.getString(LOG_ERROR_SERVER, "");
    }
}

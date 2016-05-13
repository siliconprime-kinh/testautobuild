package com.dropininc;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.dropininc.activity.MainActivity;
import com.dropininc.config.AppComponent;
import com.dropininc.config.AppModule;
import com.dropininc.config.DaggerAppComponent;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.model.LocationModel;
import com.dropininc.model.LogNetModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.RetrofitErrorModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.AppVisibilityState;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OneSignal;
import com.pubnub.api.Pubnub;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;


public class AppApplication extends MultiDexApplication implements ConnectionEventListener {

    private static String TAG = AppApplication.class.getSimpleName();

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "KRf1yOS20jSPeaEGK4LnYpHxg";
    private static final String TWITTER_SECRET = "0esx1xuHISuIynjzOCw3ptKIPZsiXjhzydJlUPMY18LMRmeuSf";

    private boolean isRunning = false;
    private boolean mIsGetLocation = true;
    private boolean mIsCheckPayment = false;
    private boolean mIsOperator = false;
    private boolean isApplicationRunning = false;
    private String mAccountId;
    private String mCurrentGigsId;
    private int userType = -1;
    private Map<String, String> mListChannel;
    private LocationModel mLocationDefault;

    private Pubnub mPubNub;
    private AppComponent appComponent;
    private boolean reconnectOnCompletedDisconnection = false;

    private Pusher mPusher;

    private Intent pushIntent = null;

    public static class Config {
        public static final boolean DEVELOPER_MODE = false;
    }

    private static AppApplication mInstance;

    public static AppComponent appComponent() {
        return mInstance.appComponent;
    }

    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();

        initAppComponent();

        AppVisibilityState.init(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        if (Config.DEVELOPER_MODE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyDeath().build());
        }
        mInstance = this;
        initImageLoader(getApplicationContext());
//        initPubNub();
        Branch.getAutoInstance(this);

        // init One Signal
        initOneSignal();

        initLog();/*KINH log*/
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void initAppComponent() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
    }

    public void addAppVisibilityStateListener(AppVisibilityState.Listener listener) {
        AppVisibilityState.get().addListener(listener);
    }

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheSize(30 * 1024 * 1024)
                .memoryCacheSize(10 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    public static synchronized AppApplication getInstance() {
        return mInstance;
    }

    public void setRunning(boolean flag) {
        isRunning = flag;
    }

    public boolean getRunning() {
        return isRunning;
    }

    public void setAppicationRunning(boolean flag) {
        isApplicationRunning = flag;
    }

    public boolean isApplicationRunning() {
        return isApplicationRunning;
    }

    public boolean isGetLocation() {
        return mIsGetLocation;
    }

    public void setGetLocation(boolean flag) {
        this.mIsGetLocation = flag;
    }

    public void setAccountId(String accountId) {
        this.mAccountId = accountId;
        if (mPubNub == null)
            initPubNub();
        mPubNub.setUUID(mAccountId + ":" + mPubNub.uuid());
        Logs.log("pubnub", "set UUID to " + mAccountId + ":" + mPubNub.uuid());
    }

    public String getAccountId() {
        return mAccountId;
    }

    public void setCheckPayment(boolean flag) {
        mIsCheckPayment = flag;
    }

    public boolean isCheckPayment() {
        return mIsCheckPayment;
    }

    public Pusher getPusher() {
        if (mPusher == null)
            initPusher();
        return mPusher;
    }

    private void initPusher() {
        reconnectOnCompletedDisconnection = true;
        HttpAuthorizer authorizer = new HttpAuthorizer(com.dropininc.Constants.SERVER_URL + "notifications/authchannel");
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer);
        mPusher = new Pusher(com.dropininc.Constants.PUSHER_KEY, options);
        connectPusher();
    }

    public void connectPusher() {
        Logs.log("i", TAG, "connectPusher");
        if (mPusher != null)
            mPusher.connect(this);
        else
            initPusher();
    }

    @Override
    public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        String socketId = mPusher.getConnection().getSocketId();
        logPusher(socketId, connectionStateChange);
        if (!TextUtils.isEmpty(socketId))
            Logs.log(TAG, " C: socketId " + socketId);
        Logs.log("i", TAG, String.format("Connection state changed from %s to %s.",
                connectionStateChange.getPreviousState(), connectionStateChange.getCurrentState()));
        if (connectionStateChange.getCurrentState().equals(ConnectionState.DISCONNECTED) && reconnectOnCompletedDisconnection) {
            reconnectOnCompletedDisconnection = false;
            if (mPusher != null) {
                mPusher.connect();
            }
        }
    }

    @Override
    public void onError(String message, String code, Exception e) {
        if (!TextUtils.isEmpty(message))
            Logs.log("AppApplication", "onError: message " + message);
        if (!TextUtils.isEmpty(code))
            Logs.log("AppApplication", "onError: code " + code);
        if (e != null)
            Logs.log("AppApplication", "onError: Exception " + e.getMessage());
        mPusher.connect();
    }

    public void disconnectPusher() {
        Logs.log("i", TAG, "disconnectPusher");
        if (mPusher != null)
            mPusher.disconnect();
    }

    public Pubnub getPubnub() {
        if (mPubNub == null)
            initPubNub();
        return mPubNub;
    }

    public void addChannel(String channel) {
        if (mListChannel == null) {
            mListChannel = new HashMap<>();
        }
        mListChannel.put(channel, channel);
    }

    public void removeChannel(String channel) {
        mListChannel.remove(channel);
    }

    public Map<String, String> getListChannel() {
        return mListChannel;
    }

    public void setLocationDefault(LocationModel location) {
        this.mLocationDefault = location;
    }

    public LocationModel getLocationDefault() {
        return mLocationDefault;
    }

    public void setOperator(boolean isOperator) {
        this.mIsOperator = isOperator;
        if (isOperator) {
            String json = DSharePreference.getProfile(this);
            VerifyModel model = VerifyModel.fromJSON(json);
            if (model.account.operator == null) {
                OperatorModel opa = MainActivity.getInstance().getDocusignStatus();
                model.account.operator = opa;
            }
            model.account.operator.status = "approved";
            DSharePreference.setProfile(this, model.toJSON());
        }
    }

    public boolean isOperator() {
        return mIsOperator;
    }

    public void setUserType(int userType) {
        this.userType = userType;
        DSharePreference.setUserType(this, userType);
    }

    public int getUserType() {
        if (userType == -1) userType = DSharePreference.getUserType(this);
        return userType;
    }

    public void updateUserTypeWhenLogOut() {
        this.userType = -1;
        mIsOperator = false;
    }

    public void setCurrentGigsId(String gigsId) {
        this.mCurrentGigsId = gigsId;
    }

    public String getCurrentGigsId() {
        return mCurrentGigsId;
    }

    private void initPubNub() {
        mPubNub = new Pubnub(com.dropininc.Constants.PUBNUB_PUBLISH_KEY,
                com.dropininc.Constants.PUBNUB_SUBCRIBE_KEY);
//        mPubNub = new Pubnub(DSharePreference.getPubnubPublishKey(getApplicationContext()),
//                DSharePreference.getPubnubSubcribeKey(getApplicationContext()));
        mPubNub.setHeartbeat(3 * 30);
        mPubNub.setHeartbeatInterval(15);
    }

    private void initOneSignal() {
//        OneSignal.startInit(this)
//                .setNotificationOpenedHandler(new MyNotificationOpenedHandler())
//                .setAutoPromptLocation(true)
//                .init();

//        OneSignal.startInit(this).init();
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.WARN);
    }

    private class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

        @Override
        public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
            Logs.log("MNotification", "additionalData: " + additionalData.toString());
            try {
                if (additionalData != null && additionalData.has(NotificationKey.CODE)) {
                    int code = Integer.parseInt(additionalData.getString(NotificationKey.CODE));
                    Logs.log("MNotification", "new noti message with code: " + code);

                    Intent data = new Intent();
                    data.setAction(com.dropininc.utils.Constants.NOTIFICATION_FILTER);
                    data.putExtra("PUSH_DATA", additionalData.toString());

                    if (isActive) {
                        Logs.log("MNotification", "sendBroadcast isActive");
                        sendBroadcast(data);
                    } else {
                        setPushIntent(data);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void setPushIntent(Intent intent) {
        pushIntent = intent;
    }

    public Intent getPushIntent() {
        return pushIntent;
    }

    /*kinh logNetWork*/
    public void logNetwork(NetworkInfo networkInfo ){
        if(logNetWork == null){
            logNetWork = new LogNetModel();
        }
        if(logNetWork.msg == null){
            logNetWork.msg = new ArrayList<String>();
        }
        String log;
        if(networkInfo == null || !networkInfo.isConnectedOrConnecting()){
            log="Netword:Disconnect";

        }else{
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                log="Netword:Connect(Wifi)";
            }else{
                log="Netword:Connect(3G)";
            }
        }
        String date = Utils.getStringTimeFormat(System.currentTimeMillis());
        log = log + " ::" + date;
        logNetWork.msg.add(log);
        LogNetModel.saveLogNetWorkPreference(logNetWork);
        Log.d("KINH", "log=" + log + "::" + date);
        //Toast.makeText(mInstance,log,Toast.LENGTH_SHORT).show();
    }

    /*kinh logPusher*/
    private LogNetModel logPusher;
    private LogNetModel logNetWork;
    private LogNetModel logErrorServer;
    public  LogNetModel getLogPusher(){
        return logPusher;
    }
    public  LogNetModel getLogErrorServer(){
        return logErrorServer;
    }
    public LogNetModel getLogNetWork(){
        return logNetWork;
    }
    void initLog(){
        logPusher = LogNetModel.getLogPusherPreference();
        logNetWork = LogNetModel.getLogNetWorkPreference();
        logErrorServer  = LogNetModel.getLogErrorServerPreference();
    }

    public  void saveLog(){
        LogNetModel.saveLogNetWorkPreference(logNetWork);
        LogNetModel.saveLogPusherPreference(logPusher);
        LogNetModel.saveLogErrorServerPreference(logErrorServer);
    }
    public void clearLog(){
        LogNetModel.saveLogNetWorkPreference(null);
        LogNetModel.saveLogPusherPreference(null);
        LogNetModel.saveLogErrorServerPreference(null);
        if(logPusher != null && logPusher.msg != null){
            logPusher.msg.clear();
        }
        if(logNetWork != null && logNetWork.msg != null){
            logNetWork.msg.clear();
        }

        if(logErrorServer != null && logErrorServer.msg != null){
            logErrorServer.msg.clear();
        }
    }

    public void logPusher(String socketId, ConnectionStateChange connectionStateChange){
        if(logPusher == null){
            logPusher = new LogNetModel();
        }
        if(logPusher.msg == null){
            logPusher.msg = new ArrayList<String>();
        }
        if(connectionStateChange == null){
            return;
        }

        ConnectionState state = connectionStateChange.getCurrentState();
        if(state == null || (!state.equals(ConnectionState.DISCONNECTED) && !state.equals(ConnectionState.CONNECTED))){
            return;
        }


        String log = null;
        //int statePusher = -1;
        if(state.equals(ConnectionState.CONNECTED)){
            log = "Pusher:Connected";
            if(!TextUtils.isEmpty(socketId)){
                log = log + " .Socket:" + socketId;
            }
            //statePusher = 1;
        }else{
            log = "Pusher:Disconnect";
            //statePusher = 0;
        }
        String date = Utils.getStringTimeFormat(System.currentTimeMillis());
        log = log + "::" + date;
        logPusher.msg.add(log);
        LogNetModel.saveLogPusherPreference(logPusher);
        Log.d("KINH", "log=" + log + " ::" + date);
        /*
        if(prestatePusher != statePusher ){
            String date = Utils.getStringTimeFormat(System.currentTimeMillis());
            log = log + "::" + date;
            Log.d("KINH","log=" + log + "::" +date);
        }
        prestatePusher = statePusher;
        */
    }

    public void logErrorServer(String api, RetrofitErrorModel error){
        String url = DSharePreference.getDebugURL(mInstance);
        String date = Utils.getStringTimeFormat(System.currentTimeMillis());
        String log = "url=" + url + ".Api=" + api + ".";
        if(error != null){
            log = log + ".Msg=" + error.log + ".Code=" + error.code;
        }
        log = log + "::" + date;
        logErrorServer.msg.add(log);
        LogNetModel.saveLogErrorServerPreference(logErrorServer);
    }

    public  boolean isMicroPhone(){
        return getPackageManager().hasSystemFeature("android.hardware.microphone");
    }

    public  boolean isHighVolumePhone(){
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int music = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        if(music <= 3){
            return false;
        }

        return true;
    }

    public void adjustVolumeRaise(){
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public void adjustVolumeLow(){
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

}

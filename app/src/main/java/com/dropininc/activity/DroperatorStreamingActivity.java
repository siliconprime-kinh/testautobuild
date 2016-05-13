package com.dropininc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.asynctask.UploadImageManager;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.dialog.ChatDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.HelpFullScreenDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.NotificationCode;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.message.MessageManager;
import com.dropininc.model.AvatarModel;
import com.dropininc.model.ChatModel;
import com.dropininc.model.DataInputStreamModel;
import com.dropininc.model.EndStreamModel;
import com.dropininc.model.LogModel;
import com.dropininc.model.NotificationModel;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.tokbox.CustomVideoRenderer;
import com.dropininc.tokbox.MyPublisher;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONObject;

import java.util.ArrayList;

public class DroperatorStreamingActivity extends BaseActivity implements
        Session.SessionListener, Publisher.PublisherListener, SubscriberKit.VideoListener, View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private final String TAG = "DroperatorStreamingActivity";

    private Activity mActivity;

    private Session mSession;
    private MyPublisher mPublisher;
    private boolean resumeHasRun = false;
    private RelativeLayout mPublisherViewContainer, mLayStartStream;
    private Button mButtonLaunchStream;
    private ProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;

    private HelpFullScreenDialog mFullScreenDialog;
    private OpenTokConfig mOpenTokConfig;
    private IntentFilter mIntentFilter;
    private Handler mHandler;

    RelativeLayout layTurnCamera;
    ImageView imgTurnCamera;
    RelativeLayout layInfo;
    ImageView imgInfo;
    RelativeLayout layCloseStream;
    ImageView imgCloseStream;

    boolean isStopPressed = false;
    private boolean mEnableReceivePush = true;

    private Subscriber mSubscriber;

    Handler handlerDialog = new Handler();
    final int TYPE_NETWORK = 0;
    final int TYPE_BATTERY = 1;
    final int TYPE_INFO = 2;
    boolean isErrorNetwork = false;
    boolean isErrorBattery = false;
    boolean isErrorInfo = false;

    int batteryLevel = 100;
    LinearLayout lay_popup_battery;
    LinearLayout lay_popup_network;
    LinearLayout lay_popup_progress;
    TextView txtPercent;
    boolean isShowPopup = false;
    private boolean mIsConnectToStream = true;

    private boolean isClickFinish = false;

    private TelephonyManager mTelephonyManager;
    private int mSignalStrength;
    private int mBatteryLevelMain;
    private IntentFilter mIntentFilterBattery;

    private RelativeLayout layChat;
    private ImageView imgChat;
    private ChatDialog chatDialog;
    private String pathImage;

    boolean isScreenActive = false;
    AutoDialog mDialog;
    public static boolean isActive = false;

    private DataInputStreamModel dataInputStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActive = true;
        setContentView(R.layout.activity_droperater_streaming);

        mActivity = this;
        initView();

        mOpenTokConfig = new OpenTokConfig();
        mIntentFilter = new IntentFilter(Constants.NOTIFICATION_STREAM);

        mTelephonyManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mIntentFilterBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        if (getIntent().getExtras() != null) {
            mIsConnectToStream = getIntent().getBooleanExtra("IsConnectToStream", true);

            dataInputStream = DataInputStreamModel.fromIntent(getIntent());
            Logs.log(TAG, "data Input: " + dataInputStream.toJSON());

            mOpenTokConfig.TOKEN = dataInputStream.token;
            mOpenTokConfig.SESSION_ID = dataInputStream.session;
            mOpenTokConfig.API_KEY = dataInputStream.apiKey;

            setupChatDialog();
        }

        if (mIsConnectToStream) {
            disableControlButton(false);
            checkShowTutorial();
            detectScreen();

            try {
                JSONObject props = new JSONObject();
                props.put("gigsId", dataInputStream.gigsId);
                mixpanel.track("Stream - Started", props);
            } catch (Exception e) {
                Logs.log(e);
            }
        } else {
            mLayStartStream.setVisibility(View.GONE);
            disableControlButton(true);
        }
        mHandler = new Handler();
        mRunnableOnlineOffline.run();
    }

    private void sessionConnect() {
        if (mSession == null) {
            try {
                LogModel logModel = new LogModel();
                logModel.message = "Android: Droper start to connect to opentok";
                logModel.data = "gigsId: " +  dataInputStream.gigsId + " - SESSION_ID" + mOpenTokConfig.SESSION_ID + " - TOKEN:" + mOpenTokConfig.TOKEN + " - API_KEY:" + mOpenTokConfig.API_KEY;
                logToServer(logModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSession = new Session(mActivity,
                    mOpenTokConfig.API_KEY, mOpenTokConfig.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(mOpenTokConfig.TOKEN);
        }
    }


    @Override
    public void onConnected(Session session) {
        Log.i("TAG", "onConnected");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - opentok Session onConnected";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPublisher == null) {
            Publisher.CameraCaptureResolution cameraCaptureResolution = Utils.isConnectedMobile(this) ?
                    Publisher.CameraCaptureResolution.LOW : Publisher.CameraCaptureResolution.MEDIUM;
            mPublisher = new MyPublisher(mActivity, "publisher", cameraCaptureResolution, Publisher.CameraCaptureFrameRate.FPS_15);
            mPublisher.setCameraId(0);
            mPublisher.setPublisherListener(this);
            attachPublisherView(mPublisher);
            mSession.publish(mPublisher);
            mLayStartStream.setVisibility(View.GONE);
            disableControlButton(true);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i("TAG", "onDisconnected");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - opentok Session onDisconnected";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
        }

        mPublisher = null;
        mSession = null;
        disableControlButton(false);
    }

    @Override
    public void onError(Session session, OpentokError exception) {
        Log.i("TAG", "Session exception: " + exception.getMessage());
        showDialogInfo(TYPE_INFO);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - opentok Session onError";
            logModel.data = "Session exception: " + exception.getMessage();
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        // public ready
        Log.i("TAG", "onStreamReceived");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - opentok Session onStreamReceived";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        subscribeToStream(stream);
        disableControlButton(true);
    }

    private void subscribeToStream(Stream stream) {
        if (mSubscriber == null) {
            mSubscriber = new Subscriber(mActivity, stream);
            mSession.subscribe(mSubscriber);
            mSubscriber.setVideoListener(this);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        // public stop
        Log.i("TAG", "onStreamDropped start timer");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - opentok Session onStreamDropped";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriber = null;
        }

        mHandler.postDelayed(mRunnableStopStream, 15000);
    }


    private void attachPublisherView(Publisher publisher) {
        Log.i("TAG", "attachPublisherView");
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        mPublisherViewContainer.addView(mPublisher.getView(), layoutParams);

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i("TAG", "onStreamCreated");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Publisher onStreamCreated and callStartGigs - URL: http://apidev.dropininc.com/gigs/start/" +  dataInputStream.gigsId;
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        networkManager.startStream( dataInputStream.gigsId)
                .subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("startStream/" +  dataInputStream.gigsId, networkManager.parseError(throwable));
                });
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i("TAG", "onStreamDestroyed");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Publisher onStreamDestroyed";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isStopPressed) {
            showDialogInfo(TYPE_INFO);
        }

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.i("TAG", "onError");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Publisher onError";
            logModel.data = "opentokError: " + opentokError.getMessage();
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBatteryChangeReceiver);
        unregisterReceiver(mBatInfoReceiver);
        if (mSession != null) {
            mSession.onPause();
        }

        if (handlerDialog != null) {
            handlerDialog.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
        registerReceiver(mBatteryChangeReceiver, mIntentFilterBattery);
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (mIsConnectToStream) {
            if (!resumeHasRun) {
                resumeHasRun = true;
                return;
            } else {
                if (mSession != null) {
                    mSession.onResume();
                }
            }
        } else {
            Intent data = new Intent();
            data.setAction(Constants.NOTIFICATION_FILTER);
            data.putExtras(getIntent().getExtras());
            sendBroadcast(data);
        }

        Utils.clearAllNotification(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        isScreenActive = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isScreenActive = false;
        if (isFinishing()) {
            if (mSession != null) {
                mSession.disconnect();
            }
        }
        removeChat();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        isActive = false;
        if (mSession != null) {
            mSession.disconnect();
        }
        mHandler.removeCallbacks(mRunnableStopStream);
        mHandler.removeCallbacks(mRunnableOnlineOffline);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        closeStream();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_launch_stream:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Camera permission has not been granted.
                    requestCameraPermission();
                } else {
                    mButtonLaunchStream.setEnabled(false);
                    startStream();
                }
                break;
            case R.id.layTurnCamera:
                if (mSession != null && mPublisher != null) {
                    swapCamera();
                }
                break;
            case R.id.layInfo:
                if (!isShowPopup) {
                    if (isErrorInfo) {
                        showDialogInfo(TYPE_INFO);
                    }
                    if (isErrorNetwork) {
                        showDialogInfo(TYPE_NETWORK);
                    }
                    if (isErrorBattery) {
                        showDialogInfo(TYPE_BATTERY);
                    }
                }
                break;
            case R.id.layCloseStream:
                closeStream();
                break;
            case R.id.layChat:
                showChatDialog();
                break;
        }
    }

    //Add by Thong Nguyen 04/05/2016
    public void swapCamera() {
        if (mPublisher == null) return;
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            requestCameraPermission();
        } else {
            // Camera permissions is already available, show the camera preview.
            mPublisher.swapCamera();
        }
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            showConfirmDialog(getString(R.string.request_permission), String.format(getString(R.string.camera_request_access_camera), getString(R.string.app_name)), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideConfirmDialog();
                    ActivityCompat.requestPermissions(DroperatorStreamingActivity.this, new String[]{Manifest.permission.CAMERA},
                            Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideConfirmDialog();
                }
            });

        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS);
        }
        // END_INCLUDE(camera_permission_request)
    }

    private void initView() {
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mLayStartStream = (RelativeLayout) findViewById(R.id.lay_start_stream);
        mButtonLaunchStream = (Button) findViewById(R.id.bt_launch_stream);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        layTurnCamera = (RelativeLayout) findViewById(R.id.layTurnCamera);
        imgTurnCamera = (ImageView) findViewById(R.id.imgTurnCamera);
        layInfo = (RelativeLayout) findViewById(R.id.layInfo);
        imgInfo = (ImageView) findViewById(R.id.imgInfo);
        layCloseStream = (RelativeLayout) findViewById(R.id.layCloseStream);
        imgCloseStream = (ImageView) findViewById(R.id.imgCloseStream);

        lay_popup_battery = (LinearLayout) findViewById(R.id.lay_popup_battery);
        lay_popup_network = (LinearLayout) findViewById(R.id.lay_popup_network);
        lay_popup_progress = (LinearLayout) findViewById(R.id.lay_popup_progress);
        txtPercent = (TextView) findViewById(R.id.txtPercent);

        layChat = (RelativeLayout) findViewById(R.id.layChat);
        imgChat = (ImageView) findViewById(R.id.imgChat);

        TextView txtMessagesProgress = (TextView) findViewById(R.id.txtMessagesProgress);
        FontUtils.typefaceTextView(txtMessagesProgress, FontType.REGULAR);
        TextView txtMessagesBattery = (TextView) findViewById(R.id.txtMessagesBattery);
        FontUtils.typefaceTextView(txtMessagesBattery, FontType.REGULAR);
        TextView txtPercent = (TextView) findViewById(R.id.txtPercent);
        FontUtils.typefaceTextView(txtPercent, FontType.REGULAR);
        TextView txtMessagesNetWork = (TextView) findViewById(R.id.txtMessagesNetWork);
        FontUtils.typefaceTextView(txtMessagesNetWork, FontType.REGULAR);

        mButtonLaunchStream.setOnClickListener(this);
        layTurnCamera.setOnClickListener(this);
        layInfo.setOnClickListener(this);
        layCloseStream.setOnClickListener(this);
        layChat.setOnClickListener(this);
    }

    private void disableControlButton(boolean isShow) {
        layCloseStream.setEnabled(isShow);
        layTurnCamera.setEnabled(isShow);
    }

    private void checkShowTutorial() {
        if (DSharePreference.isShowDroperatorHelp(mActivity)) {
            mFullScreenDialog = new HelpFullScreenDialog(mActivity, mFullScreenDialog.DROPERATOR);
            int resourceId = R.drawable.ic_help_droperator;
            mFullScreenDialog.setBackgroundImage(resourceId);
            mFullScreenDialog.setType(mFullScreenDialog.DROPERATOR);
            mFullScreenDialog.show();
        }
    }

    private void detectScreen() {
        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {
            int orientation = -1;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[1] < 6.5 && event.values[1] > -6.5) {
                    if (orientation != 1) {
                        Log.d("Sensor", "Landscape");
                        if (mFullScreenDialog != null && mFullScreenDialog.isShowing()) {
                            new Handler().postDelayed(() -> {
                                int resourceId = R.drawable.ic_help_droperator;
                                mFullScreenDialog.setBackgroundImage(resourceId);
                            }, 500);

                        }
                    }
                    orientation = 1;
                } else {
                    if (orientation != 0) {
                        Log.d("Sensor", "Portrait");
                        if (mFullScreenDialog != null && mFullScreenDialog.isShowing()) {
                            new Handler().postDelayed(() -> {
                                int resourceId = R.drawable.ic_help_droperator;
                                mFullScreenDialog.setBackgroundImage(resourceId);
                            }, 500);

                        }
                    }
                    orientation = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    private void startStream() {
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - User Click start Stream";
            logModel.data = "gigsId: " +  dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mProgressBar.setVisibility(View.VISIBLE);
        sessionConnect();
    }

    private void closeStream() {
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setMessageDialog(R.string.are_you_sure_you_want_to_stop_streaming);
        mDialog.setTitleDialog("");
        mDialog.setCancelTitleButton(R.string.cancel);
        mDialog.setOkTitleButton(R.string.yes_finish);
        mDialog.setCancelButtonClick(v -> mDialog.dismiss());
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            isStopPressed = true;
            isClickFinish = true;
            try {
                LogModel logModel = new LogModel();
                logModel.message = "Android: Droper - User Click Close Stream - URL: http://apidev.dropininc.com/gigs/stop/" +  dataInputStream.gigsId;
                logModel.data = "gigsId: " + dataInputStream.gigsId;
                logToServer(logModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            exitStream();
        });
        mDialog.show();
    }

    private void exitStream() {
        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(null);
        }
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        networkManager.endStream( dataInputStream.gigsId)
                .subscribe(endStreamModel -> {
                    // Disconnecting from the GIG channel
                    MessageManager.getInstance().unsubscribeFromGigChannel( dataInputStream.gigsId);
                    processExitStream(endStreamModel);
                }, throwable -> {
                    try {
                        LogModel logModel = new LogModel();
                        logModel.message = "Android: Droper - Close Stream response fail - URL: http://apidev.dropininc.com/gigs/stop/" + dataInputStream.gigsId;
                        AppApplication.getInstance().logErrorServer("endStream/" +  dataInputStream.gigsId, networkManager.parseError(throwable));
                        logModel.data = "Error Message: " + networkManager.parseError(throwable).message;
                        logToServer(logModel);
                    } catch (Exception e) {
                        Logs.log(e);
                    }
                    // Disconnecting from the GIG channel
                    MessageManager.getInstance().unsubscribeFromGigChannel( dataInputStream.gigsId);
                    dismissDialog();
                    if (networkManager.parseError(throwable).code.equalsIgnoreCase("UNKNOWN")) {
                        if (mSession != null) {
                            mSession.disconnect();
                        }
                        finish();
                    } else {
                        showAlertDialog("", networkManager.parseError(throwable).message);
                    }
                });
    }

    private void processExitStream(EndStreamModel model) {
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Close Stream response success - URL: http://apidev.dropininc.com/gigs/stop/" +  dataInputStream.gigsId;
            logModel.data = "Data: " + model.toJSON();
            logToServer(logModel);
        } catch (Exception e) {
            Logs.log(e);
        }
        dismissDialog();
        if (model == null || model.gig == null) return;

        onFinishStream(model.gig.duration, model.gig.price, model.gig.bypassRating);
    }

    private void processExitStream(Bundle bundle) {
        NotificationModel notification = NotificationModel.fromJSON(
                bundle.getString(NotificationModel.class.getSimpleName()));
        if (notification != null) {
            onFinishStream(notification.duration, notification.price, notification.bypassRating);
        } else {
            Logs.log("bundle null");
        }
    }

    private void showDialogStopStream(final Bundle bundle) {
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Receive Stream Stop [from Socket/push notification]";
            NotificationModel notification = NotificationModel.fromJSON(
                    bundle.getString(NotificationModel.class.getSimpleName()));
            if (notification != null) {
                logModel.data = "DATA: " + notification.toJSON();
            }
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDialog = new AutoDialog(mActivity);
        mDialog.setCancelable(false);
        mDialog.setMessageDialog(R.string.message_viewer_stop_stream);
        mDialog.setTitleDialog("");
        mDialog.setTimeAutoDismiss(5 * 1000);
        mDialog.setDialogDismissCallback(() -> {
            mDialog.dismiss();
            processExitStream(bundle);
        });
        mDialog.show();
    }

    private void showDialogStreamWasExpired(final Bundle bundle) {
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper - Receive Stream Expire [from Socket/push notification]";
            NotificationModel notification = NotificationModel.fromJSON(
                    bundle.getString(NotificationModel.class.getSimpleName()));
            if (notification != null) {
                logModel.data = "DATA: " + notification.toJSON();
            }
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        showAlertDialog("", getString(R.string.stream_expire_messages),
                v -> {
                    hideAlertDialog();
                    processExitStream(bundle);
                });
    }

    private void onFinishStream(String duration, String price, boolean bypassRating) {
        if (mSession != null) {
            mSession.disconnect();
        }
        Intent intent = new Intent();
        intent.putExtra("durationStreaming", duration);
        intent.putExtra("price", price);
        intent.putExtra("bypassRating", bypassRating);
        intent.putExtra("gigsId",  dataInputStream.gigsId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        Logs.log(TAG, "onVideoDataReceived");
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        Logs.log(TAG, "onVideoDisabled");
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        Logs.log(TAG, "onVideoEnabled");
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
        Logs.log(TAG, "onVideoDisableWarning");
        showDialogInfo(TYPE_NETWORK);
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
        Logs.log(TAG, "onVideoDisableWarningLifted");
        hideDialogInfo(TYPE_NETWORK);
    }

    private class OpenTokConfig {
        public String SESSION_ID;
        public String TOKEN;
        public String API_KEY;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("KINH", "Droperator Stream. onReceive::isScreenActive=" + isScreenActive);
            if (isScreenActive && intent != null && intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();
                int code = bundle.getInt(NotificationKey.CODE);
                Logs.log("TAG", "mBroadcastReceiver code: " + code);
                switch (code) {
                    case NotificationCode.STREAM_FINISH:
                        // Disconnecting from the GIG channel
                        MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);

                        if (mEnableReceivePush) {
                            mEnableReceivePush = false;
                            showDialogStopStream(bundle);
                        }
                        break;
                    case NotificationCode.STREAM_EXPIRE:
                        // Disconnecting from the GIG channel
                        MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);

                        if (mEnableReceivePush) {
                            mEnableReceivePush = false;
                            showDialogStreamWasExpired(bundle);
                        }
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) return;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int percent = (level * 100) / scale;
            batteryLevel = percent;
//            if (percent == 25 || percent == 20 || percent == 15) {
            if (percent <= 25 && percent > 10) {
                showDialogInfo(TYPE_BATTERY);
            } else if (percent <= 10) {
                showDialogInfo(TYPE_BATTERY);
                exitStream();
                try {
                    LogModel logModel = new LogModel();
                    logModel.message = "Android: Droper - Call end stream by low Battery";
                    logModel.data = "";
                    logToServer(logModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void showDialogInfo(int type) {
        try {
            isShowPopup = true;

            imgInfo.setImageResource(R.drawable.ic_info);
            lay_popup_battery.setVisibility(View.GONE);
            lay_popup_network.setVisibility(View.GONE);
            lay_popup_progress.setVisibility(View.GONE);
            if (type == TYPE_NETWORK) {
                isErrorNetwork = true;
                lay_popup_network.setVisibility(View.VISIBLE);
            } else if (type == TYPE_BATTERY) {
                isErrorBattery = true;
                lay_popup_battery.setVisibility(View.VISIBLE);
                txtPercent.setText(batteryLevel + "%");
            } else if (type == TYPE_INFO) {
                isErrorInfo = true;
                lay_popup_progress.setVisibility(View.VISIBLE);
            }
            handlerDialog.removeCallbacksAndMessages(null);
            handlerDialog.postDelayed(() -> {
                lay_popup_battery.setVisibility(View.GONE);
                lay_popup_network.setVisibility(View.GONE);
                lay_popup_progress.setVisibility(View.GONE);
                isShowPopup = false;
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideDialogInfo(int type) {
        try {
            isShowPopup = false;
            lay_popup_battery.setVisibility(View.GONE);
            lay_popup_network.setVisibility(View.GONE);
            lay_popup_progress.setVisibility(View.GONE);
            if (type == TYPE_NETWORK) {
                isErrorNetwork = false;
            } else if (type == TYPE_BATTERY) {
                isErrorBattery = false;
            } else if (type == TYPE_INFO) {
                isErrorInfo = false;
            }

            if (isErrorNetwork || isErrorBattery || isErrorInfo) {
                imgInfo.setImageResource(R.drawable.ic_info);
            } else {
                imgInfo.setImageResource(R.drawable.ic_info_hide);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mRunnableOnlineOffline = new Runnable() {
        @Override
        public void run() {
            setUserOnlineOffline();
            mHandler.postDelayed(mRunnableOnlineOffline, 45 * 1000);
        }
    };

    public void setUserOnlineOffline() {
        Logs.log(TAG, "setUserOnlineOffline for Droperator");
        networkManager.location(new LocationRequest("0.0", "0.0", mBatteryLevelMain + "",
                mSignalStrength, true, getNetWorkType() + "", dataInputStream.settingRadius + "", dataInputStream.gigsId))
                .subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("location/" + dataInputStream.gigsId, networkManager.parseError(throwable));
                });
    }

    private int getNetWorkType() {
        int networkType = -1;
        switch (mTelephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                Log.d("Type", "3g");
                networkType = 3;
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                Log.d("Type", "4g");
                networkType = 10;
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                Log.d("Type", "GPRS");
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                Log.d("Type", "EDGE 2g");
                networkType = 0;
                break;
            default:
                Log.i("Type", "Wifi");
                networkType = 11;
                break;
        }
        return networkType;
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength.getGsmSignalStrength();
        }
    };

    private BroadcastReceiver mBatteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBatteryLevelMain = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    private Runnable mRunnableStopStream = () -> {
        Log.i("TAG", "mRunnableStopStream Run");
        if (mEnableReceivePush) {
            mEnableReceivePush = false;
            if (!isClickFinish) {
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("Stream - Failed", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                try {
                    LogModel logModel = new LogModel();
                    logModel.message = "Android: Droper - Call stop stream by onStreamDropped - URL: http://apidev.dropininc.com/gigs/stop/" + dataInputStream.gigsId;
                    logModel.data = "gigsId: " + dataInputStream.gigsId;
                    logToServer(logModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                exitStream();
            }
        }
    };

    public void setupChatDialog() {
        if (!TextUtils.isEmpty(dataInputStream.chatChannel)) {
            layChat.setEnabled(true);
            chatDialog = new ChatDialog(mActivity, R.style.DialogFullScreenTheme);
            chatDialog.setChatChanel(dataInputStream.chatChannel, dataInputStream.chatName, dataInputStream.chatAvatar, dataInputStream.gigsId);
            chatDialog.setIsFromEnRoute(false);
            chatDialog.setDatas(dataInputStream.chatData);
            chatDialog.setHeaderHeight();
            chatDialog.setBackgroundTransparent(true);
            chatDialog.setTakePhotoCalBack(new ChatDialog.TakePhotoCallback() {
                @Override
                public void onTakePhoto() {
                    showDialogCapture();
                }

                @Override
                public void onHadNewMessages(boolean isHadNew) {
                    Logs.log("onHadNewMessages", "onHadNewMessages : " + isHadNew);
                    if (isHadNew) {
                        imgChat.setImageResource(R.drawable.img_chat_new);
                    } else {
                        imgChat.setImageResource(R.drawable.img_chat);
                    }
                }
            });
        } else {
            layChat.setEnabled(false);
            if (chatDialog != null && chatDialog.isShowing()) {
                chatDialog.dismiss();
            }
            chatDialog = null;
        }
    }

    private void showChatDialog() {
        if (chatDialog != null) {
            if (chatDialog.isShowing()) {
                chatDialog.dismiss();
            } else {
                imgChat.setImageResource(R.drawable.img_chat);
                chatDialog.show();
            }
        } else {
            setupChatDialog();
        }
    }

    private void removeChat() {
        dataInputStream.chatChannel = "";
        dataInputStream.chatName = "";
        dataInputStream.chatAvatar = "";
        dataInputStream.chatData = ChatModel.toJSONArray(new ArrayList<>());
        setupChatDialog();
    }

    private void showDialogCapture() {
        final ConfirmDialog confirmDialog = new ConfirmDialog(mActivity);
        confirmDialog.setMessageDialog(R.string.confirm_capture);
        confirmDialog.setCancelTitleButton(R.string.cancel);
        confirmDialog.setOkTitleButton(R.string.start_capture);
        confirmDialog.setCancelButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
        confirmDialog.setOkButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                takeScreenshot();
            }
        });
        confirmDialog.show();

    }

    private void takeScreenshot() {
        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(null);
        }
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
        new TakeScreenShotsAsync().execute();
    }

    private void getLinkUploadAvatar() {
        networkManager.getLinkUploadScreenshot()
                .subscribe(this::processResponseGetLinkUploadAvatar,
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("getLinkUploadScreenshot", networkManager.parseError(throwable));
                            processResponseGetLinkUploadAvatar(new AvatarModel());
                        });
    }

    private void processResponseGetLinkUploadAvatar(AvatarModel model) {
        if (model == null) {
            dismissDialog();
            return;
        }
        if (TextUtils.isEmpty(model.location)) {

            return;
        }

        new UploadImageManager(mActivity, pathImage, model.location, new UploadImageManager.UploadPhotoCallback() {
            @Override
            public void onSuccess() {
                dismissDialog();
//                mImageLoader.displayImage("file://" + mPathImage, mImageAvatar, mOptions);
                if (chatDialog != null) {
                    chatDialog.show();
                    String url = model.location.split("\\?")[0];
                    chatDialog.sendPhoto(url);
                }
            }

            @Override
            public void onError() {
                dismissDialog();
//                showErrorDialog("", getString(R.string.error_try_again));
            }
        });
    }

    public class TakeScreenShotsAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                mPublisher.saveScreenshot(new CustomVideoRenderer.SaveScreenshotListener() {
                    @Override
                    public void savePhoto(String path) {
                        if (!TextUtils.isEmpty(path)) {
                            Logs.log("TAG", "photo URL : " + path);
                            pathImage = path;
                            getLinkUploadAvatar();
                        }
                    }
                });
            } catch (Exception e) {
                Logs.log(e);
                dismissDialog();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
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

    /**
     * Add by Thong Nguyen 05/04/2016
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                swapCamera();
            } else {
                showAlertDialog(getString(R.string.camera_permission_was_not_granted));
            }
        }
    }
}


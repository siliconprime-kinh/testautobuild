package com.dropininc.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.dialog.ChatDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.HelpFullScreenDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.NotificationCode;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.message.MessageManager;
import com.dropininc.model.ChatModel;
import com.dropininc.model.DataInputStreamModel;
import com.dropininc.model.EndStreamModel;
import com.dropininc.model.LogModel;
import com.dropininc.model.NotificationModel;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.tokbox.MySubscriber;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONObject;

import java.util.ArrayList;

public class ViewerStreamingActivity extends BaseActivity implements
        Session.SessionListener, Subscriber.VideoListener, View.OnClickListener {

    private String TAG = "ViewerStreamingActivity";

    private Activity mActivity;

    private Session mSession;
    private MySubscriber mSubscriber;
    private Publisher mPublisher;
    private boolean resumeHasRun = false;

    private HelpFullScreenDialog mFullScreenDialog;

    private ProgressDialog mProgressDialog;

    private RelativeLayout mSubscriberViewContainer;
    private RelativeLayout layCloseStream;
    private ImageView imgCloseStream;
    private RelativeLayout layInfo;
    private ImageView imgInfo;
    private RelativeLayout layCamera;
    private ImageView imgCamera;
    private RelativeLayout layVideo;
    private ImageView imgVideo;
    private RelativeLayout layMute;
    private ImageView imgMute;
    private RelativeLayout layMicro;
    private ImageView imgMicro;
    private RelativeLayout mLayCountdown;
    private TextView mTextTime;

    private IntentFilter mIntentFilter;
    private CountDownTimer mCountDownTimer;

    boolean isRecording = false;

    Handler handlerDialog = new Handler();
    final int TYPE_NETWORK = 0;
    final int TYPE_BATTERY = 1;
    boolean isErrorNetwork = false;
    boolean isErrorBattery = false;

    int batteryLevel = 100;

    LinearLayout lay_popup_battery;
    LinearLayout lay_popup_network;
    LinearLayout lay_popup_progress;
    TextView txtPercent;
    boolean isShowPopup = false;
    private boolean mIsConnectToStream = true;

    private boolean isClickFinish = false;

    private Handler mHandler;
    private boolean mEnableReceivePush = true;

    private String streamID = "";

    ArrayList<String> listRecordId = new ArrayList<String>();

    private TelephonyManager mTelephonyManager;
    private int mSignalStrength;
    private int mBatteryLevelMain;
    private IntentFilter mIntentFilterBattery;

    private RelativeLayout layChat;
    private ImageView imgChat;
    private ChatDialog chatDialog;

    boolean isScreenActive = false;
    AutoDialog mDialog;

    public static boolean isActive = false;

    public DataInputStreamModel dataInputStream;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isActive = true;
        setContentView(R.layout.activity_viewer_streaming);
        setCancelAllRequestWhenStopActivity(false);

        mActivity = ViewerStreamingActivity.this;
        mIntentFilterBattery = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mTelephonyManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);

        initView();

        if (getIntent().getExtras() != null) {
            mIsConnectToStream = getIntent().getBooleanExtra("IsConnectToStream", true);

            dataInputStream = DataInputStreamModel.fromIntent(getIntent());
            Logs.log(TAG, "Input Data: " + dataInputStream.toJSON());
            setupChatDialog();
        }


        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);

        mIntentFilter = new IntentFilter(Constants.NOTIFICATION_STREAM);

        if (mIsConnectToStream) {
            disableControlButton(false);
            checkShowTutorial();
            detectScreen();
            sessionConnect();
        } else {
            disableControlButton(true);
            mLayCountdown.setVisibility(View.GONE);
        }
        mHandler = new Handler();
        mRunnableOnlineOffline.run();
    }

    private void initView() {
        layCloseStream = (RelativeLayout) findViewById(R.id.layCloseStream);
        imgCloseStream = (ImageView) findViewById(R.id.imgCloseStream);
        layInfo = (RelativeLayout) findViewById(R.id.layInfo);
        imgInfo = (ImageView) findViewById(R.id.imgInfo);
        layCamera = (RelativeLayout) findViewById(R.id.layCamera);
        imgCamera = (ImageView) findViewById(R.id.imgCamera);
        layVideo = (RelativeLayout) findViewById(R.id.layVideo);
        imgVideo = (ImageView) findViewById(R.id.imgVideo);
        layMute = (RelativeLayout) findViewById(R.id.layMute);
        imgMute = (ImageView) findViewById(R.id.imgMute);
        layMicro = (RelativeLayout) findViewById(R.id.layMicro);
        imgMicro = (ImageView) findViewById(R.id.imgMicro);
        mLayCountdown = (RelativeLayout) findViewById(R.id.lay_countdown);
        mTextTime = (TextView) findViewById(R.id.tv_time);

        layChat = (RelativeLayout) findViewById(R.id.layChat);
        imgChat = (ImageView) findViewById(R.id.imgChat);

        FontUtils.typefaceTextView(mTextTime, FontType.REGULAR);
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        FontUtils.typefaceTextView(tv_title, FontType.REGULAR);

        //
        lay_popup_battery = (LinearLayout) findViewById(R.id.lay_popup_battery);
        lay_popup_network = (LinearLayout) findViewById(R.id.lay_popup_network);
        lay_popup_progress = (LinearLayout) findViewById(R.id.lay_popup_progress);
        txtPercent = (TextView) findViewById(R.id.txtPercent);
        TextView txtMessagesProgress = (TextView) findViewById(R.id.txtMessagesProgress);
        FontUtils.typefaceTextView(txtMessagesProgress, FontType.REGULAR);
        TextView txtMessagesBattery = (TextView) findViewById(R.id.txtMessagesBattery);
        FontUtils.typefaceTextView(txtMessagesBattery, FontType.REGULAR);
        TextView txtPercent = (TextView) findViewById(R.id.txtPercent);
        FontUtils.typefaceTextView(txtPercent, FontType.REGULAR);
        TextView txtMessagesNetWork = (TextView) findViewById(R.id.txtMessagesNetWork);
        FontUtils.typefaceTextView(txtMessagesNetWork, FontType.REGULAR);

        layCloseStream.setOnClickListener(this);
        layInfo.setOnClickListener(this);
        layCamera.setOnClickListener(this);
        layVideo.setOnClickListener(this);
        layMute.setOnClickListener(this);
        layMicro.setOnClickListener(this);
        layChat.setOnClickListener(this);

        if (mIsConnectToStream) {
            mCountDownTimer = new CountDownTimer(60 * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if (mSubscriber == null) {
                        try {
                            long timeRemain = millisUntilFinished / 1000;
                            String textTime = "" + timeRemain;
                            if (timeRemain < 10) {
                                textTime = "0" + timeRemain;
                            }
                            String text = "00:%s";
                            text = String.format(text, textTime);
                            mTextTime.setText(text);
                        } catch (Exception e) {
                            mTextTime.setText("00:00");
                            Logs.log(e);
                        }
                    }
                }

                public void onFinish() {
                    mTextTime.setText("00:00");

                    cancelStream();
                }
            }.start();
        }
    }


    private void sessionConnect() {
        if (mSession == null) {
            try {
                LogModel logModel = new LogModel();
                logModel.message = "Android: Viewer start to connect to opentok";
                logModel.data = "gigsId: " + dataInputStream.gigsId + " - SESSION_ID" + dataInputStream.session + " - TOKEN:" + dataInputStream.token + " - API_KEY:" + dataInputStream.apiKey;
                logToServer(logModel);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mSession = new Session(mActivity, dataInputStream.apiKey, dataInputStream.session);
            mSession.setSessionListener(this);
            mSession.connect(dataInputStream.token);
        }
    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new MySubscriber(mActivity, stream);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);

        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mLayCountdown.setVisibility(View.GONE);
    }

    private void unsubscribeFromStream(Stream stream) {
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
        }
    }


    private void doPublicAudio() {
        if (mPublisher == null) {
            mPublisher = new Publisher(mActivity, "publisher");
            mPublisher.setPublishVideo(false);
            mSession.publish(mPublisher);
        }
    }

    private void stopPublicAudio() {
        mPublisher = null;
    }

    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        mSubscriberViewContainer.removeView(mSubscriber.getView());
        mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    @Override
    public void onConnected(Session session) {
        Log.i(TAG, "onConnected");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - opentok Session onConnected";
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TAG, "onDisconnected");
        stopPublicAudio();
        disableControlButton(false);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - opentok Session onDisconnected";
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "onStreamReceived");
        if (mSubscriber == null) {
            subscribeToStream(stream);
            doPublicAudio();
            callStartGigs();
        }
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok Session StreamReceived from Droper";
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mixpanel.track("Stream - Started", new JSONObject().put("gigsId", dataInputStream.gigsId));
        } catch (Exception e) {
            Logs.log(e);
        }

        lay_popup_progress.setVisibility(View.GONE);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "onStreamDropped");
        if (mSubscriber != null) {
            unsubscribeFromStream(stream);
        }
        Log.i("TAG", "onStreamDropped start timer");
        mHandler.postDelayed(mRunnableStopStream, 15000);

        lay_popup_progress.setVisibility(View.VISIBLE);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - opentok Session onStreamDropped";
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStartGigs() {
        Log.i(TAG, "call gigs start when viewer: onStreamReceived - update 22/12/15");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - callStartGigs - URL: http://apidev.dropininc.com/gigs/start/" + dataInputStream.gigsId;
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        networkManager.startStream(dataInputStream.gigsId)
                .subscribe(model -> {
                    streamID = model.gig.stream.id;
                    Log.i(TAG, "STREAM ID: " + streamID);
                    try {
                        LogModel logModel = new LogModel();
                        logModel.message = "Android: Viewer - callStartGigs Success";
                        logModel.data = "STREAM ID: " + streamID;
                        logToServer(logModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("startStream/" + dataInputStream.gigsId, networkManager.parseError(throwable));
                });
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(TAG, "onError " + opentokError.getMessage());
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok Session Error";
            logModel.data = "Error Messages:" + opentokError.getMessage();
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(TAG, "onVideoDataReceived");
        attachSubscriberView(mSubscriber);
        disableControlButton(true);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok onVideoDataReceived";
            logModel.data = "";
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        Log.i(TAG, "onVideoDisabled");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok onVideoDisabled";
            logModel.data = "";
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        Log.i(TAG, "onVideoEnabled");
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok onVideoEnabled";
            logModel.data = "";
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {
        Log.i(TAG, "onVideoDisableWarning");
        showDialogInfo(TYPE_NETWORK);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok onVideoDisableWarning";
            logModel.data = "";
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {
        Log.i(TAG, "onVideoDisableWarningLifted");
        hideDialogInfo(TYPE_NETWORK);
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Opentok onVideoDisableWarningLifted";
            logModel.data = "";
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBatInfoReceiver);
        unregisterReceiver(mBatteryChangeReceiver);
        if (mSession != null) {
            mSession.onPause();

            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }
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
            reloadInterface();
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

    private void disableControlButton(boolean isShow) {
        layCloseStream.setEnabled(isShow);
        layCamera.setEnabled(isShow);
        layVideo.setEnabled(isShow);
        layMute.setEnabled(isShow);
        layMicro.setEnabled(isShow);
    }

    @Override
    public void onBackPressed() {
    }

    public void reloadInterface() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> {
            if (mSubscriber != null) {
                attachSubscriberView(mSubscriber);
            }
        }, 500);
    }

    private void checkShowTutorial() {
        if (DSharePreference.isShowViewerHelp(mActivity)) {
            mFullScreenDialog = new HelpFullScreenDialog(mActivity, mFullScreenDialog.VIEWER);
            int resourceId = R.drawable.ic_help_viewer;
            mFullScreenDialog.setBackgroundImage(resourceId);
            mFullScreenDialog.setType(mFullScreenDialog.VIEWER);
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
                                int resourceId = R.drawable.ic_help_viewer;
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
                                int resourceId = R.drawable.ic_help_viewer;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layCloseStream:
                closeStream();
                break;
            case R.id.layInfo:
                if (!isShowPopup) {
                    if (isErrorNetwork) {
                        showDialogInfo(TYPE_NETWORK);
                    }
                    if (isErrorBattery) {
                        showDialogInfo(TYPE_BATTERY);
                    }
                }
                break;
            case R.id.layCamera:
                new TakeScreenShotsAsync().execute();
                break;
            case R.id.layVideo:
                recordVideo();
                break;
            case R.id.layMute:
                if (mSubscriber != null) {
                    if (mSubscriber.getSubscribeToAudio()) {
                        imgMute.setImageResource(R.drawable.ic_mute_white);
                        mSubscriber.setSubscribeToAudio(false);
                    } else {
                        imgMute.setImageResource(R.drawable.ic_speaker_on);
                        mSubscriber.setSubscribeToAudio(true);
                    }
                }
                break;
            case R.id.layMicro:
                if (mPublisher != null) {
                    if (mPublisher.getPublishAudio()) {
                        imgMicro.setImageResource(R.drawable.ic_micro_white);
                        mPublisher.setPublishAudio(false);
                    } else {
                        imgMicro.setImageResource(R.drawable.ic_talk_on);
                        mPublisher.setPublishAudio(true);
                    }
                }
                break;
            case R.id.layChat:
                showChatDialog();
                break;
        }
    }

    private void recordVideo() {
        if (!isRecording) {
            Log.d("TAG", "START RECORD CLICK");
            isRecording = true;
            imgVideo.setImageResource(R.drawable.ic_video_red);
            startRecordVideo();
        } else {
            isRecording = false;
            imgVideo.setImageResource(R.drawable.ic_video_gray);
            Log.d("TAG", "STOP RECORD CLICK");
            stopRecordVideo();
        }

    }

    public class TakeScreenShotsAsync extends AsyncTask<Void, Void, Void> {

        public TakeScreenShotsAsync() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            takeScreenshot();
            return null;
        }
    }

    private void takeScreenshot() {
        try {
            MySubscriber sub = mSubscriber;
            sub.saveScreenshot();
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    private void closeStream() {
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setMessageDialog(R.string.are_you_sure_you_want_to_stop_streaming);
        mDialog.setTitleDialog("");
        mDialog.setCancelTitleButton(R.string.no_still_watching);
        mDialog.setOkTitleButton(R.string.yes_finish);
        mDialog.setCancelButtonClick(v -> mDialog.dismiss());
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            isClickFinish = true;
            if (isRecording) {
                stopRecordVideo();
            } else {
                exitStream();
            }

        });
        mDialog.show();
    }

    private void exitStream() {
        if (handlerDialog != null) {
            handlerDialog.removeCallbacks(null);
        }
        if (mActivity != null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - User Click Request Close stream";
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        networkManager.endStream(dataInputStream.gigsId)
                .subscribe(endStreamModel -> {
                    Log.i(TAG, "END_STREAM SUCCESS");
                    // Disconnecting from the GIG channel
                    MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);

                    processExitStream(endStreamModel);
                    try {
                        LogModel logModel = new LogModel();
                        logModel.message = "Android: Viewer - END_STREAM RESPONSE - URL: http://apidev.dropininc.com/gigs/stop/" + dataInputStream.gigsId;
                        logModel.data = "Data response: " + endStreamModel.toJSON();
                        logToServer(logModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("endStream/" + dataInputStream.gigsId, networkManager.parseError(throwable));
                    // Disconnecting from the GIG channel
                    MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);
                });

    }

    private void processExitStream(EndStreamModel model) {
        dismissDialog();
        if (model == null || model.gig == null) return;

        onFinishStream(model.gig.duration, model.gig.price, model.gig.bypassRating);
    }

    private void processExitStream(Bundle bundle) {
        NotificationModel notification = NotificationModel.fromJSON(
                bundle.getString(NotificationModel.class.getSimpleName()));
        if (notification != null) {
            try {
                LogModel logModel = new LogModel();
                logModel.message = "Android: Viewer - Receive Stream Expire [from Socket/push notification]";
                logModel.data = "DATA: " + notification.toJSON();
                logToServer(logModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            onFinishStream(notification.duration, notification.price, notification.bypassRating);
        } else {
            Logs.log("bundle null");
        }
    }

    private void cancelStream() {
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Viewer - Cancel stream when time out 60s - URL: http://apidev.dropininc.com/gigs/cancel/" + dataInputStream.gigsId;
            logModel.data = "gigsId: " + dataInputStream.gigsId;
            logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        networkManager.cancelStream(dataInputStream.gigsId)
                .subscribe(endStreamModel -> {
                    Log.i(TAG, "CANCEL_STREAM SUCCESS");
                    // Disconnecting from the GIG channel
                    MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);

                    processExitStream(endStreamModel);
                    try {
                        LogModel logModel = new LogModel();
                        logModel.message = "Android: Viewer - CANCEL_STREAM RESPONSE - URL: http://apidev.dropininc.com/gigs/cancel/" + dataInputStream.gigsId;
                        logModel.data = "DATA: " + endStreamModel.toJSON();
                        logToServer(logModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    // Disconnecting from the GIG channel
                    AppApplication.getInstance().logErrorServer("cancelStream/" + dataInputStream.gigsId, networkManager.parseError(throwable));
                    MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);
                });
    }

    private void showDialogStopStream(final Bundle bundle) {
        mDialog = new AutoDialog(context);
        mDialog.setCancelable(false);
        mDialog.setMessageDialog(R.string.message_droperator_stop_stream);
        mDialog.setTitleDialog("");
        mDialog.setTimeAutoDismiss(5 * 1000);
        mDialog.setDialogDismissCallback(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                NotificationModel notification = NotificationModel.fromJSON(
                        bundle.getString(NotificationModel.class.getSimpleName()));
                if (notification != null) {
                    try {
                        LogModel logModel = new LogModel();
                        logModel.message = "Android: Viewer - Receive END_STREAM by Droper [from Socket/push notification]";
                        logModel.data = "DATA: " + notification.toJSON();
                        logToServer(logModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onFinishStream(notification.duration, notification.price,
                            notification.bypassRating);
                } else {
                    Logs.log("bundle null");
                }
            }
        });
        mDialog.show();
    }

    private void showDialogStreamWasExpired(final Bundle bundle) {
        showAlertDialog("", getString(R.string.stream_expire_messages),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideAlertDialog();
                        processExitStream(bundle);
                    }
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
        intent.putExtra("gigsId", dataInputStream.gigsId);
        if (listRecordId.size() > 0) {
            String url = "";
            for (int i = 0; i < listRecordId.size(); i++) {
                String tmp = "https://s3.amazonaws.com/tokbox.com.archive2/" + dataInputStream.apiKey +
                        "/" + listRecordId.get(i) + "/archive.mp4";
                url = url + tmp + (i < listRecordId.size() - 1 ? "\n" : "");
            }
            intent.putExtra("videoUrl", url);
            Log.d("TAG", "VIDEO URL: " + url);
        } else {
            Log.d("TAG", "VIDEO URL NULL ");
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showErrorDialog(String message) {
        dismissDialog();

        final AlertDialog mDialog = new AlertDialog(mActivity);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(message);
        mDialog.setTitleButton(R.string.ok_cap);
        mDialog.setButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void dismissDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {
            Logs.log(e);
        } finally {
            mProgressDialog = null;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isScreenActive && intent != null && intent.getExtras() != null) {
                Bundle bundle = intent.getExtras();
                int code = bundle.getInt(NotificationKey.CODE);
                switch (code) {
                    case NotificationCode.STREAM_FINISH:
                        // Disconnecting from the GIG channel
                        MessageManager.getInstance().unsubscribeFromGigChannel(dataInputStream.gigsId);

                        if (mEnableReceivePush) {
                            mEnableReceivePush = false;
                            Log.i(TAG, "END_STREAM FROM SOCKET");
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

    private void startRecordVideo() {
        if (!TextUtils.isEmpty(streamID)) {
            Log.i(TAG, "call gigs startRecordVideo: onStreamReceived - update 5/1/16");
            networkManager.startRecordVideo(streamID)
                    .subscribe(recordModel -> listRecordId.add(recordModel.archiveId),
                            throwable -> {
                                AppApplication.getInstance().logErrorServer("startRecordVideo", networkManager.parseError(throwable));
                            });
        } else {
            Log.i(TAG, "startRecordVideo: streamID is Empty");
        }
    }

    private void stopRecordVideo() {
        if (!TextUtils.isEmpty(streamID)) {
            Log.i(TAG, "call gigs startRecordVideo: onStreamReceived - update 5/1/16");
            networkManager.stopRecordVideo(streamID)
                    .doAfterTerminate(() -> {
                        if (isClickFinish) exitStream();
                    })
                    .subscribe(ignore -> {
                    }, throwable -> {
                        AppApplication.getInstance().logErrorServer("stopRecordVideo/" + streamID, networkManager.parseError(throwable));
                    });
        } else {
            Log.i(TAG, "stopRecordVideo: streamID is Empty");
        }
    }

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

            Logs.log("mBatInfoReceiver", "mBatInfoReceiver change: " + percent);
//            if (percent == 25 || percent == 20 || percent == 15 || percent == 10) {
            if (percent <= 25 && percent > 10) {
                showDialogInfo(TYPE_BATTERY);
            } else if (percent <= 10) {
                showDialogInfo(TYPE_BATTERY);
                exitStream();
                try {
                    LogModel logModel = new LogModel();
                    logModel.message = "Android: Viewer - Call end stream by low Battery";
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

            if (type == TYPE_NETWORK) {
                isErrorNetwork = true;
                lay_popup_network.setVisibility(View.VISIBLE);
            } else {
                isErrorBattery = true;
                lay_popup_battery.setVisibility(View.VISIBLE);
                txtPercent.setText(batteryLevel + "%");
            }
            handlerDialog.removeCallbacks(null);
            handlerDialog.postDelayed(() -> {
                lay_popup_battery.setVisibility(View.GONE);
                lay_popup_network.setVisibility(View.GONE);
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
            if (type == TYPE_NETWORK) {
                isErrorNetwork = false;
            } else {
                isErrorBattery = false;
            }

            if (isErrorNetwork || isErrorBattery) {
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
                mSignalStrength, false, getNetWorkType() + "", dataInputStream.settingRadius + "", dataInputStream.gigsId))
                .subscribe(ignore -> {
                }, throwable -> {
//                    AppApplication.getInstance().logErrorServer("location/" + gigsId, networkManager.parseError(throwable));
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

    private Runnable mRunnableStopStream = new Runnable() {
        @Override
        public void run() {
            Log.i("TAG", "mRunnableStopStream Run");
            if (mEnableReceivePush) {
                mEnableReceivePush = false;
                if (!isClickFinish) {
                    Log.i(TAG, "END_STREAM from Runnable");
                    try {
                        JSONObject props = new JSONObject();
                        mixpanel.track("Stream - Failed", props);
                    } catch (Exception e) {
                        Logs.log(e);
                    }
                    exitStream();
                }
            }
        }
    };

    public void setupChatDialog() {
        if (!TextUtils.isEmpty(dataInputStream.chatChannel)) {
            layChat.setEnabled(true);
            chatDialog = new ChatDialog(mActivity, R.style.DialogFullScreenTheme);
            chatDialog.setChatChanel(dataInputStream.chatChannel, dataInputStream.chatName, dataInputStream.chatAvatar, dataInputStream.gigsId);
            chatDialog.setIsFromEnRoute(true);
            chatDialog.setDatas(dataInputStream.chatData);
            chatDialog.setHeaderHeight();
            chatDialog.setBackgroundTransparent(true);
            chatDialog.setTakePhotoCalBack(new ChatDialog.TakePhotoCallback() {
                @Override
                public void onTakePhoto() {

                }

                @Override
                public void onHadNewMessages(boolean isHadNew) {
                    Logs.log(TAG, "onHadNewMessages : " + isHadNew);
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
}


package com.dropininc.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropininc.AppApplication;
import com.dropininc.BuildConfig;
import com.dropininc.R;
import com.dropininc.activity.DroperatorStreamingActivity;
import com.dropininc.activity.MainActivity;
import com.dropininc.activity.PrivacyPolicyActivity;
import com.dropininc.activity.ReferFriendActivity;
import com.dropininc.activity.ViewerStreamingActivity;
import com.dropininc.adapter.PlaceAutocompleteAdapter;
import com.dropininc.customview.CircleImageView;
import com.dropininc.customview.CustomAutoCompleteTextView;
import com.dropininc.customview.CustomSeekBar;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.ChatDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.DirectionDialog;
import com.dropininc.dialog.FullScreenDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.dialog.RatingDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.NotificationCode;
import com.dropininc.interfaces.NotificationKey;
import com.dropininc.interfaces.OperatorStatus;
import com.dropininc.interfaces.PaymentCode;
import com.dropininc.interfaces.RenderCallback;
import com.dropininc.interfaces.RequestCode;
import com.dropininc.interfaces.ResponseCode;
import com.dropininc.interfaces.UserType;
import com.dropininc.location.LocationManager;
import com.dropininc.map.MapManager;
import com.dropininc.message.MessageManager;
import com.dropininc.model.AccountSettingModel;
import com.dropininc.model.ChatModel;
import com.dropininc.model.ClaimModel;
import com.dropininc.model.ConfirmModel;
import com.dropininc.model.DataInputStreamModel;
import com.dropininc.model.LocationModel;
import com.dropininc.model.LogModel;
import com.dropininc.model.MapDirectionModel;
import com.dropininc.model.NotificationModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.PaymentStatusModel;
import com.dropininc.model.ResumeCheckModel;
import com.dropininc.model.SearchModel;
import com.dropininc.model.StartStreamModel;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.services.LocationService;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MapFragment extends BaseFragment implements OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "MapFragment";

    private Intent locationServiceIntent;

    private FragmentActivity mActivity;
    private View mRootView, mLayRadius;
    private CustomSeekBar mSeekBar;
    private Button mButtonFind, mButtonDestination, mButtonListDirection;
    private CustomAutoCompleteTextView mAutocompleteView;
    private TextView mTextRadius;
    private RelativeLayout mLaySearch;

    private GoogleMap mGoogleMap;
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private Marker mMarker;
    private Marker mMarkerViewerPosition;
    private Map<String, Marker> mListMarker;
    private RenderCallback mRenderCallback;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private int mUserType = UserType.VIEWER;
    private MapManager mMapManager;
    private LocationModel mLocationViewer;
    private LocationModel mLocationOperator;
    private LocationModel mLocationMainOperator;
    private TelephonyManager mTelephonyManager;
    private Handler mHandler;

    private boolean isCreateNew = true;
    private boolean mIsSearchBackground = false;
    private boolean mEnableGetListOperator = true;
    private boolean mShowDialogOperatorNotResponse = true;
    private boolean mEnRouteMode = false;
    private boolean mShowDialogBattery = true;
    private boolean mProcessingPayment = false;
    private boolean mNeedSwitchMode = false;
    private boolean mProcessFromNotification = false;
    private int mSignalStrength = 100;
    private int mSettingRadius = 10;
    private int mTimeGetListOperator = 0;
    private float mZoomDefault = 14.0f;
    private float mZoomViewer = 14.0f;
    private String mGigsId;
    private Map<String, String> mMapBattery;

    private LinearLayout lay_enroute;

    private RatingDialog mDialogRating;
    private Location mLastLocation;
    private boolean isLastLocationSent = false;

    private boolean isCharging = false;
    private int mBatteryLevel = 100;

    private String chatChannel = "";
    private String chatName = "";
    private String chatAvatar = "";

    private Button bt_Chat;
    private ChatDialog chatDialog;
    private Handler timeOutHandle;

    //    private Handler timerUpdateETA;
    private String viewerAddress = "";

    private Timer timerUpdateETA;

    private ConfirmModel confirmModel;
    private ClaimModel claimModel;

    private boolean isDismissDialogForEnableLocation = false;

    public static MapFragment mInstance;
    static String test = "null";

    public static MapFragment getInstance() {
        if (mInstance == null) {
            mInstance = new MapFragment();
            test = "Created";
            Logs.log("MAP_", "new MapFragment()");
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.log("MAP_", "onCreate - " + test);
        mActivity = getActivity();
        mMapManager = new MapManager(mActivity);

        chatChannel = "";
        chatName = "";
        chatAvatar = "";

        Logs.log("setUpMap", "isCreateNew: " + isCreateNew);
        if (isCreateNew) {
            isCreateNew = false;
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addOnConnectionFailedListener(this)
                    .build();
        } else {
            mGoogleMap = null;
        }
        mUserType = AppApplication.getInstance().getUserType();
        mListMarker = new HashMap<>();
        if (getArguments() != null) {
            mProcessFromNotification = true;
        }

        locationServiceIntent = new Intent(mActivity, LocationService.class);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mTelephonyManager = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mHandler = new Handler();
        mMapBattery = new HashMap<>();
        mRunnableOnlineOffline.run();

        if (mEnRouteMode) {
            showProgressDialog(getResources().getString(R.string.resuming), null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Logs.log("MAP_", "onStart");

        mGoogleApiClient.connect();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(locationReceiver,
                new IntentFilter(LocationService.LOCATION_FILTER));

        if (mUserType == UserType.OPERATOR) {
            startLocationForDroperator();
        } else {
            LocationModel defaultLocation = AppApplication.getInstance().getLocationDefault();
            if (latitude == defaultLocation.latitude && longitude == defaultLocation.longitude) {
                startLocationForViewer();
            }
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(locationReceiver);

        Logs.log("MAP_", "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mRunnableOnlineOffline);
        mHandler.removeCallbacks(mRunnableGetListOperatorBackground);
        mProgressDialog = null;

        Logs.log("MAP_", "onDestroyView");
        removeTimeOut();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_map, container, false);
        Logs.log("MAP_", "onCreateView");
        LocationModel defaultLocation = AppApplication.getInstance().getLocationDefault();
        LocationModel lastLocation = ((MainActivity) mActivity).getLastViewerLocation();
        if (lastLocation == null) {
            latitude = defaultLocation.latitude;
            longitude = defaultLocation.longitude;
        } else {
            latitude = lastLocation.latitude;
            longitude = lastLocation.longitude;
        }

        initView();
        if (DSharePreference.isHelpTutorial(mActivity, mUserType)) {
            showHelperDialog();
            return mRootView;
        }
        int TIME_DELAY = 100;

        if (((MainActivity) mActivity).isCheckLocationService()) {
            if (latitude == 0.0 && longitude == 0.0) {
                if (!DSharePreference.isAllowLocation(mActivity) || !Utils.isLocationEnabled(mActivity)) {
                    checkLocationService();
                    return mRootView;
                }
                latitude = defaultLocation.latitude;
                longitude = defaultLocation.longitude;
            }
            if (latitude == defaultLocation.latitude && longitude == defaultLocation.longitude) {
                if (!DSharePreference.isAllowLocation(mActivity) || !Utils.isLocationEnabled(mActivity)) {
                    checkLocationService();
                    return mRootView;
                }
            }
        } else {
            TIME_DELAY = 200;
        }
        new Handler().postDelayed(() -> {
            setUpMapIfNeeded();
            Logs.log(TAG, "UserType: " + mUserType);
            Logs.log(TAG, "ProcessFromNotification: " + mProcessFromNotification);
            if (!mProcessFromNotification) {
                setRadiusVisibility();
                setButtonFindVisibility();
                if (mUserType == UserType.VIEWER) {
                    if (!mProcessingPayment) {
                        getListOperator(false);
                    } else {
                        mProcessingPayment = false;
                        mEnableGetListOperator = false;
                        checkBeforeRequestStream();
                    }
                } else {
                    new Handler().postDelayed(() -> {
                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                        }
                        startLocationForDroperator();
                    }, 1000);
                }
            }
        }, TIME_DELAY);


        return mRootView;
    }

    @Override
    public void onResume() {
        Logs.log("MAP_", "onResume");
        super.onResume();
        if (mRenderCallback != null) {
            mRenderCallback.onRendered();
            mRenderCallback = null;
        }
        new Handler().postDelayed(this::handlerFromNotification, 500);
        if (mEnRouteMode) {
            if (((MainActivity) mActivity).getBatteryLevel() <= 10) {
                if (((MainActivity) mActivity).isDeviceCharging()) {
                    setUpMapIfNeeded();
                    new Handler().postDelayed(this::resumeEnroute, 1000);
                } else {
                    mShowDialogBattery = false;
                    mMapBattery.put("viewer_10", "viewer_10");
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setCancelable(false);
                    mDialog.setTitleDialog(R.string.disconnected);
                    mDialog.setMessageDialog(R.string.message_low_battery_viewer);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v -> {
                        mDialog.dismiss();
                        // Van added for kickout enroute
                        // added by Van 5/5/16
                        if (mGoogleMap != null) {
                            mGoogleMap.clear();
                        }
                        if (mListMarker != null) {
                            mListMarker.clear();
                        }
                        // end add
                        ((MainActivity) mActivity).setUserType(UserType.VIEWER);
                        setUserType(UserType.VIEWER);
                        setRadiusVisibility();
                        setButtonFindVisibility();
                    });
                    mDialog.show();
                }
            } else {
                setUpMapIfNeeded();
                new Handler().postDelayed(this::resumeEnroute, 1000);
            }
        }
    }

    private void resumeEnroute() {
        if (mUserType == UserType.VIEWER) {
            Logs.log("MAP_", "Resume enroute for User");
            processResponseConfirmRequest(confirmModel);
        } else {
            Logs.log("MAP_", "Resume enroute for droper");
            startEnRouteOperator(mGigsId);
        }
        hideProgressDialog();
    }

    @Override
    public void onPause() {
        Logs.log("MAP_", "onPause");
        super.onPause();
        setLastLocation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.atv_places:
                Logs.log("atv_places", " Click atv_places");
                mAdapter.setAutoFilter(true);
                break;
            case R.id.bt_find:
                clickRequestStream();
                break;
            case R.id.bt_destination:
                goToDestination();
                break;
            case R.id.bt_direction:
                showListDirection();
                break;
            case R.id.bt_Chat:
                showChatDialog();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.LOCATION_SETTINGS:
                if (Utils.isLocationEnabled(mActivity)) {
                    Logs.log("TAG", "LOCATION_SETTINGS");
                    DSharePreference.setAllowLocation(mActivity, true);
                    mProgressDialog = new ProgressDialog(mActivity);
                    mProgressDialog.setMessage(R.string.waiting_location);
                    mProgressDialog.show();
                    isDismissDialogForEnableLocation = true;

                    startLocationForViewer();

                } else {
                    setUpMapIfNeeded();
                    setRadiusVisibility();
                    setButtonFindVisibility();
                }
                break;
            case RequestCode.LOCATION_SETTINGS_SWITCH_MODE:
                if (Utils.isLocationEnabled(mActivity)) {
                    Logs.log("TAG", "LOCATION_SETTINGS_SWITCH_MODE");
                    DSharePreference.setAllowLocation(mActivity, true);
                    mProgressDialog = new ProgressDialog(mActivity);
                    mProgressDialog.setMessage(R.string.waiting_location);
                    mProgressDialog.show();

                    startLocationForViewer();

                    setNeedSwitchMode(false);
                    if (mUserType == UserType.VIEWER) {
                        switchOperatorMode();
                    }
                } else {
                    setUpMapIfNeeded();
                }
                break;
            case RequestCode.VIEWER_STREAM:
            case RequestCode.OPERATOR_STREAM:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        JSONObject props = new JSONObject();
                        props.put("gigsId", data.getStringExtra("gigsId"));
                        if (requestCode == RequestCode.VIEWER_STREAM)
                            props.put("Stopped By", "Viewer");
                        else
                            props.put("Stopped By", "Droperator");
                        props.put("LengthStream", data.getStringExtra("durationStreaming"));
                        props.put("CostStream", data.getStringExtra("price"));
                        mixpanel.track("Stream - Complete", props);
                    } catch (Exception e) {
                        Logs.log(e);
                    }
                    showDialogRating(requestCode, data);
                } else {
                    endStream(requestCode);
                }
                break;
            case RequestCode.POLICY_PRIVACY:
                if (resultCode == Activity.RESULT_OK) {
                    checkDocumentSignStatus();
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        lay_enroute = (LinearLayout) mRootView.findViewById(R.id.lay_enroute);
        mAutocompleteView = (CustomAutoCompleteTextView) mRootView.findViewById(R.id.atv_places);
        mSeekBar = (CustomSeekBar) mRootView.findViewById(R.id.seekBar);
        mLayRadius = mRootView.findViewById(R.id.lay_radius);
        mButtonFind = (Button) mRootView.findViewById(R.id.bt_find);
        mButtonDestination = (Button) mRootView.findViewById(R.id.bt_destination);
        mButtonListDirection = (Button) mRootView.findViewById(R.id.bt_direction);
        mTextRadius = (TextView) mRootView.findViewById(R.id.tv_value);
        mLaySearch = (RelativeLayout) mRootView.findViewById(R.id.lay_search);

        bt_Chat = (Button) mRootView.findViewById(R.id.bt_Chat);

        mButtonFind.setOnClickListener(this);
        mButtonDestination.setOnClickListener(this);
        mButtonListDirection.setOnClickListener(this);
        mAutocompleteView.setOnClickListener(this);
        mLaySearch.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        bt_Chat.setOnClickListener(this);

        mButtonFind.setText(R.string.request_stream);
        LatLngBounds BOUNDS_GREATER = new LatLngBounds(
                new LatLng(latitude, longitude), new LatLng(latitude, longitude));
        mAdapter = new PlaceAutocompleteAdapter(mActivity, R.layout.view_row_location_auto,
                mGoogleApiClient, BOUNDS_GREATER, null);
        mAutocompleteView.setAdapter(mAdapter);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        mAutocompleteView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Getting user input location
                String location = mAutocompleteView.getText().toString().trim();

                if (!location.equals("")) {
                    mAutocompleteView.dismissDropDown();
                    hideSoftKeyboard();
                    new GeocoderTask().execute(location);
                }
                return true;
            }
            return false;
        });
        mAutocompleteView.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mAdapter.setAutoFilter(true);
            }
        });

        int radius = ((MainActivity) mActivity).getSettingRadius();
        if (radius <= 0 || radius > 50) {
            radius = 10;
        }
        mSeekBarChangeListener.onProgressChanged(mSeekBar, radius, false);

        FontUtils.typefaceButton(mButtonFind, FontType.LIGHT);
        FontUtils.typefaceButton(mButtonDestination, FontType.LIGHT);
        FontUtils.typefaceButton(mButtonListDirection, FontType.LIGHT);
        FontUtils.typefaceEditText(mAutocompleteView, FontType.LIGHT);
    }

    private void startLocationForViewer() {
        Logs.log(TAG, "startLocationForViewer");

        locationServiceIntent.putExtra(LocationService.MODE_EXTRA, LocationManager.MODE.VIEWER_MODE);
        mActivity.startService(locationServiceIntent);
    }

    private void startLocationForDroperator() {
        Logs.log(TAG, "startLocationForDroperator");

        locationServiceIntent.putExtra(LocationService.MODE_EXTRA, LocationManager.MODE.DROPERATOR_MODE);
        mActivity.startService(locationServiceIntent);
    }

    private void stopGettingLocations() {
        Logs.log(TAG, "stopGettingLocations");

        locationServiceIntent.putExtra(LocationService.MODE_EXTRA, LocationManager.MODE.STREAM_MODE);
        mActivity.startService(locationServiceIntent);
    }

    private void handlerFromNotification() {
        if (mProcessFromNotification) {
            mProcessFromNotification = false;
            // TODO: Need to make refactoring this bundle
            Bundle bundle = getArguments();
            if (bundle != null && bundle.containsKey("code")) {
                int code = Integer.parseInt(bundle.getString(NotificationKey.CODE));
                boolean flag = true;
                switch (code) {
                    case NotificationCode.STREAM_START:
                        ((MainActivity) mActivity).setProcessingRequest(true);
                        ((MainActivity) mActivity).setEnableStartStream(true);
                        break;
                    case NotificationCode.STREAM_FINISH:
                        ((MainActivity) mActivity).setUserType(mUserType);
                        setUserType(mUserType);
                        setRadiusVisibility();
                        setButtonFindVisibility();
                        ((MainActivity) mActivity).setProcessingRequest(true);
                        ((MainActivity) mActivity).setMainActivityActive(false);

                        DataInputStreamModel dataInputStream = DataInputStreamModel.fromBundle(bundle);
                        dataInputStream.gigsId = AppApplication.getInstance().getCurrentGigsId();
                        dataInputStream.settingRadius = mSettingRadius;
                        dataInputStream.chatChannel = chatChannel;
                        dataInputStream.chatName = chatName;
                        dataInputStream.chatAvatar = chatAvatar;
                        dataInputStream.chatData = getChatData();

                        if (mUserType == UserType.VIEWER) {
                            Intent intent = dataInputStream.toIntent(mActivity, ViewerStreamingActivity.class);
                            intent.putExtra("IsConnectToStream", false);
                            mActivity.startActivityForResult(intent, RequestCode.VIEWER_STREAM);
                            removeChat();
                        } else {
                            Intent intent = dataInputStream.toIntent(mActivity, DroperatorStreamingActivity.class);
                            intent.putExtra("IsConnectToStream", false);
                            mActivity.startActivityForResult(intent, RequestCode.OPERATOR_STREAM);
                            removeChat();
                        }
                        flag = false;
                        break;
                    case NotificationCode.REQUEST_OPERATOR:
                        ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
                        setUserType(UserType.OPERATOR);
                        setRadiusVisibility();
                        setButtonFindVisibility();
                        break;
                    case NotificationCode.ACCEPT_DENY_REQUEST:
                        ((MainActivity) mActivity).setProcessingRequest(true);
                        break;
                    case NotificationCode.DOCUMENT_SIGN:

                        break;
                    case NotificationCode.OPERATOR_NOT_FOUND:
                        ((MainActivity) mActivity).setProcessingRequest(true);
                        break;
                    case NotificationCode.EN_ROUTE:
                        ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
                        setUserType(UserType.OPERATOR);
                        setRadiusVisibility();
                        setButtonFindVisibility();
                        ((MainActivity) mActivity).setProcessingRequest(true);
                        String data = bundle.getString("data");
                        NotificationModel.Data notificationData = NotificationModel.Data.fromJSON(data);
                        setViewerLocation(notificationData.gig.latitude, notificationData.gig.longitude);
                        break;
                }
                if (flag) {
                    Intent data = new Intent();
                    data.setAction(Constants.NOTIFICATION_FILTER);
                    data.putExtras(bundle);
                    mActivity.sendBroadcast(data);
                }
            }
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(mActivity);
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            LatLng latLng;
            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(mActivity, "No Location found", Toast.LENGTH_SHORT).show();
            } else {
                // Adding Markers on Google Map for each matching address
                Address address = addresses.get(0);
                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                if (mMarker != null) {
                    mMarker.setPosition(latLng);
                }
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
                        longitude), mZoomViewer));
                getListOperator(false);
                updateLocationAddress(latLng);
                try {
                    JSONObject props = new JSONObject();
                    props.put("longitude", longitude);
                    props.put("latitude", latitude);
                    mixpanel.track("Pin Set", props);
                } catch (JSONException e) {
                    Logs.log(e);
                }
            }
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mGoogleMap == null && getChildFragmentManager() != null) {
            final SupportMapFragment frag = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapHolder));
            Logs.log("setUpMap", "frag: " + frag);
            if (frag != null) {
                mGoogleMap = frag.getMap();
            }
        }
        if (mGoogleMap != null) {
            setUpMap();
        }
    }

    private void setUpMap() {
        if (latitude != 0.0 && longitude != 0.0) {
            // For showing a move to my location button
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                    mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setTiltGesturesEnabled(false);
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
            // For dropping a marker at a point on the Map
            addMarkerLocation();
            updateLocationAddress(new LatLng(latitude, longitude));
            // For zooming automatically to the Dropped PIN Location
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mZoomDefault));
            Logs.log("setUpMap", "moveCamera to " + latitude + "," + longitude);
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    mAdapter.setAutoFilter(false);
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    mAdapter.setAutoFilter(false);
                    LatLng latLng = marker.getPosition();
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                    removeRoutePath();
                    getListOperator(false);
                    updateLocationAddress(latLng);
                    try {
                        JSONObject props = new JSONObject();
                        props.put("longitude", longitude);
                        props.put("latitude", latitude);
                        mixpanel.track("Pin Set", props);
                    } catch (JSONException e) {
                        Logs.log(e);
                    }
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                }
            });
            mGoogleMap.setOnMarkerClickListener(marker -> {
                Logs.log("onMarkerClick", marker.toString());
                if (!marker.isDraggable()) {

                }
                return false;
            });
            mGoogleMap.setOnInfoWindowClickListener(Marker::hideInfoWindow);
            mGoogleMap.setOnMapClickListener(latLng -> {
                Logs.log("onMapClick", latLng.toString());
                if (mUserType == UserType.VIEWER) {
                    if (!mEnRouteMode) {
                        mAdapter.setAutoFilter(false);
                        latitude = latLng.latitude;
                        longitude = latLng.longitude;
                        addMarkerLocation();
                        removeRoutePath();
                        getListOperator(false);
                        updateLocationAddress(latLng);
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                    }
                }
            });
            mGoogleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
            mGoogleMap.setOnCameraChangeListener(cameraPosition -> {
                int newZoom = -1;
                if (cameraPosition.zoom > 20) newZoom = 20;
                if (cameraPosition.zoom < 10) newZoom = 10;

                if (newZoom != -1) mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(newZoom));

                if (mUserType == UserType.VIEWER) {
                    mZoomViewer = cameraPosition.zoom;
                    Logs.log("onCameraChange", mZoomViewer + "");
                }
            });
        }
    }

//    private void mapClickForOperator(final LatLng latLng) {
//        LocationModel fromLocation = new LocationModel();
//        fromLocation.latitude = mMarker.getPosition().latitude;
//        fromLocation.longitude = mMarker.getPosition().longitude;
//        LocationModel toLocation = new LocationModel();
//        toLocation.latitude = latLng.latitude;
//        toLocation.longitude = latLng.longitude;
//        updateMarkerLocation(latLng);
//        Location location = new Location("");
//        location.setLatitude(latLng.latitude);
//        location.setLongitude(latLng.longitude);
//        sendLocationMessageToViewer(location);
//        drawMapEnRoute(fromLocation, toLocation);
//    }

    private void clickRequestStream() {
        if (!isCharging && mBatteryLevel <= 5) {
            final AlertDialog mDialog = new AlertDialog(mActivity);
            mDialog.setCancelable(false);
            mDialog.setTitleDialog(R.string.low_battery);
            mDialog.setMessageDialog(R.string.message_low_battery_viewer);
            mDialog.setTitleButton(R.string.ok_cap);
            mDialog.setButtonClick(v -> mDialog.dismiss());
            mDialog.show();
            return;
        }

        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(R.string.message_confirm_before_request_stream);
        mDialog.setOkTitleButton(R.string.continues);
        mDialog.setCancelTitleButton(R.string.cancel_stream);
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            checkBeforeRequestStream();
        });
        mDialog.setCancelButtonClick(v -> mDialog.dismiss());
        mDialog.show();
        try {
            JSONObject props = new JSONObject();
            mixpanel.track("Stream - Viewer Requested", props);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void checkBeforeRequestStream() {
        if (!AppApplication.getInstance().isCheckPayment()) {
            checkPaymentMethod();
        } else {
            mEnableGetListOperator = false;
            requestStream();
        }
    }

    public void setRenderCallback(RenderCallback mRenderCallback) {
        this.mRenderCallback = mRenderCallback;
    }

    private void addMarkerLocation() {
        if (mMarker != null) {
            mMarker.remove();
        }

        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(new LatLng(latitude, longitude));
        if (mUserType == UserType.VIEWER) {
            mMarkerOptions.draggable(true);
            mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location));
        } else {
            mMarkerOptions.draggable(false);
            mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_droperator));
        }
        mMarker = mGoogleMap.addMarker(mMarkerOptions);
        try {
            JSONObject props = new JSONObject();
            props.put("longitude", longitude);
            props.put("latitude", latitude);
            mixpanel.track("Pin Set", props);
        } catch (JSONException e) {
            Logs.log(e);
        }
    }

    public void addMarkerLocationViewer(double latitude, double longitude) {
        if (mMarkerViewerPosition != null) {
            mMarkerViewerPosition.setVisible(false);
            mMarkerViewerPosition.remove();
        }
        MarkerOptions mMarkerOptions = new MarkerOptions();
        mMarkerOptions.position(new LatLng(latitude, longitude));
        mMarkerOptions.draggable(false);
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location));
        mMarkerViewerPosition = mGoogleMap.addMarker(mMarkerOptions);
    }

    public Marker addMarkerLocationOperator(String firstName, String lastName, double latitude, double longitude) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_droperator));
        markerOptions.title(firstName + " " + lastName);
        return mGoogleMap.addMarker(markerOptions);
    }

    private void updateMarkerLocation(LatLng latLng) {
        if (mMarker != null) {
            mMarker.setPosition(latLng);
        }
    }

    private void getListOperator(boolean showDialog) {
        if (mUserType == UserType.VIEWER && latitude != 0.0 && longitude != 0.0) {
            mHandler.removeCallbacks(mRunnableGetListOperatorBackground);
            Logs.log("getListOperator", "Remove: " + mRunnableGetListOperatorBackground);
            mTimeGetListOperator = 0;
            mIsSearchBackground = false;
            if (showDialog) {
                mProgressDialog = new ProgressDialog(mActivity);
                mProgressDialog.setCancelListener(dialog -> {

                });
                mProgressDialog.show();
            }

            getListOperator();
        }
    }

    private void getListOperatorBackground() {
        Logs.log(TAG, "getListOperatorBackground");
        if (mUserType == UserType.VIEWER && mEnableGetListOperator) {
            mIsSearchBackground = true;
            getListOperator();
        }
    }

    private void getListOperator() {
        networkManager.searchOperator(latitude, longitude)
                .subscribe(searchModels -> {
                    Logs.log("UserType", "" + mUserType);
                    if (mUserType == UserType.VIEWER) {
                        addOperatorToMap(searchModels);
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("searchOperator", networkManager.parseError(throwable));
                    dismissDialog();
                });
    }

    public void setRadiusVisibility() {
        if (mUserType == UserType.OPERATOR) {
            hideSoftKeyboard();/*KINH fix bug*/
            mLayRadius.setVisibility(View.VISIBLE);
            mLaySearch.setVisibility(View.GONE);
            showVolumeDialog();
        } else {
            mLayRadius.setVisibility(View.GONE);
            mLaySearch.setVisibility(View.VISIBLE);
        }
        addMarkerLocation();
    }

    public void showVolumeDialog(){
        if(mActivity == null || AppApplication.getInstance().isHighVolumePhone()){
            return;
        }
        final AlertDialog mDialog = new AlertDialog(mActivity);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog("Please turn on or increase volumn.");
        mDialog.setTitleButton(R.string.ok_cap);
        mDialog.setButtonClick(v1 -> mDialog.dismiss());
        mDialog.show();

    }

    public void setUserType(int userType) {
        mUserType = userType;
        AppApplication.getInstance().setUserType(mUserType);

        if (mUserType == UserType.OPERATOR) {
            Utils.startLocationAlarm(mActivity);

            if (mListMarker.size() > 0) {
                for (String key : mListMarker.keySet()) {
                    Marker marker = mListMarker.get(key);
                    marker.remove();
                }
                mListMarker.clear();
            }
            setLastLocation();
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
            startLocationForDroperator();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mZoomDefault));
            Logs.log("setUserType", "setUserType to " + latitude + "," + longitude);
            mEnableGetListOperator = false;
            mTimeGetListOperator = 0;
            mHandler.removeCallbacks(mRunnableGetListOperatorBackground);
            Logs.log("setUserType", "Remove: " + mRunnableGetListOperatorBackground);
            ((MainActivity) mActivity).removeAllChannel();
            ((MainActivity) mActivity).addChannelListener(AppApplication.getInstance().getAccountId());
        } else {
            Utils.stopLocationAlarm(mContext);

            mEnableGetListOperator = true;
            mButtonDestination.setVisibility(View.GONE);
            mButtonListDirection.setVisibility(View.GONE);
            LocationModel location = ((MainActivity) mActivity).getLastViewerLocation();
            if (location != null) {
                latitude = location.latitude;
                longitude = location.longitude;
            }
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), mZoomDefault));
            Logs.log("setUserType", "setUserType to " + latitude + "," + longitude);
            ((MainActivity) mActivity).removeAllChannel();
            ((MainActivity) mActivity).addChannelListener(AppApplication.getInstance().getAccountId());
            getListOperator(false);
            startLocationForViewer();
            Utils.sendMessageSwitchModeToViewer(mActivity, AppApplication.getInstance().getAccountId());
        }

        resetParamsBattery();
        ((MainActivity) mActivity).resetParams();
        removeRoutePath();
        setUserOnlineOffline();
    }

    private void requestStream() {
        Logs.log(TAG, "requestStream");
        mButtonFind.setEnabled(false);
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIconLoading(R.drawable.bg_progress);
        mProgressDialog.setMessage(R.string.searching_for_operator);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Logs.log("AddressRequest", "Address requested:" + viewerAddress);
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("address", viewerAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        networkManager.setOperatorDefault(latitude, longitude, metaData.toString())
                .subscribe(this::processResponseRequestStream,
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("setOperatorDefault", networkManager.parseError(throwable));
                            dismissDialog();
                            operatorNotFound(networkManager.parseError(throwable).code);
                        });

        try {
            LogModel logModel = new LogModel();
            logModel.level = "debug";
            logModel.message = "Android: viewer request stream - URL: http://apidev.dropininc.com/gigs";
            logModel.data = "latitude=" + latitude + "&longitude=" + longitude;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Viewer
    private void processResponseRequestStream(final ClaimModel claimModel) {
        this.claimModel = claimModel;
        try {
            LogModel logModel = new LogModel();
            logModel.level = "debug";
            logModel.message = "Android: processResponseRequestStream - URL: http://apidev.dropininc.com/gigs";
            logModel.data = claimModel.toString();
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (claimModel == null) {
            dismissDialog();
            return;
        }
        ((MainActivity) mActivity).setProcessingRequest(true);
        mShowDialogOperatorNotResponse = true;
        mEnableGetListOperator = false;
        mTimeGetListOperator = 0;
        if (mHandler != null) mHandler.removeCallbacks(mRunnableGetListOperatorBackground);
        Logs.log("selectOperatorFound", "Remove: " + mRunnableGetListOperatorBackground);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.show();
        }
        mProgressDialog.setMessage(R.string.message_waiting_response_from_droperator);

        new Handler().postDelayed(() -> {
            dismissDialog();
            Logs.log("mShowDialogOperatorNotResponse", "" + mShowDialogOperatorNotResponse);
            if (mShowDialogOperatorNotResponse) {
                resetParamsViewerMode();
                ((MainActivity) mActivity).setProcessingRequest(false);
                ((MainActivity) mActivity).setAccountListener("");
                mButtonFind.setEnabled(true);
                Logs.log("Cancel Request", "Show From Runnable");
                final AlertDialog mDialog = new AlertDialog(mActivity);
                mDialog.setTitleDialog("");
                mDialog.setMessageDialog(R.string.message_deny_request_from_droperator);
                mDialog.setTitleButton(R.string.ok_cap);
                mDialog.setButtonClick(v -> mDialog.dismiss());
                mDialog.show();
            }
        }, 30 * 1000);
    }

    private void updateLocationAddress(final LatLng latLng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(mActivity);
            try {
                List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (listAddress != null && listAddress.size() > 0) {
                    Address item = listAddress.get(0);
                    int maxIndex = item.getMaxAddressLineIndex();
                    if (maxIndex > -1) {
                        String address = "";
                        for (int i = 0; i <= maxIndex; i++) {
                            if (TextUtils.isEmpty(address)) {
                                address = item.getAddressLine(i);
                            } else {
                                address = address + ", " + item.getAddressLine(i);
                            }
                        }
                        final String textUpdate = address;
                        if (getActivity() != null) {
                            mActivity.runOnUiThread(() -> {
                                mAutocompleteView.setText(textUpdate);
                                viewerAddress = textUpdate;
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addOperatorToMap(ArrayList<SearchModel> listPerson) {
        for (String key : mListMarker.keySet()) {
            Marker marker = mListMarker.get(key);
            marker.remove();
            removeChannel(key);
        }
        mListMarker.clear();
        for (int i = 0; i < listPerson.size(); i++) {
            SearchModel model = listPerson.get(i);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(model.location.latitude, model.location.longitude));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_droperator));
            markerOptions.title(model.account.firstName + " " + model.account.lastName);
            Marker marker = mGoogleMap.addMarker(markerOptions);
            mListMarker.put(model.account.id, marker);

            addChannel(model.account.id);
        }

        if (!mIsSearchBackground) {
            dismissDialog();
        }
        if (mTimeGetListOperator == 0) {
            mRunnableGetListOperatorBackground.run();
            Logs.log("addOperatorToMap", "Add: " + mRunnableGetListOperatorBackground);
        }
    }

    private void removeChannel(String channel) {
        Logs.log("Remove Channel", channel);
        ((MainActivity) mActivity).removeChannelListener(channel);
    }

    private void addChannel(String channel) {
        Logs.log("Add Channel", channel);
        ((MainActivity) mActivity).addChannelListener(channel);
    }

    public void showHelperDialog() {
        final FullScreenDialog mFullScreenDialog = new FullScreenDialog(mActivity);
        int resourceId = 0;
        switch (mUserType) {
            case UserType.VIEWER:
                resourceId = R.drawable.ic_map_tutorial;
                mFullScreenDialog.setButtonVisibility(View.GONE);
                mFullScreenDialog.setButtonCenterVisibility(View.VISIBLE);
                mFullScreenDialog.setButtonCenterListener(view -> {
                    mFullScreenDialog.dismiss();
                    showDialogNotification();
                });
                break;
            case UserType.OPERATOR:
                resourceId = R.drawable.ic_map_operator;
                mFullScreenDialog.setButtonVisibility(View.VISIBLE);
                mFullScreenDialog.setButtonCenterVisibility(View.GONE);
                mFullScreenDialog.setButtonListener(v -> {
                    mFullScreenDialog.dismiss();
                    initView();
                    setUpMapIfNeeded();
                    if (!mNeedSwitchMode) {
                        ((MainActivity) mActivity).processMapFragment();
                    } else {
                        checkLocationService();
                    }
                });
                break;
        }
        mFullScreenDialog.setBackgroundImage(resourceId);
        mFullScreenDialog.setUserType(mUserType);
        mFullScreenDialog.show();
    }

    private void showDialogNotification() {
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setTitleDialog(R.string.title_permission_notification);
        mDialog.setMessageDialog(R.string.message_permission_notification);
        mDialog.setCancelableDialog(false);
        mDialog.setCancelTitleButton(R.string.do_not_allow);
        mDialog.setOkTitleButton(R.string.ok_cap);
        mDialog.setOkButtonClick(view -> {
            mDialog.dismiss();
            DSharePreference.setAllowNotification(mActivity, true);
            showDialogLocation();
        });
        mDialog.setCancelButtonClick(view -> {
            mDialog.dismiss();
            DSharePreference.setAllowNotification(mActivity, false);
            showDialogLocation();
        });
        mDialog.show();
    }

    private void showDialogLocation() {
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(R.string.title_permission_location);
        mDialog.setCancelableDialog(false);
        mDialog.setCancelTitleButton(R.string.do_not_allow);
        mDialog.setOkTitleButton(R.string.ok_cap);
        mDialog.setOkButtonClick(view -> {
            mDialog.dismiss();
            DSharePreference.setAllowLocation(mActivity, true);
            if (!Utils.isLocationEnabled(mActivity)) {
                initView();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivityForResult(intent, RequestCode.LOCATION_SETTINGS);
            } else {
                startLocationForViewer();
                new Handler().postDelayed(this::initView, 1000);
            }
        });

        mDialog.setCancelButtonClick(view -> {
            mDialog.dismiss();
            DSharePreference.setAllowLocation(mActivity, false);
            if (mNeedSwitchMode) {
                ((MainActivity) mActivity).setUserType(UserType.VIEWER);
                setUserType(UserType.VIEWER);
                setNeedSwitchMode(false);
            }
            setUpMapIfNeeded();
            initView();
            setRadiusVisibility();
            setButtonFindVisibility();
            getListOperator(false);
        });
        mDialog.show();
    }

    public void checkLocationService() {
        Logs.log(TAG, "checkLocationService");
        if (DSharePreference.isAllowLocation(mActivity) && Utils.isLocationEnabled(mActivity)) {
            switchOperatorMode();
            return;
        }
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setTitleDialog(R.string.title_location_service);
        mDialog.setMessageDialog(R.string.message_location_service);
        mDialog.setCancelTitleButton(R.string.cancel);
        mDialog.setOkTitleButton(R.string.enable);
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mActivity.startActivityForResult(intent, RequestCode.LOCATION_SETTINGS);
        });
        mDialog.setCancelButtonClick(v -> {
            mDialog.dismiss();
            setParamforSwitchViwer();
            setUpMapIfNeeded();
            setRadiusVisibility();
            setButtonFindVisibility();
            getListOperator(false);
            setNeedSwitchMode(false);

        });
        mDialog.show();
    }

    private void switchOperatorMode() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        if (!AppApplication.getInstance().isOperator()) {
            final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
            mDialog.setTitleDialog("");
            mDialog.setMessageDialog(R.string.message_become_operator);
            mDialog.setCancelTitleButton(R.string.cancel);
            mDialog.setOkTitleButton(R.string.yes);
            mDialog.setOkButtonClick(view -> {
                mDialog.dismiss();
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.hide();
//                checkDocumentSignStatus();
                showPolicy();
            });
            mDialog.setCancelButtonClick(view -> {
                mDialog.dismiss();
                ((MainActivity) mActivity).setIsProgreesing(false);
            });
            mDialog.show();
        } else {
            checkDocumentSignStatus();
        }

    }

    public void checkBeforeSwitchUserType() {
        Logs.log(TAG, "checkBeforeSwitchUserType");
        if (DSharePreference.isAllowLocation(mActivity) && Utils.isLocationEnabled(mActivity)) {
            switchOperatorMode();
            return;
        }
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setTitleDialog(R.string.title_location_service);
        mDialog.setMessageDialog(R.string.message_location_service);
        mDialog.setCancelTitleButton(R.string.cancel);
        mDialog.setOkTitleButton(R.string.enable);
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            setNeedSwitchMode(true);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mActivity.startActivityForResult(intent, RequestCode.LOCATION_SETTINGS_SWITCH_MODE);
        });
        mDialog.setCancelButtonClick(v -> {
            mDialog.dismiss();
            setParamforSwitchViwer();
            setUpMapIfNeeded();
            setRadiusVisibility();
            setButtonFindVisibility();
            getListOperator(false);
            setNeedSwitchMode(false);
            Log.d("mCancel", "flag 3: " + mNeedSwitchMode);
        });
        mDialog.show();
    }

    public void updateLocationToServer() {
        if (mLastLocation == null) return;
        Logs.log(TAG, "updateLocationToServer");
        if (DSharePreference.isAllowLocation(mActivity)) {
            networkManager.location(new LocationRequest(mLastLocation.getLatitude() + "",
                    mLastLocation.getLongitude() + "", MainActivity.getInstance().getBatteryLevel() + "",
                    mSignalStrength, mUserType == UserType.OPERATOR, getNetWorkType() + "",
                    mSettingRadius + "", mGigsId))
                    .subscribe(ignore -> isLastLocationSent = true, throwable -> {
                        AppApplication.getInstance().logErrorServer("location", networkManager.parseError(throwable));
                        if (mUserType == UserType.OPERATOR) updateLocationToServer();
                    });
        }
    }

    public void setButtonFindVisibility() {
        if (mUserType == UserType.VIEWER) {
            mButtonFind.setVisibility(View.VISIBLE);
        } else {
            mButtonFind.setVisibility(View.GONE);
        }
    }

    private void invalidDropinPayment() {
        final AlertDialog mDialog = new AlertDialog(mActivity);
        mDialog.setTitleDialog(R.string.invalid_dropin_payment);
        mDialog.setMessageDialog(R.string.please_enter_a_new_payment_method_to_continue);
        mDialog.setCancelableDialog(false);
        mDialog.setButtonClick(view -> {
            mDialog.dismiss();
            mButtonFind.setEnabled(true);
            mEnableGetListOperator = true;
        });
        mDialog.show();
    }

    private void referFriend() {
        final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
        mDialog.setCancelable(false);
        mDialog.setTitleDialog(R.string.no_result_found);
        mDialog.setMessageDialog(R.string.message_refer_friend);
        mDialog.setOkTitleButton(R.string.refer_a_friend);
        mDialog.setCancelTitleButton(R.string.cancel);
        mDialog.setOkButtonClick(v -> {
            mDialog.dismiss();
            mButtonFind.setEnabled(true);
            mEnableGetListOperator = true;

            Intent intent = new Intent(mActivity, ReferFriendActivity.class);
            startActivity(intent);
        });
        mDialog.setCancelButtonClick(view -> {
            mDialog.dismiss();
            mButtonFind.setEnabled(true);
            mEnableGetListOperator = true;
        });
        mDialog.show();
    }

    public void resetParamsViewerMode() {
        mEnableGetListOperator = true;
        mTimeGetListOperator = 0;
        mRunnableGetListOperatorBackground.run();
        Logs.log("resetParamsViewerMode", "Add: " + mRunnableGetListOperatorBackground);
    }

    private void checkPaymentMethod() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();

        networkManager.getPaymentOptions()
                .subscribe(this::processResponseCheckPayment, throwable -> {
                    AppApplication.getInstance().logErrorServer("getPaymentOptions", networkManager.parseError(throwable));
                    dismissDialog();
                });
    }

    private void processResponseCheckPayment(PaymentStatusModel model) {
        dismissDialog();
        if (model == null) return;

        if (model.code.equalsIgnoreCase(PaymentCode.CARD_OK)) {
            AppApplication.getInstance().setCheckPayment(true);
            requestStream();
        } else if (model.code.equalsIgnoreCase(PaymentCode.NO_CARD)) {
            final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
            mDialog.setTitleDialog("");
            mDialog.setMessageDialog(R.string.please_enter_a_payment_method_to_continue);
            mDialog.setOkTitleButton(R.string.add_payment);
            mDialog.setCancelTitleButton(R.string.cancel_request);
            mDialog.setOkButtonClick(view -> {
                mDialog.dismiss();
                mProcessingPayment = true;
                PaymentFragment paymentFragment = new PaymentFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("mIsContinueSearch", true);
                paymentFragment.setArguments(bundle);
                paymentFragment.setTitle(getString(R.string.payment));
                MainActivity.getInstance().addToolBarNormal(getString(R.string.payment_title));
                MainActivity.getInstance().pushFragments(paymentFragment);
            });
            mDialog.setCancelButtonClick(view -> {
                mDialog.dismiss();
                if (!mEnableGetListOperator) {
                    mEnableGetListOperator = true;
                    mTimeGetListOperator = 0;
                    getListOperator(false);
                }
            });
            mDialog.show();
        } else {
            final AlertDialog mDialog = new AlertDialog(mActivity);
            mDialog.setTitleDialog(R.string.opp_invalid_payment_method);
            mDialog.setMessageDialog(R.string.message_invalid_payment_method);
            mDialog.setTitleButton(R.string.go_to_payment);
            mDialog.setButtonClick(view -> {
                mDialog.dismiss();
                mProcessingPayment = true;
                PaymentFragment paymentFragment = new PaymentFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("mIsContinueSearch", true);
                paymentFragment.setArguments(bundle);
                paymentFragment.setTitle(getString(R.string.payment));
                MainActivity.getInstance().pushFragments(paymentFragment);
            });
            mDialog.show();
        }
    }

    public void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void removeTimeOut() {
        if (timeOutHandle != null) {
            timeOutHandle.removeCallbacksAndMessages(null);
            Logs.log("TAG", "remove timeout");
        }
    }

    private void updateRadiusService(int mile) {
        networkManager.updateAccountSetting(mile)
                .subscribe(this::processResponseUpdateRadius, throwable -> {
                    AppApplication.getInstance().logErrorServer("updateAccountSetting", networkManager.parseError(throwable));
                    dismissDialog();
                });
    }

    private void processResponseUpdateRadius(ArrayList<AccountSettingModel> listItem) {
        dismissDialog();
        if (listItem == null || listItem.size() == 0) return;
        ((MainActivity) mActivity).setSettingRadius(listItem.get(0).detectRadius);
        DSharePreference.setSettingRadius(mActivity, listItem.get(0).detectRadius);
        try {
            int code = NotificationCode.NEW_LOCATION;
            String operatorId = AppApplication.getInstance().getAccountId();
            int settingRadius = ((MainActivity) mActivity).getSettingRadius();
            String messageId = UUID.randomUUID().toString();
            JSONObject locationObject = new JSONObject();
            locationObject.put("latitude", latitude + "");
            locationObject.put("longitude", longitude + "");

            JSONObject message = new JSONObject();
            message.put("code", code);
            message.put("operatorId", operatorId);
            message.put("settingRadius", settingRadius + "");
            message.put("messageId", messageId);
            message.put("location", locationObject);
            message.put("pushType", "App");

            Logs.log(TAG, "publishMessage AccountId: " + operatorId + " message:" + message);
            ((MainActivity) mActivity).publishMessage(operatorId, message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void operatorNotFound(String errorCode) {
        if (errorCode.equalsIgnoreCase(ResponseCode.E_PROCESS_UNDERWAY)) {
            final AlertDialog mDialog = new AlertDialog(mActivity);
            mDialog.setCancelable(false);
            mDialog.setTitleDialog("");
            mDialog.setMessageDialog(R.string.similar_process_is_already_active);
            mDialog.setTitleButton(R.string.ok_cap);
            mDialog.setButtonClick(v -> {
                mDialog.dismiss();
                mButtonFind.setEnabled(true);
                mEnableGetListOperator = true;
            });
            mDialog.show();
        } else if (errorCode.equalsIgnoreCase(ResponseCode.E_AUTHORIZATION_FAILED)) {
            invalidDropinPayment();
        } else {
            referFriend();
        }
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: operatorNotFound - URL: http://apidev.dropininc.com/gigs";
            logModel.data = "Error code: " + errorCode;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkDocumentSignStatus() {
        OperatorModel model = ((MainActivity) mActivity).getDocusignStatus();
        if (model == null) {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.show();

            networkManager.checkOperatorProfile(AppApplication.getInstance().getAccountId())
                    .subscribe(this::processResponseCheckDocumentSign, throwable -> {
                        AppApplication.getInstance().logErrorServer("checkOperatorProfile", networkManager.parseError(throwable));
                        dismissDialog();
                    });

        } else {
            ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
            ((MainActivity) mActivity).processMapFragment();
            updateLocationToServer();
        }
        ((MainActivity) mActivity).setIsProgreesing(false);
    }

    public void processResponseCheckDocumentSign(OperatorModel model) {
        dismissDialog();
        try {
            if (model == null) return;
            if (model.status.equalsIgnoreCase(OperatorStatus.UNAPPROVED) || model.status.equalsIgnoreCase(OperatorStatus.CANCELED)) {
                // TODO skip show signup doperator
                // show faild
                final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
                mDialog.setTitleDialog("");
                mDialog.setMessageDialog(R.string.something_wrong);
                mDialog.setCancelTitleButton(R.string.skip);
                mDialog.setOkTitleButton(R.string.ok_cap);

                mDialog.setButtonCancelVisibility(View.GONE);

                mDialog.setOkButtonClick(view -> mDialog.dismiss());
                mDialog.setCancelButtonClick(view -> {
                    mDialog.dismiss();
                });
                mDialog.show();
                // end code
                try {
                    JSONObject props = new JSONObject();
                    props.put("Droperator Status", "Not Approved");
                    mixpanel.track("Droperator - Toggled", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
            } else if (model.status.equalsIgnoreCase(OperatorStatus.WAITING)) {
                final ConfirmDialog mDialog = new ConfirmDialog(mActivity);
                mDialog.setTitleDialog("");
                mDialog.setMessageDialog(R.string.message_document_not_approved);
                mDialog.setCancelTitleButton(R.string.skip);
                mDialog.setOkTitleButton(R.string.ok_cap);

                mDialog.setButtonCancelVisibility(View.GONE);

                mDialog.setOkButtonClick(view -> mDialog.dismiss());
                mDialog.setCancelButtonClick(view -> {
                    mDialog.dismiss();
                    activeOperator();
                });
                mDialog.show();
                try {
                    JSONObject props = new JSONObject();
                    props.put("Droperator Status", "Pending Approval");
                    mixpanel.track("Droperator - Toggled", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
            } else {
                ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
                ((MainActivity) mActivity).processMapFragment();
                updateLocationToServer();
                ((MainActivity) mActivity).setDocusignStatus(model);
                AppApplication.getInstance().setOperator(true);
                DSharePreference.setOperator(mActivity, true);
                ((MainActivity) mActivity).getLeftMenu().checkOperatorStatus();
                try {
                    JSONObject props = new JSONObject();
                    mixpanel.track("Droperator - Approved", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
                try {
                    JSONObject props = new JSONObject();
                    props.put("Droperator Status", "Approved");
                    mixpanel.track("Droperator - Toggled", props);
                } catch (Exception e) {
                    Logs.log(e);
                }
            }
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void activeOperator() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();

        networkManager.setActiveOperator()
                .subscribe(ignore -> processResponseActiveOperator(), throwable -> {
                    AppApplication.getInstance().logErrorServer("setActiveOperator", networkManager.parseError(throwable));
                    dismissDialog();
                });
    }

    private void processResponseActiveOperator() {
        DSharePreference.setOperator(mActivity, true);
        dismissDialog();
        ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
        ((MainActivity) mActivity).processMapFragment();
        updateLocationToServer();
    }

    public void acceptRequestViewer(String gigsId, LocationModel locationViewer, String action) {
        mGigsId = gigsId;
        AppApplication.getInstance().setCurrentGigsId(gigsId);
        mLocationViewer = locationViewer;
        if (action.equalsIgnoreCase("accept")) {
            try {
                // add by Van for fix bug reinit map when close rating dialog
                if (mDialogRating != null && mDialogRating.isShowing()) {
                    mDialogRating.dismiss();
                }
                // end
                mProgressDialog = new ProgressDialog(mActivity);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                // set confirm time out
                timeOutHandle = new Handler();
                timeOutHandle.postDelayed(() -> {
                    timeOutHandle.removeCallbacksAndMessages(null);
                    timeOutHandle = null;
                    dismissDialog();
                    Logs.log("TAG", "timeout push event");
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setCancelable(false);
                    mDialog.setTitleDialog("");
                    mDialog.setMessageDialog(R.string.message_job_is_no_longer_active);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v -> {
                        mDialog.dismiss();
                        ((MainActivity) mActivity).setProcessingRequest(false);
                        ((MainActivity) mActivity).setAccountListener("");
                        Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer: " + ((MainActivity) mActivity).getProcessingRequest());
                    });
                    mDialog.show();
                }, 60000);
                Logs.log("TAG", "start timeout");
            } catch (Exception e) {
                Logs.log(e);
            }
        }

        //TODO Accept request
        Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer: " + gigsId + " - " + action);
        networkManager.responseViewer(gigsId, action)
                .subscribe(claimModel -> {
                            Logs.log("REQUEST_OPERATOR", "responseViewer success");
                            processResponseAcceptViewer(claimModel, action, "");
                        }
                        , throwable -> {
                            AppApplication.getInstance().logErrorServer("responseViewer/" + gigsId, networkManager.parseError(throwable));
                            Logs.log("REQUEST_OPERATOR", "responseViewer Fail");
                            processResponseAcceptViewer(new ClaimModel(), action, networkManager.parseError(throwable).code);
                        });
        try {
            LogModel logModel = new LogModel();
            if (action.equalsIgnoreCase("accept")) {
                logModel.message = "Android: Droperator accept request of viewer - URL: http://apidev.dropininc.com/gigs/claim/" + gigsId;
            } else {
                logModel.message = "Android: Droperator rejected request of viewer - URL: http://apidev.dropininc.com/gigs/claim/" + gigsId;
            }
            logModel.data = "gigsId: " + gigsId + " - action: " + action;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Operator
    private void processResponseAcceptViewer(ClaimModel claimModel, String action, String errorCode) {
        this.claimModel = claimModel;
        try {
            LogModel logModel = new LogModel();
            if (action.equalsIgnoreCase("accept")) {
                logModel.message = "Android: processResponseAcceptViewer - URL: http://apidev.dropininc.com/gigs/claim/[gigsId]";
            } else {
                logModel.message = "Android: Droperator rejected request of viewer - URL: http://apidev.dropininc.com/gigs/claim/[gigsId]";
            }
            logModel.data = "Data response: " + claimModel.toString() + " - action: " + action + " - errorCode: " + errorCode;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer: " + claimModel.toJSON());
        if (claimModel == null) {
            dismissDialog();
            return;
        }
        if (action.equalsIgnoreCase("accept")) {
            if (TextUtils.isEmpty(errorCode)) {
                if (claimModel.gig != null) {
                    // add by Van for change dialog messages
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.setMessage(R.string.waiting_response_from_viewer);
                    }
                    // end
                    setViewerLocation(claimModel.gig.latitude, claimModel.gig.longitude);
                    if (mLocationOperator == null) {
                        mLocationOperator = new LocationModel();
                    }
                    if (mLastLocation != null) {
                        mLocationOperator.latitude = mLastLocation.getLatitude();
                        mLocationOperator.longitude = mLastLocation.getLongitude();
                    }
                } else {
                    Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer: gig id = null");
                    removeTimeOut();
                    dismissDialog();
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setCancelable(false);
                    mDialog.setTitleDialog("");
                    mDialog.setMessageDialog(R.string.error_try_again);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v -> {
                        mDialog.dismiss();
                        ((MainActivity) mActivity).setProcessingRequest(false);
                        ((MainActivity) mActivity).setAccountListener("");
                    });
                    mDialog.show();

                }
            } else {
                Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer error code: " + errorCode.toString());
                removeTimeOut();
                dismissDialog();
                final AlertDialog mDialog = new AlertDialog(mActivity);
                mDialog.setCancelable(false);
                mDialog.setTitleDialog("");
                mDialog.setMessageDialog(R.string.message_job_is_no_longer_active);
                mDialog.setTitleButton(R.string.ok_cap);
                mDialog.setButtonClick(v -> {
                    mDialog.dismiss();
                    ((MainActivity) mActivity).setProcessingRequest(false);
                    ((MainActivity) mActivity).setAccountListener("");
                });
                mDialog.show();

            }
        } else if (action.equalsIgnoreCase("reject")) {
            Logs.log("REQUEST_OPERATOR", "processResponseAcceptViewer: reject");
            dismissDialog();
            ((MainActivity) mActivity).setProcessingRequest(false);
            ((MainActivity) mActivity).setAccountListener("");
            showAlertDialog("", getString(R.string.message_cancel_incoming_job),
                    getString(R.string.ok_cap));
        }
    }

    private float distanceBetweenTwoLocation(Location fromLocation, Location toLocation) {
        return fromLocation.distanceTo(toLocation);
    }

    public void drawMapEnRoute(LocationModel fromLocation, LocationModel toLocation) {
        networkManager.getDirections(fromLocation.latitude, fromLocation.longitude, toLocation.latitude,
                toLocation.longitude)
                .subscribe(mapDirectionModel -> {
                    mMapManager.drawDirections(mGoogleMap, mapDirectionModel);
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getDirections", networkManager.parseError(throwable));
                    Logs.log(TAG, "error: " + networkManager.parseError(throwable).message);
                });
    }

    public void setUserOnlineOffline() {
        Logs.log(TAG, "setUserOnlineOffline");
        LocationRequest locationRequest = new LocationRequest();

        if (mUserType == UserType.VIEWER) {
            locationRequest.lat = "1.0";
            locationRequest.lng = "1.0";
            locationRequest.opr = false;
        } else {
            locationRequest.lat = "0.0";
            locationRequest.lng = "0.0";
            locationRequest.opr = true;
        }
        locationRequest.bty = ((MainActivity) mActivity).getBatteryLevel() + "";
//        locationRequest.sig = String.valueOf(mSignalStrength);
        locationRequest.net = getNetWorkType() + "";
        locationRequest.rad = mSettingRadius + "";
        locationRequest.gigId = mGigsId;

        networkManager.location(locationRequest).subscribe(ignore -> {
        }, throwable -> {
//            AppApplication.getInstance().logErrorServer("location", networkManager.parseError(throwable));
        });
    }

    //TODO Mark
    private void showDialogRating(final int requestCode, Intent intent) {
        if (intent == null || getActivity() == null) return;
        if (mDialogRating != null && mDialogRating.isShowing()) return;

        String timeStr = intent.getStringExtra("durationStreaming");
        mDialogRating = new RatingDialog(mActivity);

        try {
            int time = Integer.valueOf(timeStr);
            long minute = TimeUnit.SECONDS.toMinutes(time);
            long second = TimeUnit.SECONDS.toSeconds(time - minute * 60);
            mDialogRating.setTimeDuration((int) minute, (int) second);
        } catch (NumberFormatException e) {
            e.printStackTrace();

            mDialogRating.setTimeDuration(timeStr);
        }

        String price = intent.getStringExtra("price");
        String ratingId = intent.getStringExtra("ratingId");
        String gigsId = intent.getStringExtra("gigsId");
        boolean bypassRating = intent.getBooleanExtra("bypassRating", false);

        String videoUrl = "";
        if (intent.hasExtra("videoUrl")) {
            videoUrl = intent.getExtras().getString("videoUrl");
            Log.d("TAG", "MAP: VIDEO URL: " + videoUrl);
        } else {
            Log.d("TAG", "MAP: VIDEO URL NULL ");
        }

        mDialogRating.setUserType(mUserType);
        mDialogRating.setTotalCost(price);
        mDialogRating.setRatingId(ratingId, gigsId);
        mDialogRating.setBypassRating(bypassRating);
        if (!TextUtils.isEmpty(videoUrl)) {
            mDialogRating.setVideoUrl(videoUrl);
        }
        mDialogRating.setCancelable(false);
        mDialogRating.setCallback(new RatingDialog.Callback() {
            @Override
            public void onFinish() {
                mDialogRating.dismiss();
                Log.i("RequestCode", "" + requestCode);
            }

            @Override
            public void onRatingComplete() {
                Logs.log("RATING", "onRatingComplete");
                resetUserState(AppApplication.getInstance().getUserType()
                        == UserType.VIEWER ? RequestCode.VIEWER_STREAM : RequestCode.OPERATOR_STREAM);
            }
        });
        mDialogRating.show();
    }

    /**
     * Clean Map, Clean Enroute
     */
    public void resetUserState(int requestCode) {
        removeRoutePath();
        removeMarkerLocationViewer();
        mButtonDestination.setVisibility(View.GONE);
        mButtonListDirection.setVisibility(View.GONE);
        hideEnrouteInfoView();

        endStream(requestCode);
    }

    private void endStream(int requestCode) {
        try {
            ((MainActivity) mActivity).resetParams();
            setEnRouteMode(false);
            switch (requestCode) {
                case RequestCode.VIEWER_STREAM:
                    mGigsId = "";
                    mButtonFind.setText(R.string.request_stream);
                    mEnableGetListOperator = true;
                    mTimeGetListOperator = 0;
                    getListOperatorBackground();
                    break;
                case RequestCode.OPERATOR_STREAM:
                    mGigsId = "";
                    mLayRadius.setVisibility(View.VISIBLE);
                    updateLocationToServer();
                    startLocationForDroperator();
                    break;
            }
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            Logs.log("endStream", "endStream to " + latitude + "," + longitude);
        } catch (Exception e) {
            Logs.log(e);
        }
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

    private void setLastLocation() {
        LocationModel location = null;
        if (latitude != 0.0 && longitude != 0.0) {
            location = new LocationModel();
            location.latitude = latitude;
            location.longitude = longitude;
        }
        ((MainActivity) mActivity).setLastViewerLocation(location);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void confirmRequest(String gigsId) {
        Logs.log(TAG, "confirmRequest " + gigsId);
        this.mGigsId = gigsId;
        mEnableGetListOperator = false;
        mShowDialogOperatorNotResponse = false;

        networkManager.confirmRequest(gigsId)
                .subscribe(this::processResponseConfirmRequest,
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("confirmRequest", networkManager.parseError(throwable));
                            processResponseConfirmRequest(null);
                        });
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droper requets confirm Request - URL: /gigs/confirm/" + gigsId;
            logModel.data = "gigsId: " + gigsId;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processResponseConfirmRequest(ConfirmModel confirmModel) {
        this.confirmModel = confirmModel;
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: process Response Confirm Request";
            if (confirmModel == null) {
                logModel.data = "Confirm request fails: Data = null ";
            } else {
                logModel.data = "DATA response: " + confirmModel.toString();
            }
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dismissDialog();
        if (confirmModel == null) {
            Logs.log(TAG, "processResponseConfirmRequest: Data = null");
            resetParamsViewerMode();
            return;
        }

        if (confirmModel.operator != null && confirmModel.operator.location != null) {
            Logs.log(TAG, "processResponseConfirmRequest: OK");
            Logs.log(TAG, confirmModel.toJSON());

            // TODO : chatChannel
            Logs.log(TAG, "chatChannel: " + confirmModel.chatChannel);
            String chatName = confirmModel.gig.operator.firstName + " " + confirmModel.gig.operator.lastName.substring(0, 1);
            String chatAvatar = "";
            if (confirmModel.gig.operator.profileImage != null && !TextUtils.isEmpty(confirmModel.gig.operator.profileImage.location)) {
                chatAvatar = confirmModel.gig.operator.profileImage.location;
            }

            showChatButton(confirmModel.chatChannel, chatName, chatAvatar, confirmModel.gig.id);

            // Connecting to the GIG channel
            MessageManager.getInstance().subscribeToGigChannel(confirmModel.gig.id);

            filterOperatorMarkers(confirmModel.gig.operator.id);
            if (mListMarker.size() == 0) {
                createMainOperatorProcessOnMap(confirmModel.gig.operator.id, confirmModel.gig.operator.firstName, confirmModel.gig.operator.lastName, confirmModel.operator.location.latitude, confirmModel.operator.location.longitude);
            }

            setEnRouteMode(true);
            showEnrouteInfoView(confirmModel.gig.operator.firstName, confirmModel.gig.operator.lastName, confirmModel.operator.rate, confirmModel.gig.operator.profileImage != null ? confirmModel.gig.operator.profileImage.location : "");

            LocationModel fromLocation = new LocationModel();
            fromLocation.latitude = confirmModel.operator.location.latitude;
            fromLocation.longitude = confirmModel.operator.location.longitude;

            LocationModel toLocation = new LocationModel();
            toLocation.latitude = mMarker.getPosition().latitude;
            toLocation.longitude = mMarker.getPosition().longitude;

            drawMapEnRoute(fromLocation, toLocation);
            updateInfoDistanceETA(fromLocation, toLocation);
        } else {
            Log.d("TAG", "processResponseConfirmRequest: Fail: " + confirmModel.toJSON());
            resetParamsViewerMode();
        }
    }

    // Remove all Operator, only keep one Main Operator to connect
    public void filterOperatorMarkers(String operatorId) {
        if (mListMarker == null)
            mListMarker = new HashMap<>();
        Marker item = null;
        String keyAdd = "";
        for (String key : mListMarker.keySet()) {
            Marker marker = mListMarker.get(key);
            if (!key.equals(operatorId)) {
                marker.remove();
                removeChannel(key);
            } else {
                item = marker;
                keyAdd = key;
            }
        }
        mListMarker.clear();
        if (item != null) {
            mListMarker.put(keyAdd, item);
        }
    }

    public void createMainOperatorProcessOnMap(String operatorId, String firstName, String lastName, double latitude, double longitude) {
        Marker marker = addMarkerLocationOperator(firstName, lastName, latitude, longitude);
        mListMarker.put(operatorId, marker);
        addChannel(operatorId);
    }

    public void showEnrouteInfoView(String operatorFirstName, String operatorLastName, double operatorRate, String avatar) {
        lay_enroute.setVisibility(View.VISIBLE);
        CircleImageView img_avatar = (CircleImageView) mRootView.findViewById(R.id.img_avatar);
        TextView tv_droper_name = (TextView) mRootView.findViewById(R.id.tv_droper_name);
        TextView tv_enroute = (TextView) mRootView.findViewById(R.id.tv_enroute);
        ImageView dropStart1 = (ImageView) mRootView.findViewById(R.id.dropStart1);
        ImageView dropStart2 = (ImageView) mRootView.findViewById(R.id.dropStart2);
        ImageView dropStart3 = (ImageView) mRootView.findViewById(R.id.dropStart3);
        ImageView dropStart4 = (ImageView) mRootView.findViewById(R.id.dropStart4);
        ImageView dropStart5 = (ImageView) mRootView.findViewById(R.id.dropStart5);

        ImageLoader mImageLoader = ImageLoader.getInstance();
        DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();

        FontUtils.typefaceTextView(tv_droper_name, FontType.BOLD);
        FontUtils.typefaceTextView(tv_enroute, FontType.BOLD);

        if (!TextUtils.isEmpty(avatar))
            mImageLoader.displayImage(avatar, img_avatar, mOptions);

        tv_droper_name.setText(operatorFirstName + " " + operatorLastName.substring(0, 1));

        if (operatorRate != 0) {
            if (operatorRate > 0 && operatorRate < 1)
                dropStart1.setImageResource(R.drawable.star_half);
            if (operatorRate >= 1)
                dropStart1.setImageResource(R.drawable.star_green);
            if (operatorRate > 1 && operatorRate < 2)
                dropStart2.setImageResource(R.drawable.star_half);
            if (operatorRate >= 2)
                dropStart2.setImageResource(R.drawable.star_green);
            if (operatorRate > 2 && operatorRate < 3)
                dropStart3.setImageResource(R.drawable.star_half);
            if (operatorRate >= 3)
                dropStart3.setImageResource(R.drawable.star_green);
            if (operatorRate > 3 && operatorRate < 4)
                dropStart4.setImageResource(R.drawable.star_half);
            if (operatorRate >= 4)
                dropStart4.setImageResource(R.drawable.star_green);
            if (operatorRate > 4 && operatorRate < 5)
                dropStart5.setImageResource(R.drawable.star_half);
            if (operatorRate >= 5)
                dropStart5.setImageResource(R.drawable.star_green);
        }
        startTimerUpdateETAView();
    }

    public void hideEnrouteInfoView() {
        try {
            lay_enroute.setVisibility(View.GONE);
            stopTimerUpdateEATView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelRequest(String message) {
        dismissDialog();

        mShowDialogOperatorNotResponse = false;
        mEnableGetListOperator = true;
        mButtonFind.setEnabled(true);
        Logs.log("Cancel Request", "Show From Cancel Request");
        ((MainActivity) mActivity).setProcessingRequest(false);
        ((MainActivity) mActivity).setAccountListener("");
        final AlertDialog mDialog = new AlertDialog(getActivity());
        mDialog.setCancelable(false);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(message);
        mDialog.setTitleButton(R.string.ok_cap);
        mDialog.setButtonClick(v -> mDialog.dismiss());
        mDialog.show();
    }

    private void sendLocationMessageToViewer(Location location) {
        if (mMarker != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mMarker.setPosition(latLng);
            if (mGoogleMap != null)
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//            else // TODO
        }
        try {
            setOperatorLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            double distanceDefault = 500 * 0.3048;
            float distanceStartStream = 0;
            if (mButtonDestination.getVisibility() == View.VISIBLE) {
                Location destinationLocation = new Location("");
                destinationLocation.setLatitude(mLocationViewer.latitude);
                destinationLocation.setLongitude(mLocationViewer.longitude);
                distanceStartStream = distanceBetweenTwoLocation(location, destinationLocation);
            }
            if (distanceStartStream != 0 && distanceStartStream <= distanceDefault) {
                goToDestination();
            } else {
                Location fromLocation = new Location("");
                fromLocation.setLatitude(mLocationMainOperator.latitude);
                fromLocation.setLongitude(mLocationMainOperator.longitude);
                float distance = distanceBetweenTwoLocation(fromLocation, location);
                if (distance >= 10 || !isLastLocationSent) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    int code = NotificationCode.NEW_LOCATION;
                    String operatorId = AppApplication.getInstance().getAccountId();
                    int settingRadius = ((MainActivity) mActivity).getSettingRadius();
                    String messageId = UUID.randomUUID().toString();
                    JSONObject locationObject = new JSONObject();
                    locationObject.put("latitude", lat + "");
                    locationObject.put("longitude", lng + "");

                    JSONObject message = new JSONObject();
                    message.put("code", code);
                    message.put("operatorId", operatorId);
                    message.put("settingRadius", settingRadius + "");
                    message.put("messageId", messageId);
                    message.put("location", locationObject);

                    Logs.log(TAG, "publishMessage AccountId: " + operatorId + " message:" + message);
                    ((MainActivity) mActivity).publishMessage(operatorId, message);

                    updateLocationToServer();
                }
                setOperatorLocation(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveLocationMessageFromOperator(LocationModel location, String operatorId, int radius) {
        if (!mEnRouteMode) {
            for (String key : mListMarker.keySet()) {
                Marker marker = mListMarker.get(key);
                if (key.equals(operatorId)) {
                    Location fromLocation = new Location("");
                    fromLocation.setLatitude(marker.getPosition().latitude);
                    fromLocation.setLongitude(marker.getPosition().longitude);

                    Location toLocation = new Location("");
                    toLocation.setLatitude(location.latitude);
                    toLocation.setLongitude(location.longitude);
                    float distance = distanceBetweenTwoLocation(fromLocation, toLocation);

                    Location myLocation = new Location("");
                    myLocation.setLatitude(latitude);
                    myLocation.setLongitude(longitude);
                    float distanceMy = distanceBetweenTwoLocation(myLocation, toLocation);

                    float radiusCalculate = (float) (radius * 1609.34);
                    Logs.log("RadiusOperator", "" + radius);
                    Logs.log("RadiusCalculate", "" + radiusCalculate);
                    Logs.log("Distance Two Location", "" + distance);
                    Logs.log("Distance From my Location", "" + distanceMy);
                    if (distanceMy > radiusCalculate) {
                        marker.remove();
                        mListMarker.remove(key);
                        return;
                    }
                    if (distance > radiusCalculate) {
                        marker.remove();
                        mListMarker.remove(key);
                    } else {
                        marker.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                    break;
                }
            }
        } else {
            if (mListMarker.keySet().size() > 0) {
                String keyCheck = "";
                for (String key : mListMarker.keySet()) {
                    keyCheck = key;
                }
                Marker item = mListMarker.get(keyCheck);
                if (mLocationOperator == null) {
                    mLocationOperator = new LocationModel();
                    mLocationOperator.latitude = item.getPosition().latitude;
                    mLocationOperator.longitude = item.getPosition().longitude;
                }
                item.setPosition(new LatLng(location.latitude, location.longitude));
                setOperatorLocation(latitude, longitude);
            }
        }
    }

    public void setEnRouteMode(boolean mEnRouteMode) {
        this.mEnRouteMode = mEnRouteMode;
        ((MainActivity) mActivity).setEnroute(this.mEnRouteMode);
        if (!mEnRouteMode) {
            if (mUserType == UserType.VIEWER) {
                mMarker.setDraggable(true);
                mLaySearch.setVisibility(View.VISIBLE);
                mButtonFind.setVisibility(View.VISIBLE);
                mButtonFind.setEnabled(true);
                ((MainActivity) mActivity).setEnableStartStream(false);
            }
        } else {
            if (mUserType == UserType.VIEWER) {
                mMarker.setDraggable(false);
                mLaySearch.setVisibility(View.GONE);
                mButtonFind.setVisibility(View.GONE);
                ((MainActivity) mActivity).setEnableStartStream(true);
            }
        }
//        ((MainActivity) mActivity).setNavigationEnable(!mEnRouteMode);
        ((MainActivity) mActivity).setBackPressedEnable(!mEnRouteMode);
        ((MainActivity) mActivity).setEnableSwitchUserType(!mEnRouteMode);
        ((MainActivity) mActivity).setEnableHelp(!mEnRouteMode);

        // hide chat icon
        if (!mEnRouteMode) {
            removeChat();
        }
    }

    public boolean getEnRouteMode() {
        return mEnRouteMode;
    }

    private void goToDestination() {
        Logs.log(TAG, "goToDestination");
        stopGettingLocations();

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(R.string.launching_stream);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        networkManager.location(new LocationRequest(mLocationViewer.latitude + "",
                mLocationViewer.longitude + "", ((MainActivity) mActivity).getBatteryLevel() + "",
                mSignalStrength, mUserType == UserType.OPERATOR, getNetWorkType() + "",
                mSettingRadius + "", mGigsId))
                .doAfterTerminate(this::processResponseGoToDestination)
                .subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("location", networkManager.parseError(throwable));
                });
    }

    private void processResponseGoToDestination() {
        operatorStartGigs(mGigsId);
    }

    private void operatorStartGigs(String gigsId) {
        networkManager.handshakeStream(gigsId)
                .subscribe(this::processResponseStartGigs, throwable -> {
                    AppApplication.getInstance().logErrorServer("handshakeStream", networkManager.parseError(throwable));
                    dismissDialog();
                });
    }

    private void processResponseStartGigs(StartStreamModel model) {
        dismissDialog();
        if (model == null)
            return;
        removeRoutePath();
        removeMarkerLocationViewer();
        mButtonDestination.setVisibility(View.GONE);
        mButtonListDirection.setVisibility(View.GONE);
        ((MainActivity) mActivity).setMainActivityActive(false);

        DataInputStreamModel dataInputStreamModel = new DataInputStreamModel();
        dataInputStreamModel.gigsId = mGigsId;
        dataInputStreamModel.token = model.stream.token;
        dataInputStreamModel.session = model.stream.sessionId;
        dataInputStreamModel.apiKey = model.stream.key;
        dataInputStreamModel.settingRadius = mSettingRadius;

        dataInputStreamModel.chatChannel = chatChannel;
        dataInputStreamModel.chatName = chatName;
        dataInputStreamModel.chatAvatar = chatAvatar;
        dataInputStreamModel.chatData = getChatData();
        ((MainActivity) mActivity).navigateOperatorStreamActivity(dataInputStreamModel);
    }

    public void removeRoutePath() {
        mMapManager.removePolyLines();
    }

    public void setViewerLocation(double latitude, double longitude) {
        if (mLocationViewer == null)
            mLocationViewer = new LocationModel();
        mLocationViewer.latitude = latitude;
        mLocationViewer.longitude = longitude;
    }

    public void setOperatorLocation(double latitude, double longitude) {
        if (mLocationMainOperator == null) {
            mLocationMainOperator = new LocationModel();
        }
        mLocationMainOperator.latitude = latitude;
        mLocationMainOperator.longitude = longitude;
    }

    public void removeMarkerLocationViewer() {
        if (mMarkerViewerPosition == null) return;
        mMarkerViewerPosition.remove();
    }

    public void startEnRouteOperator(String gigsId) {
        removeTimeOut();
        try {
            LogModel logModel = new LogModel();
            logModel.message = "Android: Droperator En Route";
            logModel.data = "Gigs ID: " + gigsId;
            ((MainActivity) mActivity).logToServer(logModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logs.log(TAG, "startEnRouteOperator " + gigsId);
        try {
            JSONObject props = new JSONObject();
            props.put("latitude", getLatitude());
            props.put("longitude", getLongitude());
            mixpanel.track("Stream - Droperator En Route", props);
        } catch (Exception e) {
            Logs.log(e);
        }
        mGigsId = gigsId;
        dismissDialog();
        if ("dev".equals(BuildConfig.FLAVOR) || "qa".equals(BuildConfig.FLAVOR)) { // Only Display in dev or qa flavour of apk
            mButtonDestination.setVisibility(View.VISIBLE);
        }
        mLayRadius.setVisibility(View.GONE);

        addMarkerLocationViewer(mLocationViewer.latitude, mLocationViewer.longitude);
        setEnRouteMode(true);

        LocationModel fromLocation = new LocationModel();
        fromLocation.latitude = mMarker.getPosition().latitude;
        fromLocation.longitude = mMarker.getPosition().longitude;

        LocationModel toLocation = new LocationModel();
        toLocation.latitude = mLocationViewer.latitude;
        toLocation.longitude = mLocationViewer.longitude;

        drawMapEnRoute(fromLocation, toLocation);
        mButtonListDirection.setVisibility(View.VISIBLE);

        isLastLocationSent = false;
    }

    public void removeMarkerOperator(String operatorId) {
        for (String key : mListMarker.keySet()) {
            Marker marker = mListMarker.get(key);
            if (key.equals(operatorId)) {
                marker.remove();
                mListMarker.remove(key);
                break;
            }
        }
    }

    public void setNeedSwitchMode(boolean mNeedSwitchMode) {
        this.mNeedSwitchMode = mNeedSwitchMode;
    }

    private void showListDirection() {
        int distance = mMapManager.getDistance();

        DirectionDialog mDialog = new DirectionDialog(mActivity);
        mDialog.setData(mMapManager.getListDirection());
        mDialog.setDistance(distance);
        mDialog.show();
    }

    public void setIsCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    public void handlerLowBattery(int mBatteryLevel) {
        this.mBatteryLevel = mBatteryLevel;

        Logs.log("mBatteryChangeReceiver", mBatteryLevel + "");
        if (mUserType == UserType.VIEWER) {
            if (mBatteryLevel <= 15 && mBatteryLevel > 10 && mShowDialogBattery
                    && !mMapBattery.containsKey("viewer_15")) {
                mShowDialogBattery = false;
                mMapBattery.put("viewer_15", "viewer_15");
                final AlertDialog mDialog = new AlertDialog(mActivity);
                mDialog.setCancelable(false);
                mDialog.setTitleDialog(R.string.low_battery);
                mDialog.setMessageDialog(R.string.message_low_battery_viewer);
                mDialog.setTitleButton(R.string.ok_cap);
                mDialog.setButtonClick(v -> {
                    mDialog.dismiss();
                    mShowDialogBattery = true;
                });
                mDialog.show();
            } else if (mBatteryLevel <= 10 && mShowDialogBattery && !mMapBattery.containsKey("viewer_10")) {
                if (mButtonFind.getVisibility() == View.GONE) {
                    mShowDialogBattery = false;
                    mMapBattery.put("viewer_10", "viewer_10");
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setCancelable(false);
                    mDialog.setTitleDialog(R.string.disconnected);
                    mDialog.setMessageDialog(R.string.message_low_battery_viewer);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v -> {
                        mDialog.dismiss();
                        // Van added for kickout enroute
                        // added by Van 5/5/16
                        if (mGoogleMap != null) {
                            mGoogleMap.clear();
                        }
                        if (mListMarker != null) {
                            mListMarker.clear();
                        }
                        // end add
                        ((MainActivity) mActivity).setUserType(UserType.VIEWER);
                        setUserType(UserType.VIEWER);
                        setRadiusVisibility();
                        setButtonFindVisibility();
                    });
                    mDialog.show();
                }
            }
        } else {
            if (mBatteryLevel <= 15 && mBatteryLevel > 10 && mShowDialogBattery
                    && !mMapBattery.containsKey("operator_15")) {
                if (mButtonListDirection.getVisibility() == View.VISIBLE) {
                    mShowDialogBattery = false;
                    mMapBattery.put("operator_15", "operator_15");
                    final AlertDialog mDialog = new AlertDialog(mActivity);
                    mDialog.setCancelable(false);
                    mDialog.setTitleDialog(R.string.title_low_battery_droperator_15);
                    mDialog.setMessageDialog(R.string.message_low_battery_droperator_15);
                    mDialog.setTitleButton(R.string.ok_cap);
                    mDialog.setButtonClick(v -> {
                        mDialog.dismiss();
                        mShowDialogBattery = true;
                    });
                    mDialog.show();
                }
            } else if (mBatteryLevel <= 10 && mShowDialogBattery && !mMapBattery.containsKey("operator_10")) {
                mShowDialogBattery = false;
                mMapBattery.put("operator_10", "operator_10");
                final AlertDialog mDialog = new AlertDialog(mActivity);
                mDialog.setCancelable(false);
                mDialog.setTitleDialog(R.string.disconnected);
                mDialog.setMessageDialog(R.string.message_low_battery_droperator_10);
                mDialog.setTitleButton(R.string.ok_cap);
                mDialog.setButtonClick(v -> {
                    mDialog.dismiss();
                    // added by Van 5/5/16
                    if (mGoogleMap != null) {
                        mGoogleMap.clear();
                    }
                    if (mListMarker != null) {
                        mListMarker.clear();
                    }
                    // end add
                    ((MainActivity) mActivity).setUserType(UserType.VIEWER);
                    setUserType(UserType.VIEWER);
                    setRadiusVisibility();
                    setButtonFindVisibility();
                });
                mDialog.show();
            }
        }
    }

    public void resetParamsBattery() {
        if (!mShowDialogBattery) {
            mShowDialogBattery = true;
        }
        if (mMapBattery.size() > 0) {
            mMapBattery.clear();
        }
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private int increment = 10;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Logs.log("onProgressChanged", "" + seekBar.getProgress());
            int currentProgress = ((int) Math.round(progress / increment)) * increment;
            Logs.log("Current Progress", "" + currentProgress);
            if (currentProgress <= 0) {
                currentProgress = 1;
            } else if (currentProgress > 50) {
                currentProgress = 50;
            }
            String radius = "%smi";
            String text = String.format(radius, currentProgress);
            mTextRadius.setText(text);
            mSeekBar.setProgress(currentProgress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Logs.log("onStartTrackingTouch", "" + seekBar.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Logs.log("onStopTrackingTouch", "" + seekBar.getProgress());
            int mile = seekBar.getProgress();
            if (mile == 1) {
                mSettingRadius = mile;
                updateRadiusService(mSettingRadius);
            } else if (mile > 0 && mile != mSettingRadius && mile % increment == 0) {
                mSettingRadius = mile;
                updateRadiusService(mSettingRadius);
            }
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            hideSoftKeyboard();
            Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            LatLng latLng = place.getLatLng();
            if (mMarker != null) {
                mMarker.setPosition(latLng);
            }
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
                    longitude), mZoomViewer));
            getListOperator(false);

            places.release();
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logs.log(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(LocationService.GET_LOCATION_EXTRA)) {
                Logs.log(TAG, "onLocationChanged: user type - " + mUserType);

                mLastLocation = intent.getParcelableExtra(LocationService.GET_LOCATION_EXTRA);

                if (mUserType == UserType.VIEWER) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    setUpMapIfNeeded();

                    if (!mNeedSwitchMode) {
                        dismissDialog();
                        setRadiusVisibility();
                        setButtonFindVisibility();
                        getListOperator(false);
                    } else {
                        new Handler().postDelayed(() -> {
                            dismissDialog();
                            setNeedSwitchMode(false);
                            ((MainActivity) mActivity).setUserType(UserType.OPERATOR);
                            setUserType(UserType.OPERATOR);
                            setRadiusVisibility();
                            setButtonFindVisibility();
                            updateLocationToServer();
                        }, 1000);
                    }
                } else {
                    if (isDismissDialogForEnableLocation) {
                        dismissDialog();
                        isDismissDialogForEnableLocation = false;
                    }
                    Logs.log("LocationChangedForDropertor", "Latitude: " + mLastLocation.getLatitude()
                            + ", Longitude: " + mLastLocation.getLongitude());
                    sendLocationMessageToViewer(mLastLocation);

                    LocationModel location = new LocationModel();
                    location.latitude = mLastLocation.getLatitude();
                    location.longitude = mLastLocation.getLongitude();
                    ((MainActivity) mActivity).setLastDroperatorLocation(location);
                }
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (signalStrength.getGsmSignalStrength() > 0)
                mSignalStrength = signalStrength.getGsmSignalStrength();
        }
    };

    private Runnable mRunnableOnlineOffline = new Runnable() {
        @Override
        public void run() {
            setUserOnlineOffline();
            mHandler.postDelayed(mRunnableOnlineOffline, 45 * 1000);
        }
    };

    private Runnable mRunnableGetListOperatorBackground = new Runnable() {
        @Override
        public void run() {
            if (mTimeGetListOperator == 0) {
                mTimeGetListOperator = 60 * 1000;
            } else {
                getListOperatorBackground();
            }
            mHandler.postDelayed(mRunnableGetListOperatorBackground, mTimeGetListOperator);
        }
    };

    /**
     * RESUME PROCESS
     */

    private ResumeCheckModel resumeCheckModel;

    public ResumeCheckModel getResumeCheckData() {
        return resumeCheckModel;
    }

    public void checkOldGigStatus() {
        Logs.log(TAG, "checkOldGigStatus");
        networkManager.getAllGigs(AppApplication.getInstance().getAccountId(), "purchased,operatorEnroute,streaming").subscribe(gisModels -> {
            if (gisModels != null && gisModels.size() > 0) {
                boolean hasNotRate = false;
                for (ResumeCheckModel gisModel : gisModels) {
                    Logs.log(TAG, "checkOldGigStatus " + gisModel.toJSON());
                    if (gisModel.status.equalsIgnoreCase("purchased")) {
                        if (!(gisModel.customerRating != null && gisModel.operatorRating != null)) {
                            Logs.log(TAG, "checkOldGigStatus need to check purchased");
                            if ((gisModel.customerRating != null && !AppApplication.getInstance().getAccountId().equalsIgnoreCase(gisModel.customerRating.id))) {
                                hasNotRate = true;
                                Logs.log(TAG, "checkOldGigStatus Operator not purchased");
                                Intent i = new Intent();
                                i.putExtra("durationStreaming", String.valueOf(gisModel.duration));
                                i.putExtra("price", gisModel.operatorPrice);
                                i.putExtra("ratingId", "");
                                i.putExtra("gigsId", gisModel.id);
                                showDialogRating(RequestCode.VIEWER_STREAM, i);
                            } else if ((gisModel.operatorRating != null && !AppApplication.getInstance().getAccountId().equalsIgnoreCase(gisModel.operatorRating.id))) {
                                hasNotRate = true;
                                Logs.log(TAG, "checkOldGigStatus Viewer not purchased");
                                Intent i = new Intent();
                                i.putExtra("durationStreaming", String.valueOf(gisModel.duration));
                                i.putExtra("price", gisModel.customerPrice);
                                i.putExtra("ratingId", "");
                                i.putExtra("gigsId", gisModel.id);
                                showDialogRating(RequestCode.VIEWER_STREAM, i);
                            } else {
                                Logs.log(TAG, "user not is customerRating or operatorRating");
                            }
                        } else {
                            Logs.log(TAG, "checkOldGigStatus customerRating and operatorRating has data");
                        }
                    }
                }

                if (!hasNotRate) {
                    resumeCheckModel = gisModels.get(0);
                    Logs.log(TAG, "checkOldGigStatus Status: " + resumeCheckModel.status);
                    if (resumeCheckModel.status.equalsIgnoreCase("operatorEnroute")) {
                        ((MainActivity) mActivity).showDialogResumeEnroute();
                    } else if (resumeCheckModel.status.equalsIgnoreCase("streaming")) {
                        DataInputStreamModel inputStream = new DataInputStreamModel();
                        inputStream.gigsId = resumeCheckModel.id;
                        inputStream.token = resumeCheckModel.metaData.stream.customerStreamToken;
                        inputStream.session = resumeCheckModel.metaData.stream.sessionId;
                        inputStream.apiKey = resumeCheckModel.metaData.stream.key;

                        inputStream.chatChannel = resumeCheckModel.chatChannel;
                        inputStream.chatName = resumeCheckModel.operator.firstName + " " + resumeCheckModel.operator.lastName;
                        inputStream.chatAvatar = resumeCheckModel.operator.profileImage != null ? resumeCheckModel.operator.profileImage.location : "";
                        inputStream.chatData = "";
                        if ((resumeCheckModel.customer != null && !AppApplication.getInstance().getAccountId().equalsIgnoreCase(resumeCheckModel.customer.id))) {
                            ((MainActivity) mActivity).navigateViewerStreamActivity(inputStream);
                        } else {
                            ((MainActivity) mActivity).navigateOperatorStreamActivity(inputStream);
                        }
                    }
                }
            } else {
                Logs.log(TAG, "checkOldGigStatus array null");
            }
        }, throwable -> {
            AppApplication.getInstance().logErrorServer("getAllGigs", networkManager.parseError(throwable));
            Log.d(TAG, "checkOldGigStatus: ERROR: " + networkManager.parseError(throwable).message);
        });
    }

    public void checkGisStatus() {
        Logs.log(TAG, "checkGisStatus");
        networkManager.checkGisStatus()
                .subscribe(accountModel -> {
                    if (accountModel != null) {
                        if (!TextUtils.isEmpty(accountModel.type)) {
                            setUpMapIfNeeded();
                            setRadiusVisibility();
                            setButtonFindVisibility();
                        }
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("checkGisStatus", networkManager.parseError(throwable));
                    Log.d(TAG, "checkGisStatus: ERROR: " + networkManager.parseError(throwable).message);
                });
    }

    // TODO Chat feature
    public String getChatChannel() {
        return chatChannel;
    }

    public String getChatName() {
        return chatName;
    }

    public String getChatAvatar() {
        return chatAvatar;
    }

    public void showChatButton(String chatChanel, String chatName, String chatAvatar, String gigID) {
        this.chatChannel = chatChanel;
        this.chatName = chatName;
        this.chatAvatar = chatAvatar;

        if (!TextUtils.isEmpty(this.chatChannel)) {
            bt_Chat.setVisibility(View.VISIBLE);
            chatDialog = new ChatDialog(mActivity, android.R.style.Theme_Translucent);
            chatDialog.setChatChanel(chatChanel, chatName, chatAvatar, gigID);
            chatDialog.setIsFromEnRoute(true);
            chatDialog.setBackgroundTransparent(false);

            chatDialog.setTakePhotoCalBack(new ChatDialog.TakePhotoCallback() {
                @Override
                public void onTakePhoto() {

                }

                @Override
                public void onHadNewMessages(boolean isHadNew) {
                    Logs.log(TAG, "onHadNewMessages : " + isHadNew);
                    if (isHadNew) {
                        bt_Chat.setBackgroundResource(R.drawable.img_chat_blue_new);
                    } else {
                        bt_Chat.setBackgroundResource(R.drawable.img_chat_blue);
                    }
                }
            });

        } else {
            bt_Chat.setVisibility(View.GONE);
            if (chatDialog != null && chatDialog.isShowing()) {
                chatDialog.dismiss();
            }
            chatDialog = null;
        }
    }

    private void showChatDialog() {
        if (chatDialog.isShowing()) {
            chatDialog.dismiss();
        } else {
            bt_Chat.setBackgroundResource(R.drawable.img_chat_blue);
            chatDialog.show();
        }

    }

    public void removeChat() {
        chatChannel = "";
        chatName = "";
        chatAvatar = "";
        showChatButton(chatChannel, chatName, chatAvatar, "");
    }

    public String getChatData() {
        if (chatDialog != null)
            return chatDialog.getDatas();
        return ChatModel.toJSONArray(new ArrayList<ChatModel>());
    }

    private void showPolicy() {
        Intent i = new Intent(mActivity, PrivacyPolicyActivity.class);
        mActivity.startActivityForResult(i, RequestCode.POLICY_PRIVACY);
    }

    private void setParamforSwitchViwer() {
        ((MainActivity) mActivity).setShowHelpIcon(true);
        ((MainActivity) mActivity).setUserType(UserType.VIEWER);
        setUserType(UserType.VIEWER);
    }

    public void startTimerUpdateETAView() {
        timerUpdateETA = new Timer();
        timerUpdateETA.schedule(new MyTimerTask(), 0, 600000);
    }

    class MyTimerTask extends TimerTask {
        public void run() {
            LocationModel loViewer = new LocationModel();
            loViewer.latitude = latitude;
            loViewer.longitude = longitude;
            updateInfoDistanceETA(loViewer, mLocationMainOperator);
        }
    }

    public void stopTimerUpdateEATView() {
        if (timerUpdateETA != null) {
            timerUpdateETA.cancel();
            timerUpdateETA = null;
        }
    }

    public void updateInfoDistanceETA(LocationModel from, LocationModel to) {
        try {

            if (from == null) {
                Logs.log("TAG", "ETA Location from null");
            }
            if (to == null) {
                Logs.log("TAG", "ETA Location to null");
            }
            TextView tv_title_1 = (TextView) mRootView.findViewById(R.id.tv_title_1);
            TextView tv_title_2 = (TextView) mRootView.findViewById(R.id.tv_title_2);
            TextView tv_mile = (TextView) mRootView.findViewById(R.id.tv_mile);
            TextView tv_minute = (TextView) mRootView.findViewById(R.id.tv_minute);
            FontUtils.typefaceTextView(tv_title_1, FontType.LIGHT);
            FontUtils.typefaceTextView(tv_title_2, FontType.LIGHT);
            FontUtils.typefaceTextView(tv_mile, FontType.REGULAR);
            FontUtils.typefaceTextView(tv_minute, FontType.REGULAR);

            networkManager.getDirections(from.latitude, from.longitude, to.latitude,
                    to.longitude)
                    .subscribe(mapDirectionModel -> {
                        if (mapDirectionModel.routeList.size() > 0) {
                            MapDirectionModel.Route route = mapDirectionModel.routeList.get(0);
                            List<MapDirectionModel.Leg> legs = route.legs;
                            if (legs.size() > 0) {
                                double mile = (legs.get(0).distance.value / 1609.34);
                                String text = "mile";
                                if (mile > 2) {
                                    text = "miles";
                                }
                                String distance = String.format("%.2f " + text, mile);
                                String duration = legs.get(0).steps.get(0).duration.text;
                                Logs.log("TAG", "distance: " + distance + " -  duration: " + duration);
                                tv_mile.setText(distance);
                                tv_minute.setText(duration);
                            }
                        }

                    }, throwable -> {
                        AppApplication.getInstance().logErrorServer("getDirections", networkManager.parseError(throwable));
                        Logs.log("TAG", "GET Location error");
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ConfirmModel getConfirmModel() {
        return confirmModel;
    }

    public ClaimModel getClaimModel() {
        return claimModel;
    }
}

package com.dropininc.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.asynctask.UploadImageManager;
import com.dropininc.customview.CircleImageView;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.dialog.SelectImageDialog;
import com.dropininc.image.CropImage;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.OperatorStatus;
import com.dropininc.interfaces.PictureCallback;
import com.dropininc.interfaces.RequestCode;
import com.dropininc.interfaces.UserType;
import com.dropininc.model.AccountModel;
import com.dropininc.model.AvatarModel;
import com.dropininc.model.IdentitiesModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.network.request.AccountRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ProfileFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;
    private Button mButtonCancel, mButtonSave, mButtonSubmit, mButtonDelete;
    private EditText mEditFirstName, mEditLastName, mEditCity, txtBankAccount, txtCheckingRouting;
    private TextView mTextEmail, mTextPhone, tv_add_profile;
    private RelativeLayout mLayFirstName, mLayLastName, mLayCity;
    private CircleImageView mImageAvatar;
    private ImageView imgHelp;
    @Bind(R.id.layoutDroperator)
    protected LinearLayout layoutDroperator;

    private ProgressDialog mProgressDialog;
    private int mUserType = UserType.VIEWER;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;

    private Uri mFileUri;
    private String mDocusignStatus;
    private String mPathImage = "";

    View viewEmail;
    View viewPhone;

    LinearLayout lay_rating;
    ImageView dropStart1, dropStart2, dropStart3, dropStart4, dropStart5;
    ImageView viewStart1, viewStart2, viewStart3, viewStart4, viewStart5;

    AccountModel model = new AccountModel();

    boolean isResumeFromState = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_avatar)
                .showImageOnFail(R.drawable.ic_avatar)
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();
        if (savedInstanceState != null) {
            isResumeFromState = true;
            Bundle savedState = savedInstanceState.getBundle("SAVE_STATE");
            mUserType = savedState.getInt("mUserType");
            mDocusignStatus = savedState.getString("mDocusignStatus");

            model = AccountModel.fromJSON(savedState.getString("model"));

        } else {
            isResumeFromState = false;
            if (getArguments() != null) {
                mUserType = getArguments().getInt("UserType", UserType.VIEWER);
                mDocusignStatus = getArguments().getString("DocusignStatus");
            }
        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Logs.log(TAG, "onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("TAG", "Profile Frag: onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Bundle state = new Bundle();
        state.putString("mDocusignStatus", mDocusignStatus);
        state.putInt("mUserType", mUserType);
        state.putString("model", model.toJSON());

        outState.putBundle("SAVE_STATE", state);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

            initView();
            checkingStatus();
            if (!isResumeFromState) {
                getDataFromServer();
            } else {
                processResponseProfile();
            }
        }
        Logs.log(TAG, "onCreateView");
        ButterKnife.bind(this, mRootView);

        // show account id
        TextView tvID = (TextView) mRootView.findViewById(R.id.tvID);
        tvID.setText("Accout ID: " + AppApplication.getInstance().getAccountId());
        return mRootView;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Logs.log(TAG, "onViewStateRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logs.log(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.log(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Logs.log(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logs.log(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
        Logs.log(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.log(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancel:
                hideKeyboard();
                onBackPressed();
                break;
            case R.id.bt_save:
                saveProfile();
                break;
            case R.id.bt_submit:
                submitApplication();
                break;
            case R.id.bt_delete:
//                deleteAccount();
                break;
            case R.id.img_avatar:
            case R.id.tv_add_profile:
                changeAvatar();
                break;
            case R.id.imgHelp:
                openHelp();
                break;

        }
    }

    private void processResponseCheckDocumentSign(OperatorModel model) {
        dismissDialog();
        if (model == null) return;
        if (model.status.equalsIgnoreCase(OperatorStatus.UNAPPROVED)
                || model.status.equalsIgnoreCase(OperatorStatus.CANCELED)) {
            // TODO skip show signup doperator
//            ((MainActivity) mActivity).openDroperatorFlow();
            // show faild: remove in complete
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
            mDialog.show();
            try {
                JSONObject props = new JSONObject();
                props.put("Droperator Status", "Pending Approval");
                mixpanel.track("Droperator - Toggled", props);
            } catch (Exception e) {
                Logs.log(e);
            }
        }
    }

    private void openHelp() {
        showAlertDialog("", getResources().getString(R.string.we_need_your_bank));

    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG", "Profile Frag: onActivityResult");
        if (resultCode == FragmentActivity.RESULT_OK) {
            switch (requestCode) {
                case RequestCode.PICK_IMAGE:
                    mPathImage = Utils.getImagePath(mActivity, data);
                    DSharePreference.setTMPAvatarUrl(mContext, mPathImage);
                    cropImage();
                    break;
                case RequestCode.TAKE_IMAGE:
                    mPathImage = DSharePreference.getTMPAvatarUrl(mContext);
                    DSharePreference.setTMPAvatarUrl(mContext, mPathImage);
                    cropImage();
                    break;
                case RequestCode.CROP_IMAGE:
                    DSharePreference.setTMPAvatarUrl(mContext, "");
                    mPathImage = data.getStringExtra(CropImage.IMAGE_PATH);
                    getLinkUploadAvatar();
                    break;
            }
        }
    }

    private void initView() {
        mButtonCancel = (Button) mRootView.findViewById(R.id.bt_cancel);
        mButtonSave = (Button) mRootView.findViewById(R.id.bt_save);
        mButtonSubmit = (Button) mRootView.findViewById(R.id.bt_submit);
        mButtonDelete = (Button) mRootView.findViewById(R.id.bt_delete);

        mEditFirstName = (EditText) mRootView.findViewById(R.id.ed_first_name);
        mEditLastName = (EditText) mRootView.findViewById(R.id.ed_last_name);
        mEditCity = (EditText) mRootView.findViewById(R.id.ed_city);

        mTextEmail = (TextView) mRootView.findViewById(R.id.ed_email);
        mTextPhone = (TextView) mRootView.findViewById(R.id.ed_phone);

        mLayFirstName = (RelativeLayout) mRootView.findViewById(R.id.lay_first_name);
        mLayLastName = (RelativeLayout) mRootView.findViewById(R.id.lay_last_name);
        mLayCity = (RelativeLayout) mRootView.findViewById(R.id.lay_city);

        mImageAvatar = (CircleImageView) mRootView.findViewById(R.id.img_avatar);

        viewEmail = mRootView.findViewById(R.id.viewEmail);
        viewPhone = mRootView.findViewById(R.id.viewPhone);

        lay_rating = (LinearLayout) mRootView.findViewById(R.id.lay_rating);

        dropStart1 = (ImageView) mRootView.findViewById(R.id.dropStart1);
        dropStart2 = (ImageView) mRootView.findViewById(R.id.dropStart2);
        dropStart3 = (ImageView) mRootView.findViewById(R.id.dropStart3);
        dropStart4 = (ImageView) mRootView.findViewById(R.id.dropStart4);
        dropStart5 = (ImageView) mRootView.findViewById(R.id.dropStart5);

        viewStart1 = (ImageView) mRootView.findViewById(R.id.viewStart1);
        viewStart2 = (ImageView) mRootView.findViewById(R.id.viewStart2);
        viewStart3 = (ImageView) mRootView.findViewById(R.id.viewStart3);
        viewStart4 = (ImageView) mRootView.findViewById(R.id.viewStart4);
        viewStart5 = (ImageView) mRootView.findViewById(R.id.viewStart5);

        tv_add_profile = (TextView) mRootView.findViewById(R.id.tv_add_profile);

        imgHelp = (ImageView) mRootView.findViewById(R.id.imgHelp);
        txtBankAccount = (EditText) mRootView.findViewById(R.id.txtBankAccount);
        txtCheckingRouting = (EditText) mRootView.findViewById(R.id.txtCheckingRouting);

        imgHelp.setOnClickListener(this);

        mButtonCancel.setOnClickListener(this);
        mButtonSave.setOnClickListener(this);
        mButtonSubmit.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);
        tv_add_profile.setOnClickListener(this);

        mEditFirstName.addTextChangedListener(mTextWatcherFirstName);
        mEditLastName.addTextChangedListener(mTextWatcherLastName);

        mButtonSubmit.setVisibility(View.GONE);
        mButtonSave.setVisibility(View.VISIBLE);

        FontUtils.typefaceButton(mButtonCancel, FontType.REGULAR);
        FontUtils.typefaceButton(mButtonSave, FontType.REGULAR);
        FontUtils.typefaceButton(mButtonSubmit, FontType.REGULAR);
        FontUtils.typefaceButton(mButtonDelete, FontType.LIGHT);

        FontUtils.typefaceTextView(tv_add_profile, FontType.BOLD);
        FontUtils.typefaceTextView(mEditFirstName, FontType.LIGHT);
        FontUtils.typefaceTextView(mEditLastName, FontType.LIGHT);
        FontUtils.typefaceTextView(mEditCity, FontType.LIGHT);
        FontUtils.typefaceTextView(mTextEmail, FontType.LIGHT);
        FontUtils.typefaceTextView(mTextPhone, FontType.LIGHT);

        TextView txtTitlePayment = (TextView) mRootView.findViewById(R.id.txtTitlePayment);
        FontUtils.typefaceTextView(txtTitlePayment, FontType.BOLD);

        TextView tv_title = (TextView) mRootView.findViewById(R.id.tv_title);
        FontUtils.typefaceTextView(tv_title, FontType.LIGHT);
        FontUtils.typefaceTextView(txtBankAccount, FontType.LIGHT);
        FontUtils.typefaceTextView(txtCheckingRouting, FontType.LIGHT);

        TextView txtDroperRating = (TextView) mRootView.findViewById(R.id.txtDroperRating);
        FontUtils.typefaceTextView(txtDroperRating, FontType.BOLD);
        TextView txtViewerRating = (TextView) mRootView.findViewById(R.id.txtViewerRating);
        FontUtils.typefaceTextView(txtViewerRating, FontType.BOLD);

        if (mUserType == UserType.OPERATOR) {
            viewEmail.setVisibility(View.GONE);
            viewPhone.setVisibility(View.GONE);
            tv_add_profile.setVisibility(View.GONE);
            lay_rating
                    .setVisibility(View.GONE);
            mEditFirstName.setKeyListener(null);
            mEditLastName.setKeyListener(null);
            mEditCity.setKeyListener(null);
        } else {
            viewEmail.setVisibility(View.VISIBLE);
            viewPhone.setVisibility(View.VISIBLE);
            mEditFirstName.setOnFocusChangeListener(firstNameFocus);
            mEditLastName.setOnFocusChangeListener(lastNameFocus);
            mEditCity.setOnFocusChangeListener(cityFocus);
            tv_add_profile.setVisibility(View.VISIBLE);
            lay_rating.setVisibility(View.VISIBLE);
            mImageAvatar.setOnClickListener(this);
        }
    }

    private void loadStart(double dropStar, double viewerStar) {
        // load droper
        if (dropStar != 0) {
            if (dropStar > 0 && dropStar < 1)
                dropStart1.setImageResource(R.drawable.star_half);
            if (dropStar >= 1)
                dropStart1.setImageResource(R.drawable.star_green);
            if (dropStar > 1 && dropStar < 2)
                dropStart2.setImageResource(R.drawable.star_half);
            if (dropStar >= 2)
                dropStart2.setImageResource(R.drawable.star_green);
            if (dropStar > 2 && dropStar < 3)
                dropStart3.setImageResource(R.drawable.star_half);
            if (dropStar >= 3)
                dropStart3.setImageResource(R.drawable.star_green);
            if (dropStar > 3 && dropStar < 4)
                dropStart4.setImageResource(R.drawable.star_half);
            if (dropStar >= 4)
                dropStart4.setImageResource(R.drawable.star_green);
            if (dropStar > 4 && dropStar < 5)
                dropStart5.setImageResource(R.drawable.star_half);
            if (dropStar >= 5)
                dropStart5.setImageResource(R.drawable.star_green);
        }
        // load viewer
        if (viewerStar != 0) {
            if (viewerStar > 0 && viewerStar < 1)
                viewStart1.setImageResource(R.drawable.star_half);
            if (viewerStar >= 1)
                viewStart1.setImageResource(R.drawable.star_green);
            if (viewerStar > 1 && viewerStar < 2)
                viewStart2.setImageResource(R.drawable.star_half);
            if (viewerStar >= 2)
                viewStart2.setImageResource(R.drawable.star_green);
            if (viewerStar > 2 && viewerStar < 3)
                viewStart3.setImageResource(R.drawable.star_half);
            if (viewerStar >= 3)
                viewStart3.setImageResource(R.drawable.star_green);
            if (viewerStar > 3 && viewerStar < 4)
                viewStart4.setImageResource(R.drawable.star_half);
            if (viewerStar >= 4)
                viewStart4.setImageResource(R.drawable.star_green);
            if (viewerStar > 4 && viewerStar < 5)
                viewStart5.setImageResource(R.drawable.star_half);
            if (viewerStar >= 5)
                viewStart5.setImageResource(R.drawable.star_green);
        }
    }

    private TextWatcher mTextWatcherFirstName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mLayFirstName.setBackgroundResource(R.drawable.ic_bg_red);
                mButtonSave.setEnabled(false);
            } else {
                if (!TextUtils.isEmpty(mEditLastName.getText().toString().trim())) {
                    mButtonSave.setEnabled(true);
                }
            }
        }
    };

    private View.OnFocusChangeListener firstNameFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mLayFirstName.setBackgroundResource(R.drawable.ic_bg_blue);
            } else {
                if (mEditFirstName.length() == 0) {
                    mLayFirstName.setBackgroundResource(R.drawable.ic_bg_red);
                    mButtonSave.setEnabled(false);
                } else {
                    mLayFirstName.setBackgroundResource(R.drawable.ic_bg_white);
                    if (!TextUtils.isEmpty(mEditLastName.getText().toString().trim())) {
                        mButtonSave.setEnabled(true);
                    }
                }

            }
        }
    };

    private TextWatcher mTextWatcherLastName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mLayLastName.setBackgroundResource(R.drawable.ic_bg_red);
                mButtonSave.setEnabled(false);
            } else {
                if (!TextUtils.isEmpty(mEditFirstName.getText().toString().trim())) {
                    mButtonSave.setEnabled(true);
                }
            }
        }
    };

    private View.OnFocusChangeListener lastNameFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mLayLastName.setBackgroundResource(R.drawable.ic_bg_blue);
            } else {
                if (mEditFirstName.length() == 0) {
                    mLayLastName.setBackgroundResource(R.drawable.ic_bg_red);
                    mButtonSave.setEnabled(false);
                } else {
                    mLayLastName.setBackgroundResource(R.drawable.ic_bg_white);
                    if (!TextUtils.isEmpty(mEditFirstName.getText().toString().trim())) {
                        mButtonSave.setEnabled(true);
                    }
                }
            }
        }
    };

    private View.OnFocusChangeListener cityFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mLayCity.setBackgroundResource(R.drawable.ic_bg_blue);
            } else {
                mLayCity.setBackgroundResource(R.drawable.ic_bg_white);
            }
        }
    };

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showErrorDialog(String title, String message) {
        final AlertDialog mDialog = new AlertDialog(mActivity);
        mDialog.setTitleDialog(title);
        mDialog.setMessageDialog(message);
        mDialog.setTitleButton(R.string.ok_cap);
        mDialog.setButtonClick(v -> mDialog.dismiss());
        mDialog.show();
    }

    private void getDataFromServer() {
        showProgressDialog();

        networkManager.getProfile(AppApplication.getInstance().getAccountId())
                .doAfterTerminate(this::dismissDialog)
                .subscribe(accountModel -> {
                    model = accountModel;
                    processResponseProfile();
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getProfile" , networkManager.parseError(throwable));
                    showAlertDialog("", networkManager.parseError(throwable).message, v -> {
                        hideAlertDialog();
                        onBackPressed();
                    });
                });
    }

    private void saveProfile() {
        String firstName = mEditFirstName.getText().toString();
        String lastName = mEditLastName.getText().toString();
        String city = mEditCity.getText().toString();

        showProgressDialog();
        String accountId = AppApplication.getInstance().getAccountId();
        networkManager.updateProfile(accountId,
                new AccountRequest(accountId, firstName, lastName, city))
                .doAfterTerminate(this::dismissDialog)
                .subscribe(ignore -> {
                    processResponseUpdateProfile();
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("updateProfile", networkManager.parseError(throwable));
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    private void deleteAccount() {
        ((MainActivity) mActivity).hideSoftKeyboard();
        DeleteAccountFragment fragment = new DeleteAccountFragment();
        fragment.setTitle(getString(R.string.delete_account));
        ((MainActivity) mActivity).pushFragments(fragment);
    }

    private void submitApplication() {
        showProgressDialog();
        networkManager.checkOperatorProfile(AppApplication.getInstance().getAccountId())
                .subscribe(this::processResponseCheckDocumentSign, throwable -> {
                    AppApplication.getInstance().logErrorServer("checkOperatorProfile" , networkManager.parseError(throwable));
                    dismissDialog();});

        try {
            JSONObject props = new JSONObject();
            mixpanel.track("Droperator - Application Submitted", props);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void changeAvatar() {
        SelectImageDialog mDialog = new SelectImageDialog(mActivity);
        mDialog.setPictureCallback(new PictureCallback() {
            @Override
            public void fromGalley() {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mActivity.startActivityForResult(Intent.createChooser(galleryIntent,
                        getString(R.string.choose_photo)), RequestCode.PICK_IMAGE);
            }

            @Override
            public void fromCamera() {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                mFileUri = Uri.fromFile(Utils.getOutputMediaFile());
//                DSharePreference.setTMPAvatarUrl(mContext, mFileUri.getPath());
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
//                mActivity.startActivityForResult(intent, RequestCode.TAKE_IMAGE);
                openCamera();
            }
        });
        mDialog.show();
    }

    //Add by Thong Nguyen 05/04/2016
    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            requestCameraPermission();
        } else {
            // Camera permissions is already available, show the camera preview.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mFileUri = Uri.fromFile(Utils.getOutputMediaFile());
            DSharePreference.setTMPAvatarUrl(mContext, mFileUri.getPath());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            mActivity.startActivityForResult(intent, RequestCode.TAKE_IMAGE);
        }
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {

            showConfirmDialog(getString(R.string.request_permission), String.format(getString(R.string.camera_request_access_camera), getString(R.string.app_name)), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideConfirmDialog();
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CODE_ASK_PHOTO_PERMISSIONS);
        }
        // END_INCLUDE(camera_permission_request)
    }

    //End Add

    private void processResponseProfile() {
        if (!TextUtils.isEmpty(model.firstName)) {
            mEditFirstName.setText(model.firstName);
        }
        if (!TextUtils.isEmpty(model.lastName)) {
            mEditLastName.setText(model.lastName);
        }
        if (!TextUtils.isEmpty(model.city)) {
            mEditCity.setText(model.city);
        }
        if (model.identities != null && model.identities.size() > 0) {
            int size = model.identities.size();
            for (int i = 0; i < size; i++) {
                IdentitiesModel identities = model.identities.get(i);
                if (identities.type.equalsIgnoreCase("email")) {
                    mTextEmail.setText(identities.value);
                } else if (identities.type.equalsIgnoreCase("phone")) {
                    mTextPhone.setText(identities.value);
                }
            }
        }
        if (model.profileImage != null) {
            mImageLoader.displayImage(model.profileImage.location, mImageAvatar, mOptions);
        }
        checkingStatus();

        if (model.operator != null && model.operator.status.equals("approved")) {
            layoutDroperator.setVisibility(View.VISIBLE);
        }

        loadStart(model.operatorRating, model.customerRating);
    }

    private void checkingStatus() {
        if (!TextUtils.isEmpty(mDocusignStatus)) {

            if (mDocusignStatus.equalsIgnoreCase(OperatorStatus.UNAPPROVED) || mDocusignStatus.equalsIgnoreCase(OperatorStatus.CANCELED)) {
                mButtonSubmit.setText(R.string.submit_application);
                mButtonSubmit.setEnabled(true);
            } else if (mDocusignStatus.equalsIgnoreCase(OperatorStatus.WAITING)) {
                mButtonSubmit.setText(R.string.pending);
                mButtonSubmit.setEnabled(false);
            } else if (mDocusignStatus.equalsIgnoreCase(OperatorStatus.APPROVED)) {
                mButtonSubmit.setText(R.string.pending);
                mButtonSubmit.setEnabled(false);

            }
            mButtonSubmit.setVisibility(View.VISIBLE);
            mButtonSave.setVisibility(View.GONE);
        }
    }

    private void processResponseUpdateProfile() {
        final AutoDialog mDialog = new AutoDialog(mActivity);
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(R.string.save_successfully);
        mDialog.setTimeAutoDismiss(3000);
        mDialog.setDialogDismissCallback(() -> {
            mDialog.dismiss();
            onBackPressed();
        });
        mDialog.show();
    }

    private void processResponseSubmitApplication(OperatorModel model) {
        if (model == null) return;
        if (model.status.equalsIgnoreCase(OperatorStatus.WAITING)) {
            mButtonSubmit.setText(R.string.pending);
            mButtonSubmit.setEnabled(false);
        }
        ((MainActivity) mActivity).setCheckLocationService(false);
        // update model
        String json = DSharePreference.getProfile(mActivity);
        VerifyModel verify = new Gson().fromJson(json, VerifyModel.class);
        verify.account.operator = model;
        DSharePreference.setProfile(mActivity, verify.toJSON());
        AppApplication.getInstance().setOperator(true);

        final AutoDialog mDialog = new AutoDialog(mActivity);
        mDialog.setTitleDialog(R.string.application_submitted);
        mDialog.setMessageDialog(R.string.message_application_submitted);
        mDialog.setTimeAutoDismiss(5000);
        mDialog.setDialogDismissCallback(() -> {
            mDialog.dismiss();
            onBackPressed();
        });
        mDialog.show();
    }

    private void getLinkUploadAvatar() {
        showProgressDialog();

        networkManager.getLinkUploadAvatar()
                .subscribe(this::processResponseGetLinkUploadAvatar,
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("getLinkUploadAvatar" , networkManager.parseError(throwable));
                            processResponseGetLinkUploadAvatar(new AvatarModel());});
    }

    private void processResponseGetLinkUploadAvatar(AvatarModel model) {
        if (model == null) {
            dismissDialog();
            return;
        }
        if (TextUtils.isEmpty(model.location)) {
            showErrorDialog("", getString(R.string.error_try_again));
            return;
        }

        new UploadImageManager(mActivity, mPathImage, model.location, new UploadImageManager.UploadPhotoCallback() {
            @Override
            public void onSuccess() {
                dismissDialog();
                mImageLoader.displayImage("file://" + mPathImage, mImageAvatar, mOptions);
            }

            @Override
            public void onError() {
                dismissDialog();
                showErrorDialog("", getString(R.string.error_try_again));
            }
        });
    }

    private void cropImage() {
        Intent intent = new Intent(mActivity, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mPathImage);
        intent.putExtra(CropImage.SCALE, true);

        intent.putExtra(CropImage.ASPECT_X, 2);
        intent.putExtra(CropImage.ASPECT_Y, 2);

        mActivity.startActivityForResult(intent, RequestCode.CROP_IMAGE);
    }
}

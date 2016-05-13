package com.dropininc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.dialog.AutoIconDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.LocationModel;
import com.dropininc.model.TokenModel;
import com.dropininc.network.request.DeviceTokenRequest;
import com.dropininc.network.request.LoginRequest;
import com.dropininc.network.request.VerifyTokenRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.ViewUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyActivity extends BaseActivity implements View.OnClickListener {

    private Activity mActivity;
    private EditText txtCode;
    private TextView txtError;

    private TokenModel token = null;
    private String phone = "";
    private String email = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = VerifyActivity.this;
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_verify);

        setupView();
        addListener();

        // hard for test
        if (getIntent().getExtras() != null) {
            String tokenString = getIntent().getExtras().getString("DATA");
            token = new Gson().fromJson(tokenString, TokenModel.class);
            phone = getIntent().getExtras().getString("PHONE");
            email = getIntent().getExtras().getString("EMAIL");

            Log.d("TAG", "VERIFY_DATA: " + tokenString);
            // Commend out when build product
            if (!TextUtils.isEmpty(token.code)) {
                showCode(token.code);
            }
        }
    }

    private void showCode(String code) {
        TextView tvCode = (TextView) findViewById(R.id.tvCode);
        tvCode.setVisibility(View.VISIBLE);
        tvCode.setText("Your verify code: " + code);
    }

    @Override
    public void onBackPressed() {
    }

    private void setupView() {
        txtCode = (EditText) findViewById(R.id.txtCode);
        Button btnResend = (Button) findViewById(R.id.btnResend);
        Button btnSend = (Button) findViewById(R.id.btnSend);
        txtError = (TextView) findViewById(R.id.txtError);

        btnSend.setOnClickListener(this);
        btnResend.setOnClickListener(this);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        FontUtils.typefaceTextView(txtTitle, FontType.LIGHT);
        FontUtils.typefaceTextView(txtCode, FontType.LIGHT);
        FontUtils.typefaceTextView(txtError, FontType.BOLD);
        FontUtils.typefaceButton(btnSend, FontType.LIGHT);
        FontUtils.typefaceButton(btnResend, FontType.BOLD);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                if (isValidData()) {
                    if (isOnline()) {
                        try {
                            JSONObject props = new JSONObject();
                            if (!getIntent().hasExtra("LOGIN")) {
                                mixpanel.track("Sign up - Code sent", props);
                            } else {
                                mixpanel.track("Log in - Code Sent", props);
                            }

                        } catch (Exception e) {
                            Logs.log(e);
                        }
                        confirmAction();
                    } else {
                        showAlertDialog(getResources().getString(R.string.network_problem), getResources().getString(R.string.please_connect_to_continue));
                    }
                }

                break;
            case R.id.btnResend:
                try {
                    if (!getIntent().hasExtra("LOGIN")) {
                        JSONObject props = new JSONObject();
                        props.put("Resend Code", "TRUE");
                        mixpanel.track("Sign Up - Resend Code", props);
                    } else {
                        JSONObject props = new JSONObject();
                        mixpanel.track("Log in - Resend Code", props);
                    }
                } catch (Exception e) {
                    Logs.log(e);
                }
                reSendCodeAction();
                break;
        }
    }

    private void addListener() {
        txtCode.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                ViewUtils.setEditTextGreen(getApplicationContext(), txtCode);
            } else {
                String data = txtCode.getText().toString().trim();
                if (TextUtils.isEmpty(data)) {
                    ViewUtils.setEditTextRed(getApplicationContext(), txtCode);
                } else {
                    ViewUtils.setEditTextWhite(getApplicationContext(), txtCode);
                }
            }
        });
        txtCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtError.setTextColor(getResources().getColor(R.color.black));
                txtError.setText(R.string.enter_your_confirmation);
                ViewUtils.setEditTextGreen(getApplicationContext(), txtCode);
                txtError.setTextColor(getResources().getColor(R.color.black));
            }
        });

    }

    private boolean isValidData() {
        String code = txtCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            ViewUtils.setEditTextRed(getApplicationContext(), txtCode);
            txtError.setText(R.string.error_verify);
            txtError.setTextColor(getResources().getColor(R.color.text_red));
            txtCode.requestFocus();
            return false;
        }
        return true;
    }

    private void confirmAction() {
        showProgressDialog(getString(R.string.processing), null);
        networkManager.verifyToken(new VerifyTokenRequest(token.id, txtCode.getText().toString().trim()))
                .subscribe(verifyModel -> {
                    Log.d("TAG", "API: VERIFY_TOKEN");
//                    Logs.log(VerifyActivity.class.getSimpleName(), "Save : publishKey: " + verifyModel.config.pubnub.publishKey + " - subscribeKey: " + verifyModel.config.pubnub.subscribeKey);
//                    DSharePreference.setPubnubPublishKey(VerifyActivity.this, verifyModel.config.pubnub.publishKey);
//                    DSharePreference.setPubnubSubcribeKey(VerifyActivity.this, verifyModel.config.pubnub.subscribeKey);
                    DSharePreference.setProfile(mActivity, verifyModel.toJSON());
                    DSharePreference.setAccessToken(mActivity, verifyModel.token);
                    DSharePreference.setMyReferralCode(VerifyActivity.this,
                            verifyModel.account.referralCode);
                    addDeviceToken();
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("verifyToken", networkManager.parseError(throwable));
                    Log.d("TAG", "Fail API: VERIFY_TOKEN");
                    ViewUtils.setEditTextRed(getApplicationContext(), txtCode);
                    hideProgressDialog();
//                    showAlertDialog(networkManager.parseError(throwable).message);
                    showAlertDialog(getResources().getString(R.string.verify_fail));
                });
    }

    private void reSendCodeAction() {
        networkManager.logIn(new LoginRequest(email, phone))
                .subscribe(tokenModel -> {
                    Log.d("TAG", "API: RESEND_CODE");
                    token.id = tokenModel.id;
                    if (!TextUtils.isEmpty(tokenModel.code)) {
                        showCode(tokenModel.code);
                    }
                    hideProgressDialog();
                    showAlertDialog("", getString(R.string.new_code_send));
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("logIn/" + email +"/" + phone, networkManager.parseError(throwable));
                    Log.d("TAG", "Fail API: RESEND_CODE");
                    ViewUtils.setEditTextRed(getApplicationContext(), txtCode);
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    private void showAutoDialog() {
        hideProgressDialog();
        try {
            JSONObject props = new JSONObject();
            props.put("Sign Up - Step", "Completed");
            mixpanel.track("Sign Up - Completed", props);
        } catch (JSONException e) {
            Logs.log(e);
        }
        final AutoIconDialog mDialog = new AutoIconDialog(this);
        mDialog.setMessageDialog(R.string.message_verify_done);
        mDialog.setTimeAutoDismiss(1500);
        mDialog.setDialogDismissCallback(() -> {
            mDialog.dismiss();
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        mDialog.show();
    }

    private void addDeviceToken() {
        networkManager.addDeviceToken(new DeviceTokenRequest(DSharePreference.getRegistrationId(mActivity)))
                .doAfterTerminate(this::getProfileSetting)
                .subscribe(object -> {
                    Log.d("TAG", "API: ADD_DEVICE_TOKEN");
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("addDeviceToken", networkManager.parseError(throwable));
                    Log.d("TAG", "Fail API: ADD_DEVICE_TOKEN");
                });
    }

    private void getProfileSetting() {
        networkManager.getAccountSettings()
                .subscribe(accountModel -> {
                    Log.d("TAG", "API: GET_ACCOUNT_SETTING");
                    LocationModel location = new LocationModel();
                    location.latitude = accountModel.defaultLatitude;
                    location.longitude = accountModel.defaultLongitude;
                    DSharePreference.setAccountSetting(mActivity, accountModel);
                    DSharePreference.setDefaultLocation(mActivity, location);
                    AppApplication.getInstance().setLocationDefault(location);
                    checkUserStatus();
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getAccountSettings", networkManager.parseError(throwable));
                    Log.d("TAG", "Fail API: GET_ACCOUNT_SETTING");
                    checkUserStatus();
                });
    }

    public void checkUserStatus() {
        showAutoDialog();
//        String json = DSharePreference.getProfile(mActivity);
//        VerifyModel model = new Gson().fromJson(json, VerifyModel.class);
//        networkManager.checkOperatorProfile(model.account.id)
//                .subscribe(operatorModel -> {
//                    Log.d("TAG", "API: CHECK_PROFILE_OPERATOR");
//                    // TODO commend for quick test skip
//                    model.account.operator = operatorModel;
//                    DSharePreference.setProfile(mActivity, model.toJSON());
//
//                    if (operatorModel.status.equalsIgnoreCase(OperatorStatus.APPROVED)) {
//                        DSharePreference.setOperator(mActivity, true);
//                    } else {
//                        // operator ready
//                        DSharePreference.setOperator(mActivity, false);
//                    }
//                    showAutoDialog();
//                }, throwable -> {
//                    Log.d("TAG", "Fail API: CHECK_PROFILE_OPERATOR");
//                    showAutoDialog();
//                });
    }

}

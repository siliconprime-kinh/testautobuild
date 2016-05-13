package com.dropininc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.customview.EmailHighlightEditText;
import com.dropininc.customview.HighlightEditText;
import com.dropininc.dialog.ListDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.network.request.LoginRequest;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity implements HighlightEditText.OnStateChangeListener {

    @Bind(R.id.loginCountryCodeEditText)
    protected HighlightEditText countryCodeEditText;
    @Bind(R.id.loginPhoneNumberEditText)
    protected HighlightEditText phoneNumberEditText;
    @Bind(R.id.loginEmailAddressEditText)
    protected EmailHighlightEditText emailAddressEditText;
    @Bind(R.id.loginSmsDescription)
    protected TextView smsDescriptionTextView;
    @Bind(R.id.loginTitleTextView)
    protected TextView titleTextView;
    @Bind(R.id.loginButton)
    protected Button loginButton;

    int countryIndex = -1;
    private ArrayList<String> telephoneCodes;
    private String countryCode = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setUpListeners(countryCodeEditText, phoneNumberEditText, emailAddressEditText);
        setupCountryCode();
        setupFonts();
    }

    private void setupFonts() {
        FontUtils.typefaceTextView(titleTextView, FontType.LIGHT);
        FontUtils.typefaceTextView(smsDescriptionTextView, FontType.LIGHT);
        FontUtils.typefaceTextView(countryCodeEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(phoneNumberEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(emailAddressEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(loginButton, FontType.LIGHT);
    }

    private void setUpListeners(HighlightEditText... highlightEditTexts) {
        for (HighlightEditText highlightEditText : highlightEditTexts) {
            highlightEditText.setOnStateChangeListener(this);
        }
    }

    private void setupCountryCode() {
        telephoneCodes = new ArrayList<>(Arrays.asList(Constants.countryAreaCodes));
        // default value
        countryCodeEditText.setText(telephoneCodes.get(30));
        countryCode = telephoneCodes.get(30);
    }

    @OnClick(R.id.loginCountryCodeEditText)
    protected void showCountryCodes() {
        ListDialog listDialog = new ListDialog(this, new ArrayList<>(Arrays.asList(Constants.countryNames)), countryIndex) {
            @Override
            public void onClick(int index) {
                countryIndex = index;
                countryCodeEditText.setText(telephoneCodes.get(index));
                countryCode = telephoneCodes.get(index);
                phoneNumberEditText.requestFocus();
            }
        };
        listDialog.show();
    }

    @OnClick(R.id.loginButton)
    protected void onLoginButtonClicked() {
        if (isOnline()) {
            loginAction();
        } else {
            showAlertDialog(getResources().getString(R.string.network_problem), getResources().getString(R.string.please_connect_to_continue));
        }
    }

    private void loginAction() {
        showProgressDialog(getString(R.string.processing), null);
        networkManager.logIn(new LoginRequest(Utils.getString(emailAddressEditText),
                countryCode + Utils.getString(phoneNumberEditText)))
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(tokenModel -> {
                    mixpanel.track("Logged In");

                    Intent intent = new Intent(this, VerifyActivity.class);
                    intent.putExtra("LOGIN", true);
                    intent.putExtra("DATA", tokenModel.toJSON());
                    intent.putExtra("PHONE", countryCode + Utils.getString(phoneNumberEditText));
                    intent.putExtra("EMAIL", Utils.getString(emailAddressEditText));
                    startActivity(intent);
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("logIn", networkManager.parseError(throwable));
//                    showAlertDialog(networkManager.parseError(throwable).message);
                    showAlertDialog(getResources().getString(R.string.login_fail));
                });
    }

    @Override
    public void onStateChanged(HighlightEditText.State state) {
        boolean inputIsValid = checkStates(countryCodeEditText, phoneNumberEditText, emailAddressEditText);
        loginButton.setEnabled(inputIsValid);
    }

    private boolean checkStates(HighlightEditText... editTexts) {
        for (HighlightEditText editText : editTexts) {
            if (editText.isRequired() && !editText.getCurrentState().equals(HighlightEditText.State.HIGHLIGHTED)) {
                return false;
            }
        }
        return true;
    }
}


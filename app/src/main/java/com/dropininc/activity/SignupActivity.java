package com.dropininc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.customview.EmailHighlightEditText;
import com.dropininc.customview.HighlightEditText;
import com.dropininc.dialog.ListDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.SignupModel;
import com.dropininc.network.request.AccountRequest;
import com.dropininc.utils.Constants;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends BaseActivity implements HighlightEditText.OnStateChangeListener {

    @Bind(R.id.signUpFirstNameEditText)
    protected HighlightEditText firstNameEditText;
    @Bind(R.id.signUpLastNameEditText)
    protected HighlightEditText lastNameEditText;
    @Bind(R.id.signUpCountryCodeEditText)
    protected HighlightEditText countryCodeEditText;
    @Bind(R.id.signUpPhoneNumberEditText)
    protected HighlightEditText phoneNumberEditText;
    @Bind(R.id.signUpEmailAddressEditText)
    protected EmailHighlightEditText emailAddressEditText;
    @Bind(R.id.signUpReferralCodeEditText)
    protected HighlightEditText referralCodeEditText;
    @Bind(R.id.signUpButton)
    protected Button signUpButton;
    @Bind(R.id.signUpTitleTextView)
    protected TextView titleTextView;
    @Bind(R.id.signUpSmsConfirmationTextView)
    protected TextView smsConfirmationTextView;
    @Bind(R.id.signUpReferralHintTextView)
    protected TextView referralHintTextView;
    @Bind(R.id.signUpReferralDescriptionTextView)
    protected TextView referralDescriptionTextView;

    private int countryIndex = -1;
    private ArrayList<String> telephoneCodes;
    private String countryCode = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelAllRequestWhenStopActivity(false);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        setUpListeners(firstNameEditText, lastNameEditText, countryCodeEditText,
                phoneNumberEditText, emailAddressEditText, referralCodeEditText);
        setupCountryCode();
        setupFonts();
        if (!TextUtils.isEmpty(SplashActivity.referralId)) {
            referralCodeEditText.setText(SplashActivity.referralId);
        }

        referralCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    boolean inputIsValid = checkStates(firstNameEditText, lastNameEditText, countryCodeEditText,
                            phoneNumberEditText, emailAddressEditText);
                    if (inputIsValid) {
                        onSignUpClicked();
                    } else {
                        hideSoftKeyboard();
                    }
                }
                return false;
            }
        });
    }

    private void setUpListeners(HighlightEditText... highlightEditTexts) {
        for (HighlightEditText highlightEditText : highlightEditTexts) {
            highlightEditText.setOnStateChangeListener(this);
        }
    }

    private void setupFonts() {
        FontUtils.typefaceTextView(titleTextView, FontType.LIGHT);
        FontUtils.typefaceTextView(smsConfirmationTextView, FontType.LIGHT);
        FontUtils.typefaceTextView(referralHintTextView, FontType.LIGHT);
        FontUtils.typefaceTextView(referralDescriptionTextView, FontType.LIGHT);

        FontUtils.typefaceTextView(firstNameEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(lastNameEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(countryCodeEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(phoneNumberEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(emailAddressEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(referralCodeEditText, FontType.LIGHT);
        FontUtils.typefaceTextView(signUpButton, FontType.LIGHT);
    }

    private void setupCountryCode() {
        telephoneCodes = new ArrayList<>(Arrays.asList(Constants.countryAreaCodes));
        // default value
        countryCodeEditText.setText(telephoneCodes.get(30));
        countryCode = telephoneCodes.get(30);
    }

    @OnClick(R.id.signUpCountryCodeEditText)
    protected void showCountryCodes() {
        ListDialog listDialog = new ListDialog(context,
                new ArrayList<>(Arrays.asList(Constants.countryNames)), countryIndex) {
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

    @OnClick(R.id.signUpButton)
    protected void onSignUpClicked() {
        if (isOnline()) {
            signUpAction();
        } else {
            showAlertDialog(getResources().getString(R.string.network_problem),
                    getResources().getString(R.string.please_connect_to_continue));
        }
    }

    private void processSignUp(SignupModel signupModel) {
        Intent intent = new Intent(this, VerifyActivity.class);
        intent.putExtra("DATA", signupModel.token.toJSON());
        intent.putExtra("PHONE", countryCode + Utils.getString(phoneNumberEditText));
        intent.putExtra("EMAIL", Utils.getString(emailAddressEditText));
        intent.putExtra("IS_SIGN_UP", true);
        try {
            JSONObject props = new JSONObject();
            props.put("First Name", signupModel.account.firstName);
            props.put("Last Name", signupModel.account.lastName);
            props.put("ID", signupModel.account.id);
            mixpanel.getPeople().set("First Name", signupModel.account.firstName);
            mixpanel.getPeople().set("Last Name", signupModel.account.lastName);
            mixpanel.getPeople().set("ID", signupModel.account.id);
            mixpanel.track("Sign Up - Register", props);
        } catch (JSONException e) {
            Logs.log(e);
        }
        startActivity(intent);
    }

    private void signUpAction() {
        showProgressDialog(getString(R.string.processing), null);
        networkManager.signUp(new AccountRequest(Utils.getString(firstNameEditText),
                Utils.getString(lastNameEditText), Utils.getString(referralCodeEditText),
                Utils.getString(emailAddressEditText), countryCode + Utils.getString(phoneNumberEditText)))
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(this::processSignUp,
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("signUp", networkManager.parseError(throwable));
//                            showAlertDialog(networkManager.parseError(throwable).message);
                            showAlertDialog(getResources().getString(R.string.signup_fail));
                        });
    }

    @Override
    public void onStateChanged(HighlightEditText.State state) {
        boolean inputIsValid = checkStates(firstNameEditText, lastNameEditText, countryCodeEditText,
                phoneNumberEditText, emailAddressEditText);
        signUpButton.setEnabled(inputIsValid);
        signUpButton.setText(inputIsValid ? R.string.sign_up : R.string.field_completion_hint);
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

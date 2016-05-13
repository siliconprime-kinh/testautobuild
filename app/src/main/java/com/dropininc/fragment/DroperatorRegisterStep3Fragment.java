package com.dropininc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dropininc.AppApplication;
import com.dropininc.Constants;
import com.dropininc.R;
import com.dropininc.customview.HighlightEditText;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.model.OperatorFlowModel;
import com.dropininc.network.request.UpdateOperatorProfileRequest;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Token;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 14.03.16.
 */
public class DroperatorRegisterStep3Fragment extends BaseFragment implements
        HighlightEditText.OnStateChangeListener {

    private static final String OPERATOR_FLOW_BUNDLE = "OPERATOR_FLOW_BUNDLE";

    @Bind(R.id.dropRegisterAccountNumberEditText)
    protected HighlightEditText accountNumberEditText;
    @Bind(R.id.dropRegisterRoutingNumberEditText)
    protected HighlightEditText routingNumberEditText;
    @Bind(R.id.dropRegisterDoneButton)
    protected Button doneButton;

    private ProgressDialog mProgressDialog;
    private boolean isCancelRequest = false;
    private OperatorFlowModel mOperatorFlowModel;
    private Activity mActivity;
    private Handler handler;

    public static DroperatorRegisterStep3Fragment getInstance(OperatorFlowModel model) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(OPERATOR_FLOW_BUNDLE, model);
        DroperatorRegisterStep3Fragment fragment = new DroperatorRegisterStep3Fragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        mOperatorFlowModel = (OperatorFlowModel)getArguments().getSerializable(OPERATOR_FLOW_BUNDLE);
        handler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_droperator_register_step_3, container, false);
        ButterKnife.bind(this, view);

        initStateChangedListener(this);

        return view;
    }

    private void initStateChangedListener(HighlightEditText.OnStateChangeListener listener) {
        accountNumberEditText.setOnStateChangeListener(listener);
        routingNumberEditText.setOnStateChangeListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initStateChangedListener(null);
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        isCancelRequest = true;
        super.onDestroy();
    }

    @Override
    public void onStateChanged(HighlightEditText.State state) {
        boolean inputIsValid = checkStates(accountNumberEditText, routingNumberEditText);
        doneButton.setEnabled(inputIsValid);
        doneButton.setText(inputIsValid ? R.string.done : R.string.field_completion_hint);
    }

    @OnClick(R.id.dropRegisterDoneButton)
    protected void onDoneClicked() {
        saveProfile();
    }

    private boolean checkStates(HighlightEditText... editTexts) {
        for (HighlightEditText editText : editTexts) {
            if (editText.isRequired() && !editText.getCurrentState().equals(HighlightEditText.State.HIGHLIGHTED)) {
                return false;
            }
        }
        return true;
    }

    private void skipDroperatorSign() {
        networkManager.skipDroperator(AppApplication.getInstance().getAccountId()).subscribe();
    }

    private void processGoodResponse() {
        final AutoDialog mDialog = new AutoDialog(mActivity);
        mDialog.setTitleDialog(R.string.application_submitted);
        mDialog.setMessageDialog(R.string.message_application_submitted);
        mDialog.setTimeAutoDismiss(5000);
        mDialog.setDialogDismissCallback(() -> {
            mDialog.dismiss();
            getActivity().finish();
        });
        mDialog.show();
    }

    private void onError(String errorText) {
        dismissDialog();

        final AlertDialog alertDialog = new AlertDialog(mActivity);
        alertDialog.setMessageDialog(errorText);
        alertDialog.setButtonClick(view -> alertDialog.cancel());
        alertDialog.show();
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void saveProfile() {
        isCancelRequest = false;
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();

        networkManager.updateOperatorsProfile(new UpdateOperatorProfileRequest(
                mOperatorFlowModel.socialSecurityNumber,
                new UpdateOperatorProfileRequest.Dob(mOperatorFlowModel.month, mOperatorFlowModel.day,
                        mOperatorFlowModel.year),
                new UpdateOperatorProfileRequest.Address(mOperatorFlowModel.city, mOperatorFlowModel.street,
                        mOperatorFlowModel.zipCode, mOperatorFlowModel.state, "US")))
                .subscribe(ignore -> runCreatingBankAccountToken(),
                        throwable ->{
                            AppApplication.getInstance().logErrorServer("updateOperatorsProfile", networkManager.parseError(throwable));
                            onError(networkManager.parseError(throwable).message);});
    }

    private void runCreatingBankAccountToken() {
        new Thread(() -> {
            handler.post(new TokenRunnable(createBankAccountToken()));
        }).start();
    }

    private Token createBankAccountToken() {
        Stripe.apiKey = Constants.STRIPE_PUBLISHABLE_KEY;

        Map<String, Object> tokenParams = new HashMap<>();
        Map<String, Object> bank_accountParams = new HashMap<>();
        bank_accountParams.put("country", "US");
        bank_accountParams.put("currency", "usd");
        bank_accountParams.put("routing_number", routingNumberEditText.getText().toString());
        bank_accountParams.put("account_number", accountNumberEditText.getText().toString());
        tokenParams.put("bank_account", bank_accountParams);

        try {
            return Token.create(tokenParams);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException
                | CardException | APIException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void savePayoutOptions(String token) {
        networkManager.addCreditCard(token)
                .subscribe(ignore -> {
                    dismissDialog();
                    skipDroperatorSign();
                    processGoodResponse();
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("addCreditCard", networkManager.parseError(throwable));
                    onError(networkManager.parseError(throwable).message);});
    }

    private class TokenRunnable implements Runnable {

        Token token;

        TokenRunnable(Token token) {
            this.token = token;
        }

        @Override
        public void run() {
            if (token == null) {
                onError(getString(R.string.message_invalid_routing_number));
            } else {
                savePayoutOptions(token.getId());
            }
        }
    }
}

package com.dropininc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.activity.SignupTutorialActivity;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.network.request.FeedbackRequest;
import com.dropininc.sharepreference.DSharePreference;

public class DeleteAccountFragment extends BaseFragment implements View.OnClickListener {
    private FragmentActivity mActivity;
    private View mRootView;
    private Button mButtonFeedback;
    private EditText mEditFeedback;
    private LinearLayout mLayDelete, mLayFeedback;
    private ProgressDialog mProgressDialog;

    private boolean isSkip = false;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 0) {
                mButtonFeedback.setEnabled(false);
            } else {
                mButtonFeedback.setEnabled(true);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        ((MainActivity) mActivity).addToolBarNormal(getTitle());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_delete_account, container, false);

        initView();

        return mRootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_cancel:
                onBackPressed();
                break;
            case R.id.bt_delete:
                clickDeleteAccount();
                break;
            case R.id.bt_send:
                sendFeedback();
                break;
            case R.id.bt_skip:
                isSkip = true;
                deleteAccount();
                break;
        }
    }

    private void initView() {
        Button mButtonDelete = (Button) mRootView.findViewById(R.id.bt_delete);
        Button mButtonCancel = (Button) mRootView.findViewById(R.id.bt_cancel);
        mButtonFeedback = (Button) mRootView.findViewById(R.id.bt_send);
        Button mButtonSkip = (Button) mRootView.findViewById(R.id.bt_skip);
        mEditFeedback = (EditText) mRootView.findViewById(R.id.ed_feedback);
        mLayDelete = (LinearLayout) mRootView.findViewById(R.id.lay_delete);
        mLayFeedback = (LinearLayout) mRootView.findViewById(R.id.lay_feedback);

        mButtonDelete.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        mButtonFeedback.setOnClickListener(this);
        mButtonSkip.setOnClickListener(this);

        mButtonFeedback.addTextChangedListener(mTextWatcher);
    }

    private void showProgress() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void deleteAccount() {
        if (isSkip) {
            showProgress();
        }

        networkManager.deleteProfile(AppApplication.getInstance().getAccountId())
                .subscribe(ignore -> {
                    mProgressDialog.dismiss();
                    if (!isSkip) {
                        processResponseSendFeedback();
                    } else {
                        skipFeedback();
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("deleteProfile", networkManager.parseError(throwable));
                    showAlertDialog(networkManager.parseError(throwable).message);});
    }

    private void sendFeedback() {
        String title = "Feedback";
        String content = mEditFeedback.getText().toString();
        if (TextUtils.isEmpty(content)) return;

        showProgress();

        networkManager.sendFeedback(new FeedbackRequest(title, content))
                .subscribe(ignore -> deleteAccount(),
                        throwable ->{
                            AppApplication.getInstance().logErrorServer("sendFeedback", networkManager.parseError(throwable));
                            showAlertDialog(networkManager.parseError(throwable).message);});
    }

    private void skipFeedback() {
        DSharePreference.logout(mActivity);
        ((MainActivity) mActivity).clearStack();
        Intent intent = new Intent(mActivity, SignupTutorialActivity.class);
        startActivity(intent);
        mActivity.finish();
    }

    private void clickDeleteAccount() {
        mLayDelete.setVisibility(View.GONE);
        mLayFeedback.setVisibility(View.VISIBLE);
        ((MainActivity) mActivity).setNavigationEnable(false);
        ((MainActivity) mActivity).setBackPressedEnable(false);
    }

    private void processResponseSendFeedback() {
        ThanksFragment fragment = new ThanksFragment();
        ((MainActivity) mActivity).pushFragments(fragment);
    }
}

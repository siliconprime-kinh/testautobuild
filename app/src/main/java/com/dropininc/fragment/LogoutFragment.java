package com.dropininc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.activity.SignupTutorialActivity;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.GeneralModel;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;


public class LogoutFragment extends BaseFragment implements View.OnClickListener {

    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_logout, container, false);
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.logout_title));
        initView();

        return mRootView;
    }

    private void initView() {
        Button btnLogout = (Button) mRootView.findViewById(R.id.btnLogout);
        Button btnCancel = (Button) mRootView.findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        TextView tv_title = (TextView) mRootView.findViewById(R.id.tv_title);
        FontUtils.typefaceTextView(tv_title, FontType.LIGHT);

        FontUtils.typefaceButton(btnLogout, FontType.LIGHT);
        FontUtils.typefaceButton(btnCancel, FontType.LIGHT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout:
                logout();
                break;
            case R.id.btnCancel:
                onBackPressed();
                break;
        }

    }

    private void logout() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();

        networkManager.logout(DSharePreference.getRegistrationId(mActivity))
                .subscribe(this::processResponseLogout, throwable ->
                {
                    AppApplication.getInstance().logErrorServer("logout", networkManager.parseError(throwable));
                    processResponseLogout(new GeneralModel());
                });
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void processResponseLogout(GeneralModel model) {
        try {
            dismissDialog();
            boolean isAllowLocation = DSharePreference.isAllowLocation(mActivity);
            boolean iSAllowNotification =  DSharePreference.isAllowNotification(mActivity);
            DSharePreference.clearAll(mContext);
            // keep old config
            DSharePreference.setAllowLocation(mActivity, isAllowLocation);
            DSharePreference.setAllowNotification(mActivity, iSAllowNotification);
            
            setUserOffline();
            Utils.sendMessageSwitchModeToViewer(mActivity, AppApplication.getInstance().getAccountId());
            ((MainActivity) mActivity).removeAllChannel();
            AppApplication.getInstance().setCheckPayment(false);
            DSharePreference.logout(mActivity);
            AppApplication.getInstance().updateUserTypeWhenLogOut();

            Intent intent = new Intent(mActivity, SignupTutorialActivity.class);
            startActivity(intent);
            mActivity.finish();
            MainActivity.setInstance(null);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void setUserOffline() {
        networkManager.location(new LocationRequest("1.0", "1.0", "0", 0, false, "11",
                ((MainActivity) mActivity).getSettingRadius() + "", ""))
                .subscribe(ignore -> {
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("location", networkManager.parseError(throwable));
                });
    }
}

package com.dropininc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.activity.SignupTutorialActivity;
import com.dropininc.dialog.AutoDialog;
import com.dropininc.sharepreference.DSharePreference;


public class ThanksFragment extends BaseFragment {
    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        ((MainActivity)mActivity).addToolBarCustom(false, false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_thanks, container, false);

        showDialogThanks();

        return mRootView;
    }

    private void showDialogThanks(){
        final AutoDialog mDialog = new AutoDialog(mActivity);
        mDialog.setTitleDialog(R.string.thanks_for_your_feedback);
        mDialog.setMessageDialog(R.string.we_are_sorry_to_see_you_go);
        mDialog.setTimeAutoDismiss(3000);
        mDialog.setCancelable(false);
        mDialog.setDialogDismissCallback(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();

                ((MainActivity) mActivity).clearStack();
                DSharePreference.logout(mActivity);
                Intent intent = new Intent(mActivity, SignupTutorialActivity.class);
                startActivity(intent);
                mActivity.finish();
            }
        });
        mDialog.show();
    }
}

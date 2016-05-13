package com.dropininc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.Navigation;
import com.dropininc.interfaces.NavigationDrawerCallBack;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;

public class MainMenuFragment extends BaseFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private TextView mLayHome;
    private TextView mLayPayment;
    private TextView mLayTerm;
    private TextView mLayHistory;
    private TextView mLaySettings;
    private TextView mLaySupport;
    private TextView mTextNotification;
    private TextView mTextNumber;
    private TextView lay_become_operator;
    private TextView lay_refer_friend;
    private RelativeLayout mLayNotification;
    private View view_become_operator;

    private NavigationDrawerCallBack mCallback;
    private static MainMenuFragment mInstance;

    public static synchronized MainMenuFragment getInstance() {
        if (mInstance == null) {
            mInstance = new MainMenuFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main_menu, null);

        initialView();
        onItemSelected(Navigation.HOME);
        getNotificationCount();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkOperatorStatus();
        Log.d("TAG", "Menu onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setMenuItemSelectedCallBack(NavigationDrawerCallBack callBack) {
        this.mCallback = callBack;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lay_home:
                mCallback.onItemSelected(Navigation.HOME);
                onItemSelected(Navigation.HOME);
                break;
            case R.id.lay_notification:
                mCallback.onItemSelected(Navigation.NOTIFICATION);
                onItemSelected(Navigation.NOTIFICATION);
                break;
            case R.id.lay_payment:
                mCallback.onItemSelected(Navigation.PAYMENT);
                onItemSelected(Navigation.PAYMENT);
                break;
            case R.id.lay_term:
                mCallback.onItemSelected(Navigation.TERM_OF_SERVICE);
                onItemSelected(Navigation.TERM_OF_SERVICE);
                break;
            case R.id.lay_history:
                mCallback.onItemSelected(Navigation.HISTORY);
                onItemSelected(Navigation.HISTORY);
                break;
            case R.id.lay_setting:
                mCallback.onItemSelected(Navigation.SETTINGS);
                onItemSelected(Navigation.SETTINGS);
                break;
            case R.id.lay_support:
                mCallback.onItemSelected(Navigation.SUPPORT);
                onItemSelected(Navigation.SUPPORT);
                break;
            case R.id.lay_logout:
                mCallback.onItemSelected(Navigation.LOGOUT);
                break;
            case R.id.lay_become_operator:
                mCallback.onItemSelected(Navigation.BECOME_OPERATOR);
                onItemSelected(Navigation.BECOME_OPERATOR);
                break;
            case R.id.lay_refer_friend:
                mCallback.onItemSelected(Navigation.REFER_FRIEND);
                onItemSelected(Navigation.REFER_FRIEND);
                break;
        }
    }

    private void initialView() {
        mLayHome = (TextView) mRootView.findViewById(R.id.lay_home);
        mLayPayment = (TextView) mRootView.findViewById(R.id.lay_payment);
        mLayTerm = (TextView) mRootView.findViewById(R.id.lay_term);
        mLayHistory = (TextView) mRootView.findViewById(R.id.lay_history);
        mLaySettings = (TextView) mRootView.findViewById(R.id.lay_setting);
        mLaySupport = (TextView) mRootView.findViewById(R.id.lay_support);
        TextView mLayLogout = (TextView) mRootView.findViewById(R.id.lay_logout);
        mTextNotification = (TextView) mRootView.findViewById(R.id.tv_notification);
        mTextNumber = (TextView) mRootView.findViewById(R.id.tv_number);
        mLayNotification = (RelativeLayout) mRootView.findViewById(R.id.lay_notification);
        lay_become_operator = (TextView) mRootView.findViewById(R.id.lay_become_operator);
        lay_refer_friend = (TextView) mRootView.findViewById(R.id.lay_refer_friend);
        view_become_operator = mRootView.findViewById(R.id.view_become_operator);

        mLayHome.setOnClickListener(this);
        mLayNotification.setOnClickListener(this);
        mLayPayment.setOnClickListener(this);
        mLayTerm.setOnClickListener(this);
        mLayHistory.setOnClickListener(this);
        mLaySettings.setOnClickListener(this);
        mLaySupport.setOnClickListener(this);
        mLayLogout.setOnClickListener(this);
        lay_become_operator.setOnClickListener(this);
        lay_refer_friend.setOnClickListener(this);

        TextView lay_general = (TextView) mRootView.findViewById(R.id.lay_general);
        FontUtils.typefaceTextView(lay_general, FontType.BOLD);

        FontUtils.typefaceTextView(mLayHome, FontType.REGULAR);
        FontUtils.typefaceTextView(mLayPayment, FontType.REGULAR);
        FontUtils.typefaceTextView(mLayTerm, FontType.REGULAR);
        FontUtils.typefaceTextView(mLayHistory, FontType.REGULAR);
        FontUtils.typefaceTextView(mLaySettings, FontType.REGULAR);
        FontUtils.typefaceTextView(mLaySupport, FontType.REGULAR);
        FontUtils.typefaceTextView(mTextNotification, FontType.REGULAR);
        FontUtils.typefaceTextView(mLayLogout, FontType.REGULAR);
        FontUtils.typefaceTextView(mTextNumber, FontType.REGULAR);
        FontUtils.typefaceTextView(lay_become_operator, FontType.REGULAR);
        FontUtils.typefaceTextView(lay_refer_friend, FontType.REGULAR);

        checkOperatorStatus();

    }

    public void checkOperatorStatus() {
        try {
            boolean isOperator = DSharePreference.isOperator(mContext);
            // hard for hide become
            isOperator = true;
            if (isOperator) {
                lay_become_operator.setVisibility(View.GONE);
                view_become_operator.setVisibility(View.GONE);
            } else {
                lay_become_operator.setVisibility(View.VISIBLE);
                view_become_operator.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onItemSelected(int item) {
        mLayHome.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mLayPayment.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mLayTerm.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mLayHistory.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mLaySettings.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mLaySupport.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mTextNotification.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        mTextNumber.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        lay_become_operator.setTextColor(getResources().getColor(R.color.text_blue_navigation));
        lay_refer_friend.setTextColor(getResources().getColor(R.color.text_blue_navigation));

        mLayHome.setSelected(false);
        mLayNotification.setSelected(false);
        mLayPayment.setSelected(false);
        mLayTerm.setSelected(false);
        mLayHistory.setSelected(false);
        mLaySettings.setSelected(false);
        mLaySupport.setSelected(false);
        lay_become_operator.setSelected(false);
        lay_refer_friend.setSelected(false);
        switch (item) {
            case Navigation.HOME:
                mLayHome.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLayHome.setSelected(true);
                break;
            case Navigation.NOTIFICATION:
                mTextNotification.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mTextNumber.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLayNotification.setSelected(true);
                break;
            case Navigation.PAYMENT:
                mLayPayment.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLayPayment.setSelected(true);
                break;
            case Navigation.TERM_OF_SERVICE:
                mLayTerm.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLayTerm.setSelected(true);
                break;
            case Navigation.HISTORY:
                mLayHistory.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLayHistory.setSelected(true);
                break;
            case Navigation.SETTINGS:
                mLaySettings.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLaySettings.setSelected(true);
                break;
            case Navigation.SUPPORT:
                mLaySupport.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                mLaySupport.setSelected(true);
                break;
            case Navigation.BECOME_OPERATOR:
                lay_become_operator.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                lay_become_operator.setSelected(true);
                break;
            case Navigation.REFER_FRIEND:
                lay_refer_friend.setTextColor(getResources().getColor(R.color.text_blue_navigation_selected));
                lay_refer_friend.setSelected(true);
                break;
            case Navigation.LOGOUT:
                break;
        }
    }

    public void setNotificationNumber(int number) {
        if (number != 0) {
            mTextNumber.setText("" + number);
        } else {
            mTextNumber.setText("");
        }
    }
/*
    private void updateNotification(int number) {
        if (number != 0) {
            mTextNotification.setTypeface(null, Typeface.BOLD);
            mTextNumber.setTypeface(null, Typeface.BOLD);
            mTextNumber.setText("" + number);
            mTextNotification.setTextColor(getResources().getColor(R.color.blue_button_selected));
            mTextNumber.setTextColor(getResources().getColor(R.color.blue_button_selected));
        } else {
            mTextNotification.setTypeface(null, Typeface.NORMAL);
            mTextNumber.setTypeface(null, Typeface.NORMAL);
            mTextNumber.setText("");
            mTextNotification.setTextColor(getResources().getColor(R.color.text_green));
            mTextNotification.setTextColor(getResources().getColor(R.color.text_green));
        }
    }
*/
    public void getNotificationCount() {
        // received is read
        networkManager.getNotificationCount()
                .subscribe(countModel -> {
                    if (countModel != null) setNotificationNumber(countModel.count);
                }, throwable ->{
                    AppApplication.getInstance().logErrorServer("getNotificationCount", networkManager.parseError(throwable));
                    setNotificationNumber(0);});
    }
}

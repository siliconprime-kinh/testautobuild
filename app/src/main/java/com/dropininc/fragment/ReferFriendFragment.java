package com.dropininc.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropininc.R;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;


public class ReferFriendFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = getClass().getName();

    private final int CHANNEL_EMAIL     = 0;
    private final int CHANNEL_SMS       = 1;
    private final int CHANNEL_FACEBOOK  = 2;
    private final int CHANNEL_TWITTER   = 3;
    private final int CHANNEL_REFERRAL  = 4;

    private FragmentActivity mActivity;
    private View mRootView;
    CallbackManager callbackManager;

    private LinearLayout lay_share_item;
    private RelativeLayout lay_email;
    private RelativeLayout lay_sms;
    private RelativeLayout lay_facebook;
    private RelativeLayout lay_twitter;
    private RelativeLayout lay_link;

    private TextView txtLink;

    private Button btnShare;
    AlertDialog alertDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        FacebookSdk.sdkInitialize(mActivity.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        alertDialog = new AlertDialog(mActivity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_refer_friend, container, false);
        setupView();

        return mRootView;
    }

    private void setupView() {
        txtLink = (TextView) mRootView.findViewById(R.id.txtLink);
        lay_share_item = (LinearLayout) mRootView.findViewById(R.id.lay_share_item);
        lay_email = (RelativeLayout) mRootView.findViewById(R.id.lay_email);
        lay_sms = (RelativeLayout) mRootView.findViewById(R.id.lay_sms);
        lay_facebook = (RelativeLayout) mRootView.findViewById(R.id.lay_facebook);
        lay_twitter = (RelativeLayout) mRootView.findViewById(R.id.lay_twitter);
        lay_link = (RelativeLayout) mRootView.findViewById(R.id.lay_link);
        lay_link.setEnabled(false);

        btnShare = (Button) mRootView.findViewById(R.id.btnShare);

        FontUtils.typefaceButton(btnShare, FontType.LIGHT);
        FontUtils.typefaceTextView(txtLink, FontType.LIGHT);

        TextView txtDes = (TextView) mRootView.findViewById(R.id.txtDes);
        FontUtils.typefaceTextView(txtDes, FontType.BOLD);
        TextView txtDes2 = (TextView) mRootView.findViewById(R.id.txtDes2);
        FontUtils.typefaceTextView(txtDes2, FontType.LIGHT);
        TextView txtEmail = (TextView) mRootView.findViewById(R.id.txtEmail);
        FontUtils.typefaceTextView(txtEmail, FontType.LIGHT);
        TextView txtSMS = (TextView) mRootView.findViewById(R.id.txtSMS);
        FontUtils.typefaceTextView(txtSMS, FontType.LIGHT);
        TextView txtFacebook = (TextView) mRootView.findViewById(R.id.txtFacebook);
        FontUtils.typefaceTextView(txtFacebook, FontType.LIGHT);
        TextView txtTwitter = (TextView) mRootView.findViewById(R.id.txtTwitter);
        FontUtils.typefaceTextView(txtTwitter, FontType.LIGHT);


        lay_email.setOnClickListener(this);
        lay_sms.setOnClickListener(this);
        lay_facebook.setOnClickListener(this);
        lay_twitter.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        lay_link.setOnClickListener(this);

        shareAction("referral", CHANNEL_REFERRAL);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.i("MyApp", "Frag onActivityResult : " + resultCode);
        if (resultCode == Activity.RESULT_OK) {
//            String message = mActivity.getResources().getString(R.string.shared_via_facebook);
//            if (requestCode == 111) {// email
//                message = mActivity.getResources().getString(R.string.shared_via_email);
//            }
//            if (requestCode == 112) {// sms
//                message = mActivity.getResources().getString(R.string.shared_via_sms);
//            }
//
//            alertDialog.setTitleDialog("");
//            alertDialog.setMessageDialog(message);
//            alertDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShare:
                if (btnShare.getText().toString().equals(mActivity.getResources().getString(R.string.share))) {
                    lay_share_item.setVisibility(View.VISIBLE);
                    btnShare.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_button_gray));
                    btnShare.setText(R.string.cancel);
                } else {
                    lay_share_item.setVisibility(View.GONE);
                    btnShare.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector_button_blue));
                    btnShare.setText(R.string.share);
                }
                break;

            case R.id.lay_email:
                shareAction("email", CHANNEL_EMAIL);
                break;

            case R.id.lay_sms:
                shareAction("sms", CHANNEL_SMS);
                break;
            case R.id.lay_facebook:
                shareAction("facebook", CHANNEL_FACEBOOK);
                break;

            case R.id.lay_twitter:
                shareAction("twitter", CHANNEL_TWITTER);
                break;

            case R.id.lay_link:
                if (!TextUtils.isEmpty(txtLink.getText().toString())) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                        clipboard.setText(txtLink.getText().toString());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied to Clipboard", txtLink.getText().toString());
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(mActivity.getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void shareAction(String channel, final int channelId) {
        String code = DSharePreference.getMyReferralCode(mActivity);
        Logs.log("My referral code = " + code);
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject();
        LinkProperties linkProperties = new LinkProperties()
                .setChannel(channel) // "email,sms,facebook,twitter,referral"
                .setFeature("sharing")
                .addControlParameter("referralId", code);
        branchUniversalObject.generateShortUrl(mActivity, linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    Log.i("MyApp", "got my Branch link to share: " + url);

                    switch (channelId) {
                        case CHANNEL_EMAIL:
                            referFriendViaEmail(url);
                            break;

                        case CHANNEL_SMS:
                            sendSMS(url);
                            break;

                        case CHANNEL_FACEBOOK:
                            shareFaceBook(url);
                            break;

                        case CHANNEL_TWITTER:
                            Utils.shareTwitter(mActivity,
                                    String.format(mActivity.getString(R.string.refer_friend_twitter),
                                    url), null);
                            break;

                        case CHANNEL_REFERRAL:
                            txtLink.setText(url);
                            lay_link.setEnabled(true);
                            break;
                    }
                }
            }
        });
    }

    private void sendSMS(String url) {
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body", String.format(mActivity.getString(R.string.send_mail_body),
                url));
        mActivity.startActivityForResult(smsIntent, 112);
    }


    private void referFriendViaEmail(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
        intent.putExtra(Intent.EXTRA_SUBJECT, mActivity.getString(R.string.send_mail_subject));
        intent.putExtra(Intent.EXTRA_TEXT, String.format(mActivity.getString(R.string.send_mail_body),
                url));
        mActivity.startActivityForResult(Intent.createChooser(intent, ""), 111);
    }


    public void shareFaceBook(String url) {
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
//                String mess = mActivity.getResources().getString(R.string.shared_via_facebook);
//                Log.d("TAG", mess);
//                alertDialog.setTitleDialog("");
//                alertDialog.setMessageDialog(mess);
//                alertDialog.show();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(mActivity.getString(R.string.send_mail_subject))
                    .setContentDescription(
                            String.format(mActivity.getString(R.string.send_mail_body),
                                    url))
                    .setContentUrl(Uri.parse(url))
                    .build();
            shareDialog.show(linkContent);
        }
    }


}

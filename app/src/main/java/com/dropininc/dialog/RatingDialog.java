package com.dropininc.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.ContactUsActivity;
import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.UserType;
import com.dropininc.model.RateStreamModel;
import com.dropininc.network.request.RatingRequest;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;


public class RatingDialog extends BaseDialog implements View.OnClickListener {
    public interface Callback {
        void onFinish();
        void onRatingComplete();
    }

    private Context mContext;

    private TextView mTextTitle, mTextTitleTime, mTextValueTime,
            mTextTitleCost, mTextValueCost, mTextTitleRate;
    private ImageView mImageOne, mImageTwo, mImageThree, mImageFour, mImageFive;
    private ProgressBar mProgressBar;

    private Callback mCallback;

    private boolean mEnableClick = true;
    private String mRatingId;
    private String mGigsId;
    private int mUserType;
    private int mRate;

    LinearLayout lay_share;
    LinearLayout lay_rate;
    ImageView imgClose;
    TextView tv_title_share;
    Button btnSendFeedBack;
    LinearLayout lay_share_item;
    LinearLayout layFacebook;
    LinearLayout layTwitter;
    LinearLayout layEmail;
    LinearLayout laySMS;
    @Bind(R.id.buttonClose)
    Button buttonClose;
    @Bind(R.id.tv_title_rate)
    TextView textViewTitleRate;
    @Bind(R.id.lay_star)
    RelativeLayout layoutStar;

    private String videoUrl = "";

    public MixpanelAPI mixpanel;

    private String costShare = "";

    public RatingDialog(Context context) {
        super(context, android.R.style.Theme_Translucent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = context;

        setContentView(R.layout.dialog_rating);
        ButterKnife.bind(this, this);
        setCancelable(true);

        mixpanel = MixpanelAPI.getInstance(context, com.dropininc.Constants.MIXPANEL_TOKEN);

        setupView();
    }

    @Override
    public void dismiss() {
        ButterKnife.unbind(this);
        super.dismiss();
    }

    @OnClick(R.id.buttonClose)
    public void onCloseClick() {
        dismiss();
        if (mCallback != null) {
            mCallback.onRatingComplete();
        }
    }

    @Override
    public void onClick(View v) {
        if (mEnableClick) {
            switch (v.getId()) {
                case R.id.img_star_one:
                    mRate = 1;
                    mImageOne.setImageResource(R.drawable.ic_star_green);
                    break;
                case R.id.img_star_two:
                    mRate = 2;
                    mImageOne.setImageResource(R.drawable.ic_star_green);
                    mImageTwo.setImageResource(R.drawable.ic_star_green);
                    break;
                case R.id.img_star_three:
                    mRate = 3;
                    mImageOne.setImageResource(R.drawable.ic_star_green);
                    mImageTwo.setImageResource(R.drawable.ic_star_green);
                    mImageThree.setImageResource(R.drawable.ic_star_green);
                    break;
                case R.id.img_star_four:
                    mRate = 4;
                    mImageOne.setImageResource(R.drawable.ic_star_green);
                    mImageTwo.setImageResource(R.drawable.ic_star_green);
                    mImageThree.setImageResource(R.drawable.ic_star_green);
                    mImageFour.setImageResource(R.drawable.ic_star_green);
                    break;
                case R.id.img_star_five:
                    mRate = 5;
                    mImageOne.setImageResource(R.drawable.ic_star_green);
                    mImageTwo.setImageResource(R.drawable.ic_star_green);
                    mImageThree.setImageResource(R.drawable.ic_star_green);
                    mImageFour.setImageResource(R.drawable.ic_star_green);
                    mImageFive.setImageResource(R.drawable.ic_star_green);
                    break;
            }
            mEnableClick = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rateStream();
                }
            }, 0);

//            actionAfterRate();
        }
        switch (v.getId()) {
            case R.id.laySMS:
                BranchUniversalObject branchUniversalObject2 = new BranchUniversalObject();
                LinkProperties linkProperties2 = new LinkProperties()
                        .setChannel("sms") // "email,sms,facebook,twitter,referral"
                        .setFeature("sharing")
                        .addControlParameter("referralId", "http://example.com/home"); // referralId
                branchUniversalObject2.generateShortUrl(mContext, linkProperties2, new Branch.BranchLinkCreateListener() {
                    @Override
                    public void onLinkCreate(String url, BranchError error) {
                        if (error == null) {
                            Log.i("MyApp", "got my Branch link to share: " + url);
                            sendSMS(url);
                        }
                    }
                });
                break;
            case R.id.layEmail:
                BranchUniversalObject branchUniversalObject = new BranchUniversalObject();
                LinkProperties linkProperties = new LinkProperties()
                        .setChannel("email") // "email,sms,facebook,twitter,referral"
                        .setFeature("sharing")
                        .addControlParameter("referralId", "http://example.com/home"); // referralId
                branchUniversalObject.generateShortUrl(mContext, linkProperties, new Branch.BranchLinkCreateListener() {
                    @Override
                    public void onLinkCreate(String url, BranchError error) {
                        if (error == null) {
                            Log.i("MyApp", "got my Branch link to share: " + url);
                            referFriendViaEmail(url);
                        }
                    }
                });
                break;
            case R.id.layTwitter:
                BranchUniversalObject branchUniversalObject4 = new BranchUniversalObject();
                LinkProperties linkProperties4 = new LinkProperties()
                        .setChannel("twitter") // "email,sms,facebook,twitter,referral"
                        .setFeature("sharing")
                        .addControlParameter("referralId", "http://example.com/home"); // referralId
                branchUniversalObject4.generateShortUrl(mContext, linkProperties4, new Branch.BranchLinkCreateListener() {
                    @Override
                    public void onLinkCreate(String url, BranchError error) {
                        if (error == null) {
                            Log.i("MyApp", "got my Branch link to share: " + url);
                            String mess = String.format(mContext.getString(R.string.twitter_sharing),
                                    url);
                            if (mUserType == UserType.OPERATOR) {
                                mess = String.format(mContext.getString(R.string.twitter_sharing_droper),
                                        costShare, url);
                            }
                            ((MainActivity) mContext).shareTwitter(mess);
                        }
                    }
                });
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeDialog();
//                    }
//                }, 2000);

                break;
            case R.id.layFacebook:
                BranchUniversalObject branchUniversalObject3 = new BranchUniversalObject();
                LinkProperties linkProperties3 = new LinkProperties()
                        .setChannel("facebook") // "email,sms,facebook,twitter,referral"
                        .setFeature("sharing")
                        .addControlParameter("referralId", "http://example.com/home"); // referralId
                branchUniversalObject3.generateShortUrl(mContext, linkProperties3, new Branch.BranchLinkCreateListener() {
                    @Override
                    public void onLinkCreate(String url, BranchError error) {
                        if (error == null) {
                            Log.i("MyApp", "got my Branch link to share: " + url);
                            ((MainActivity) mContext).shareFaceBook(url);
                        }
                    }
                });
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeDialog();
//                    }
//                }, 2000);
                break;
            case R.id.imgClose:
                closeDialog();
                break;
            case R.id.btnSendFeedBack:
                Intent intent = new Intent(mContext, ContactUsActivity.class);
                mContext.startActivity(intent);
                closeDialog();
                break;

        }
    }

    private void closeDialog() {
        dismiss();
        if (mCallback != null) {
            mCallback.onFinish();
        }
    }

    private void sendSMS(String url) {
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        String mess = "";
        if (mUserType == UserType.VIEWER) {
            mess = mContext.getString(R.string.send_mail_subject) + "\n";
            mess = mess + String.format(mContext.getString(R.string.viewer_sharing_mess),
                    url);
            if (!TextUtils.isEmpty(videoUrl)) {
                mess = mess + String.format(mContext.getString(R.string.viewer_sharing_video),
                        videoUrl);
            }
            mess = mess + mContext.getString(R.string.enjoy);
        } else {
            mess = mContext.getString(R.string.send_mail_subject);
            mess = mess + String.format(mContext.getString(R.string.droper_sharing_mess),
                    costShare, url);
            mess = mess + mContext.getString(R.string.enjoy);
        }
        smsIntent.putExtra("sms_body", mess);
        ((MainActivity) mContext).startActivityForResult(smsIntent, 112);
    }

    private void referFriendViaEmail(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
        if (mUserType == UserType.VIEWER) {
            intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.send_mail_subject));
            String mess = String.format(mContext.getString(R.string.viewer_sharing_mess),
                    url);
            if (!TextUtils.isEmpty(videoUrl)) {
                mess = mess + String.format(mContext.getString(R.string.viewer_sharing_video),
                        videoUrl);
            }
            mess = mess + mContext.getString(R.string.enjoy);
            intent.putExtra(Intent.EXTRA_TEXT, mess);
        } else {
            intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.send_mail_subject));
            String mess = String.format(mContext.getString(R.string.droper_sharing_mess),
                    costShare, url);
            mess = mess + mContext.getString(R.string.enjoy);
            intent.putExtra(Intent.EXTRA_TEXT, mess);
        }

        ((MainActivity) mContext).startActivityForResult(Intent.createChooser(intent, ""), 111);
    }

    private void setupView() {
        mTextTitle = (TextView) findViewById(R.id.tv_title);
        mTextTitleTime = (TextView) findViewById(R.id.tv_title_time);
        mTextValueTime = (TextView) findViewById(R.id.tv_value_time);
        mTextTitleCost = (TextView) findViewById(R.id.tv_title_cost);
        mTextValueCost = (TextView) findViewById(R.id.tv_value_cost);
        mTextTitleRate = (TextView) findViewById(R.id.tv_title_rate);

        mImageOne = (ImageView) findViewById(R.id.img_star_one);
        mImageTwo = (ImageView) findViewById(R.id.img_star_two);
        mImageThree = (ImageView) findViewById(R.id.img_star_three);
        mImageFour = (ImageView) findViewById(R.id.img_star_four);
        mImageFive = (ImageView) findViewById(R.id.img_star_five);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        lay_share = (LinearLayout) findViewById(R.id.lay_share);
        lay_rate = (LinearLayout) findViewById(R.id.lay_rate);
        imgClose = (ImageView) findViewById(R.id.imgClose);
        tv_title_share = (TextView) findViewById(R.id.tv_title_share);
        btnSendFeedBack = (Button) findViewById(R.id.btnSendFeedBack);
        lay_share_item = (LinearLayout) findViewById(R.id.lay_share_item);
        layFacebook = (LinearLayout) findViewById(R.id.layFacebook);
        layTwitter = (LinearLayout) findViewById(R.id.layTwitter);
        layEmail = (LinearLayout) findViewById(R.id.layEmail);
        laySMS = (LinearLayout) findViewById(R.id.laySMS);

        laySMS.setOnClickListener(this);
        layEmail.setOnClickListener(this);
        layTwitter.setOnClickListener(this);
        layFacebook.setOnClickListener(this);
        imgClose.setOnClickListener(this);
        btnSendFeedBack.setOnClickListener(this);

        mImageOne.setOnClickListener(this);
        mImageTwo.setOnClickListener(this);
        mImageThree.setOnClickListener(this);
        mImageFour.setOnClickListener(this);
        mImageFive.setOnClickListener(this);

        TextView tv_title_share = (TextView) findViewById(R.id.tv_title_share);
        FontUtils.typefaceTextView(tv_title_share, FontType.REGULAR);
        TextView tv_facebook = (TextView) findViewById(R.id.tv_facebook);
        FontUtils.typefaceTextView(tv_facebook, FontType.BOLD);
        TextView tv_twitter = (TextView) findViewById(R.id.tv_twitter);
        FontUtils.typefaceTextView(tv_twitter, FontType.BOLD);
        TextView tv_email = (TextView) findViewById(R.id.tv_email);
        FontUtils.typefaceTextView(tv_email, FontType.BOLD);
        TextView tv_sms = (TextView) findViewById(R.id.tv_sms);
        FontUtils.typefaceTextView(tv_sms, FontType.BOLD);

        FontUtils.typefaceTextView(mTextTitle, FontType.BOLD);
        FontUtils.typefaceTextView(mTextTitleTime, FontType.REGULAR);
        FontUtils.typefaceTextView(mTextValueTime, FontType.BOLD);
        FontUtils.typefaceTextView(mTextTitleCost, FontType.REGULAR);
        FontUtils.typefaceTextView(mTextValueCost, FontType.BOLD);
        FontUtils.typefaceTextView(mTextTitleRate, FontType.REGULAR);

        FontUtils.typefaceButton(btnSendFeedBack, FontType.REGULAR);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setUserType(int userType) {
        mUserType = userType;
        if (userType == UserType.VIEWER) {
            mTextTitle.setText(R.string.thanks_for_dropping_in);
            mTextTitleRate.setText(R.string.rate_your_droperator);
            mTextTitleCost.setText(R.string.total_cost);
        } else {
            mTextTitle.setText(R.string.request_complete);
            mTextTitleRate.setText(R.string.rate_your_viewer);
            mTextTitleCost.setText(R.string.total_amount_charged);
        }
    }

    public void setTimeDuration(int minute, int second) {
        String text = "%smin %ssec";
        text = String.format(text, minute, second);
        mTextValueTime.setText(text);
    }

    public void setTimeDuration(String time) {
        mTextValueTime.setText(time);
    }

    public void setTotalCost(String totalCost) {
        String text = "$%s";
        text = String.format(text, totalCost);
        String[] temp = text.split("\\.");
        if (temp.length == 2) {
            if (temp[1].length() == 1) {
                text = text + "0";
            }
        }
        costShare = text;
        mTextValueCost.setText(text);
    }

    public void setRatingId(String ratingId, String gigsId) {
        mRatingId = ratingId;
        mGigsId = gigsId;
    }

    public void setVideoUrl(String url) {
        this.videoUrl = url;
    }

    public void setBypassRating(boolean bypassRating) {
        if (bypassRating) {
            textViewTitleRate.setVisibility(View.GONE);
            layoutStar.setVisibility(View.GONE);
            buttonClose.setVisibility(View.VISIBLE);
            mTextTitle.setText(R.string.stream_cancelled_title_rating_dialog);
        }
    }

    private void rateStream() {
        mProgressBar.setVisibility(View.VISIBLE);

        networkManager.rateStream(mGigsId, new RatingRequest(mUserType == UserType.VIEWER ? "viewer" : "operator", mRate))
                .subscribe(this::processResponseRateStream, throwable -> {
                    AppApplication.getInstance().logErrorServer("rateStream/" + mGigsId, networkManager.parseError(throwable));
                    processResponseRateStream(new RateStreamModel());
                });

        try {
            JSONObject props = new JSONObject();
            props.put("gigsId", mGigsId);
            if (mUserType == UserType.VIEWER) {
                props.put("Rated By", "Viewer");
            } else {
                props.put("Rated By", "Droperator");
            }
            mixpanel.track("Stream - Rated", props);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void processResponseRateStream(RateStreamModel model) {
//        dismiss();
//        if (mCallback != null) {
//            mCallback.onFinish();
//        }
        actionAfterRate();
    }

    private void actionAfterRate() {
        mProgressBar.setVisibility(View.GONE);
        lay_rate.setVisibility(View.GONE);
        lay_share.setVisibility(View.VISIBLE);

        // title
        if (mRate <= 3) {
            tv_title_share.setText(mContext.getString(R.string.your_satisfaction));
            btnSendFeedBack.setVisibility(View.VISIBLE);
        } else {
            if (mUserType == UserType.VIEWER) {
                tv_title_share.setText(mContext.getString(R.string.viewer_sharing_title));
            } else {
                tv_title_share.setText(mContext.getString(R.string.you_just_earned));
            }
            lay_share_item.setVisibility(View.VISIBLE);
        }

        if (mCallback != null) {
            mCallback.onRatingComplete();
        }
    }
}

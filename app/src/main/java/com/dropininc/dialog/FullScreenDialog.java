package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;

public class FullScreenDialog extends Dialog {
    private Context mContext;
    private ImageView mImageView;
    private Button mButton, mButtonBottom, mButtonCenter;
    private RelativeLayout mLayItem;
    private int mUserType = -1;

    public FullScreenDialog(Context mContext) {
        super(mContext, android.R.style.Theme_Translucent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = mContext;

        setContentView(R.layout.dialog_full_screen);

        mImageView = (ImageView) findViewById(R.id.img_bg);
        mButton = (Button) findViewById(R.id.bt_close);
        mButtonCenter = (Button) findViewById(R.id.bt_close_center);
        mButtonBottom = (Button) findViewById(R.id.bt_bottom);
        mLayItem = (RelativeLayout) findViewById(R.id.lay_item);

        setCanceledOnTouchOutside(false);
        setCancelable(true);

        FontUtils.typefaceButton(mButton, FontType.BOLD);
        FontUtils.typefaceButton(mButtonCenter, FontType.BOLD);
        FontUtils.typefaceButton(mButtonBottom, FontType.LIGHT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void dismiss() {
        if (mUserType != -1) {
            DSharePreference.setHelpTutorial(mContext, false, mUserType);
        }
        super.dismiss();
    }

    public void setTextButton(String text) {
        mButton.setText(text);
    }

    public void setTextButton(int resourceId) {
        mButton.setText(resourceId);
    }

    public void setButtonListener(View.OnClickListener mListener) {
        mButton.setOnClickListener(mListener);
        mLayItem.setOnClickListener(mListener);
    }

    public void setBackgroundImage(int resourceId) {
        mImageView.setImageResource(resourceId);
    }

    public void setUserType(int mUserType) {
        this.mUserType = mUserType;
    }

    public void setTextButtonBottom(String text) {
        mButtonBottom.setText(text);
    }

    public void setTextButtonBottom(int resourceId) {
        mButtonBottom.setText(resourceId);
    }

    public void setButtonBottomListener(View.OnClickListener mListener) {
        mButtonBottom.setOnClickListener(mListener);
    }

    public void setButtonBottomVisibility(int visibility) {
        mButtonBottom.setVisibility(visibility);
    }

    public void setButtonCenterListener(View.OnClickListener mListener) {
        mButtonCenter.setOnClickListener(mListener);
        mLayItem.setOnClickListener(mListener);
    }

    public void setButtonVisibility(int visibility) {
        mButton.setVisibility(visibility);
    }

    public void setButtonCenterVisibility(int visibility) {
        mButtonCenter.setVisibility(visibility);
    }

    public void setBackgroundLayItem() {

    }
}

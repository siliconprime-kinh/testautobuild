package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.customview.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SelectOperatorDialog extends Dialog{
    private Context mContext;
    private CircleImageView mImageAvatar;
    private Button mButtonClose, mButtonRequest;
    private TextView mTextName;

    private ImageLoader mImageLoader;

    public SelectOperatorDialog(Context mContext) {
        super(mContext, android.R.style.Theme_Translucent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = mContext;

        setContentView(R.layout.dialog_select_operator);

        mImageAvatar = (CircleImageView)findViewById(R.id.img_avatar);
        mButtonRequest = (Button)findViewById(R.id.bt_request);
        mButtonClose = (Button)findViewById(R.id.bt_close);
        mTextName = (TextView)findViewById(R.id.tv_name);

        setCanceledOnTouchOutside(false);

        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void setButtonListener(View.OnClickListener mListener){
        mButtonClose.setOnClickListener(mListener);
    }

    public void setButtonBottomListener(View.OnClickListener mListener){
        mButtonRequest.setOnClickListener(mListener);
    }

    public void setTextName(String text){
        mTextName.setText(text);
    }
}

package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;


public class HelpFullScreenDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private ImageView mImageView;
    public static int DROPERATOR = 1;
    public static int VIEWER = 2;

    int type = 1;

    public HelpFullScreenDialog(final Context mContext, int userType) {
        super(mContext, android.R.style.Theme_Translucent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = mContext;
        type = userType;

        setContentView(R.layout.dialog_help_full_screen);

        mImageView = (ImageView) findViewById(R.id.img_bg);

        setCanceledOnTouchOutside(true);
        setCancelable(true);

        mImageView.setOnClickListener(this);

        Button button = (Button) findViewById(R.id.button);
        if (type == DROPERATOR) {
            button.setText(mContext.getResources().getString(R.string.start));
        } else {
            button.setText(mContext.getResources().getString(R.string.close));
        }
        button.setOnClickListener(this);

        FontUtils.typefaceButton(button, FontType.LIGHT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setType(int helpType) {
        type = helpType;
    }

    public void setBackgroundImage(int resourceId) {
        mImageView.setImageResource(resourceId);
    }


    @Override
    public void onClick(View view) {
        if (type == DROPERATOR) {
            DSharePreference.setIsShowDroperatorHelp(mContext, false);
        } else {
            DSharePreference.setIsShowViewerHelp(mContext, false);
        }
        dismiss();
    }
}

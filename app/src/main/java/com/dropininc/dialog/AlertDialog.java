package com.dropininc.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;


public class AlertDialog extends BaseDialog {
    Context mContext;

    TextView tv_title;
    TextView tv_message;
    public TextView tv_url;
    Button bt_cancel;
    RelativeLayout mLayButton;

    public AlertDialog(Context context) {
        super(context, R.style.Theme_Transparent);
        this.mContext = context;


        setContentView(R.layout.dialog_alert);
        setCancelable(true);

        setupView();

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();

        int margin_window = (int) getContext().getResources().getDimension(R.dimen.dialog_margin_window);
        lp.copyFrom(window.getAttributes());
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.width = displayMetrics.widthPixels - margin_window * 12;
        } else {
            lp.width = displayMetrics.widthPixels - margin_window * 12 * 3;
        }
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.dimAmount = 0.85f;
        window.setAttributes(lp);
    }

    public void hideButtonCancel() {
        if (bt_cancel != null) {
            bt_cancel.setVisibility(View.GONE);
        }
    }

    private void setupView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_message = (TextView) findViewById(R.id.tv_message);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        mLayButton = (RelativeLayout) findViewById(R.id.lay_single_button);
        tv_url = (TextView) findViewById(R.id.tv_url);
        tv_url.setPaintFlags(tv_url.getPaintFlags()
                | Paint.UNDERLINE_TEXT_FLAG);
        tv_url.setTypeface(null, Typeface.ITALIC);
        tv_url.setVisibility(View.GONE);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        FontUtils.typefaceTextView(tv_title, FontType.BOLD);
        FontUtils.typefaceTextView(tv_message, FontType.REGULAR);
        FontUtils.typefaceButton(bt_cancel, FontType.LIGHT);
    }

    public void setLinkClicked(View.OnClickListener listener) {
        tv_url.setVisibility(View.VISIBLE);
        tv_url.setOnClickListener(listener);
    }

    public void setTitleDialog(String title) {
        tv_title.setText(title);
        if (TextUtils.isEmpty(title)) {
            tv_title.setVisibility(View.GONE);
        }
    }

    public void setTitleDialog(int titleId) {
        tv_title.setText(titleId);
        if (titleId == 0) {
            tv_title.setVisibility(View.GONE);
        }
    }

    public void setMessageDialog(String message) {
        tv_message.setText(message);
        if (TextUtils.isEmpty(message)) {
            tv_message.setVisibility(View.GONE);
        }
    }

    public void setMessageDialog(int messageId) {
        tv_message.setText(messageId);
        if (messageId == 0) {
            tv_message.setVisibility(View.GONE);
        }
    }

    public void setButtonClick(View.OnClickListener listener) {
        bt_cancel.setOnClickListener(listener);
    }

    public void setTitleButton(String title) {
        bt_cancel.setText(title);
    }

    public void setTitleButton(int titleId) {
        bt_cancel.setText(titleId);
    }

    public void hideButton(int visibility) {
        mLayButton.setVisibility(visibility);
    }

    public void setCancelableDialog(boolean flag) {
        setCancelable(flag);
    }
}

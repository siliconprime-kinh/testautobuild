package com.dropininc.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;


public class ConfirmDialog extends BaseDialog {
	Context mContext;

	TextView tv_title;
	TextView tv_message;
    Button bt_ok;
    Button bt_cancel;

	public ConfirmDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		this.mContext = context;

		setContentView(R.layout.dialog_confirm);
		setCancelable(true);

		setupView();

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();

        int margin_window = (int)getContext().getResources().getDimension(R.dimen.dialog_margin_window);
        lp.copyFrom(window.getAttributes());
		int orientation = mContext.getResources().getConfiguration().orientation;
		if(orientation == Configuration.ORIENTATION_PORTRAIT){
			lp.width = displayMetrics.widthPixels - margin_window * 12;
		}else{
			lp.width = displayMetrics.widthPixels - margin_window * 12 * 3;
		}
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.dimAmount = 0.85f;
        window.setAttributes(lp);
	}

	private void setupView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_message = (TextView) findViewById(R.id.tv_message);
		bt_ok = (Button) findViewById(R.id.bt_ok);
		bt_cancel = (Button) findViewById(R.id.bt_cancel);

		bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		FontUtils.typefaceTextView(tv_title, FontType.REGULAR);
		FontUtils.typefaceTextView(tv_message, FontType.LIGHT);
		FontUtils.typefaceButton(bt_cancel, FontType.LIGHT);
		FontUtils.typefaceButton(bt_ok, FontType.LIGHT);
	}

	public void setTitleOk(String title) {
		bt_ok.setText(title);
	}

	public void setTitleCancel(String title) {
		bt_cancel.setText(title);
	}

	public void setTitleDialog(String title) {
		tv_title.setText(title);
        if(TextUtils.isEmpty(title)){
            tv_title.setVisibility(View.GONE);
        }
	}

	public void setTitleDialog(int titleId) {
		tv_title.setText(titleId);
        if(titleId == 0){
            tv_title.setVisibility(View.GONE);
        }
	}

	public void setMessageDialog(String message) {
		tv_message.setText(message);
		if(TextUtils.isEmpty(message)){
			tv_message.setVisibility(View.GONE);
		}
	}

	public void setMessageDialog(int messageId) {
		tv_message.setText(messageId);
		if(messageId == 0){
			tv_message.setVisibility(View.GONE);
		}
	}

	public void setOkButtonClick(View.OnClickListener listener) {
		bt_ok.setOnClickListener(listener);
	}

	public void setCancelButtonClick(View.OnClickListener listener) {
		if (listener != null) {
			bt_cancel.setOnClickListener(listener);
		}
	}

	public void setOkTitleButton(String title) {
		bt_ok.setText(title);
	}

	public void setOkTitleButton(int titleId) {
		bt_ok.setText(titleId);
	}

	public void setCancelTitleButton(String title) {
		bt_cancel.setText(title);
	}

	public void setCancelTitleButton(int titleId) {
		bt_cancel.setText(titleId);
	}

	public void setMessageTextColor(int color){
		tv_message.setTextColor(color);
	}

	public void setButtonOkVisibility(int visibility){
		bt_ok.setVisibility(visibility);
	}

	public void setButtonCancelVisibility(int visibility){
		bt_cancel.setVisibility(visibility);
	}

	public void setCancelableDialog(boolean flag){
		setCancelable(flag);
	}
}

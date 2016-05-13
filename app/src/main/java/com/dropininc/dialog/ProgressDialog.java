package com.dropininc.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;


public class ProgressDialog extends BaseDialog {
	Context mContext;
	ProgressBar mProgressBar;
	TextView tv_loading;

	public ProgressDialog(Context context) {
        super(context, R.style.Theme_DialogCustom);
		this.mContext = context;
		setContentView(R.layout.dialog_loading);
		setCancelable(true);
		tv_loading = (TextView) findViewById(R.id.tv_loading);
		mProgressBar = (ProgressBar)findViewById(R.id.pb_loading);

		FontUtils.typefaceTextView(tv_loading, FontType.REGULAR);
	}

	public void showDialog(String message,
			OnCancelListener listener) {
		if (!TextUtils.isEmpty(message)) {
			tv_loading.setVisibility(View.VISIBLE);
		} else {
			tv_loading.setVisibility(View.GONE);
		}
		tv_loading.setText(message);
		setOnCancelListener(listener);
		show();
	}

	public void setMessage(String message) {
		tv_loading.setText(message);
	}

	public void setMessage(int resourceId){
		tv_loading.setText(resourceId);
	}

	public void setIconLoading(int resourceId){
		mProgressBar.setIndeterminateDrawable(mContext.getResources().getDrawable(resourceId));
	}

	public void setCancelListener(OnCancelListener listener){
		setOnCancelListener(listener);
	}
}

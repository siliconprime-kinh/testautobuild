package com.dropininc.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;


public class AutoDialog extends BaseDialog {
	private Context mContext;

	private TextView tv_title;
	private TextView tv_message;

	private int mTimeDelay;
	private Runnable mRunnable;

	private boolean mAutoDismiss = true;

    public AutoDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		this.mContext = context;


        setContentView(R.layout.dialog_auto);
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
			lp.width = displayMetrics.widthPixels - margin_window * 12 * 2;
		}
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
	}

	@Override
	public void show() {
		super.show();
		if(mAutoDismiss){
			if(mRunnable == null){
				mRunnable = new Runnable() {
					@Override
					public void run() {
						dismiss();
					}
				};
			}
			new Handler().postDelayed(mRunnable, mTimeDelay);
		}
	}

	private void setupView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_message = (TextView) findViewById(R.id.tv_message);

		FontUtils.typefaceTextView(tv_title, FontType.REGULAR);
		FontUtils.typefaceTextView(tv_message, FontType.LIGHT);
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

	public void setTimeAutoDismiss(int timeDelay){
		mTimeDelay = timeDelay;
	}

	public void setDialogDismissCallback(Runnable mRunnable){
		this.mRunnable = mRunnable;
	}

	public void setAutoDismiss(boolean mAutoDismiss){
		this.mAutoDismiss = mAutoDismiss;
	}
}

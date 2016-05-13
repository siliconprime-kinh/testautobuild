package com.dropininc.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;


public class AutoIconDialog extends BaseDialog {
	private Context mContext;

	private ImageView mImageIcon;
	private TextView tv_message;

	private int mTimeDelay;
	private Runnable mRunnable;

    public AutoIconDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		this.mContext = context;


        setContentView(R.layout.dialog_auto_icon);
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
		if(mRunnable == null){
			mRunnable = new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			};
		}
		new Handler().postDelayed(mRunnable, mTimeDelay);
		super.show();
	}

	private void setupView() {
		mImageIcon = (ImageView) findViewById(R.id.img_icon);
		tv_message = (TextView) findViewById(R.id.tv_message);

		FontUtils.typefaceTextView(tv_message, FontType.LIGHT);
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

	public void setImageIcon(int resId){
		mImageIcon.setImageResource(resId);
	}

	public void setDialogDismissCallback(Runnable mRunnable){
		this.mRunnable = mRunnable;
	}
}

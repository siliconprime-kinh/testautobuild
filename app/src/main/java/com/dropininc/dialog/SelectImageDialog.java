package com.dropininc.dialog;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.interfaces.PictureCallback;
import com.dropininc.utils.FontUtils;


public class SelectImageDialog extends BaseDialog implements View.OnClickListener{
	private Context mContext;

	private TextView mTextTitle;
	private TextView mTextGallery, mTextCamera;
	private Button mButton;

    private PictureCallback mPictureCallback;

    public SelectImageDialog(Context context) {
		super(context, R.style.Theme_Transparent);
		this.mContext = context;


        setContentView(R.layout.dialog_select_image);
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

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.tv_from_gallery:
                mPictureCallback.fromGalley();
				break;
			case R.id.tv_take_picture:
                mPictureCallback.fromCamera();
				break;
			case R.id.bt_cancel:
				break;
		}
        dismiss();
	}

	private void setupView() {
		mTextTitle = (TextView) findViewById(R.id.tv_title);
		mTextGallery = (TextView) findViewById(R.id.tv_from_gallery);
		mTextCamera = (TextView)findViewById(R.id.tv_take_picture);
		mButton = (Button) findViewById(R.id.bt_cancel);

		mTextGallery.setOnClickListener(this);
		mTextCamera.setOnClickListener(this);
		mButton.setOnClickListener(this);

		FontUtils.typefaceTextView(mTextTitle, FontType.REGULAR);
		FontUtils.typefaceTextView(mTextCamera, FontType.LIGHT);
		FontUtils.typefaceTextView(mTextGallery, FontType.LIGHT);
		FontUtils.typefaceButton(mButton, FontType.LIGHT);
	}

    public void setPictureCallback(PictureCallback mPictureCallback){
        this.mPictureCallback = mPictureCallback;
    }
}

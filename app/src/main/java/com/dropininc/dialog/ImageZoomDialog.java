package com.dropininc.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dropininc.R;
import com.dropininc.image.TouchImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ImageZoomDialog extends BaseDialog {

    @Bind(R.id.image)
    TouchImageView image;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.button_close)
    ImageView imgClose;
    @Bind(R.id.lay_close)
    RelativeLayout layClose;

    private Context mContext;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public ImageZoomDialog(Context context) {
        super(context, R.style.DialogFullScreenTheme);
        this.mContext = context;

        setContentView(R.layout.dialog_image);
        ButterKnife.bind(this, this);
        setCancelable(true);

        setupView();
    }


    @Override
    public void dismiss() {
        ButterKnife.unbind(this);
        super.dismiss();
    }

    @OnClick(R.id.lay_close)
    public void onCloseClick() {
        dismiss();
    }

    public void setImageResource(String url) {
        imageLoader.displayImage(url, image, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupView() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(150))
                .delayBeforeLoading(0).build();
    }
}

package com.dropininc.adapter;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public abstract class BaseAdapter extends android.widget.BaseAdapter {
    Context mContext;
    ImageLoader mImageLoader;
    DisplayImageOptions mDisplayImageOptions;

    public BaseAdapter(Context mContext){
        this.mContext = mContext;
        mImageLoader = ImageLoader.getInstance();
        mDisplayImageOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();
    }

    public ImageLoader getImageLoader(){
        return mImageLoader;
    }

    public DisplayImageOptions getDisplayImageOptions(){
        return mDisplayImageOptions;
    }
}

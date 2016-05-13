package com.dropininc.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CustomWebView extends WebView {
    private String TAG = getClass().getSimpleName();
    private Context mContext;
    private OnScrollListener mListener;
    private int mContentHeight;
    private boolean mOnScrollChanged = false;

    public CustomWebView(Context context) {
        super(context);
        mContext = context;
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//        View view = (View) getChildAt(getChildCount()-1);    getB
        if(!mOnScrollChanged){
            mOnScrollChanged = true;
        }
        int contentHeight = (int) Math.floor(getContentHeight() * getScale());
        if (mContentHeight <= 0) {
            mContentHeight = contentHeight;
        }
        int diff = (contentHeight - (getHeight() + getScrollY()));// Calculate the scrolldiff
        if (diff < 10) {  // if diff is zero, then the bottom has been reached
            mListener.onScrollDown();
        } else {
            mListener.onScrollUp();
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.mListener = listener;
    }

    public int getContentHeightWebView() {
        return mContentHeight;
    }

    public boolean isOnScrollChanged(){
        return mOnScrollChanged;
    }

    public interface OnScrollListener {
        void onScrollDown();

        void onScrollUp();
    }
}

package com.dropininc.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by VanNguyen
 */
public class FontUtils {


    public static void typefaceButton(Button view, String fontStyle) {
        if (view == null)
            return;
        Typeface typeface = view.getTypeface();
        int style = (typeface == null) ? Typeface.NORMAL : view.getTypeface()
                .getStyle();
        view.setTypeface(getTypeface(view.getContext(), fontStyle), style);
        view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static void typefaceTextView(TextView view, String fontStyle) {
        if (view == null)
            return;
        Typeface typeface = view.getTypeface();
        int style = (typeface == null) ? Typeface.NORMAL : view.getTypeface()
                .getStyle();
        view.setTypeface(getTypeface(view.getContext(), fontStyle), style);
        view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static void typefaceRadioButton(RadioButton view, String fontStyle) {
        if (view == null)
            return;
        Typeface typeface = view.getTypeface();
        int style = (typeface == null) ? Typeface.NORMAL : view.getTypeface()
                .getStyle();
        view.setTypeface(getTypeface(view.getContext(), fontStyle), style);
        view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static void typefaceEditText(EditText view, String fontStyle) {
        if (view == null)
            return;
        Typeface typeface = view.getTypeface();
        int style = (typeface == null) ? Typeface.NORMAL : view.getTypeface()
                .getStyle();
        view.setTypeface(getTypeface(view.getContext(), fontStyle), style);
        view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public static Typeface getTypeface(Context context, String fontStyle) {
        return Typeface.createFromAsset(context.getAssets(),
                "fonts/" + fontStyle);
    }
}

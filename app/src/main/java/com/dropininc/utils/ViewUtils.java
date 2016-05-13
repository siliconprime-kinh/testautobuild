package com.dropininc.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.widget.EditText;

import com.dropininc.R;

public class ViewUtils {

    public final static int HELVETICA_BC = 0;
    public final static int HELVETICA_C = 1;

    // using for set font: view.setTypeface(getTypeface(code));
    public static Typeface getTypeface(Context context, int typefaceValue) {
        Typeface typeface;
        switch (typefaceValue) {
            case HELVETICA_BC:
                typeface = Typeface.createFromAsset(context.getAssets(),
                        "Fonts/HelveticaNeueLTStd-BdCn.ttf");
                break;
            default:
                typeface = Typeface.createFromAsset(context.getAssets(),
                        "Fonts/ProximaNova-Regular.otf");
        }
        return typeface;
    }

    public static void setEditTextWhite(Context context, EditText view) {
        view.setBackgroundResource(R.drawable.bg_edittext_white);
        view.setPadding((int) convertDpToPixel(16, context), 0, (int) convertDpToPixel(16, context), 0);
    }

    public static void setEditTextRed(Context context, EditText view) {
        view.setBackgroundResource(R.drawable.bg_edittext_red);
        view.setPadding((int) convertDpToPixel(16, context), 0, (int) convertDpToPixel(16, context), 0);
    }

    public static void setEditTextGreen(Context context, EditText view) {
        view.setBackgroundResource(R.drawable.bg_edittext_green);
        view.setPadding((int) convertDpToPixel(16, context), 0, (int) convertDpToPixel(16, context), 0);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }


}

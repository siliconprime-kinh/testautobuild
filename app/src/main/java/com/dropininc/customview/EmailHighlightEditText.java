package com.dropininc.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 17.03.16.
 */
public class EmailHighlightEditText extends HighlightEditText {

    private final Pattern pattern = Patterns.EMAIL_ADDRESS;

    public EmailHighlightEditText(Context context) {
        super(context);
    }

    public EmailHighlightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmailHighlightEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EmailHighlightEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected boolean checkText() {
        Matcher matcher = pattern.matcher(getText().toString());
        return matcher.matches();
    }
}

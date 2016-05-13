package com.dropininc.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.dropininc.R;

/**
 * Created on 14.03.16.
 */
public class HighlightEditText extends EditText {

    private int minSymbols;
    private int maxSymbols;
    private Drawable defaultStateDrawable;
    private Drawable highlightedStateDrawable;
    private Drawable errorStateDrawable;
    private boolean required;
    private boolean trim;
    private boolean capitalized;
    protected State currentState = State.DEFAULT;
    private OnStateChangeListener onStateChangeListener;

    public HighlightEditText(Context context) {
        super(context);
        setDefaultStateDrawables();
    }

    public HighlightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, -1, 0);
    }

    public HighlightEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HighlightEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyle, int defStyleRes) {
        TypedArray a;
        if (defStyle == -1) {
            a = getContext().obtainStyledAttributes(attrs, R.styleable.HighlightEditText);
        } else {
            a = getContext().obtainStyledAttributes(attrs, R.styleable.HighlightEditText, defStyle, defStyleRes);
        }

        minSymbols = a.getInt(R.styleable.HighlightEditText_het_min_symbols, -1);
        maxSymbols = a.getInt(R.styleable.HighlightEditText_het_max_symbols, -1);
        Drawable parsedDefault = a.getDrawable(R.styleable.HighlightEditText_het_default_res_id);
        Drawable parsedHighlighted = a.getDrawable(R.styleable.HighlightEditText_het_highlighted_res_id);
        Drawable parsedError = a.getDrawable(R.styleable.HighlightEditText_het_error_res_id);
        required = a.getBoolean(R.styleable.HighlightEditText_het_required, false);
        trim = a.getBoolean(R.styleable.HighlightEditText_het_disable_trim, false);
        capitalized = a.getBoolean(R.styleable.HighlightEditText_het_capitalized, false);

        defaultStateDrawable = parsedDefault == null ? ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_white) : parsedDefault;
        highlightedStateDrawable = parsedDefault == null ? ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_green) : parsedHighlighted;
        errorStateDrawable = parsedDefault == null ? ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_red) : parsedError;

        a.recycle();
        setNewState(State.DEFAULT);
    }

    private void setDefaultStateDrawables() {
        defaultStateDrawable = ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_white);
        highlightedStateDrawable = ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_green);
        errorStateDrawable = ContextCompat.getDrawable(getContext(), R.drawable.bg_edittext_red);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (isRequired()) {
            setNewState(focused && currentState != State.HIGHLIGHTED ? State.ERROR : checkText() ? State.HIGHLIGHTED : State.ERROR);
        } else {
            setNewState(focused ? State.HIGHLIGHTED : checkText() ? State.DEFAULT : State.ERROR);
        }
        if (capitalized && !focused && getText().length() != 0) {
            String normalText = getText().toString();
            String capitalizedText = normalText.substring(0,1).toUpperCase() + normalText.substring(1);
            setText(capitalizedText);
        }
    }

    protected boolean checkText() {
        int length = getText().length();
        return !(minSymbols >= 0 && length < minSymbols) && !(maxSymbols >= 0 && length > maxSymbols);
    }

    @Override
    public Editable getText() {
        return isTrim()? new Editable.Factory().newEditable(super.getText().toString().trim()) : super.getText();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        setNewState(getText().length() == 0 ?
                isRequired() ? State.ERROR : State.DEFAULT :
                        checkText() ? State.HIGHLIGHTED : State.ERROR);
    }

    protected void setNewState(State state) {
        currentState = state;
        Drawable drawableToSet = null;
        switch (state) {
            case DEFAULT:
                drawableToSet = defaultStateDrawable;
                break;
            case HIGHLIGHTED:
                drawableToSet = highlightedStateDrawable;
                break;
            case ERROR:
                drawableToSet = errorStateDrawable;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawableToSet);
        } else {
            setBackgroundDrawable(drawableToSet);
        }

        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChanged(state);
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public int getMinSymbols() {
        return minSymbols;
    }

    public void setMinSymbols(int minSymbols) {
        this.minSymbols = minSymbols;
    }

    public int getMaxSymbols() {
        return maxSymbols;
    }

    public void setMaxSymbols(int maxSymbols) {
        this.maxSymbols = maxSymbols;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public Drawable getErrorStateDrawable() {
        return errorStateDrawable;
    }

    public void setErrorStateDrawable(Drawable errorStateDrawable) {
        this.errorStateDrawable = errorStateDrawable;
    }

    public Drawable getHighlightedStateDrawable() {
        return highlightedStateDrawable;
    }

    public void setHighlightedStateDrawable(Drawable highlightedStateDrawable) {
        this.highlightedStateDrawable = highlightedStateDrawable;
    }

    public Drawable getDefaultStateDrawable() {
        return defaultStateDrawable;
    }

    public void setDefaultStateDrawable(Drawable defaultStateDrawable) {
        this.defaultStateDrawable = defaultStateDrawable;
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public enum State {
        DEFAULT,
        HIGHLIGHTED,
        ERROR
    }

    public interface OnStateChangeListener {
        void onStateChanged(State state);
    }
}

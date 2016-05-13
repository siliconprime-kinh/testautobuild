package com.dropininc.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.dropininc.R;

/** Customizing AutoCompleteTextView to return Place Description   
 *  corresponding to the selected item
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

	private Drawable imgCloseButton = getResources().getDrawable(R.drawable.ic_clear);

	public CustomAutoCompleteTextView(Context context) {
		super(context);
		init();
	}

	public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		// Set bounds of the Clear button so it will look ok
		imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
		// There may be initial text in the field, so we may need to display the  button
		handleClearButton();
		//if the Close image is displayed and the user remove his finger from the button, clear it. Otherwise do nothing
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				CustomAutoCompleteTextView et = CustomAutoCompleteTextView.this;
				if (event.getX() > et.getWidth() - et.getPaddingRight() - imgCloseButton.getIntrinsicWidth()) {
					et.setText("");
					CustomAutoCompleteTextView.this.handleClearButton();
				}else{
					CustomAutoCompleteTextView.this.handleClearButton();
				}
				return false;
			}
		});

		this.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				CustomAutoCompleteTextView.this.handleClearButton();
			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
		});

		this.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					clearButtonWhenLostFocus();
				}else{
					handleClearButton();
				}
			}
		});
	}

	void handleClearButton() {
		if (this.getText().toString().equals("")){
			this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
		}else{
			if(this.isFocused()){
				this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], imgCloseButton, this.getCompoundDrawables()[3]);
			}
		}
	}

	void clearButtonWhenLostFocus(){
		this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
				this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
}

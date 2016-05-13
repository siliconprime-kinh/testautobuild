package com.dropininc.interfaces;

import android.content.DialogInterface;
import android.view.View.OnClickListener;

public interface BaseInterface {
	void showProgressDialog(String message,
								   DialogInterface.OnCancelListener onCancelListener);

	void hideProgressDialog();

	void showAlertDialog(String message);

	void showAlertDialog(String title, String message);

	void showAlertDialog(String title, String message, String titleButton);

	void showAlertDialog(String title, String message,
								OnClickListener onClick);

	void showAlertDialogNoButton(String title, String message);

	void hideAlertDialog();

	void showConfirmDialog(String title, String message,
								  OnClickListener onOk, OnClickListener onCancel);

	void hideConfirmDialog();

	boolean isOnline();

	void hideSoftKeyboard();

	void showConfirmDialog(String title, String message, String titleOk,
								  String titleCancel, OnClickListener onOk, OnClickListener onCancel);
}

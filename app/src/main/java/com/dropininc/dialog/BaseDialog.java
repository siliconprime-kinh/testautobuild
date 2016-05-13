package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;

import com.dropininc.AppApplication;
import com.dropininc.network.NetworkManager;

import javax.inject.Inject;

public class BaseDialog extends Dialog {

	@Inject
	protected NetworkManager networkManager;

	public BaseDialog(Context context, int theme) {
        super(context, theme);

		AppApplication.appComponent().inject(this);
	}

}

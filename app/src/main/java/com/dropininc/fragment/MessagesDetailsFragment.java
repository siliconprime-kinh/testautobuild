package com.dropininc.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.MessagesModel;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Utils;
import com.google.gson.Gson;


public class MessagesDetailsFragment extends BaseFragment {

    private FragmentActivity mActivity;
    private View mRootView;

    private TextView txtTitle;
    private TextView txtDate;
    private TextView txtSubject;
    private TextView txtContent;

    MessagesModel messagesModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString("MODEL");
            messagesModel = new Gson().fromJson(json, MessagesModel.class);
        }
        mActivity = getActivity();
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.messages));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_messages_details, container, false);

        initView();
        markMessagesAsRead();
        loadDataToView(messagesModel);
        return mRootView;
    }

    private void initView() {
        txtTitle = (TextView) mRootView.findViewById(R.id.txtTitle);
        txtDate = (TextView) mRootView.findViewById(R.id.txtDate);
        txtSubject = (TextView) mRootView.findViewById(R.id.txtSubject);
        txtContent = (TextView) mRootView.findViewById(R.id.txtContent);
        Button btnBack = (Button) mRootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> ((MainActivity) mActivity).popFragments());
        FontUtils.typefaceButton(btnBack, FontType.LIGHT);


        FontUtils.typefaceTextView(txtTitle, FontType.REGULAR);
        FontUtils.typefaceTextView(txtDate, FontType.REGULAR);
        FontUtils.typefaceTextView(txtSubject, FontType.BOLD);
        FontUtils.typefaceTextView(txtContent, FontType.REGULAR);
    }

    private void loadDataToView(MessagesModel data) {
        txtTitle.setText("From: " + data.sender.firstName + " "
                + (TextUtils.isEmpty(data.sender.lastName) ? "" : data.sender.lastName));
        txtDate.setText(Utils.formatStringDate("MM/dd/yy, hh:mm", data.updatedAt));
        txtSubject.setText(data.title);
        txtContent.setText(data.message);
    }

    private void markMessagesAsRead() {
        if (!messagesModel.status.equalsIgnoreCase("received")) {

            networkManager.markMessageAsRead(messagesModel.id)
                    .subscribe(ignore -> {
                    }, throwable -> {
                        AppApplication.getInstance().logErrorServer("markMessageAsRead" , networkManager.parseError(throwable));
                    });
        }
    }


}

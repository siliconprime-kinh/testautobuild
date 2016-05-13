package com.dropininc.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.Constants;
import com.dropininc.R;
import com.dropininc.adapter.ChatAdapter;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.ChatModel;
import com.dropininc.model.ChatSaveModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.network.request.SaveChatRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;


public class ChatDialog extends BaseDialog implements View.OnClickListener {

    final String TAG = "ChatDialog";

    public interface TakePhotoCallback {
        void onTakePhoto();

        void onHadNewMessages(boolean isHadNew);
    }

    ImageView btnCapture;
    RelativeLayout layHeader;
    ListView list;
    EditText txtChat;
    Button btnSend;
    TextView txtDes;
    TextView txtName;
    ImageView imgClose;
    private Context mContext;
    public MixpanelAPI mixpanel;

    TakePhotoCallback callBack;

    private ChatAdapter adapter;
    private Pubnub pubnub;
    private Pusher pusher;
    private PrivateChannel privateChannel;

    private String chatChannel;
    private String chatName;
    private String chatAvatar;
    private String userId;
    private String myFullName = "";
    private String myAvatar = "";
    private boolean isEnroute = true;
    private RelativeLayout lay_parent;
    private LinearLayout lay_chat;
    private ProgressBar pb_loading;
    private String gigId = "";

    private ArrayList<ChatModel> messages = new ArrayList<>();

    public ChatDialog(Context context, int theme) {
        super(context, theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        this. (context, R.style.DialogFullScreenTheme);
//        android.R.style.Theme_Translucent

        this.mContext = context;

        setContentView(R.layout.dialog_chat);
        setCancelable(true);

        mixpanel = MixpanelAPI.getInstance(context, Constants.MIXPANEL_TOKEN);

        // get User Info
        String json = DSharePreference.getProfile(mContext);
        VerifyModel model = new Gson().fromJson(json, VerifyModel.class);
        userId = model.account.id;
        myFullName = model.account.firstName + " " + model.account.lastName;
        if (model.account.profileImage != null && !TextUtils.isEmpty(model.account.profileImage.location)) {
            myAvatar = model.account.profileImage.location;
        }

        setupView();
    }

    public void setChatChanel(String chatChannel, String chatName, String chatAvatar, String gigIs) {
        this.chatChannel = chatChannel;
        this.chatName = chatName;
        this.chatAvatar = chatAvatar;
        this.gigId = gigIs;
        Log.d(TAG, "chatChannel: " + chatChannel);
        Log.d(TAG, "chatName: " + chatName);
        Log.d(TAG, "chatAvatar: " + chatAvatar);
        if (!TextUtils.isEmpty(chatChannel)) {
            try {
                messages = new ArrayList<>();
                setupPusher();
//                getHistoryChat();
            } catch (Exception e) {
                Logs.log(e);
            }
        }
        txtName.setText(chatName);
        adapter.setAvatar(chatAvatar);
    }

    public void setIsFromEnRoute(boolean isEnroute) {
        this.isEnroute = isEnroute;
        if (isEnroute) {
            btnCapture.setVisibility(View.GONE);
        } else {
            btnCapture.setVisibility(View.VISIBLE);
        }
    }

    public void setIsViewHistory(String chatChanel, String chatName, String gigId) {
        this.chatChannel = chatChanel;
        this.chatName = chatName;
        lay_chat.setVisibility(View.GONE);
        txtName.setText(chatName);
        pb_loading.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(this.chatChannel)) {
            try {
                messages = new ArrayList<>();
                getAllChat(gigId);
            } catch (Exception e) {
                Logs.log(e);
            }
        }
        adapter.setAvatar(this.chatAvatar);
    }

    public void setBackgroundTransparent(boolean isTransparent) {
        if (isTransparent) {
            lay_parent.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            layHeader.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            txtName.setGravity(Gravity.LEFT);
        } else {
            lay_parent.setBackgroundColor(mContext.getResources().getColor(R.color.bg_chat));
            layHeader.setBackgroundColor(mContext.getResources().getColor(R.color.navigation_bg_black));
            txtName.setGravity(Gravity.CENTER);
        }

        adapter.setTransparent(isTransparent);
    }

    public void setHeaderHeight() {
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) px);
        layHeader.setLayoutParams(layoutParams);
    }

    public void setTakePhotoCalBack(TakePhotoCallback callBack) {
        this.callBack = callBack;
        this.callBack.onHadNewMessages(false);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void setupView() {
        lay_parent = (RelativeLayout) findViewById(R.id.lay_parent);
        btnCapture = (ImageView) findViewById(R.id.btnCapture);
        layHeader = (RelativeLayout) findViewById(R.id.lay_header);
        list = (ListView) findViewById(R.id.list);
        txtChat = (EditText) findViewById(R.id.txtChat);
        btnSend = (Button) findViewById(R.id.btnSend);
        txtDes = (TextView) findViewById(R.id.txtDes);
        txtName = (TextView) findViewById(R.id.txtName);
        imgClose = (ImageView) findViewById(R.id.imgClose);
        lay_chat = (LinearLayout) findViewById(R.id.lay_chat);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);


        adapter = new ChatAdapter(mContext);
        adapter.setMyUserId(userId);
        list.setAdapter(adapter);

        btnCapture.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        imgClose.setOnClickListener(this);

        FontUtils.typefaceTextView(txtName, FontType.REGULAR);
        FontUtils.typefaceEditText(txtChat, FontType.REGULAR);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCapture:
                if (callBack != null) {
                    callBack.onTakePhoto();
                }
                dismiss();
                break;
            case R.id.btnSend:
                String msg = txtChat.getText().toString().trim();
                if (!TextUtils.isEmpty(msg)) {
                    ChatModel message = new ChatModel();
                    message.messageContent = msg;
                    message.userId = userId;
                    message.date = new Date().getTime();
                    message.avatarUrl = myAvatar;
                    message.fullName = myFullName;

                    sendMessagesAction(message);
                }
                break;
            case R.id.imgClose:
                dismiss();
                break;
        }
    }

    private void onTakePhoto() {
        dismiss();
        if (callBack != null) {
            callBack.onTakePhoto();
        }
    }

    private void getHistoryChat() {
        if (pb_loading != null)
            pb_loading.setVisibility(View.VISIBLE);
        if (pubnub != null) {
            pubnub.history(chatChannel, 100, false, new Callback() {
                @Override
                public void successCallback(String channel, final Object message) {
                    super.successCallback(channel, message);
                    Logs.log(TAG, "history|successCallback: channel: " + channel + " - message: " + message.toString());
                    if (mContext == null)
                        return;
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray = new JSONArray(message.toString());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Object o = jsonArray.get(i);
                                    if (o instanceof JSONArray) {
                                        Logs.log(TAG, "history jsonArray : " + jsonArray.get(i).toString());
                                        messages = ChatModel.fromJSONArray(jsonArray.get(i).toString());
                                        Logs.log(TAG, "history messages : " + messages.size());
                                        adapter.addAll(messages);
                                    }
                                }
                                if (pb_loading != null)
                                    pb_loading.setVisibility(View.GONE);
                            } catch (Exception e) {
                                Logs.log(e);
                            }
                        }
                    });
                }
            });
        }
    }

    public void sendPhoto(String photoUrl) {
        Log.d(TAG, "photoUrl: " + photoUrl);
        ChatModel message = new ChatModel();
        message.messageContent = photoUrl;
        message.userId = userId;
        message.date = new Date().getTime();
        message.avatarUrl = myAvatar;
        message.fullName = myFullName;
        message.isPhoto = true;

        sendMessagesAction(message);
    }

    private void sendMessagesAction(ChatModel message) {
        try {
            Logs.log(TAG, "SEND: " + message.toJSON());
            txtChat.setText("");
            if (privateChannel != null && privateChannel.isSubscribed()) {
                privateChannel.trigger("client-gig", message.toJSON());
                saveChatToServer(message);
            } else
                setupPusher();
            messages.add(message);
            adapter.add(message);
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    list.setStackFromBottom(true);
                }
            });
            if (callBack != null) {
                if (!isShowing()) {
                    callBack.onHadNewMessages(true);
                } else {
                    callBack.onHadNewMessages(false);
                }
            }
            list.setSelection(adapter.getCount() - 1);
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    private void setupPusher() {
        try {
            privateChannel = AppApplication.getInstance().getPusher().getPrivateChannel("private-" + chatChannel);
            if (privateChannel == null || !privateChannel.isSubscribed()) {
                privateChannel = AppApplication.getInstance().getPusher().subscribePrivate("private-" + chatChannel);
            }
            privateChannel.bind("client-gig", new PrivateChannelEventListener() {
                @Override
                public void onAuthenticationFailure(String s, Exception e) {

                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    Logs.log(TAG, "CHAT: onSubscriptionSucceeded : " + channelName);
                }

                @Override
                public void onEvent(String channelName, String eventName, String data) {
                    Logs.log(TAG, "CHAT: onEvent : " + channelName + " : " + data.toString());
                    if (mContext == null)
                        return;
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ChatModel chatModel = ChatModel.fromJSON(data.toString());
                                if (chatModel != null && !TextUtils.isEmpty(chatModel.messageContent)) {
                                    Logs.log(TAG, "CHAT: MSG :" + chatModel.messageContent);
                                    messages.add(chatModel);
                                    adapter.add(chatModel);
                                    adapter.notifyDataSetChanged();
                                    Logs.log(TAG, "CHAT: Adapter :" + adapter.getCount());
                                    if (callBack != null) {
                                        if (!isShowing()) {
                                            callBack.onHadNewMessages(true);
                                        } else {
                                            callBack.onHadNewMessages(false);
                                        }
                                    }
                                    list.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                                            list.setStackFromBottom(true);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Logs.log(e);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Logs.log(e);
        }
    }

    public String getDatas() {
        return ChatModel.toJSONArray(messages);
    }

    public void setDatas(String data) {
        messages = new ArrayList<>();
        messages = ChatModel.fromJSONArray(data);
        if(messages != null && messages.size() > 0) {
            Logs.log(TAG, "history messages : " + messages.size());
            adapter.clear();
            adapter.addAll(messages);
        }
    }

    public void saveChatToServer(ChatModel message) {
        SaveChatRequest re = new SaveChatRequest(gigId, userId, message.toJSON().toString());
        networkManager.saveChatHistory(re).subscribe(chatSaveModel -> {
            Logs.log("CHAT", "Save chat: " + chatSaveModel.message);
        }, throwable -> {
            AppApplication.getInstance().logErrorServer("saveChatHistory/" + gigId + "/" + userId, networkManager.parseError(throwable));
            Logs.log("CHAT", "Save chat fail");
        });
    }

    public void getAllChat(String gigID) {
        networkManager.getAllChat(gigID).subscribe(arrayChat -> {
            loadHistoryToView(arrayChat);
            Logs.log("CHAT", "get chat: " + arrayChat.size());
            pb_loading.setVisibility(View.GONE);
        }, throwable -> {
            AppApplication.getInstance().logErrorServer("getAllChat/" + gigID, networkManager.parseError(throwable));
            Logs.log("CHAT", "get chat fail");
            pb_loading.setVisibility(View.GONE);
        });
    }

    private void loadHistoryToView(ArrayList<ChatSaveModel> arr) {
        if (arr != null && arr.size() > 0) {
            messages = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                ChatSaveModel model = arr.get(i);
                ChatModel chat = ChatModel.fromJSON(model.message);
                messages.add(chat);
            }

            Logs.log(TAG, "history messages : " + messages.size());
            adapter.clear();
            adapter.addAll(messages);
        }
    }
}

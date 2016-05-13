package com.dropininc.model;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.dropininc.utils.Logs;

/**
 * @author TamMai
 */
public class DataInputStreamModel extends BaseModel {

    public String token;
    public String apiKey;
    public String session;
    public String gigsId;

    public String chatChannel;
    public String chatName;
    public String chatAvatar;
    public String chatData;

    public int settingRadius;

    public DataInputStreamModel() {

    }

    public DataInputStreamModel(String token, String apiKey, String session, String gigsId, String chatChannel, String chatName, String chatAvatar, String chatData) {
        this.token = token;
        this.apiKey = apiKey;
        this.session = session;
        this.gigsId = gigsId;
        this.chatChannel = chatChannel;
        this.chatName = chatName;
        this.chatAvatar = chatAvatar;
        this.chatData = chatData;
    }

    public static DataInputStreamModel fromJSON(String json) {
        return gson.fromJson(json, DataInputStreamModel.class);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("token", token);
        bundle.putString("apiKey", apiKey);
        bundle.putString("session", session);
        bundle.putString("gigsId", gigsId);

        bundle.putString("chatChannel", chatChannel);
        bundle.putString("chatName", chatName);
        bundle.putString("chatAvatar", chatAvatar);
        bundle.putString("chatData", chatData);

        bundle.putInt("settingRadius", settingRadius);
        return bundle;
    }

    public static DataInputStreamModel fromBundle(Bundle bundle) {
        DataInputStreamModel model = new DataInputStreamModel();
        try {
            if (bundle.containsKey("token"))
                model.token = bundle.getString("token");
            if (bundle.containsKey("apiKey"))
                model.apiKey = bundle.getString("apiKey");
            if (bundle.containsKey("session"))
                model.session = bundle.getString("session");
            if (bundle.containsKey("gigsId"))
                model.gigsId = bundle.getString("gigsId");
            if (bundle.containsKey("chatChannel"))
                model.chatChannel = bundle.getString("chatChannel");
            if (bundle.containsKey("chatName"))
                model.chatName = bundle.getString("chatName");
            if (bundle.containsKey("chatAvatar"))
                model.chatAvatar = bundle.getString("chatAvatar");
            if (bundle.containsKey("chatData"))
                model.chatData = bundle.getString("chatData");
            if (bundle.containsKey("settingRadius"))
                model.settingRadius = bundle.getInt("settingRadius");
        } catch (Exception e) {
            Logs.log(e);
        }
        return model;
    }

    public Intent toIntent(Activity mActivity, Class destination) {
        Intent intent = new Intent(mActivity, destination);
        intent.putExtra("token", token);
        intent.putExtra("apiKey", apiKey);
        intent.putExtra("session", session);
        intent.putExtra("gigsId", gigsId);

        intent.putExtra("chatChannel", chatChannel);
        intent.putExtra("chatName", chatName);
        intent.putExtra("chatAvatar", chatAvatar);
        intent.putExtra("chatData", chatData);

        intent.putExtra("settingRadius", settingRadius);
        return intent;
    }

    public static DataInputStreamModel fromIntent(Intent intent) {
        DataInputStreamModel streamModel = new DataInputStreamModel();
        streamModel.gigsId = intent.getStringExtra("gigsId");
        streamModel.token = intent.getStringExtra("token");
        streamModel.session = intent.getStringExtra("session");
        streamModel.apiKey = intent.getStringExtra("apiKey");

        streamModel.settingRadius = intent.getIntExtra("settingRadius", 10);

        streamModel.chatChannel = intent.getStringExtra("chatChannel");
        streamModel.chatName = intent.getStringExtra("chatName");
        streamModel.chatAvatar = intent.getStringExtra("chatAvatar");
        streamModel.chatData = intent.getStringExtra("chatData");
        return streamModel;
    }
}

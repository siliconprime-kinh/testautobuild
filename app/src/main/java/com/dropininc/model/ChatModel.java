package com.dropininc.model;

import com.dropininc.utils.Logs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ChatModel extends BaseModel {

    public String id;
    public String userId;
    public String messageContent;
    public double date;
    public String avatarUrl;
    public boolean isPhoto;
    public int code = 21;
    public String fullName;

    public static ChatModel fromJSON(String json) {
        return new Gson().fromJson(json, ChatModel.class);
    }

    public static ArrayList<ChatModel> fromJSONArray(String array) {
        Type collectionType = new TypeToken<ArrayList<ChatModel>>() {
        }.getType();
        return new GsonBuilder().create().fromJson(array, collectionType);
    }

    public static String toJSONArray(ArrayList<ChatModel> datas) {
        return new GsonBuilder().create().toJson(datas);
    }

    @Override
    public String toJSON() {
        return new GsonBuilder().create().toJson(this);
    }

    public JSONObject toJsonObject() {
        try {
            return new JSONObject(toJSON());
        } catch (Exception e) {
            Logs.log(e);
        }
        return null;
    }
}

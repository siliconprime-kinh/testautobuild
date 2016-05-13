package com.dropininc.model;

import com.dropininc.utils.Logs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class LogModel extends BaseModel {

    public String level;
    public String message;
    public String data;

    public static LogModel fromJSON(String json) {
        return new Gson().fromJson(json, LogModel.class);
    }

    public static ArrayList<LogModel> fromJSONArray(String array) {
        Type collectionType = new TypeToken<ArrayList<LogModel>>() {
        }.getType();
        return new GsonBuilder().create().fromJson(array, collectionType);
    }

    public static String toJSONArray(ArrayList<LogModel> datas) {
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

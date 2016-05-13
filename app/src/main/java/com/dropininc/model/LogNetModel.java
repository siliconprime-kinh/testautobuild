package com.dropininc.model;

import android.text.TextUtils;

import com.dropininc.sharepreference.DSharePreference;

import java.util.ArrayList;

/**
 * Created by kinhnm on 4/28/2016.
 */
public class LogNetModel extends BaseModel{
    public ArrayList<String> msg = new  ArrayList<String>();

    public static LogNetModel fromJSON(String json) {
        return gson.fromJson(json, LogNetModel.class);
    }

    public static void saveLogNetWorkPreference(LogNetModel logNetModel) {
        if(logNetModel == null){
            DSharePreference.setLogNetWork("");
        }
        DSharePreference.setLogNetWork(logNetModel.toJSON());
    }

    public static LogNetModel getLogNetWorkPreference() {
        String json = DSharePreference.getLogNetWork();
        if(TextUtils.isEmpty(json)){
            return new LogNetModel();
        }
        return LogNetModel.fromJSON(json);
    }

    public static void saveLogPusherPreference(LogNetModel logNetModel) {
        if(logNetModel == null){
            DSharePreference.setLogPuhser("");
        }
        DSharePreference.setLogPuhser(logNetModel.toJSON());
    }

    public static LogNetModel getLogPusherPreference() {
        String json = DSharePreference.getLogPusher();
        if(TextUtils.isEmpty(json)){
            return new LogNetModel();
        }
        return LogNetModel.fromJSON(json);
    }
    //log error server
    public static void saveLogErrorServerPreference(LogNetModel logNetModel) {
        if(logNetModel == null){
            DSharePreference.setLogErrorServer("");
        }
        DSharePreference.setLogErrorServer(logNetModel.toJSON());
    }

    public static LogNetModel getLogErrorServerPreference() {
        String json = DSharePreference.getLogErrorServer();
        if(TextUtils.isEmpty(json)){
            return new LogNetModel();
        }
        return LogNetModel.fromJSON(json);
    }
}

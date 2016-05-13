package com.dropininc.model;

import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * Created by kelvin on 5/12/2016.
 */
public class LogPusher  extends BaseModel {
    public int code;
    public String messageId;
    public String gig;
    public MetaData metaData =  new MetaData();
    public static class MetaData{
        public String msg;
    }

    public static LogPusher getNotification(NotificationModel notifi){
        if(notifi == null) return null;
        LogPusher logPusher = new LogPusher();
        logPusher.code = notifi.code;


        if(notifi.data != null && notifi.data.gig != null){
            logPusher.gig = notifi.data.gig.id;
        }

        if(TextUtils.isEmpty(logPusher.gig)){
            logPusher.gig = (notifi.data != null) ? notifi.data.gigId:null;
        }

        if(TextUtils.isEmpty(logPusher.gig)){
            logPusher.gig = notifi.gigId;
        }

        logPusher.messageId = notifi.messageId;
        return logPusher;
    };

    public static LogModel fromJSON(String json) {
        return new Gson().fromJson(json, LogModel.class);
    }
}

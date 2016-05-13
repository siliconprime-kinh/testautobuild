package com.dropininc.model;

/**
 * Created by sondinh on 10/19/2015.
 */
public class StreamModel extends BaseModel {
    public String token;
    public String key;
    public String sessionId;
    public String id;

    public static StreamModel fromJSON(String json){
        return gson.fromJson(json, StreamModel.class);
    }
}

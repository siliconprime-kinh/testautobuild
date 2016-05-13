package com.dropininc.model;

public class RateStreamModel extends BaseModel {
    public String durationStreaming;
    public float distanceRouting;
    public String userId;
    public String operatorId;
    public String price;
    public String ratingId;

    public static RateStreamModel fromJSON(String json){
        return gson.fromJson(json, RateStreamModel.class);
    }
}

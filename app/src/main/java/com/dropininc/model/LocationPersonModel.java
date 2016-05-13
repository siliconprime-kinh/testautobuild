package com.dropininc.model;

public class LocationPersonModel extends BaseModel {
    public String lastTime;
    public double latitude;
    public double longitude;
    public String key;
    public int distance;

    public static LocationPersonModel fromJSON(String json) {
        return gson.fromJson(json, LocationPersonModel.class);
    }
}

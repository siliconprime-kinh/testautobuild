package com.dropininc.model;

public class LocationModel extends BaseModel {
    public double latitude = 0.0;
    public double longitude = 0.0;

    public static LocationModel fromJSON(String json) {
        return gson.fromJson(json, LocationModel.class);
    }
}

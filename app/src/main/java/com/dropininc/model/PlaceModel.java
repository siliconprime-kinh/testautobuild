package com.dropininc.model;

public class PlaceModel extends BaseModel {
    public String description;
    public String id;
    public String place_id;
    public String reference;

    public static PlaceModel fromJSON(String json) {
        return gson.fromJson(json, PlaceModel.class);
    }
}

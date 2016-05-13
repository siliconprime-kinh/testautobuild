package com.dropininc.model;

public class CountModel extends BaseModel {
    public int count;

    public static CountModel fromJSON(String json) {
        return gson.fromJson(json, CountModel.class);
    }
}

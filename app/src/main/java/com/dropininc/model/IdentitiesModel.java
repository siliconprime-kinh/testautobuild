package com.dropininc.model;

public class IdentitiesModel extends BaseModel {
    public String value;
    public String type;
    public String status;
    public String createdAt;
    public String updatedAt;
    public String id;

    public static IdentitiesModel fromJSON(String json) {
        return gson.fromJson(json, IdentitiesModel.class);
    }
}



package com.dropininc.model;

public class TokenModel extends BaseModel {
    public String account;
    public int attempts;
    public String status;
    public String expiresOn;
    public String createdAt;
    public String updatedAt;
    public String id;
    public LocationModel location;
    public String code;

    public static TokenModel fromJSON(String json) {
        return gson.fromJson(json, TokenModel.class);
    }
}



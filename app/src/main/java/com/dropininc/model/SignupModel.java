package com.dropininc.model;

public class SignupModel extends BaseModel {
    public TokenModel token;
    public AccountModel account;
    public LocationModel location;

    public static SignupModel fromJSON(String json) {
        return gson.fromJson(json, SignupModel.class);
    }
}



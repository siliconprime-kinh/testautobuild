package com.dropininc.model;

public class VerifyModel extends BaseModel {

    public String token;
    public AccountModel account;
//    public ConfigModel config;

    public static VerifyModel fromJSON(String json) {
        return gson.fromJson(json, VerifyModel.class);
    }
}



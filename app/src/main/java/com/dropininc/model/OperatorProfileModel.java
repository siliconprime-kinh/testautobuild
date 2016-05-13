package com.dropininc.model;

public class OperatorProfileModel extends BaseModel {
    public AccountModel account;
    public OperatorModel operator;

    public static OperatorProfileModel fromJSON(String json){
        return gson.fromJson(json, OperatorProfileModel.class);
    }
}

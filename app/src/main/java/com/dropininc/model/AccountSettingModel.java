package com.dropininc.model;

public class AccountSettingModel extends BaseModel {
    public String account;
    public int detectRadius;
    public double defaultLatitude;
    public double defaultLongitude;
    public int numberOfNotificationPerPage;
    public String createAt;
    public String updateAt;
    public String id;

    public static AccountSettingModel fromJSON(String json){
        return gson.fromJson(json, AccountSettingModel.class);
    }
}

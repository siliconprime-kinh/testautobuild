package com.dropininc.model;

public class GigStreamModel extends BaseModel {

    public String id;
    public String createdAt;
    public String updateAt;
    public String type;
    public String status;
    public String price;
    public int reconnectionAttempts;
    public String duration;
    public double latitude;
    public double longitude;
    public boolean bypassRating;
//    public AccountModel customer;
//    public AccountModel operator;

    public static GigStreamModel fromJSON(String json){
        return gson.fromJson(json, GigStreamModel.class);
    }
}

package com.dropininc.model;

public class GigModel extends BaseModel {
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
    public AccountModel customer;
    public AccountModel operator;
    public String chatChannel;
    public MetaData metaData;

    public static GigModel fromJSON(String json) {
        return gson.fromJson(json, GigModel.class);
    }

    public class MetaData {
        public String address;
    }
}

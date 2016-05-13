package com.dropininc.model;

public class RecipientModel extends BaseModel {

    public boolean archive;
    public String city;
    public String country;
    public String createdAt;
    public String updatedAt;
    public String id;
    public String type;
    public String firstName;
    public String lastName;
    public String state;
    public String status;
    public String street;
    public String language;
    public String operator;

    public static RecipientModel fromJSON(String json) {
        return gson.fromJson(json, RecipientModel.class);
    }
}
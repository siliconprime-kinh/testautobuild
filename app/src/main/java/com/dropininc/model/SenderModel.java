package com.dropininc.model;

public class SenderModel extends BaseModel {
    public String firstName;
    public String lastName;

    public static SenderModel fromJSON(String json){
        return gson.fromJson(json, SenderModel.class);
    }
}

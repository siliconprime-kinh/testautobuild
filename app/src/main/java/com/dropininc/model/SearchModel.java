package com.dropininc.model;

public class SearchModel extends BaseModel {
    public AccountModel account;
    public LocationModel location;

    public static SearchModel fromJSON(String json){
        return gson.fromJson(json, SearchModel.class);
    }
}

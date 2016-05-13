package com.dropininc.model;

public class EndStreamModel extends BaseModel {

    public GigStreamModel gig;

    public static EndStreamModel fromJSON(String json){
        return gson.fromJson(json, EndStreamModel.class);
    }
}

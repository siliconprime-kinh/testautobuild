package com.dropininc.model;

public class GeneralModel extends BaseModel {
    public String message;
    public String paymentStatus;

    public static GeneralModel fromJSON(String json){
        return gson.fromJson(json, GeneralModel.class);
    }
}

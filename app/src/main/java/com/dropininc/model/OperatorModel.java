package com.dropininc.model;

public class OperatorModel extends BaseModel {
    public String account;
    public String createdAt;
    public String updatedAt;
    public String taxFormId;
    public String vendorId;
    public String id;
    public String status;

    public static OperatorModel fromJSON(String json){
        return gson.fromJson(json, OperatorModel.class);
    }
}

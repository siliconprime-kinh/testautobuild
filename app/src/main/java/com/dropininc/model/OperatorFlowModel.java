package com.dropininc.model;

public class OperatorFlowModel extends BaseModel {

    public String firstName;
    public String lastName;
    public String socialSecurityNumber;
    public int month;
    public int day;
    public int year;
    public String city;
    public String street;
    public String zipCode;
    public String state;

    public static OperatorProfileModel fromJSON(String json){
        return gson.fromJson(json, OperatorProfileModel.class);
    }
}

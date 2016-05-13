package com.dropininc.model;

import java.util.ArrayList;

public class AccountModel extends BaseModel {
    public String firstName;
    public String lastName;
    public String status;
    public String type;
    public String createdAt;
    public String updatedAt;
    public String id;
    public String city;
    public String street;
    public String country;
    public String state;
    public boolean archive;
    public ArrayList<IdentitiesModel> identities;
    public OperatorModel operator;
    public double operatorRating;
    public double customerRating;
    public AvatarModel profileImage;
    public String referralCode;
    public String customer;

    public static AccountModel fromJSON(String json) {
        return gson.fromJson(json, AccountModel.class);
    }
}



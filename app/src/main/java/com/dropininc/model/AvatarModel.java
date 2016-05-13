package com.dropininc.model;

public class AvatarModel extends BaseModel{
    public String type;
    public String location;
    public String createdAt;
    public String updatedAt;
    public String id;
    public boolean archive;
    public Account account;

    public static AvatarModel fromJSON(String json){
        return gson.fromJson(json, AvatarModel.class);
    }

    public class Account{
        public boolean archive;
        public String city;
        public String country;
        public String createdAt;
        public String firstName;
        public String lastName;
        public String language;
        public String operator;
        public String state;
        public String status;
        public String street;
        public String type;
        public String updatedAt;
        public String id;
    }
}

package com.dropininc.model;

import java.util.ArrayList;

public class ConfirmModel extends BaseModel {
    public Operator operator;
    public Gig gig;
    public String chatChannel;

    public static class Operator {
        public LocationModel location;
        public float rate;
    }

    public class Gig{
        public ArrayList<User> reservedOperators;
        public User customer;
        public User operator;
        public double latitude;
        public double longitude;
        public int reconnectionAttempts;
        public String duration;
        public boolean archive;
        public String status;
        public String type;
        public String createdAt;
        public String updatedAt;
        public String id;
    }

    public class User{
        public String firstName;
        public String lastName;
        public String state;
        public String country;
        public String city;
        public String street;
        public String language;
        public boolean archive;
        public String status;
        public String type;
        public String operator;
        public String createdAt;
        public String updatedAt;
        public String id;
        public ProfileImage profileImage;
    }

    public static class ProfileImage {
        public String type;
        public String location;
        public String createdAt;
        public String updatedAt;
        public String id;
        public boolean archive;
        public String account;
    }
}

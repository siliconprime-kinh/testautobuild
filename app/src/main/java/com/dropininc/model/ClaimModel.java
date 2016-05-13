package com.dropininc.model;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class ClaimModel extends BaseModel {
    public Gig gig;

    public static ClaimModel fromJSON(String json){
        return gson.fromJson(json, ClaimModel.class);
    }

    @Override
    public String toJSON() {
        return new GsonBuilder().create().toJson(this);
    }

    public class Gig {
        public String id;
        public String createdAt;
        public String updatedAt;
        public String type;
        public String status;
        public boolean archive;
        public String duration;
        public int reconnectionAttempts;
        public double latitude;
        public double longitude;
        public String operator;

        public ArrayList<Customer> reservedOperators;
        public Customer customer;
    }

    public class Customer {
        public boolean archive;
        public String city;
        public String country;
        public String createdAt;
        public String updatedAt;
        public String firstName;
        public String language;
        public String lastName;
        public String operator;
        public String status;
        public String state;
        public String street;
        public String type;
        public String id;
    }
}

package com.dropininc.model;

import android.os.Bundle;

import com.dropininc.utils.Logs;

import java.util.ArrayList;


public class NotificationModel extends BaseModel {
    public int code;
    public String price;
    public float distanceRouting;
    public String duration;
    public boolean bypassRating;

    public String id;
    public String messageId;
    public String viewerId;
    public String operatorId;
    public String firstName;
    public String lastName;
    public String title;
    public String token;
    public String apiKey;
    public String session;
    public String action;
    public String message;
    public String userId;
    public String ratingId;
    public String accountId;
    public String settingRadius;

    public ArrayList<String> viewerIds;

    public LocationModel location;
    public Data data;

    public String gigId;

    public static NotificationModel fromJSON(String json) {
        return gson.fromJson(json, NotificationModel.class);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("operatorId", operatorId);
        bundle.putString("token", token);
        bundle.putString("apiKey", apiKey);
        bundle.putString("session", session);
        bundle.putString("gigsId", data.gig.id);
        return bundle;
    }

    public static NotificationModel fromBundle(Bundle bundle) {
        NotificationModel notificationModel = new NotificationModel();
        try {
            if (bundle.containsKey("operatorId"))
                notificationModel.operatorId = bundle.getString("operatorId");
            if (bundle.containsKey("token"))
                notificationModel.token = bundle.getString("token");
            if (bundle.containsKey("apiKey"))
                notificationModel.apiKey = bundle.getString("apiKey");
            if (bundle.containsKey("session"))
                notificationModel.session = bundle.getString("session");
            if (bundle.containsKey("gigsId")) {
                notificationModel.data = new Data();
                notificationModel.data.gig = new Gig();
                notificationModel.data.gig.id = bundle.getString("gigsId");
            }
            if (bundle.containsKey("bypassRating"))
                notificationModel.bypassRating = Boolean.parseBoolean(bundle.getString("bypassRating"));
            if (bundle.containsKey("duration"))
                notificationModel.duration = bundle.getString("duration");
            if (bundle.containsKey("messageId"))
                notificationModel.messageId = bundle.getString("messageId");
            if (bundle.containsKey("distanceRouting"))
                notificationModel.distanceRouting = Float.parseFloat(bundle.get("distanceRouting").toString());
            if (bundle.containsKey("userId"))
                notificationModel.userId = bundle.getString("userId");
            if (bundle.containsKey("code"))
                notificationModel.code = Integer.parseInt(bundle.get("code").toString());
            if (bundle.containsKey("price"))
                notificationModel.price = bundle.getString("price");
            if (bundle.containsKey("title"))
                notificationModel.title = bundle.getString("title");
            if (bundle.containsKey("message"))
                notificationModel.message = bundle.getString("message");
            if (bundle.containsKey("gigId"))
                notificationModel.gigId = bundle.getString("gigId");
            if (bundle.containsKey("id"))
                notificationModel.id = bundle.getString("id");
            if(bundle.containsKey("location"))
                notificationModel.location = LocationModel.fromJSON(bundle.getString("location"));
        } catch (Exception e) {
            Logs.log(e);
        }
        return notificationModel;
    }

    public static class Data extends BaseModel {
        public Gig gig;
        public String gigId;
        public String chatChannel;

        public static Data fromJSON(String json) {
            return gson.fromJson(json, Data.class);
        }
    }

    public static class Gig {
        public Customer customer;
        public double latitude;
        public double longitude;
        public String duration;
        public boolean archive;
        public String status;
        public String type;
        public String createdAt;
        public String updatedAt;
        public String id;
    }

    public static class Customer {
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

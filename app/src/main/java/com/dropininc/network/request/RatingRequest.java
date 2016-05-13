package com.dropininc.network.request;

public class RatingRequest {

    public String type;
    public int rating;

    public RatingRequest(String type, int rating) {
        this.type = type;
        this.rating = rating;
    }
}

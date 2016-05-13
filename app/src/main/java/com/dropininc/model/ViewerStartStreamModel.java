package com.dropininc.model;

public class ViewerStartStreamModel extends BaseModel {

    public Gig gig;

    public static class Gig {
        public StreamModel stream;
    }

    public static ViewerStartStreamModel fromJSON(String json) {
        return gson.fromJson(json, ViewerStartStreamModel.class);
    }
}

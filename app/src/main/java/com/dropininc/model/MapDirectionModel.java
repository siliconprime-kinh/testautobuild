package com.dropininc.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MapDirectionModel {

    @SerializedName("routes")
    public List<Route> routeList;


    public static class Route {

        public List<Leg> legs;

    }

    public static class Leg {

        public Info distance;
        public List<Step> steps;

    }

    public static class Step {

        public Info distance;
        public Info duration;
        @SerializedName("end_location")
        public Location endLocation;
        @SerializedName("html_instructions")
        public String instructions;
        public Polyline polyline;
        @SerializedName("start_location")
        public Location startLocation;

    }

    public static class Location {

        public double lat;
        public double lng;

    }

    public static class Info {

        public String text;
        public int value;

    }

    public static class Polyline {

        public String points;

    }
}

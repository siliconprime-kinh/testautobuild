package com.dropininc.network.request;

public class LocationRequest {

    public String lat;
    public String lng;
    public String bty;
    public String sig = "100";
    public boolean opr;
    public String net;
    public String rad;
    public String gigId;

    public LocationRequest() {
    }

    public LocationRequest(String lat, String lng, String bty, int sig, boolean opr, String net, String rad, String gigId) {
        this.lat = lat;
        this.lng = lng;
        this.bty = bty;
//        if (sig > 0)
//            this.sig = String.valueOf(sig);
        this.opr = opr;
        this.net = net;
        this.rad = rad;
        this.gigId = gigId;
    }
}

package com.dropininc.network.request;


public class DeviceTokenRequest {

    public String deviceAddress;
    public String deviceType;

    public DeviceTokenRequest(String deviceAddress) {
        this.deviceAddress = deviceAddress;
        deviceType = "android";
    }

}

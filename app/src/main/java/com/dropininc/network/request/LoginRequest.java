package com.dropininc.network.request;


public class LoginRequest {

    public String email;
    public String phone;

    public LoginRequest(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }
}

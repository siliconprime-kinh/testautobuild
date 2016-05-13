package com.dropininc.network.request;

import java.util.ArrayList;

public class AccountRequest {

    public String id;
    public String firstName = "";
    public String lastName = "";
    public String state = "";
    public String country = "";
    public String city = "";
    public String street = "";
    public String language = "";
    public boolean archive = false;
    public String status = "";
    public String type = "";
    public String referredBy = "";
    public ArrayList<IdentitiesRequest> identities = new ArrayList<>();

    public AccountRequest(String firstName, String lastName, String referredBy,
                          String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.referredBy = referredBy;
        identities.add(new IdentitiesRequest("email", email));
        identities.add(new IdentitiesRequest("phone", phone));
    }

    public AccountRequest(String id, String firstName, String lastName, String city) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
    }
}

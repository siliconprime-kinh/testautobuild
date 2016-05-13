package com.dropininc.network.request;

public class UpdateOperatorProfileRequest {

    public String ssn;
    public Dob dob;
    public Address address;

    public UpdateOperatorProfileRequest(String ssn, Dob dob, Address address) {
        this.ssn = ssn;
        this.dob = dob;
        this.address = address;
    }

    public static class Dob {
        public int month;
        public int day;
        public int year;

        public Dob(int month, int day, int year) {
            this.month = month;
            this.day = day;
            this.year = year;
        }
    }

    public static class Address {
        public String city;
        public String street;
        public String postalCode;
        public String state;
        public String country;

        public Address(String city, String street, String postalCode, String state, String country) {
            this.city = city;
            this.street = street;
            this.postalCode = postalCode;
            this.state = state;
            this.country = country;
        }
    }

}

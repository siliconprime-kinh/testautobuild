package com.dropininc.model;

import com.google.gson.annotations.SerializedName;


public class PaymentModel extends BaseModel {
    public String id; // card id
    public String brand; // "Visa"
    public String last4; // "1881"
    @SerializedName("default")
    public boolean isDefault;

    public PaymentModel(String id, String brand, String last4, boolean isDefault) {
        this.id = id;
        this.brand = brand;
        this.last4 = last4;
        this.isDefault = isDefault;
    }

    public static PaymentModel fromJSON(String json){
        return gson.fromJson(json, PaymentModel.class);
    }
}
